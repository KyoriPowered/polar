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
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.assistedinject.Assisted;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.awt.Color;
import java.util.concurrent.ExecutorService;
import javax.inject.Inject;
import net.kyori.kassel.guild.Guild;
import net.kyori.kassel.guild.role.Role;
import net.kyori.mu.Maybe;
import net.kyori.peppermint.Json;
import net.kyori.polar.ForPolar;
import net.kyori.polar.http.HttpClient;
import net.kyori.polar.http.RateLimitedHttpClient;
import net.kyori.polar.http.endpoint.Endpoints;
import net.kyori.polar.refresh.Refreshable;
import net.kyori.polar.snowflake.SnowflakedImpl;
import net.kyori.polar.util.Colors;
import okhttp3.RequestBody;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class RoleImpl extends SnowflakedImpl implements Refreshable, Role {
  private final ExecutorService executor;
  private final RateLimitedHttpClient httpClient;
  private final Gson gson;
  private final RoleRefresher refresher;
  private final Guild guild;
  private @NonNull String name;
  private @Nullable Color color;
  private boolean mentionable;
  private boolean managed;
  private boolean hoist;

  public static LongSet roles(final JsonArray array) {
    final LongSet roles = new LongArraySet(array.size());
    for(final JsonElement role : array) {
      roles.add(Json.needLong(role, "id"));
    }
    return roles;
  }

  @Inject
  private RoleImpl(final ExecutorService executor, final RateLimitedHttpClient httpClient, final @ForPolar Gson gson, final RoleRefresher refresher, final @Assisted Guild guild, final @Assisted JsonObject json) {
    super(Json.needLong(json, "id"));
    this.executor = executor;
    this.httpClient = httpClient;
    this.gson = gson;
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
      public @NonNull RoleImpl target() {
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
  public @NonNull Maybe<Color> color() {
    return Maybe.maybe(this.color);
  }

  void color(final @NonNull Maybe<Color> color) {
    this.color = color.orDefault(null);
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

  @Override
  public void edit(final @NonNull Edit edit) {
    this.executor.submit(() -> this.httpClient.json(Endpoints.editGuildRole(this.guild.id(), this.id).request(builder -> {
      final JsonObject json = this.gson.toJsonTree(edit).getAsJsonObject();
      builder.patch(RequestBody.create(HttpClient.JSON_MEDIA_TYPE, json.toString()));
    })));
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
