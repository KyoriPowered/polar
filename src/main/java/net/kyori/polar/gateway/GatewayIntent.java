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
package net.kyori.polar.gateway;

import com.google.common.collect.ImmutableSet;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.NonNull;

public enum GatewayIntent {
  @SuppressWarnings("PointlessBitwiseExpression")
  GUILDS(1 << 0),
  @Privileged GUILD_MEMBERS(1 << 1),
  GUILD_BANS(1 << 2),
  GUILD_EMOJIS(1 << 3),
  GUILD_INTEGRATIONS(1 << 4),
  GUILD_WEBHOOKS(1 << 5),
  GUILD_INVITES(1 << 6),
  GUILD_VOICE_STATES(1 << 7),
  @Privileged GUILD_PRESENCES(1 << 8),
  GUILD_MESSAGES(1 << 9),
  GUILD_MESSAGE_REACTIONS(1 << 10),
  GUILD_MESSAGE_TYPING(1 << 11),
  DIRECT_MESSAGES(1 << 12),
  DIRECT_MESSAGE_REACTIONS(1 << 13),
  DIRECT_MESSAGE_TYPING(1 << 14);

  final int flag;

  GatewayIntent(final int flag) {
    this.flag = flag;
  }

  /*
   * excluding:
   * - GUILD_MEMBERS          (privileged)
   * - GUILD_WEBHOOKS         (useless)
   * - GUILD_VOICE_STATES     (useless)
   * - GUILD_PRESENCES        (privileged)
   * - GUILD_MESSAGE_TYPING   (useless)
   * - DIRECT_MESSAGE_TYPING  (useless)
   */
  public static @NonNull Set<GatewayIntent> defaults() {
    return ImmutableSet.of(
      GUILDS,
      GUILD_BANS,
      GUILD_EMOJIS,
      GUILD_INTEGRATIONS,
      GUILD_INVITES,
      GUILD_MESSAGES,
      GUILD_MESSAGE_REACTIONS,
      DIRECT_MESSAGES,
      DIRECT_MESSAGE_REACTIONS
    );
  }

  static int flags(final Set<GatewayIntent> intents) {
    int flags = 0;
    for(final GatewayIntent intent : intents) {
      flags |= intent.flag;
    }
    return flags;
  }

  @Retention(RetentionPolicy.SOURCE)
  private @interface Privileged {}
}
