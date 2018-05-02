/*
 * This file is part of polar, licensed under the MIT License.
 *
 * Copyright (c) 2018 KyoriPowered
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.kyori.polar.http;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.JsonElement;
import net.kyori.lunar.EvenMoreObjects;
import net.kyori.lunar.exception.Exceptions;
import net.kyori.polar.PolarConfiguration;
import net.kyori.polar.http.endpoint.EndpointRequest;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
final class RateLimitedHttpClientImpl extends AbstractHttpClient implements RateLimitedHttpClient {
  private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitedHttpClientImpl.class);
  private static final long OFFSET_NOT_SET = Long.MIN_VALUE;
  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(
    1,
    new ThreadFactoryBuilder()
      .setNameFormat("Polar RateLimit HTTP Queue - %d")
      .build()
  );
  private final LoadingCache<String, Bucket> buckets = CacheBuilder.newBuilder()
    .expireAfterAccess(5, TimeUnit.MINUTES)
    .build(CacheLoader.from(Bucket::new));
  private long offset = OFFSET_NOT_SET;

  @Inject
  private RateLimitedHttpClientImpl(final PolarConfiguration configuration, final OkHttpClient httpClient) {
    super(configuration, httpClient);
  }

  @Override
  public @NonNull CompletableFuture<Optional<JsonElement>> json(final @NonNull EndpointRequest request, final int flags) {
    final CompletableFuture<Response> future = new CompletableFuture<>();
    final Bucket bucket = this.buckets.getUnchecked(request.identity());
    bucket.submit(this.request(EvenMoreObjects.make(new Request.Builder(), request::configure), flags), future);
    if(bucket.future == null) {
      bucket.future = this.scheduler.schedule(bucket::processQueue, bucket.delay(), TimeUnit.MILLISECONDS);
    }
    return this.json(future);
  }

  private void calculateOffset(final Response response) {
    final @Nullable String date = response.header("Date");
    if(date != null) {
      this.offset = OffsetDateTime.parse(date, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().toEpochMilli() - System.currentTimeMillis();
    }
  }

  final class Bucket {
    final Queue<Entry> queue = new ConcurrentLinkedQueue<>();
    ScheduledFuture<?> future;
    volatile int rateLimitRemaining;
    volatile long rateLimitReset;

    void submit(final Request request, final CompletableFuture<Response> future) {
      this.queue.add(new Entry(request, future));
    }

    int delay() {
      if(this.rateLimitRemaining > 0) {
        return 0;
      }
      final long diff = System.currentTimeMillis() + (RateLimitedHttpClientImpl.this.offset == OFFSET_NOT_SET ? 0 : RateLimitedHttpClientImpl.this.offset);
      return (int) (this.rateLimitReset - diff);
    }

    private boolean available() {
      return this.rateLimitRemaining > 0 || this.delay() <= 0;
    }

    void processQueue() {
      while(!this.queue.isEmpty()) {
        if(!this.available()) {
          try {
            final int delay = this.delay();
            if(delay > 0) {
              Thread.sleep(delay);
            }
          } catch(final InterruptedException e) {
            LOGGER.error("Encountered an interruption while sleeping in rate limit bucket", e);
          }
        }

        final Entry entry = this.queue.peek();
        this.processEntry(entry);
      }
    }

    private void processEntry(final Entry entry) {
      try {
        final Response response = RateLimitedHttpClientImpl.this.httpClient.newCall(entry.request).execute();

        if(RateLimitedHttpClientImpl.this.offset == OFFSET_NOT_SET) {
          RateLimitedHttpClientImpl.this.calculateOffset(response);
        }

        if(response.code() == 429) {
          this.rateLimitRemaining = 0;
          this.rateLimitReset = System.currentTimeMillis() + this.retryAfter(response.body());
        } else {
          this.rateLimitRemaining = Integer.parseInt(response.header("X-RateLimit-Remaining", "1"));
          this.rateLimitReset = Integer.parseInt(response.header("X-RateLimit-Reset", "0"));
          entry.future.complete(response);
          this.queue.remove();
        }
      } catch(final IOException e) {
        entry.future.completeExceptionally(e);
      }
    }

    private int retryAfter(final @Nullable ResponseBody response) {
      return Optional.ofNullable(response)
        .map(Exceptions.rethrowFunction(body -> {
          final String string = body.string();
          body.close();
          return string;
        }))
        .map(Exceptions.rethrowFunction(PARSER::parse))
        .map(JsonElement::getAsJsonObject)
        .map(json -> json.getAsJsonPrimitive("retry_after").getAsInt())
        .orElse(0);
    }

    final class Entry {
      final Request request;
      final CompletableFuture<Response> future;

      Entry(final Request request, final CompletableFuture<Response> future) {
        this.request = request;
        this.future = future;
      }
    }
  }
}
