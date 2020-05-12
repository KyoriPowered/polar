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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.kyori.kassel.Connectable;
import net.kyori.kassel.channel.Channel;
import net.kyori.kassel.channel.PrivateChannel;
import net.kyori.kassel.client.Client;
import net.kyori.kassel.guild.Guild;
import net.kyori.kassel.snowflake.Snowflake;
import net.kyori.kassel.user.Activity;
import net.kyori.kassel.user.Status;
import net.kyori.kassel.user.User;
import net.kyori.mu.Composer;
import net.kyori.mu.Maybe;
import net.kyori.mu.function.ThrowingConsumer;
import net.kyori.peppermint.Json;
import net.kyori.polar.PolarConfiguration;
import net.kyori.polar.channel.Channels;
import net.kyori.polar.channel.PrivateChannelImpl;
import net.kyori.polar.http.HttpClient;
import net.kyori.polar.http.RateLimitedHttpClient;
import net.kyori.polar.http.endpoint.Endpoints;
import net.kyori.polar.shard.Shard;
import net.kyori.polar.shard.ShardImpl;
import net.kyori.polar.user.UserImpl;
import okhttp3.RequestBody;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public final class ClientImpl implements Client {
  private static final Logger LOGGER = LoggerFactory.getLogger(ClientImpl.class);

  private final ExecutorService executor;
  private final RateLimitedHttpClient httpClient;

  private final List<Shard> shards;

  // Users
  private final Long2ObjectMap<User> users = new Long2ObjectOpenHashMap<>();
  private final UserImpl.Factory userFactory;

  // Channels
  private final Long2ObjectMap<Channel> channels = new Long2ObjectOpenHashMap<>();
  private final PrivateChannelImpl.Factory channelFactory;

  // Presence
  private @NonNull Status status = Status.ONLINE;
  private @Nullable Activity activityType;
  private @Nullable String activityName;

  @Inject
  private ClientImpl(final PolarConfiguration configuration, final ShardImpl.Factory shard, final UserImpl.Factory userFactory, final ExecutorService executor, final RateLimitedHttpClient httpClient, final PrivateChannelImpl.Factory channelFactory) {
    this.userFactory = userFactory;
    this.executor = executor;
    this.httpClient = httpClient;
    this.channelFactory = channelFactory;
    this.shards = new ArrayList<>(configuration.shards());
    for(int i = 0; i < configuration.shards(); i++) {
      this.shards.add(i, shard.create(i));
    }
  }

  @Override
  public void connect() {
    LOGGER.debug("Connecting shards...");
    this.shards.forEach(ThrowingConsumer.of(Connectable::connect));
  }

  @Override
  public void disconnect() {
    LOGGER.debug("Disconnecting shards...");
    this.shards.forEach(ThrowingConsumer.of(Connectable::disconnect));
  }

  @Override
  public @NonNull Stream<Guild> guilds() {
    return this.shards.stream()
      .flatMap(Shard::guilds);
  }

  @Override
  public @NonNull Maybe<Guild> guild(final @Snowflake long id) {
    return this.shards.stream()
      .map(shard -> shard.guild(id))
      .filter(Maybe::isJust)
      .map(Maybe::orThrow)
      .collect(Maybe.collector());
  }

  @Override
  public @NonNull Maybe<User> user(final @Snowflake long id) {
    return Maybe.maybe(this.users.get(id));
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

  public @NonNull ExecutorService executor() {
    return this.executor;
  }

  public @NonNull RateLimitedHttpClient httpClient() {
    return this.httpClient;
  }

  public @NonNull User userOrCreate(final JsonObject json) {
    final @Snowflake long id = Json.needLong(json, "id");
    return this.user(id).orGet(() -> {
      final User user = this.userFactory.create(json);
      this.users.put(id, user);
      return user;
    });
  }

  public Maybe<Channel> channel(final @Snowflake long id) {
    return Maybe.maybe(this.channels.get(id));
  }

  public CompletableFuture<PrivateChannel> requestPrivateChannel(final UserImpl user) {
    final CompletableFuture<PrivateChannel> future = new CompletableFuture<>();
    this.executor.submit(() -> {
      this.httpClient
        .json(Endpoints.createPrivateChannel().request(builder -> builder.post(RequestBody.create(HttpClient.JSON_MEDIA_TYPE, Composer.accept(new JsonObject(), object -> object.addProperty("recipient_id", user.id())).toString()))))
        .whenComplete((element, throwable) -> {
          if(throwable != null) {
            future.completeExceptionally(throwable);
          } else {
            element.map(JsonElement::getAsJsonObject)
              .ifJust(object -> {
                final @Snowflake long id = Channels.firstRecipient(object);
                future.complete(this.privateChannel(user, id));
              });
          }
        });
    });
    return future;
  }

  public PrivateChannel privateChannel(final UserImpl user, final @Snowflake long id) {
    final PrivateChannel channel = this.channelFactory.create(id);
    this.channels.put(id, channel);
    user.channel(channel);
    return channel;
  }
}
