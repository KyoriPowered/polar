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

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import okhttp3.Request;

import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ParameterizedEndpoint {
  private static final Pattern PATTERN = Pattern.compile("\\{(.*?)\\}");
  private final String route;
  private final String identity;
  private final IntList majorParameters;

  ParameterizedEndpoint(String route, final String... majorParameters) {
    this.route = PATTERN.matcher(route).replaceAll("%s");
    this.majorParameters = majorParameters.length > 0 ? new IntArrayList(majorParameters.length) : IntLists.EMPTY_LIST;

    if(majorParameters.length != 0) {
      final Matcher matcher = PATTERN.matcher(route);
      int i = 0;
      while(matcher.find()) {
        final String match = matcher.group(1);
        for(final String majorParameter : majorParameters) {
          if(match.equals(majorParameter)) {
            route = route.replace(matcher.group(0), "%s");
            this.majorParameters.add(i);
          }
        }
        i++;
      }

      this.identity = route;
    } else {
      this.identity = route;
    }
  }

  @SuppressWarnings("RedundantCast")
  Endpoint with(final Object... args) {
    final String url = String.format(this.route, (Object[]) args);
    String identity = this.identity;
    if(this.majorParameters.size() > 0) {
      final Object[] majorParameters = new Object[this.majorParameters.size()];
      for(int i = 0, length = majorParameters.length; i < length; i++) {
        majorParameters[i] = args[this.majorParameters.getInt(i)];
      }
      identity = String.format(identity, (Object[]) majorParameters);
    }
    return new ParameterizedEndpointInstance(identity, url);
  }
}

final class ParameterizedEndpointInstance implements Endpoint {
  private final String identity;
  private final String url;

  ParameterizedEndpointInstance(final String identity, final String url) {
    this.identity = identity;
    this.url = url;
  }

  @Override
  public EndpointRequest request(final Consumer<Request.Builder> request) {
    return new EndpointRequestImpl(this.identity, this.url, request);
  }
}
