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
import com.google.gson.JsonObject;

import java.util.function.Consumer;
import java.util.function.Supplier;

interface GatewayPayload {
  String EVENT_DATA = "d";
  String OPCODE = "op";
  String SEQUENCE = "s";
  String EVENT_NAME = "t";

  static String create(final int opcode, final Supplier<JsonElement> supplier) {
    final JsonObject json = new JsonObject();
    json.addProperty(OPCODE, opcode);
    json.add(EVENT_DATA, supplier.get());
    return json.toString();
  }

  static String create(final int opcode, final Consumer<JsonObject> consumer) {
    return create(opcode, () -> {
      final JsonObject json = new JsonObject();
      consumer.accept(json);
      return json;
    });
  }
}