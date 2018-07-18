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
package net.kyori.polar.channel;

import com.google.gson.JsonObject;
import com.google.inject.assistedinject.Assisted;
import net.kyori.kassel.channel.PrivateChannel;
import net.kyori.kassel.channel.TextChannel;
import net.kyori.kassel.channel.message.Message;
import net.kyori.kassel.channel.message.embed.Embed;
import net.kyori.kassel.snowflake.Snowflake;
import net.kyori.peppermint.Json;
import net.kyori.polar.snowflake.SnowflakedImpl;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

public class PrivateChannelImpl extends SnowflakedImpl implements PrivateChannel {
  private static final int MAX_CACHED_MESSAGES = 20;
  private final TextChannel textChannel;

  public static @Snowflake long id(final JsonObject object) {
    final JsonObject recipient;
    if(object.has("recipients")) {
      recipient = object.getAsJsonArray("recipients").get(0).getAsJsonObject();
    } else {
      recipient = object.getAsJsonObject("recipient");
    }
    return Json.needLong(recipient, "id");
  }

  @Inject
  private PrivateChannelImpl(final TextChannelImpl.Factory textChannel, final @Assisted @Snowflake long id) {
    super(id);
    this.textChannel = textChannel.create(this, MAX_CACHED_MESSAGES);
  }

  @Override
  public @NonNull Optional<Message> message(final @Snowflake long id) {
    return this.textChannel.message(id);
  }

  @Override
  public @NonNull CompletableFuture<Message> message(final @Nullable String content, final @Nullable Embed embed) {
    return this.textChannel.message(content, embed);
  }

  public interface Factory {
    PrivateChannelImpl create(final @Snowflake long id);
  }
}
