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
package net.kyori.polar.gateway;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.kyori.mu.function.ThrowingSupplier;
import net.kyori.polar.Polar;
import net.kyori.polar.http.HttpClient;
import net.kyori.polar.http.ImmediateHttpClient;
import net.kyori.polar.http.endpoint.Endpoints;
import net.kyori.violet.Lazy;

import javax.inject.Inject;

final class GatewayUrl extends Lazy<String> {
  @Inject
  private GatewayUrl(final ImmediateHttpClient httpClient) {
    super(ThrowingSupplier.of(() -> {
      final String url = httpClient.json(Endpoints.gateway().request(), HttpClient.UNAUTHENTICATED).get()
        .map(JsonElement::getAsJsonObject)
        .map(o -> o.getAsJsonPrimitive("url"))
        .map(JsonPrimitive::getAsString)
        .orElseThrow(() -> new IllegalStateException("Could not fetch gateway url"));
      return url + String.format("?compress=%s&encoding=%s&v=%d", Polar.GATEWAY_COMPRESSION, Polar.GATEWAY_ENCODING, Polar.GATEWAY_VERSION);
    }));
  }
}
