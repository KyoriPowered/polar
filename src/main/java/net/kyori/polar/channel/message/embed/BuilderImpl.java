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

import net.kyori.kassel.channel.message.embed.Embed;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.awt.Color;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

final class BuilderImpl implements Embed.Builder {
  @Nullable String title;
  @Nullable String description;
  @Nullable String url;
  @Nullable Color color;
  @Nullable Instant timestamp;
  // author
  @Nullable String authorName;
  @Nullable String authorUrl;
  @Nullable String authorIcon;
  // image
  @Nullable String imageUrl;
  // thumbnail
  @Nullable String thumbnailUrl;
  // fields
  final List<Embed.Field> fields = new ArrayList<>();
  // footer
  @Nullable String footerText;
  @Nullable String footerIcon;

  @Override
  public Embed.@NonNull Builder title(final @Nullable String title) {
    this.title = title;
    return this;
  }

  @Override
  public Embed.@NonNull Builder description(final @Nullable String description) {
    this.description = description;
    return this;
  }

  @Override
  public Embed.@NonNull Builder url(final @Nullable String url) {
    this.url = url;
    return this;
  }

  @Override
  public Embed.@NonNull Builder color(final @Nullable Color color) {
    this.color = color;
    return this;
  }

  @Override
  public Embed.@NonNull Builder timestamp(final @Nullable Instant timestamp) {
    this.timestamp = timestamp;
    return this;
  }

  @Override
  public Embed.@NonNull Builder authorName(final @Nullable String name) {
    this.authorName = name;
    return this;
  }

  @Override
  public Embed.@NonNull Builder authorUrl(final @Nullable String url) {
    this.authorUrl = url;
    return this;
  }

  @Override
  public Embed.@NonNull Builder authorIcon(final @Nullable String icon) {
    this.authorIcon = icon;
    return this;
  }

  @Override
  public Embed.@NonNull Builder imageUrl(final @Nullable String url) {
    this.imageUrl = url;
    return this;
  }

  @Override
  public Embed.@NonNull Builder thumbnailUrl(final @Nullable String url) {
    this.thumbnailUrl = url;
    return this;
  }

  @Override
  public Embed.@NonNull Builder field(final @NonNull String name, final @NonNull String value, final boolean inline) {
    requireNonNull(name, "name");
    requireNonNull(value, "value");
    this.fields.add(new FieldImpl(name, value, inline));
    return this;
  }

  @Override
  public Embed.@NonNull Builder footerText(final @Nullable String text) {
    this.footerText = text;
    return this;
  }

  @Override
  public Embed.@NonNull Builder footerIcon(final @Nullable String icon) {
    this.footerIcon = icon;
    return this;
  }

  @Override
  public @NonNull Embed build() {
    return new EmbedImpl(this);
  }
}
