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
package net.kyori.polar.guild.member;

import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.kyori.kassel.guild.Guild;
import net.kyori.kassel.guild.member.Member;
import net.kyori.kassel.guild.member.event.GuildMemberNickChangeEvent;
import net.kyori.kassel.guild.member.event.GuildMemberRoleAddEvent;
import net.kyori.kassel.guild.member.event.GuildMemberRoleRemoveEvent;
import net.kyori.kassel.guild.role.Role;
import net.kyori.peppermint.Json;
import net.kyori.polar.refresh.RefreshContext;
import net.kyori.polar.refresh.Refresher;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Singleton;

@Singleton
final class MemberRefresher extends Refresher<MemberImpl, MemberRefresher.Context> {
  @Override
  protected void register() {
    this.field(MemberImpl::nick, json -> Optional.ofNullable(Json.getString(json, "nick", null)), MemberImpl::nick, (context, oldNick, newNick) -> new GuildMemberNickChangeEvent() {
      @Override
      public @NonNull Guild guild() {
        return context.guild();
      }

      @Override
      public @NonNull Member member() {
        return context.target();
      }

      @Override
      public @NonNull Optional<String> oldNick() {
        return oldNick;
      }

      @Override
      public @NonNull Optional<String> newNick() {
        return newNick;
      }
    });
    this.complexField(member -> new LongArraySet(member.roles), json -> {
      final LongSet roles = new LongArraySet();
      for(final JsonElement role : json.getAsJsonArray("roles")) {
        roles.add(Json.needLong(role, "id"));
      }
      return roles;
    }, MemberImpl::roles, (context, oldValue, newValue) -> {
      final Guild guild = context.guild();
      final Member member = context.target();
      final Set<Role> oldRoles = Arrays.stream(oldValue.toLongArray())
        .mapToObj(guild::role)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toSet());
      final Set<Role> newRoles = Arrays.stream(newValue.toLongArray())
        .mapToObj(guild::role)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toSet());
      Sets.difference(newRoles, oldRoles).forEach(role -> MemberRefresher.this.bus.post(new GuildMemberRoleAddEvent() {
        @Override
        public @NonNull Guild guild() {
          return guild;
        }

        @Override
        public @NonNull Member member() {
          return member;
        }

        @Override
        public @NonNull Role role() {
          return role;
        }
      }));
      Sets.difference(oldRoles, newRoles).forEach(role -> MemberRefresher.this.bus.post(new GuildMemberRoleRemoveEvent() {
        @Override
        public @NonNull Guild guild() {
          return guild;
        }

        @Override
        public @NonNull Member member() {
          return member;
        }

        @Override
        public @NonNull Role role() {
          return role;
        }
      }));
    });
  }

  interface Context extends RefreshContext<MemberImpl> {
    @NonNull Guild guild();
  }
}
