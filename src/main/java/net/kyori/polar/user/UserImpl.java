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
package net.kyori.polar.user;

import com.google.common.base.MoreObjects;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.assistedinject.Assisted;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import net.kyori.kassel.channel.PrivateChannel;
import net.kyori.kassel.user.User;
import net.kyori.mu.Maybe;
import net.kyori.peppermint.Json;
import net.kyori.polar.client.ClientImpl;
import net.kyori.polar.refresh.Refreshable;
import net.kyori.polar.snowflake.SnowflakedImpl;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class UserImpl extends SnowflakedImpl implements Refreshable, User {
  private final UserRefresher refresher;
  private final ClientImpl client;
  private @NonNull String username;
  private @NonNull String discriminator;
  private @Nullable String avatar;
  private boolean bot;
  private @Nullable PrivateChannel channel;

  @Inject
  private UserImpl(final UserRefresher refresher, final ClientImpl client, final @Assisted JsonObject json) {
    super(Json.needLong(json, "id"));
    this.refresher = refresher;
    this.client = client;
    this.username = Json.needString(json, "username");
    this.discriminator = Json.needString(json, "discriminator");
    this.avatar = Json.getString(json, "avatar", null);
    this.bot = Json.getBoolean(json, "bot", false);
  }

  @Override
  public void refresh(final JsonElement json) {
    this.refresher.refresh(() -> this, json);
  }

  @Override
  public @NonNull String username() {
    return this.username;
  }

  void username(final @NonNull String username) {
    this.username = username;
  }

  @Override
  public @NonNull String discriminator() {
    return this.discriminator;
  }

  void discriminator(final @NonNull String discriminator) {
    this.discriminator = discriminator;
  }

  @Override
  public @NonNull Maybe<String> avatar() {
    return Maybe.maybe(this.avatar);
  }

  void avatar(final @NonNull Maybe<String> avatar) {
    this.avatar = avatar.orDefault(null);
  }

  @Override
  public boolean bot() {
    return this.bot;
  }

  @Override
  public @NonNull CompletableFuture<PrivateChannel> channel() {
    if(this.channel != null) {
      return CompletableFuture.completedFuture(this.channel);
    }
    return this.client.requestPrivateChannel(this);
  }

  public void channel(final PrivateChannel channel) {
    this.channel = channel;
  }

  @Override
  protected MoreObjects.ToStringHelper toStringer() {
    return super.toStringer()
      .add("username", this.username)
      .add("discriminator", this.discriminator)
      .add("avatar", this.avatar)
      .add("bot", this.bot);
  }

  public interface Factory {
    UserImpl create(final JsonObject json);
  }
}
