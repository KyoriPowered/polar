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

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import net.kyori.mu.Maybe;
import net.kyori.mu.concurrent.CompletableFutures;
import net.kyori.polar.Polar;
import net.kyori.polar.PolarConfiguration;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

abstract class AbstractHttpClient implements HttpClient {
  static final JsonParser PARSER = new JsonParser();
  private final PolarConfiguration configuration;
  final OkHttpClient httpClient;

  @Inject
  AbstractHttpClient(final PolarConfiguration configuration, final OkHttpClient httpClient) {
    this.configuration = configuration;
    this.httpClient = httpClient;
  }

  final Request request(final Request.@NonNull Builder request, final int flags) {
    if((flags & UNAUTHENTICATED) == 0) {
      request.header("Authorization", this.configuration.token());
    }
    request.header("User-Agent", Polar.API_USER_AGENT);
    return request.build();
  }

  final CompletableFuture<Maybe<JsonElement>> json(final CompletableFuture<Response> future) {
    return future.thenCompose(response -> {
      final @Nullable ResponseBody body = response.body();
      if(body == null) {
        return CompletableFuture.completedFuture(Maybe.nothing());
      }
      final JsonElement json;
      try {
        json = PARSER.parse(body.string());
      } catch(final IOException e) {
        return CompletableFutures.completedExceptionally(e);
      }
      body.close();
      return CompletableFuture.completedFuture(Maybe.just(json));
    });
  }
}
