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
package net.kyori.polar.guild.role;

import com.google.common.base.MoreObjects;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.assistedinject.Assisted;
import net.kyori.kassel.guild.Guild;
import net.kyori.kassel.guild.role.Role;
import net.kyori.peppermint.Json;
import net.kyori.polar.refresh.Refreshable;
import net.kyori.polar.snowflake.SnowflakedImpl;
import net.kyori.polar.util.Colors;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.awt.Color;
import java.util.Optional;

import javax.inject.Inject;

public final class RoleImpl extends SnowflakedImpl implements Refreshable, Role {
  private final RoleRefresher refresher;
  private final Guild guild;
  private @NonNull String name;
  private @Nullable Color color;
  private boolean mentionable;
  private boolean managed;
  private boolean hoist;

  @Inject
  private RoleImpl(final RoleRefresher refresher, final @Assisted Guild guild, final @Assisted JsonObject json) {
    super(Json.needLong(json, "id"));
    this.refresher = refresher;
    this.guild = guild;
    this.name = Json.needString(json, "name");
    this.color = Colors.color(Json.getInt(json, "color", Colors.NOT_SET));
    this.mentionable = Json.needBoolean(json, "mentionable");
    this.managed = Json.needBoolean(json, "managed");
    this.hoist = Json.needBoolean(json, "hoist");
  }

  @Override
  public void refresh(final JsonElement json) {
    this.refresher.refresh(new RoleRefresher.Context() {
      @Override
      public @NonNull Guild guild() {
        return RoleImpl.this.guild;
      }

      @Override
      public RoleImpl target() {
        return RoleImpl.this;
      }
    }, json);
  }

  @Override
  public @NonNull String name() {
    return this.name;
  }

  void name(final @NonNull String name) {
    this.name = name;
  }

  @Override
  public @NonNull Optional<Color> color() {
    return Optional.ofNullable(this.color);
  }

  void color(final @NonNull Optional<Color> color) {
    this.color = color.orElse(null);
  }

  @Override
  public boolean mentionable() {
    return this.mentionable;
  }

  void mentionable(final boolean mentionable) {
    this.mentionable = mentionable;
  }

  @Override
  public boolean managed() {
    return this.managed;
  }

  void managed(final boolean managed) {
    this.managed = managed;
  }

  @Override
  public boolean hoist() {
    return this.hoist;
  }

  void hoist(final boolean hoist) {
    this.hoist = hoist;
  }

  @Override
  protected MoreObjects.ToStringHelper toStringer() {
    return super.toStringer()
      .add("name", this.name)
      .add("color", this.color)
      .add("mentionable", this.mentionable)
      .add("managed", this.managed)
      .add("hoist", this.hoist);
  }

  public interface Factory {
    RoleImpl create(final Guild guild, final JsonObject json);
  }
}
