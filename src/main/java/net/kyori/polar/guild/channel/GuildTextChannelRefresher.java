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

import javax.inject.Singleton;
import net.kyori.kassel.channel.Channel;
import net.kyori.kassel.guild.Guild;
import net.kyori.kassel.guild.channel.event.GuildChannelNameChangeEvent;
import net.kyori.peppermint.Json;
import net.kyori.polar.refresh.RefreshContext;
import net.kyori.polar.refresh.Refresher;
import org.checkerframework.checker.nullness.qual.NonNull;

@Singleton
final class GuildTextChannelRefresher extends Refresher<GuildTextChannelImpl, GuildTextChannelRefresher.Context> {
  @Override
  protected void register() {
    this.field(GuildTextChannelImpl::name, json -> Json.needString(json, "name"), GuildTextChannelImpl::name, (context, oldName, newName) -> new GuildChannelNameChangeEvent() {
      @Override
      public @NonNull Guild guild() {
        return context.guild();
      }

      @Override
      public @NonNull Channel channel() {
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
  }

  interface Context extends RefreshContext<GuildTextChannelImpl> {
    @NonNull Guild guild();
  }
}
