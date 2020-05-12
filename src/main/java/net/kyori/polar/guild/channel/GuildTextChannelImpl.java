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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.assistedinject.Assisted;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import net.kyori.kassel.channel.message.Message;
import net.kyori.kassel.channel.message.embed.Embed;
import net.kyori.kassel.guild.Guild;
import net.kyori.kassel.guild.channel.GuildTextChannel;
import net.kyori.kassel.snowflake.Snowflake;
import net.kyori.mu.Maybe;
import net.kyori.peppermint.Json;
import net.kyori.polar.channel.TextChannelImpl;
import net.kyori.polar.refresh.Refreshable;
import net.kyori.polar.snowflake.SnowflakedImpl;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class GuildTextChannelImpl extends SnowflakedImpl implements GuildTextChannel, Refreshable {
  private static final int MAX_CACHED_MESSAGES = 20;
  private final GuildTextChannelRefresher refresher;
  private final TextChannelImpl textChannel;
  private final Guild guild;
  private @NonNull String name;
  private @Nullable String topic;

  @Inject
  private GuildTextChannelImpl(final GuildTextChannelRefresher refresher, final TextChannelImpl.Factory textChannel, final @Assisted Guild guild, final @Assisted JsonObject json) {
    super(Json.needLong(json, "id"));
    this.refresher = refresher;
    this.textChannel = textChannel.create(this, MAX_CACHED_MESSAGES);
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
      public @NonNull GuildTextChannelImpl target() {
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
  public @NonNull Maybe<String> topic() {
    return Maybe.maybe(this.topic);
  }

  @Override
  public @NonNull Maybe<Message> message(final @Snowflake long id) {
    return this.textChannel.message(id);
  }

  public void putMessage(final @Snowflake long id, final Message message) {
    this.textChannel.putMessage(id, message);
  }

  public @NonNull Maybe<Message> removeMessage(final @Snowflake long id) {
    return this.textChannel.removeMessage(id);
  }

  @Override
  public @NonNull CompletableFuture<Message> message(final @Nullable String content, final @Nullable Embed embed) {
    return this.textChannel.message(content, embed);
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
