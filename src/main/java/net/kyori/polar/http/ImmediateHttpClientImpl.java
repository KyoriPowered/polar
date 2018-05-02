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
import net.kyori.lunar.EvenMoreObjects;
import net.kyori.polar.PolarConfiguration;
import net.kyori.polar.http.endpoint.EndpointRequest;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
final class ImmediateHttpClientImpl extends AbstractHttpClient implements ImmediateHttpClient {
  @Inject
  private ImmediateHttpClientImpl(final PolarConfiguration configuration, final OkHttpClient httpClient) {
    super(configuration, httpClient);
  }

  @Override
  public @NonNull CompletableFuture<Optional<JsonElement>> json(final @NonNull EndpointRequest request, final int flags) {
    final CompletableFuture<Response> future = new CompletableFuture<>();
    this.request(request, future, flags);
    return this.json(future);
  }

  private void request(final @NonNull EndpointRequest request, final CompletableFuture<Response> response, final int flags) {
    this.request(EvenMoreObjects.make(new Request.Builder(), request::configure), response, flags);
  }

  private void request(final Request.@NonNull Builder request, final CompletableFuture<Response> response, final int flags) {
    try {
      response.complete(this.httpClient.newCall(this.request(request, flags)).execute());
    } catch(final IOException e) {
      response.completeExceptionally(e);
    }
  }
}
