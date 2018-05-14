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
package net.kyori.polar.channel.message;

import com.google.common.base.MoreObjects;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.assistedinject.Assisted;
import net.kyori.kassel.channel.Channel;
import net.kyori.kassel.channel.message.Message;
import net.kyori.kassel.channel.message.embed.Embed;
import net.kyori.kassel.channel.message.emoji.Emoji;
import net.kyori.kassel.guild.Guild;
import net.kyori.kassel.guild.channel.GuildChannel;
import net.kyori.kassel.guild.role.Role;
import net.kyori.kassel.user.User;
import net.kyori.peppermint.Json;
import net.kyori.polar.ForPolar;
import net.kyori.polar.channel.message.embed.EmbedImpl;
import net.kyori.polar.client.ClientImpl;
import net.kyori.polar.http.HttpClient;
import net.kyori.polar.http.RateLimitedHttpClient;
import net.kyori.polar.http.endpoint.Endpoints;
import net.kyori.polar.refresh.Refreshable;
import net.kyori.polar.snowflake.SnowflakedImpl;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;

import javax.inject.Inject;

public final class MessageImpl extends SnowflakedImpl implements Message, Refreshable {
  private final MessageRefresher refresher;
  private final ExecutorService executor;
  private final RateLimitedHttpClient httpClient;
  private final Gson gson;
  private final ClientImpl client;
  private final Channel channel;
  private @NonNull User author;
  private @NonNull String content;
  private @NonNull List<Embed> embeds;
  private final Set<User> mentionedUsers;
  private final Set<Role> mentionedRoles;

  @Inject
  private MessageImpl(final MessageRefresher refresher, final ExecutorService executor, final RateLimitedHttpClient httpClient, final @ForPolar Gson gson, final ClientImpl client, final @Assisted Channel channel, final @Assisted JsonObject json) {
    super(Json.needLong(json, "id"));
    this.refresher = refresher;
    this.executor = executor;
    this.httpClient = httpClient;
    this.gson = gson;
    this.client = client;
    this.channel = channel;
    this.author = client.userOrCreate(json.getAsJsonObject("author"));
    this.content = Json.needString(json, "content");

    if(Json.isArray(json, "embeds")) {
      final JsonArray embeds = json.getAsJsonArray("embeds");
      this.embeds = new ArrayList<>(embeds.size());
      for(final JsonElement embed : embeds) {
        this.embeds.add(new EmbedImpl(embed.getAsJsonObject()));
      }
    } else {
      this.embeds = Collections.emptyList();
    }

    if(Json.isArray(json, "mentions")) {
      final JsonArray mentions = json.getAsJsonArray("mentions");
      if(mentions.size() > 0) {
        this.mentionedUsers = new HashSet<>(mentions.size());
        for(final JsonElement mention : mentions) {
          this.mentionedUsers.add(client.userOrCreate(mention.getAsJsonObject()));
        }
      } else {
        this.mentionedUsers = Collections.emptySet();
      }
    } else {
      this.mentionedUsers = Collections.emptySet();
    }

    if(Json.isArray(json, "mention_roles")) {
      final JsonArray mentionRoles = json.getAsJsonArray("mention_roles");
      if(mentionRoles.size() > 0 && channel instanceof GuildChannel) {
        final Guild guild = ((GuildChannel) channel).guild();
        this.mentionedRoles = new HashSet<>(mentionRoles.size());
        for(final JsonElement mentionRole : mentionRoles) {
          guild.role(Json.needLong(mentionRole, "id")).ifPresent(this.mentionedRoles::add);
        }
      } else {
        this.mentionedRoles = Collections.emptySet();
      }
    } else {
      this.mentionedRoles = Collections.emptySet();
    }
  }

  @Override
  public void refresh(final JsonElement json) {
    this.refresher.refresh(new MessageRefresher.Context() {
      @Override
      public @NonNull Channel channel() {
        return MessageImpl.this.channel;
      }

      @Override
      public MessageImpl target() {
        return MessageImpl.this;
      }
    }, json);
  }

  @Override
  public @NonNull User author() {
    return this.author;
  }

  @Override
  public @NonNull String content() {
    return this.content;
  }

  void content(final @NonNull String content) {
    this.content = content;
  }

  @Override
  public @NonNull List<Embed> embeds() {
    return this.embeds;
  }

  @Override
  public @NonNull Stream<User> mentionedUsers() {
    return this.mentionedUsers.stream();
  }

  @Override
  public @NonNull Stream<Role> mentionedRoles() {
    return this.mentionedRoles.stream();
  }

  @Override
  public @NonNull Reactions reactions() {
    return new Reactions() {
      @Override
      public void add(final @NonNull Emoji emoji) {
        MessageImpl.this.executor.submit(() -> MessageImpl.this.httpClient.json(Endpoints.addReaction(MessageImpl.this.channel.id(), MessageImpl.this.id, emoji).request(builder -> builder.put(RequestBody.create(null, new byte[0])))));
      }

      @Override
      public void remove(final @NonNull Emoji emoji) {
        MessageImpl.this.executor.submit(() -> MessageImpl.this.httpClient.json(Endpoints.deleteReaction(MessageImpl.this.channel.id(), MessageImpl.this.id, emoji).request(Request.Builder::delete)));
      }

      @Override
      public void remove(final @NonNull User user, final @NonNull Emoji emoji) {
        MessageImpl.this.executor.submit(() -> MessageImpl.this.httpClient.json(Endpoints.deleteReaction(MessageImpl.this.channel.id(), MessageImpl.this.id, user, emoji).request(Request.Builder::delete)));
      }

      @Override
      public void removeAll() {
        MessageImpl.this.executor.submit(() -> MessageImpl.this.httpClient.json(Endpoints.deleteReactions(MessageImpl.this.channel.id(), MessageImpl.this.id).request(Request.Builder::delete)));
      }
    };
  }

  @Override
  public void edit(final @NonNull Edit edit) {
    this.executor.submit(() -> this.httpClient.json(Endpoints.editMessage(this.channel.id(), this.id).request(builder -> {
      final JsonObject json = this.gson.toJsonTree(edit).getAsJsonObject();
      // message must have content
      if(!json.has("content")) {
        json.addProperty("content", this.content);
      } else if(json.get("content").isJsonNull()) {
        json.addProperty("content", "");
      }
      if(json.has("embed")) {
        json.getAsJsonObject("embed").addProperty("type", "rich");
      }
      builder.patch(RequestBody.create(HttpClient.JSON_MEDIA_TYPE, json.toString()));
    })));
  }

  @Override
  public void delete() {
    this.executor.submit(() -> this.httpClient.json(Endpoints.deleteMessage(this.channel.id(), this.id).request(Request.Builder::delete)));
  }

  void embeds(final @NonNull List<Embed> embeds) {
    this.embeds = embeds;
  }

  @Override
  protected MoreObjects.ToStringHelper toStringer() {
    return super.toStringer()
      .add("author", this.author)
      .add("content", this.content)
      .add("embeds", this.embeds);
  }

  public interface Factory {
    MessageImpl create(final Channel channel, final JsonObject json);
  }
}
