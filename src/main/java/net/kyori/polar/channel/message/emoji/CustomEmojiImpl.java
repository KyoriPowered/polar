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
import net.kyori.kassel.channel.message.emoji.CustomEmoji;
import net.kyori.kassel.snowflake.Snowflake;
import net.kyori.peppermint.Json;
import net.kyori.polar.snowflake.SnowflakedImpl;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class CustomEmojiImpl extends SnowflakedImpl implements CustomEmoji {
  private @NonNull String name;
  private final boolean animated;

  CustomEmojiImpl(final @NonNull JsonObject json) {
    this(
      Json.needLong(json, "id"),
      Json.needString(json, "name"),
      Json.needBoolean(json, "animated")
    );
  }

  private CustomEmojiImpl(final @Snowflake long id, final @NonNull String name, final boolean animated) {
    super(id);
    this.name = name;
    this.animated = animated;
  }

  @Override
  public @NonNull String name() {
    return this.name;
  }

  public void name(final @NonNull String name) {
    this.name = name;
  }

  @Override
  public boolean animated() {
    return this.animated;
  }
}
