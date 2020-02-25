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
package net.kyori.polar.channel.message.emoji;

import com.google.gson.JsonObject;
import java.util.Objects;
import net.kyori.kassel.channel.message.emoji.UnicodeEmoji;
import net.kyori.peppermint.Json;
import net.kyori.polar.util.Equality;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class UnicodeEmojiImpl implements UnicodeEmoji {
  private final String name;

  UnicodeEmojiImpl(final @NonNull JsonObject json) {
    this(Json.needString(json, "name"));
  }

  UnicodeEmojiImpl(final @NonNull String name) {
    this.name = name;
  }

  @Override
  public @NonNull String name() {
    return this.name;
  }

  @Override
  public boolean animated() {
    return false;
  }

  @Override
  public boolean equals(final Object other) {
    return Equality.equals(this, other, that -> this.name.equals(that.name));
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(this.name);
  }
}
