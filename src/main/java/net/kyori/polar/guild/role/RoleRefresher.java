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
package net.kyori.polar.guild.role;

import java.awt.Color;
import javax.inject.Singleton;
import net.kyori.kassel.guild.Guild;
import net.kyori.kassel.guild.role.Role;
import net.kyori.kassel.guild.role.event.GuildRoleColorChangeEvent;
import net.kyori.kassel.guild.role.event.GuildRoleHoistChangeEvent;
import net.kyori.kassel.guild.role.event.GuildRoleManagedChangeEvent;
import net.kyori.kassel.guild.role.event.GuildRoleMentionableChangeEvent;
import net.kyori.kassel.guild.role.event.GuildRoleNameChangeEvent;
import net.kyori.mu.Maybe;
import net.kyori.peppermint.Json;
import net.kyori.polar.refresh.RefreshContext;
import net.kyori.polar.refresh.Refresher;
import net.kyori.polar.util.Colors;
import org.checkerframework.checker.nullness.qual.NonNull;

@Singleton
final class RoleRefresher extends Refresher<RoleImpl, RoleRefresher.Context> {
  @Override
  protected void register() {
    this.field(RoleImpl::name, json -> Json.needString(json, "name"), RoleImpl::name, (context, oldName, newName) -> new GuildRoleNameChangeEvent() {
      @Override
      public @NonNull Guild guild() {
        return context.guild();
      }

      @Override
      public @NonNull Role role() {
        return context.target();
      }

      @Override
      public @NonNull String oldName() {
        return oldName;
      }

      @Override
      public @NonNull String newName() {
        return newName;
      }
    });
    this.field(RoleImpl::color, json -> Maybe.maybe(Colors.color(Json.needInt(json, "color"))), RoleImpl::color, (context, oldColor, newColor) -> new GuildRoleColorChangeEvent() {
      @Override
      public @NonNull Guild guild() {
        return context.guild();
      }

      @Override
      public @NonNull Role role() {
        return context.target();
      }

      @Override
      public @NonNull Maybe<Color> oldColor() {
        return oldColor;
      }

      @Override
      public @NonNull Maybe<Color> newColor() {
        return newColor;
      }
    });
    this.field(RoleImpl::mentionable, json -> Json.needBoolean(json, "mentionable"), RoleImpl::mentionable, (context, oldMentionable, newMentionable) -> new GuildRoleMentionableChangeEvent() {
      @Override
      public @NonNull Guild guild() {
        return context.guild();
      }

      @Override
      public @NonNull Role role() {
        return context.target();
      }

      @Override
      public boolean oldMentionable() {
        return oldMentionable;
      }

      @Override
      public boolean newMentionable() {
        return newMentionable;
      }
    });
    this.field(RoleImpl::managed, json -> Json.needBoolean(json, "managed"), RoleImpl::managed, (context, oldManaged, newManaged) -> new GuildRoleManagedChangeEvent() {
      @Override
      public @NonNull Guild guild() {
        return context.guild();
      }

      @Override
      public @NonNull Role role() {
        return context.target();
      }

      @Override
      public boolean oldManaged() {
        return oldManaged;
      }

      @Override
      public boolean newManaged() {
        return newManaged;
      }
    });
    this.field(RoleImpl::hoist, json -> Json.needBoolean(json, "hoist"), RoleImpl::hoist, (context, oldHoist, newHoist) -> new GuildRoleHoistChangeEvent() {
      @Override
      public @NonNull Guild guild() {
        return context.guild();
      }

      @Override
      public @NonNull Role role() {
        return context.target();
      }

      @Override
      public boolean oldHoist() {
        return oldHoist;
      }

      @Override
      public boolean newHoist() {
        return newHoist;
      }
    });
  }

  interface Context extends RefreshContext<RoleImpl> {
    @NonNull Guild guild();
  }
}
