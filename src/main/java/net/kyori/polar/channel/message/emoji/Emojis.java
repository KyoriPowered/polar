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
import net.kyori.kassel.channel.message.emoji.Emoji;
import net.kyori.kassel.channel.message.emoji.UnicodeEmoji;
import net.kyori.peppermint.Json;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface Emojis {
  static @NonNull Emoji from(final @NonNull JsonObject json) {
    return Json.isNumber(json, "id") ? new CustomEmojiImpl(json) : new UnicodeEmojiImpl(json);
  }

  static @NonNull UnicodeEmoji unicode(final @NonNull JsonObject json) {
    return new UnicodeEmojiImpl(json);
  }

  static @NonNull UnicodeEmoji unicode(final @NonNull String name) {
    return new UnicodeEmojiImpl(name);
  }

  static @NonNull CustomEmoji custom(final @NonNull JsonObject json) {
    return new CustomEmojiImpl(json);
  }

  static @NonNull String api(final @NonNull Emoji emoji) {
    if(emoji instanceof UnicodeEmoji) {
      return emoji.name();
    } else if(emoji instanceof CustomEmoji) {
      return emoji.name() + ":" + ((CustomEmoji) emoji).id();
    }
    throw new IllegalArgumentException(emoji.getClass().getName());
  }
}
