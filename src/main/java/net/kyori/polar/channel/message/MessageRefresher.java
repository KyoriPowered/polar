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

import com.google.gson.JsonElement;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Singleton;
import net.kyori.kassel.channel.Channel;
import net.kyori.kassel.channel.message.Message;
import net.kyori.kassel.channel.message.embed.Embed;
import net.kyori.kassel.channel.message.event.ChannelMessageContentChangeEvent;
import net.kyori.kassel.channel.message.event.ChannelMessageEmbedsChangeEvent;
import net.kyori.peppermint.Json;
import net.kyori.polar.channel.message.embed.EmbedImpl;
import net.kyori.polar.refresh.RefreshContext;
import net.kyori.polar.refresh.Refresher;
import org.checkerframework.checker.nullness.qual.NonNull;

@Singleton
final class MessageRefresher extends Refresher<MessageImpl, MessageRefresher.Context> {
  @Override
  protected void register() {
    this.field(MessageImpl::content, json -> Json.isString(json, "content"), json -> Json.needString(json, "content"), MessageImpl::content, (context, oldContent, newContent) -> new ChannelMessageContentChangeEvent() {
      @Override
      public @NonNull Channel channel() {
        return context.channel();
      }

      @Override
      public @NonNull Message message() {
        return context.target();
      }

      @Override
      public @NonNull String oldContent() {
        return oldContent;
      }

      @Override
      public @NonNull String newContent() {
        return newContent;
      }
    });
    this.field(MessageImpl::embeds, json -> {
      final List<Embed> embeds = new ArrayList<>(json.getAsJsonArray("embeds").size());
      for(final JsonElement embed : json.getAsJsonArray("embeds")) {
        embeds.add(new EmbedImpl(embed.getAsJsonObject()));
      }
      return embeds;
    }, MessageImpl::embeds, (context, oldEmbeds, newEmbeds) -> new ChannelMessageEmbedsChangeEvent() {
      @Override
      public @NonNull Channel channel() {
        return context.channel();
      }

      @Override
      public @NonNull Message message() {
        return context.target();
      }

      @Override
      public @NonNull List<Embed> oldEmbeds() {
        return oldEmbeds;
      }

      @Override
      public @NonNull List<Embed> newEmbeds() {
        return newEmbeds;
      }
    });
  }

  interface Context extends RefreshContext<MessageImpl> {
    @NonNull Channel channel();
  }
}
