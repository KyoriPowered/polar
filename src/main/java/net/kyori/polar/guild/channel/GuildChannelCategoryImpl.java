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

import com.google.gson.JsonObject;
import net.kyori.kassel.guild.Guild;
import net.kyori.kassel.guild.channel.GuildChannelCategory;
import net.kyori.peppermint.Json;
import net.kyori.polar.snowflake.SnowflakedImpl;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class GuildChannelCategoryImpl extends SnowflakedImpl implements GuildChannelCategory {
  private final Guild guild;
  private @NonNull String name;

  public GuildChannelCategoryImpl(final @NonNull Guild guild, final @NonNull JsonObject json) {
    super(Json.needLong(json, "id"));
    this.guild = guild;
    this.name = Json.needString(json, "name");
  }

  @Override
  public @NonNull Guild guild() {
    return this.guild;
  }

  @Override
  public @NonNull String name() {
    return this.name;
  }
}
