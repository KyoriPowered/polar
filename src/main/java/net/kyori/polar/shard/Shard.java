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

import java.util.stream.Stream;
import net.kyori.kassel.Connectable;
import net.kyori.kassel.guild.Guild;
import net.kyori.kassel.snowflake.Snowflake;
import net.kyori.mu.Maybe;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A shard.
 */
public interface Shard extends Connectable, net.kyori.kassel.client.shard.Shard {
  /**
   * Gets the id.
   *
   * @return the id
   */
  int id();

  /**
   * Gets a stream of all guilds.
   *
   * @return a stream of all guilds
   */
  @NonNull Stream<Guild> guilds();

  /**
   * Gets a guild by its snowflake id.
   *
   * @param id the snowflake id
   * @return the guild
   */
  @NonNull Maybe<Guild> guild(final @Snowflake long id);

  /**
   * Puts a guild.
   *
   * @param id the snowflake id
   * @param guild the guild
   */
  void putGuild(final @Snowflake long id, final @NonNull Guild guild);

  /**
   * Removes a guild.
   *
   * @param id the snowflake id
   * @return the guild
   */
  @NonNull Maybe<Guild> removeGuild(final @Snowflake long id);
}
