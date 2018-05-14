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
package net.kyori.polar.guild;

import com.google.common.base.MoreObjects;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.assistedinject.Assisted;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.kyori.kassel.channel.Channel;
import net.kyori.kassel.channel.message.emoji.CustomEmoji;
import net.kyori.kassel.guild.Guild;
import net.kyori.kassel.guild.member.Member;
import net.kyori.kassel.guild.role.Role;
import net.kyori.kassel.snowflake.Snowflake;
import net.kyori.peppermint.Json;
import net.kyori.polar.channel.ChannelTypes;
import net.kyori.polar.channel.message.emoji.CustomEmojiImpl;
import net.kyori.polar.channel.message.emoji.Emojis;
import net.kyori.polar.refresh.Refreshable;
import net.kyori.polar.snowflake.SnowflakedImpl;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;
import java.util.function.LongConsumer;
import java.util.stream.Stream;

import javax.inject.Inject;

public final class GuildImpl extends SnowflakedImpl implements Guild, Refreshable {
  private final Long2ObjectMap<Channel> channels = new Long2ObjectOpenHashMap<>();
  final Long2ObjectMap<CustomEmoji> emojis = new Long2ObjectOpenHashMap<>();
  private final Long2ObjectMap<Member> members = new Long2ObjectOpenHashMap<>();
  private final Long2ObjectMap<Role> roles = new Long2ObjectOpenHashMap<>();
  private final GuildFactories factories;
  private final GuildRefresher refresher;
  private @NonNull String name;

  @Inject
  private GuildImpl(final GuildFactories factories, final GuildRefresher refresher, final @Assisted JsonObject json) {
    super(Json.needLong(json, "id"));

    this.factories = factories;
    this.refresher = refresher;

    this.name = Json.needString(json, "name");

    if(Json.isArray(json, "channels")) {
      this.readChannels(json.getAsJsonArray("channels"));
    }

    if(Json.isArray(json, "emojis")) {
      this.readEmojis(json.getAsJsonArray("emojis"));
    }

    if(Json.isArray(json, "roles")) {
      this.readRoles(json.getAsJsonArray("roles"));
    }

    if(Json.isArray(json, "members")) {
      this.readMembers(json.getAsJsonArray("members"));
    }
  }

  private void readChannels(final JsonArray channels) {
    for(final JsonElement channel : channels) {
      this.readChannel(channel.getAsJsonObject());
    }
  }

  private void readChannel(final JsonObject json) {
    this.putChannel(Json.needLong(json, "id"), json);
  }

  private void readEmojis(final JsonArray emojis) {
    for(final JsonElement emoji : emojis) {
      this.readEmoji(emoji.getAsJsonObject());
    }
  }

  private void readEmoji(final JsonObject json) {
    this.putEmoji(Json.needLong(json, "id"), json);
  }

  private void readRoles(final JsonArray roles) {
    for(final JsonElement role : roles) {
      this.putRole(role.getAsJsonObject());
    }
  }

  private void readMembers(final JsonArray members) {
    for(final JsonElement member : members) {
      this.putMember(member.getAsJsonObject());
    }
  }

  @Override
  public void refresh(final JsonElement json) {
    this.refresher.refresh(() -> this, json);
  }

  @Override
  public @NonNull String name() {
    return this.name;
  }

  void name(final @NonNull String name) {
    this.name = name;
  }

  @Override
  public @NonNull Stream<Channel> channels() {
    return this.channels.values().stream();
  }

  @Override
  public @NonNull Optional<Channel> channel(final @Snowflake long id) {
    return Optional.ofNullable(this.channels.get(id));
  }

  public @NonNull Optional<Channel> putChannel(final @Snowflake long id, final JsonObject json) {
    final @Nullable Channel channel;
    switch(Json.needInt(json, "type")) {
      case ChannelTypes.GUILD_CATEGORY: channel = this.factories.channelCategory(this, json); break;
      case ChannelTypes.GUILD_TEXT: channel = this.factories.textChannel(this, json); break;
      case ChannelTypes.GUILD_VOICE: channel = this.factories.voiceChannel(this, json); break;
      case ChannelTypes.DM: throw new UnsupportedOperationException("dm");
      case ChannelTypes.GROUP_DM: throw new UnsupportedOperationException("group_dm");
      default: throw new IllegalArgumentException(String.valueOf(Json.needInt(json, "type")));
    }

    if(channel != null) {
      this.channels.put(id, channel);
    }

    return Optional.ofNullable(channel);
  }

  public @NonNull Optional<Channel> removeChannel(final @Snowflake long id) {
    return Optional.ofNullable(this.channels.remove(id));
  }

  @Override
  public @NonNull Stream<CustomEmoji> emojis() {
    return this.emojis.values().stream();
  }

  @Override
  public @NonNull Optional<CustomEmoji> emoji(final @Snowflake long id) {
    return Optional.ofNullable(this.emojis.get(id));
  }

  private void putEmoji(final @Snowflake long id, final JsonObject json) {
    this.emojis.put(id, Emojis.custom(json));
  }

  public void refreshEmojis(final JsonArray emojis) {
    final EmojiRefresher refresher = new EmojiRefresher();

    for(final JsonElement emoji : emojis) {
      refresher.refresh(emoji.getAsJsonObject());
    }

    refresher.removeDead();
  }

  @Override
  public @NonNull Optional<Member> member(final @Snowflake long id) {
    return Optional.ofNullable(this.members.get(id));
  }

  public boolean requiresMemberChunking(final int expected) {
    return expected > this.members.size();
  }

  @Override
  public @NonNull Stream<Role> roles() {
    return this.roles.values().stream();
  }

  public @NonNull Member putMember(final JsonObject json) {
    final Member member = this.factories.member(this, json);
    this.members.put(Json.needLong(json.getAsJsonObject("user"), "id"), member);
    return member;
  }

  public @NonNull Optional<Member> removeMember(final @Snowflake long id) {
    return Optional.ofNullable(this.members.remove(id));
  }

  @Override
  public @NonNull Optional<Role> role(final @Snowflake long id) {
    return Optional.ofNullable(this.roles.get(id));
  }

  public @NonNull Role putRole(final JsonObject json) {
    final Role role = this.factories.role(this, json);
    this.roles.put(Json.needLong(json, "id"), role);
    return role;
  }

  public @NonNull Optional<Role> removeRole(final @Snowflake long id) {
    return Optional.ofNullable(this.roles.remove(id));
  }

  @Override
  protected MoreObjects.ToStringHelper toStringer() {
    return super.toStringer()
      .add("name", this.name);
  }

  public interface Factory {
    GuildImpl create(final @NonNull JsonObject json);
  }

  private final class EmojiRefresher {
    final Long2ObjectMap<CustomEmoji> emojis = GuildImpl.this.emojis;
    final LongSet encountered = new LongArraySet();

    void refresh(final JsonObject json) {
      final CustomEmojiImpl emoji = this.emoji(json);
      emoji.name(Json.needString(json, "name"));
    }

    private CustomEmojiImpl emoji(final JsonObject json) {
      final @Snowflake long id = Json.needLong(json, "id");
      this.encountered.add(id);
      @Nullable CustomEmoji emoji = this.emojis.get(id);
      if(emoji == null) {
        emoji = Emojis.custom(json);
        this.emojis.put(id, emoji);
      }
      return (CustomEmojiImpl) emoji;
    }

    void removeDead() {
      final LongSet removed = new LongArraySet();
      this.emojis.keySet().forEach((LongConsumer) id -> {
        if(!this.encountered.contains(id)) {
          removed.add(id);
        }
      });
      removed.forEach((LongConsumer) this.emojis::remove);
    }
  }
}
