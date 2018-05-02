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

import net.kyori.kassel.snowflake.Snowflake;
import net.kyori.polar.Polar;

public final class Endpoints {
  private static final ParameterizedEndpoint SEND_MESSAGE = new ParameterizedEndpoint(Polar.API_URL + "/channels/{channel_id}/messages", "channel_id");
  private static final ParameterizedEndpoint DELETE_MESSAGE = new ParameterizedEndpoint(Polar.API_URL + "/channels/{channel_id}/messages/{message_id}", "channel_id");
  private static final ParameterizedEndpoint EDIT_MESSAGE = new ParameterizedEndpoint(Polar.API_URL + "/channels/{channel_id}/messages/{message_id}", "channel_id");
  private static final SimpleEndpoint GATEWAY = new SimpleEndpoint(Polar.API_URL + "/gateway");

  private Endpoints() {
  }

  public static Endpoint sendMessage(final @Snowflake long channel_id) {
    return SEND_MESSAGE.with(channel_id);
  }

  public static Endpoint deleteMessage(final @Snowflake long channel_id, final @Snowflake long message_id) {
    return DELETE_MESSAGE.with(channel_id, message_id);
  }

  public static Endpoint editMessage(final @Snowflake long channel_id, final @Snowflake long message_id) {
    return EDIT_MESSAGE.with(channel_id, message_id);
  }

  public static Endpoint gateway() {
    return GATEWAY;
  }
}
