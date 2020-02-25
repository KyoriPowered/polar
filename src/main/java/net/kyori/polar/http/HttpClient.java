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
import java.util.concurrent.CompletableFuture;
import net.kyori.mu.Maybe;
import net.kyori.polar.http.endpoint.EndpointRequest;
import okhttp3.MediaType;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface HttpClient {
  // Flags
  int UNAUTHENTICATED = 0x01;
  // Media Types
  MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json");

  default @NonNull CompletableFuture<Maybe<JsonElement>> json(final @NonNull EndpointRequest request) {
    return this.json(request, 0);
  }

  @NonNull CompletableFuture<Maybe<JsonElement>> json(final @NonNull EndpointRequest request, final int flags);
}
