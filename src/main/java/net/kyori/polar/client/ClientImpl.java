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
package net.kyori.polar.client;

import com.google.common.collect.MoreCollectors;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.kyori.kassel.Connectable;
import net.kyori.kassel.client.Client;
import net.kyori.kassel.guild.Guild;
import net.kyori.kassel.snowflake.Snowflake;
import net.kyori.kassel.user.Activity;
import net.kyori.kassel.user.Status;
import net.kyori.kassel.user.User;
import net.kyori.lunar.exception.Exceptions;
import net.kyori.peppermint.Json;
import net.kyori.polar.PolarConfiguration;
import net.kyori.polar.shard.Shard;
import net.kyori.polar.shard.ShardImpl;
import net.kyori.polar.user.UserImpl;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.inject.Inject;

public final class ClientImpl implements Client {
  private static final Logger LOGGER = LoggerFactory.getLogger(ClientImpl.class);
  private final Long2ObjectMap<User> users = new Long2ObjectOpenHashMap<>();
  private final UserImpl.Factory userFactory;
  private final List<Shard> shards;
  // Presence
  private @NonNull Status status = Status.ONLINE;
  private @Nullable Activity activityType;
  private @Nullable String activityName;

  @Inject
  private ClientImpl(final PolarConfiguration configuration, final UserImpl.Factory userFactory, final ShardImpl.Factory shard) {
    this.userFactory = userFactory;
    this.shards = new ArrayList<>(configuration.shards());
    for(int i = 0; i < configuration.shards(); i++) {
      this.shards.add(i, shard.create(i));
    }
  }

  @Override
  public void connect() {
    LOGGER.debug("Connecting shards...");
    this.shards.forEach(Exceptions.rethrowConsumer(Connectable::connect));
  }

  @Override
  public void disconnect() {
    LOGGER.debug("Disconnecting shards...");
    this.shards.forEach(Exceptions.rethrowConsumer(Connectable::disconnect));
  }

  @Override
  public @NonNull Stream<Guild> guilds() {
    return this.shards.stream()
      .flatMap(Shard::guilds);
  }

  @Override
  public @NonNull Optional<Guild> guild(final @Snowflake long id) {
    return this.shards.stream()
      .map(shard -> shard.guild(id))
      .filter(Optional::isPresent)
      .map(Optional::get)
      .collect(MoreCollectors.toOptional());
  }

  @Override
  public @NonNull Optional<User> user(final @Snowflake long id) {
    return Optional.ofNullable(this.users.get(id));
  }

  @Override
  public void status(final @NonNull Status status) {
    this.status = status;
    this.presenceChanged();
  }

  @Override
  public void activity(final @Nullable Activity activityType, final @Nullable String activityName) {
    this.activityType = activityType;
    this.activityName = activityName;
    this.presenceChanged();
  }

  private void presenceChanged() {
    this.shards.forEach(shard -> shard.presence(this.status, this.activityType, this.activityName));
  }

  public @NonNull User userOrCreate(final JsonObject json) {
    return this.user(Json.needLong(json, "id")).orElseGet(() -> {
      final User user = this.userFactory.create(json);
      this.users.put(Json.needLong(json, "id"), user);
      return user;
    });
  }
}
