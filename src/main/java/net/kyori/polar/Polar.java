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
package net.kyori.polar;

import net.kyori.kassel.client.Client;
import net.kyori.mu.Maybe;
import net.kyori.polar.client.ClientImpl;

public interface Polar {
  String API_URL = "https://discordapp.com/api/v" + Polar.API_VERSION; // dumb forward references
  String API_USER_AGENT = String.format(
    "DiscordBot (%s, kassel@%s polar@%s)",
    "https://github.com/KyoriPowered/polar",
    Maybe.maybe(Client.class.getPackage().getImplementationVersion()).orDefault("dev"),
    Maybe.maybe(ClientImpl.class.getPackage().getImplementationVersion()).orDefault("dev")
  );
  int API_VERSION = 6;

  String GATEWAY_COMPRESSION = "zlib-stream";
  String GATEWAY_ENCODING = "json";
  int GATEWAY_VERSION = 6;
}
