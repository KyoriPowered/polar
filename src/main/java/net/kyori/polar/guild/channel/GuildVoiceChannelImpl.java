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
import javax.inject.Inject;
import net.kyori.kassel.guild.Guild;
import net.kyori.kassel.guild.channel.GuildVoiceChannel;
import net.kyori.peppermint.Json;
import net.kyori.polar.refresh.Refreshable;
import net.kyori.polar.snowflake.SnowflakedImpl;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class GuildVoiceChannelImpl extends SnowflakedImpl implements GuildVoiceChannel, Refreshable {
  private final GuildVoiceChannelRefresher refresher;
  private final Guild guild;
  private @NonNull String name;

  @Inject
  private GuildVoiceChannelImpl(final GuildVoiceChannelRefresher refresher, final @Assisted Guild guild, final @Assisted JsonObject json) {
    super(Json.needLong(json, "id"));
    this.refresher = refresher;
    this.guild = guild;
    this.name = Json.needString(json, "name");
  }

  @Override
  public void refresh(final JsonElement json) {
    this.refresher.refresh(new GuildVoiceChannelRefresher.Context() {
      @Override
      public @NonNull Guild guild() {
        return GuildVoiceChannelImpl.this.guild;
      }

      @Override
      public GuildVoiceChannelImpl target() {
        return GuildVoiceChannelImpl.this;
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
  protected MoreObjects.ToStringHelper toStringer() {
    return super.toStringer()
      .add("name", this.name);
  }

  public interface Factory {
    GuildVoiceChannelImpl create(final Guild guild, final JsonObject json);
  }
}
