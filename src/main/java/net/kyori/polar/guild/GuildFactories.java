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

import com.google.gson.JsonObject;
import net.kyori.kassel.channel.ChannelCategory;
import net.kyori.kassel.guild.Guild;
import net.kyori.kassel.guild.channel.GuildTextChannel;
import net.kyori.kassel.guild.channel.GuildVoiceChannel;
import net.kyori.kassel.guild.member.Member;
import net.kyori.kassel.guild.role.Role;
import net.kyori.polar.guild.channel.GuildChannelCategoryImpl;
import net.kyori.polar.guild.channel.GuildTextChannelImpl;
import net.kyori.polar.guild.channel.GuildVoiceChannelImpl;
import net.kyori.polar.guild.member.MemberImpl;
import net.kyori.polar.guild.role.RoleImpl;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.inject.Inject;
import javax.inject.Singleton;

public interface GuildFactories {
  @NonNull ChannelCategory channelCategory(final @NonNull Guild guild, final @NonNull JsonObject json);

  @NonNull GuildTextChannel textChannel(final @NonNull Guild guild, final @NonNull JsonObject json);

  @NonNull GuildVoiceChannel voiceChannel(final @NonNull Guild guild, final @NonNull JsonObject json);

  @NonNull Member member(final @NonNull Guild guild, final @NonNull JsonObject json);

  @NonNull Role role(final @NonNull Guild guild, final @NonNull JsonObject json);
}

@Singleton
final class GuildFactoriesImpl implements GuildFactories {
  private final GuildTextChannelImpl.Factory textChannel;
  private final GuildVoiceChannelImpl.Factory voiceChannel;
  private final MemberImpl.Factory member;
  private final RoleImpl.Factory role;

  @Inject
  private GuildFactoriesImpl(final GuildTextChannelImpl.Factory textChannel, final GuildVoiceChannelImpl.Factory voiceChannel, final MemberImpl.Factory member, final RoleImpl.Factory role) {
    this.textChannel = textChannel;
    this.voiceChannel = voiceChannel;
    this.member = member;
    this.role = role;
  }

  @Override
  public @NonNull ChannelCategory channelCategory(final @NonNull Guild guild, final @NonNull JsonObject json) {
    return new GuildChannelCategoryImpl(guild, json);
  }

  @Override
  public @NonNull GuildTextChannel textChannel(final @NonNull Guild guild, final @NonNull JsonObject json) {
    return this.textChannel.create(guild, json);
  }

  @Override
  public @NonNull GuildVoiceChannel voiceChannel(final @NonNull Guild guild, final @NonNull JsonObject json) {
    return this.voiceChannel.create(guild, json);
  }

  @Override
  public @NonNull Member member(final @NonNull Guild guild, final @NonNull JsonObject json) {
    return this.member.create(guild, json);
  }

  @Override
  public @NonNull Role role(final @NonNull Guild guild, final @NonNull JsonObject json) {
    return this.role.create(guild, json);
  }
}
