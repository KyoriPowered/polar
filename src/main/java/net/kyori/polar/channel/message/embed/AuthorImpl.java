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
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;

final class AuthorImpl implements Embed.Author {
  private final @Nullable String name;
  private final @Nullable String url;
  private final @Nullable String icon;

  AuthorImpl(final @NonNull JsonObject json) {
    this(
      Json.getString(json, "name", null),
      Json.getString(json, "url", null),
      Json.getString(json, "icon_url", null)
    );
  }

  AuthorImpl(final @Nullable String name, final @Nullable String url, final @Nullable String icon) {
    this.name = name;
    this.url = url;
    this.icon = icon;
  }

  @Override
  public @NonNull Optional<String> name() {
    return Optional.ofNullable(this.name);
  }

  @Override
  public @NonNull Optional<String> url() {
    return Optional.ofNullable(this.url);
  }

  @Override
  public @NonNull Optional<String> icon() {
    return Optional.ofNullable(this.icon);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("name", this.name)
      .add("url", this.url)
      .add("icon", this.icon)
      .toString();
  }
}
