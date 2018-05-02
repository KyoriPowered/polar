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
package net.kyori.polar.guild.channel;

import com.google.common.base.MoreObjects;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.assistedinject.Assisted;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.kyori.kassel.channel.message.Message;
import net.kyori.kassel.channel.message.embed.Embed;
import net.kyori.kassel.guild.Guild;
import net.kyori.kassel.guild.channel.GuildTextChannel;
import net.kyori.kassel.snowflake.Snowflake;
import net.kyori.peppermint.Json;
import net.kyori.polar.channel.message.MessageImpl;
import net.kyori.polar.http.HttpClient;
import net.kyori.polar.http.RateLimitedHttpClient;
import net.kyori.polar.http.endpoint.Endpoints;
import net.kyori.polar.refresh.Refreshable;
import net.kyori.polar.snowflake.SnowflakedImpl;
import net.kyori.polar.util.BoundedLong2ObjectLinkedOpenHashMap;
import okhttp3.RequestBody;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkState;

public final class GuildTextChannelImpl extends SnowflakedImpl implements GuildTextChannel, Refreshable {
  private static final int MAX_CACHED_MESSAGES = 20;
  private final Long2ObjectMap<Message> messages = BoundedLong2ObjectLinkedOpenHashMap.sync(MAX_CACHED_MESSAGES);
  private final GuildTextChannelRefresher refresher;
  private final ExecutorService executor;
  private final RateLimitedHttpClient httpClient;
  private final Gson gson;
  private final MessageImpl.Factory messageFactory;
  private final Guild guild;
  private @NonNull String name;
  private @Nullable String topic;

  @Inject
  private GuildTextChannelImpl(final GuildTextChannelRefresher refresher, final ExecutorService executor, final RateLimitedHttpClient httpClient, final Gson gson, final MessageImpl.Factory messageFactory, final @Assisted Guild guild, final @Assisted JsonObject json) {
    super(Json.needLong(json, "id"));
    this.refresher = refresher;
    this.executor = executor;
    this.httpClient = httpClient;
    this.gson = gson;
    this.messageFactory = messageFactory;
    this.guild = guild;
    this.name = Json.needString(json, "name");
    this.topic = Json.getString(json, "topic", null);
  }

  @Override
  public void refresh(final JsonElement json) {
    this.refresher.refresh(new GuildTextChannelRefresher.Context() {
      @Override
      public @NonNull Guild guild() {
        return GuildTextChannelImpl.this.guild;
      }

      @Override
      public GuildTextChannelImpl target() {
        return GuildTextChannelImpl.this;
      }
    }, json);
  }

  @Override
  public @NonNull Guild guild() {
    return this.guild;
  }

  @Override
  public @NonNull String name() {
    return this.name;
  }

  void name(final @NonNull String name) {
    this.name = name;
  }

  @Override
  public @NonNull Optional<String> topic() {
    return Optional.ofNullable(this.topic);
  }

  @Override
  public @NonNull Optional<Message> message(final @Snowflake long id) {
    return Optional.ofNullable(this.messages.get(id));
  }

  public void putMessage(final @Snowflake long id, final Message message) {
    this.messages.put(id, message);
  }

  public @NonNull Optional<Message> removeMessage(final @Snowflake long id) {
    return Optional.ofNullable(this.messages.remove(id));
  }

  @Override
  public @NonNull CompletableFuture<Message> message(final @Nullable String content, final @Nullable Embed embed) {
    checkState(!(content == null && embed == null), "content and embed are both null");
    final JsonObject json = new JsonObject();
    if(content != null) {
      checkState(content.length() <= Message.MAXIMUM_LENGTH, "content too long; %s > %s", content.length(), Message.MAXIMUM_LENGTH);
      json.addProperty("content", content);
    } else {
      json.addProperty("content", "");
    }
    if(embed != null) {
      json.add("embed", this.gson.toJsonTree(embed));
    }
    final CompletableFuture<Message> future = new CompletableFuture<>();
    this.executor.submit(() -> this.httpClient
      .json(Endpoints.sendMessage(this.id).request(builder -> builder.post(RequestBody.create(HttpClient.JSON_MEDIA_TYPE, json.toString()))))
      .whenComplete((element, throwable) -> {
        if(throwable != null) {
          future.completeExceptionally(throwable);
        } else {
          element.map(JsonElement::getAsJsonObject)
            .ifPresent(object -> future.complete(GuildTextChannelImpl.this.messageFactory.create(GuildTextChannelImpl.this, object)));
        }
      }));
    return future;
  }

  @Override
  protected MoreObjects.ToStringHelper toStringer() {
    return super.toStringer()
      .add("name", this.name)
      .add("topic", this.topic);
  }

  public interface Factory {
    GuildTextChannelImpl create(final Guild guild, final JsonObject json);
  }
}
