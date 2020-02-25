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
package net.kyori.polar.http.endpoint;

import java.util.function.Consumer;
import okhttp3.Request;

public interface EndpointRequest {
  String identity();

  void configure(final Request.Builder request);
}

final class EndpointRequestImpl implements EndpointRequest {
  private final String identity;
  private final String url;
  private final Consumer<Request.Builder> request;

  EndpointRequestImpl(final String identity, final String url, final Consumer<Request.Builder> request) {
    this.identity = identity;
    this.url = url;
    this.request = request;
  }

  @Override
  public String identity() {
    return this.identity;
  }

  @Override
  public void configure(final Request.Builder request) {
    this.request.accept(request);
    request.url(this.url);
  }
}
