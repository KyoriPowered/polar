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
package net.kyori.polar.shard;

import com.google.inject.assistedinject.Assisted;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.kyori.kassel.guild.Guild;
import net.kyori.kassel.snowflake.Snowflake;
import net.kyori.polar.gateway.Gateway;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.stream.Stream;

import javax.inject.Inject;

public final class ShardImpl implements Shard {
  private static final Logger LOGGER = LoggerFactory.getLogger(ShardImpl.class);
  private final Gateway gateway;
  private final int id;
  private final Long2ObjectMap<Guild> guilds = new Long2ObjectOpenHashMap<>();

  @Inject
  private ShardImpl(final Gateway.Factory gateway, final @Assisted int id) {
    this.gateway = gateway.create(this);
    this.id = id;
  }

  @Override
  public int id() {
    return this.id;
  }

  @Override
  public @NonNull Stream<Guild> guilds() {
    return this.guilds.values().stream();
  }

  @Override
  public @NonNull Optional<Guild> guild(final @Snowflake long id) {
    return Optional.ofNullable(this.guilds.get(id));
  }

  @Override
  public void putGuild(final @Snowflake long id, final @NonNull Guild guild) {
    this.guilds.put(id, guild);
  }

  @Override
  public @NonNull Optional<Guild> removeGuild(final @Snowflake long id) {
    return Optional.ofNullable(this.guilds.remove(id));
  }

  @Override
  public void connect() {
    LOGGER.debug("Connecting shard {}...", this.id);
    this.gateway.connect();
  }

  @Override
  public void disconnect() {
    LOGGER.debug("Disconnecting shard {}...", this.id);
    this.gateway.disconnect();
  }

  public interface Factory {
    ShardImpl create(final int id);
  }
}
