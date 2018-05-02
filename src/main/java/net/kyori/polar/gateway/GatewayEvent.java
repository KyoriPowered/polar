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

interface GatewayEvent {
  String CHANNEL_CREATE = "CHANNEL_CREATE";
  String CHANNEL_DELETE = "CHANNEL_DELETE";
  String CHANNEL_PINS_UPDATE = "CHANNEL_PINS_UPDATE";
  String CHANNEL_UPDATE = "CHANNEL_UPDATE";
  String GUILD_BAN_ADD = "GUILD_BAN_ADD";
  String GUILD_BAN_REMOVE = "GUILD_BAN_REMOVE";
  String GUILD_CREATE = "GUILD_CREATE";
  String GUILD_DELETE = "GUILD_DELETE";
  String GUILD_EMOJIS_UPDATE = "GUILD_EMOJIS_UPDATE";
  String GUILD_MEMBER_ADD = "GUILD_MEMBER_ADD";
  String GUILD_MEMBER_REMOVE = "GUILD_MEMBER_REMOVE";
  String GUILD_MEMBER_UPDATE = "GUILD_MEMBER_UPDATE";
  String GUILD_MEMBERS_CHUNK = "GUILD_MEMBERS_CHUNK";
  String GUILD_ROLE_CREATE = "GUILD_ROLE_CREATE";
  String GUILD_ROLE_DELETE = "GUILD_ROLE_DELETE";
  String GUILD_ROLE_UPDATE = "GUILD_ROLE_UPDATE";
  String GUILD_UPDATE = "GUILD_UPDATE";
  String MESSAGE_CREATE = "MESSAGE_CREATE";
  String MESSAGE_DELETE = "MESSAGE_DELETE";
  String MESSAGE_DELETE_BULK = "MESSAGE_DELETE_BULK";
  String MESSAGE_REACTION_ADD = "MESSAGE_REACTION_ADD";
  String MESSAGE_REACTION_REMOVE = "MESSAGE_REACTION_REMOVE";
  String MESSAGE_REACTION_REMOVE_ALL = "MESSAGE_REACTION_REMOVE_ALL";
  String MESSAGE_UPDATE = "MESSAGE_UPDATE";
  String PRESENCE_UPDATE = "PRESENCE_UPDATE";
  String READY = "READY";
  String RESUMED = "RESUMED";
  String TYPING_START = "TYPING_START";
  String USER_UPDATE = "USER_UPDATE";
  String VOICE_STATE_UPDATE = "VOICE_STATE_UPDATE";
  String WEBHOOKS_UPDATE = "WEBHOOKS_UPDATE";
}
