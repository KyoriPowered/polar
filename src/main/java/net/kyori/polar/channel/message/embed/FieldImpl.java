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
package net.kyori.polar.channel.message.embed;

import com.google.common.base.MoreObjects;
import com.google.gson.JsonObject;
import net.kyori.kassel.channel.message.embed.Embed;
import net.kyori.peppermint.Json;
import org.checkerframework.checker.nullness.qual.NonNull;

final class FieldImpl implements Embed.Field {
  private final String name;
  private final String value;
  private final boolean inline;

  FieldImpl(final @NonNull JsonObject json) {
    this(
      Json.needString(json, "name"),
      Json.needString(json, "value"),
      Json.needBoolean(json, "inline")
    );
  }

  FieldImpl(final @NonNull String name, final @NonNull String value, final boolean inline) {
    this.name = name;
    this.value = value;
    this.inline = inline;
  }

  @Override
  public @NonNull String name() {
    return this.name;
  }

  @Override
  public @NonNull String value() {
    return this.value;
  }

  @Override
  public boolean inline() {
    return this.inline;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("name", this.name)
      .add("value", this.value)
      .add("inline", this.inline)
      .toString();
  }
}
