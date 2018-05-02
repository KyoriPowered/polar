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
package net.kyori.polar;

import net.kyori.polar.channel.message.MessageModule;
import net.kyori.polar.client.ClientModule;
import net.kyori.polar.gateway.GatewayModule;
import net.kyori.polar.guild.GuildModule;
import net.kyori.polar.http.HttpModule;
import net.kyori.polar.shard.ShardModule;
import net.kyori.polar.user.UserModule;
import net.kyori.violet.AbstractModule;

public final class PolarModule extends AbstractModule {
  @Override
  protected void configure() {
    this.install(new HttpModule());

    this.install(new ClientModule());
    this.install(new GatewayModule());
    this.install(new ShardModule());

    this.install(new GuildModule());
    this.install(new MessageModule());
    this.install(new UserModule());
  }
}
