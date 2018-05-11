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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kyori.kassel.channel.message.embed.Embed;
import net.kyori.peppermint.Json;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.awt.Color;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class EmbedImpl implements Embed {
  final @Nullable String title;
  final @Nullable String description;
  final @Nullable String url;
  final @Nullable Color color;
  final @Nullable Instant timestamp;
  final @Nullable Author author;
  final @Nullable Image image;
  final @Nullable Thumbnail thumbnail;
  final List<Field> fields;
  final @Nullable Footer footer;

  public EmbedImpl(final @NonNull JsonObject json) {
    this.title = Json.getString(json, "title", null);
    this.description = Json.getString(json, "description", null);
    this.url = Json.getString(json, "url", null);
    this.color = Json.isNumber(json, "color") ? new Color(Json.needInt(json, "color")) : null;
    this.timestamp = Json.isString(json, "timestamp") ? OffsetDateTime.parse(Json.needString(json, "timestamp")).toInstant() : null;

    if(Json.isObject(json, "author")) {
      this.author = new AuthorImpl(json.getAsJsonObject("author"));
    } else {
      this.author = null;
    }

    if(Json.isObject(json, "image")) {
      this.image = new ImageImpl(json.getAsJsonObject("image"));
    } else {
      this.image = null;
    }

    if(Json.isObject(json, "thumbnail")) {
      this.thumbnail = new ThumbnailImpl(json.getAsJsonObject("thumbnail"));
    } else {
      this.thumbnail = null;
    }

    if(Json.isArray(json, "fields")) {
      final JsonArray fields = json.getAsJsonArray("fields");
      this.fields = new ArrayList<>(fields.size());
      for(final JsonElement field : fields) {
        this.fields.add(new FieldImpl(field.getAsJsonObject()));
      }
    } else {
      this.fields = Collections.emptyList();
    }

    if(Json.isObject(json, "footer")) {
      this.footer = new FooterImpl(json.getAsJsonObject("footer"));
    } else {
      this.footer = null;
    }
  }

  EmbedImpl(final @NonNull BuilderImpl builder) {
    this.title = builder.title;
    this.description = builder.description;
    this.url = builder.url;
    this.color = builder.color;
    this.timestamp = builder.timestamp;
    if(builder.authorName != null || builder.authorUrl != null || builder.authorIcon != null) {
      this.author = new AuthorImpl(builder.authorName, builder.authorUrl, builder.authorIcon);
    } else {
      this.author = null;
    }
    if(builder.imageUrl != null) {
      this.image = new ImageImpl(builder.imageUrl);
    } else {
      this.image = null;
    }
    if(builder.thumbnailUrl != null) {
      this.thumbnail = new ThumbnailImpl(builder.thumbnailUrl);
    } else {
      this.thumbnail = null;
    }
    this.fields = builder.fields;
    if(builder.footerText != null || builder.footerIcon != null) {
      this.footer = new FooterImpl(builder.footerText, builder.footerIcon);
    } else {
      this.footer = null;
    }
  }

  @Override
  public @NonNull Optional<String> title() {
    return Optional.ofNullable(this.title);
  }

  @Override
  public @NonNull Optional<String> description() {
    return Optional.ofNullable(this.description);
  }

  @Override
  public @NonNull Optional<String> url() {
    return Optional.ofNullable(this.url);
  }

  @Override
  public @NonNull Optional<Color> color() {
    return Optional.ofNullable(this.color);
  }

  @Override
  public @NonNull Optional<Instant> timestamp() {
    return Optional.ofNullable(this.timestamp);
  }

  @Override
  public @NonNull Optional<Author> author() {
    return Optional.ofNullable(this.author);
  }

  @Override
  public @NonNull Optional<Image> image() {
    return Optional.ofNullable(this.image);
  }

  @Override
  public @NonNull Optional<Thumbnail> thumbnail() {
    return Optional.ofNullable(this.thumbnail);
  }

  @NonNull
  @Override
  public List<Field> fields() {
    return this.fields;
  }

  @Override
  public @NonNull Optional<Footer> footer() {
    return Optional.ofNullable(this.footer);
  }

  @Override
  public @NonNull Builder toBuilder() {
    return new BuilderImpl(this);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("title", this.title)
      .add("description", this.description)
      .add("url", this.url)
      .add("color", this.color)
      .add("timestamp", this.timestamp)
      .add("author", this.author)
      .add("image", this.image)
      .add("thumbnail", this.thumbnail)
      .add("fields", this.fields)
      .add("footer", this.footer)
      .toString();
  }
}
