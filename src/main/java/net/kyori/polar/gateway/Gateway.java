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

import com.google.common.base.MoreObjects;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.inject.assistedinject.Assisted;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.kyori.event.EventBus;
import net.kyori.kassel.Connectable;
import net.kyori.kassel.channel.Channel;
import net.kyori.kassel.channel.TextChannel;
import net.kyori.kassel.channel.message.Message;
import net.kyori.kassel.channel.message.emoji.Emoji;
import net.kyori.kassel.channel.message.event.ChannelMessageCreateEvent;
import net.kyori.kassel.channel.message.event.ChannelMessageDeleteEvent;
import net.kyori.kassel.channel.message.event.ChannelMessageReactionAddEvent;
import net.kyori.kassel.channel.message.event.ChannelMessageReactionClearEvent;
import net.kyori.kassel.channel.message.event.ChannelMessageReactionRemoveEvent;
import net.kyori.kassel.client.Client;
import net.kyori.kassel.client.shard.event.ShardConnectedEvent;
import net.kyori.kassel.client.shard.event.ShardResumedEvent;
import net.kyori.kassel.guild.Guild;
import net.kyori.kassel.guild.channel.event.GuildChannelCreateEvent;
import net.kyori.kassel.guild.channel.event.GuildChannelDeleteEvent;
import net.kyori.kassel.guild.event.GuildCreateEvent;
import net.kyori.kassel.guild.event.GuildDeleteEvent;
import net.kyori.kassel.guild.member.Member;
import net.kyori.kassel.guild.member.event.GuildMemberAddEvent;
import net.kyori.kassel.guild.member.event.GuildMemberRemoveEvent;
import net.kyori.kassel.guild.role.Role;
import net.kyori.kassel.guild.role.event.GuildRoleCreateEvent;
import net.kyori.kassel.guild.role.event.GuildRoleDeleteEvent;
import net.kyori.kassel.snowflake.Snowflaked;
import net.kyori.kassel.user.Activity;
import net.kyori.kassel.user.Status;
import net.kyori.mu.Composer;
import net.kyori.mu.Optionals;
import net.kyori.peppermint.Json;
import net.kyori.polar.PolarConfiguration;
import net.kyori.polar.channel.ChannelTypes;
import net.kyori.polar.channel.Channels;
import net.kyori.polar.channel.message.MessageImpl;
import net.kyori.polar.channel.message.emoji.Emojis;
import net.kyori.polar.client.ClientImpl;
import net.kyori.polar.guild.GuildImpl;
import net.kyori.polar.guild.channel.GuildTextChannelImpl;
import net.kyori.polar.refresh.Refreshable;
import net.kyori.polar.shard.Shard;
import net.kyori.polar.snowflake.SnowflakedImpl;
import net.kyori.polar.user.Activities;
import net.kyori.polar.user.UserImpl;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

import javax.inject.Inject;
import javax.net.ssl.SSLContext;

public final class Gateway extends WebSocketAdapter implements Connectable {
  private static final Logger LOGGER = LoggerFactory.getLogger(Gateway.class);
  private static final JsonParser PARSER = new JsonParser();
  private static final int RECONNECT_SECONDS = 5;
  private static final int MAX_RECONNECT_ATTEMPTS = 3;

  private final PolarConfiguration configuration;
  private final ScheduledExecutorService scheduler;
  private final EventBus<Object> bus;
  private final Client client;
  private final Shard shard;
  private final GatewayUrl url;

  private final GuildImpl.Factory guildFactory;
  private final MessageImpl.Factory messageFactory;

  private WebSocket ws;
  private Inflater inflater;

  private final AtomicInteger connectionAttempts = new AtomicInteger();

  // Heartbeat
  private final AtomicReference<Future<?>> heartbeat = new AtomicReference<>();
  private final AtomicBoolean heartbeatAck = new AtomicBoolean();

  // Session
  private State state;
  private @Nullable String sessionId;
  private long lastSequence = -1;
  private final Int2ObjectMap<Instant> lastMessage = new Int2ObjectOpenHashMap<>();

  @Inject
  private Gateway(final PolarConfiguration configuration, final ScheduledExecutorService scheduler, final EventBus<Object> bus, final Client client, final @Assisted Shard shard, final GatewayUrl url, final GuildImpl.Factory guildFactory, final MessageImpl.Factory messageFactory) {
    this.configuration = configuration;
    this.scheduler = scheduler;
    this.bus = bus;
    this.client = client;
    this.shard = shard;
    this.url = url;
    this.guildFactory = guildFactory;
    this.messageFactory = messageFactory;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("lastMessage", this.lastMessage)
      .add("lastSequence", this.lastSequence)
      .add("state", this.state)
      .toString();
  }

  @Override
  public void connect() {
    LOGGER.info("Connecting shard {} to gateway...", this.shard.id());
    this.state = State.CONNECTING;
    final WebSocketFactory factory = new WebSocketFactory();
    try {
      factory.setSSLContext(SSLContext.getDefault());
    } catch(final NoSuchAlgorithmException e) {
      LOGGER.error("Encountered an exception while setting SSL context", e);
    }
    while(!this.tryConnect(factory)) {
      if(this.connectionAttempts.incrementAndGet() >= MAX_RECONNECT_ATTEMPTS) {
        break;
      }
    }
  }

  private boolean tryConnect(final WebSocketFactory factory) {
    try {
      final WebSocket ws = factory.createSocket(this.url.get())
        .addHeader("Accept-Encoding", "gzip")
        .addListener(this);
      ws.connect();
      this.ws = ws;
      this.connectionAttempts.set(0);
      return true;
    } catch(final IOException | WebSocketException e) {
      LOGGER.error("Encountered an exception while creating socket", e);
      return false;
    }
  }

  @Override
  public void disconnect() {
    LOGGER.info("Disconnecting shard {} from gateway...", this.shard.id());
    this.state = State.DISCONNECTING;
    this.ws.sendClose(1000);
    this.ws = null;
  }

  @Override
  public void onConnected(final WebSocket ws, final Map<String, List<String>> headers) {
    this.state = State.CONNECTED;
    this.inflater = new Inflater();

    if(this.sessionId != null) {
      this.resume(ws);
    }
  }

  @Override
  public void onDisconnected(final WebSocket ws, final WebSocketFrame serverCloseFrame, final WebSocketFrame clientCloseFrame, final boolean closedByServer) {
    boolean reconnect = true;
    if(closedByServer) {
      if(serverCloseFrame != null) {
        reconnect = this.canReconnect(serverCloseFrame.getCloseCode());
      }
      this.frameClosed(serverCloseFrame, "server");
    } else if(clientCloseFrame != null) {
      this.frameClosed(clientCloseFrame, "client");
    }

    if(this.state == State.RESUMING) {
      return;
    }

    this.resetState();

    this.resetHeartbeat();

    if(!reconnect) {
      this.state = State.DISCONNECTED;
      LOGGER.info("Shard {} disconnected from gateway ({})", this.shard.id(), this);
      return;
    }

    this.state = State.RESUMING;
    LOGGER.info("Reconnecting shard {} to gateway ({}) in {} seconds...", this.shard.id(), this, RECONNECT_SECONDS);
    this.scheduler.schedule(this::connect, RECONNECT_SECONDS, TimeUnit.SECONDS);
  }

  private void frameClosed(final @Nullable WebSocketFrame frame, final String name) {
    if(frame != null) {
      final String reason = frame.getCloseReason();
      if(reason != null) {
        LOGGER.info("Shard {} disconnected from gateway ({}) by {} ({}: {})", this.shard.id(), this, name, frame.getCloseCode(), reason);
      } else {
        LOGGER.info("Shard {} disconnected from gateway ({}) by {} ({})", this.shard.id(), this, name, frame.getCloseCode());
      }
    } else {
      LOGGER.info("Shard {} disconnected from gateway ({}) by {}", this.shard.id(), this, name);
    }
  }

  private boolean canReconnect(final int code) {
    if(this.state == State.DISCONNECTING) {
      return false;
    }

    switch(code) {
      case GatewayCloseCode.NOT_AUTHENTICATED:
      case GatewayCloseCode.AUTHENTICATION_FAILED:
      case GatewayCloseCode.ALREADY_AUTHENTICATED:
      case GatewayCloseCode.INVALID_SHARD:
        return false;
    }

    return true;
  }

  private void resetState() {
    this.inflater = null;
    this.ws = null;
  }

  public void presence(final @NonNull Status status, final @Nullable Activity activityType, final @Nullable String activityName) {
    this.ws.sendText(GatewayPayload.create(GatewayOpcode.STATUS_UPDATE, (d) -> {
      d.addProperty("afk", false);
      d.add("since", JsonNull.INSTANCE);
      d.addProperty("status", status.name().toLowerCase(Locale.ENGLISH));
      if(activityType != null && activityName != null) {
        d.add("game", Composer.accept(new JsonObject(), game -> {
          game.addProperty("type", Activities.activity(activityType));
          game.addProperty("name", activityName);
        }));
      } else {
        d.add("game", JsonNull.INSTANCE);
      }
    }));
  }

  @Override
  public void onBinaryMessage(final WebSocket ws, final byte[] bytes) {
    try(final ByteArrayOutputStream baos = new ByteArrayOutputStream(bytes.length); final InflaterOutputStream ios = new InflaterOutputStream(baos, this.inflater)) {
      ios.write(bytes);
      this.onTextMessage(ws, new String(baos.toByteArray(), StandardCharsets.UTF_8));
    } catch(final IOException e) {
      LOGGER.error("Encountered an exception while decompressing", e);
    }
  }

  @Override
  public void onTextMessage(final WebSocket ws, final String text) {
    final JsonObject json = PARSER.parse(text).getAsJsonObject();
    try {
      this.onMessage(ws, json);
    } catch(final Throwable t) {
      LOGGER.error("Encountered an exception while processing message {}", json, t);
    }
  }

  private void onMessage(final WebSocket ws, final JsonObject json) {
    final int opcode = Json.needInt(json, GatewayPayload.OPCODE);
    this.lastMessage.put(opcode, Instant.now());
    switch(opcode) {
      case GatewayOpcode.DISPATCH: this.dispatch(ws, json); break;
      case GatewayOpcode.HEARTBEAT: this.heartbeat(ws); break;
      case GatewayOpcode.RECONNECT: this.reconnect(ws); break;
      case GatewayOpcode.INVALID_SESSION: this.invalidSession(ws); break;
      case GatewayOpcode.HELLO: this.hello(ws, json.getAsJsonObject(GatewayPayload.EVENT_DATA)); break;
      case GatewayOpcode.HEARTBEAT_ACK: this.heartbeatAck(); break;
      default: LOGGER.warn("Unknown opcode {}", opcode); break;
    }
  }

  /*
   * DISPATCH
   */

  private void dispatch(final WebSocket ws, final JsonObject json) {
    this.lastSequence = Json.needInt(json, GatewayPayload.SEQUENCE);

    final String eventName = Json.needString(json, GatewayPayload.EVENT_NAME);
    final JsonElement eventData = json.get(GatewayPayload.EVENT_DATA);

    switch(eventName) {
      case GatewayEvent.CHANNEL_CREATE: this.dispatchChannelCreate(eventData.getAsJsonObject()); break;
      case GatewayEvent.CHANNEL_DELETE: this.dispatchChannelDelete(eventData.getAsJsonObject()); break;
      case GatewayEvent.CHANNEL_PINS_UPDATE: /* don't care */ break;
      case GatewayEvent.CHANNEL_UPDATE: this.dispatchChannelUpdate(eventData.getAsJsonObject()); break;
      case GatewayEvent.GUILD_BAN_ADD: /* don't care */ break;
      case GatewayEvent.GUILD_BAN_REMOVE: /* don't care */ break;
      case GatewayEvent.GUILD_CREATE: this.dispatchGuildCreate(ws, eventData.getAsJsonObject()); break;
      case GatewayEvent.GUILD_DELETE: this.dispatchGuildDelete(eventData.getAsJsonObject()); break;
      case GatewayEvent.GUILD_EMOJIS_UPDATE: this.dispatchGuildEmojisUpdate(eventData.getAsJsonObject()); break;
      case GatewayEvent.GUILD_MEMBER_ADD: this.dispatchGuildMemberAdd(eventData.getAsJsonObject()); break;
      case GatewayEvent.GUILD_MEMBER_REMOVE: this.dispatchGuildMemberRemove(eventData.getAsJsonObject()); break;
      case GatewayEvent.GUILD_MEMBER_UPDATE: this.dispatchGuildMemberUpdate(eventData.getAsJsonObject()); break;
      case GatewayEvent.GUILD_MEMBERS_CHUNK: this.dispatchGuildMembersChunk(eventData.getAsJsonObject()); break;
      case GatewayEvent.GUILD_ROLE_CREATE: this.dispatchGuildRoleCreate(eventData.getAsJsonObject()); break;
      case GatewayEvent.GUILD_ROLE_DELETE: this.dispatchGuildRoleDelete(eventData.getAsJsonObject()); break;
      case GatewayEvent.GUILD_ROLE_UPDATE: this.dispatchGuildRoleUpdate(eventData.getAsJsonObject()); break;
      case GatewayEvent.GUILD_UPDATE: this.dispatchGuildUpdate(eventData.getAsJsonObject()); break;
      case GatewayEvent.MESSAGE_CREATE: this.dispatchMessageCreate(eventData.getAsJsonObject()); break;
      case GatewayEvent.MESSAGE_DELETE: this.dispatchMessageDelete(eventData.getAsJsonObject()); break;
      case GatewayEvent.MESSAGE_DELETE_BULK: this.dispatchMessageDeleteBulk(eventData.getAsJsonObject()); break;
      case GatewayEvent.MESSAGE_REACTION_ADD: this.dispatchMessageReactionAdd(eventData.getAsJsonObject()); break;
      case GatewayEvent.MESSAGE_REACTION_REMOVE: this.dispatchMessageReactionRemove(eventData.getAsJsonObject()); break;
      case GatewayEvent.MESSAGE_REACTION_REMOVE_ALL: this.dispatchMessageReactionRemoveAll(eventData.getAsJsonObject()); break;
      case GatewayEvent.MESSAGE_UPDATE: this.dispatchMessageUpdate(eventData.getAsJsonObject()); break;
      case GatewayEvent.PRESENCE_UPDATE: /* don't care */ break;
      case GatewayEvent.READY: this.dispatchReady(eventData.getAsJsonObject()); break;
      case GatewayEvent.RESUMED: this.dispatchResumed(); break;
      case GatewayEvent.TYPING_START: /* don't care */ break;
      case GatewayEvent.USER_UPDATE: /* don't care */ break;
      case GatewayEvent.VOICE_STATE_UPDATE: /* don't care */ break;
      case GatewayEvent.WEBHOOKS_UPDATE: /* don't care */ break;
      default: LOGGER.warn("Encountered an unknown event: {} {}", eventName, eventData);
    }
  }

  private void dispatchChannelCreate(final JsonObject json) {
    switch(Json.needInt(json, "type")) {
      case ChannelTypes.GUILD_CATEGORY:
      case ChannelTypes.GUILD_TEXT:
      case ChannelTypes.GUILD_VOICE:
        Optionals.cast(this.shard.guild(Json.needLong(json, "guild_id")), GuildImpl.class)
          .ifPresent(guild -> guild.putChannel(Json.needLong(json, "id"), json)
            .ifPresent(channel -> this.bus.post(new GuildChannelCreateEvent() {
              @Override
              public @NonNull Guild guild() {
                return guild;
              }

              @Override
              public @NonNull Channel channel() {
                return channel;
              }
            })));
        break;
      case ChannelTypes.DM:
        if(Channels.hasRecipient(json)) {
          Optionals.cast(this.client.user(Channels.recipient(json)), UserImpl.class)
            .ifPresent(user -> ((ClientImpl) this.client).privateChannel(user, Json.needLong(json, "id")));
        }
        break;
      case ChannelTypes.GROUP_DM: /* NOOP */ break;
    }
  }

  private void dispatchChannelDelete(final JsonObject json) {
    switch(Json.needInt(json, "type")) {
      case ChannelTypes.GUILD_CATEGORY:
      case ChannelTypes.GUILD_TEXT:
      case ChannelTypes.GUILD_VOICE:
        Optionals.cast(this.shard.guild(Json.needLong(json, "guild_id")), GuildImpl.class)
          .ifPresent(guild -> guild.removeChannel(Json.needLong(json, "id"))
            .ifPresent(channel -> this.bus.post(new GuildChannelDeleteEvent() {
              @Override
              public @NonNull Guild guild() {
                return guild;
              }

              @Override
              public @NonNull Channel channel() {
                return channel;
              }
            })));
        break;
      case ChannelTypes.DM: /* NOOP */ break;
      case ChannelTypes.GROUP_DM: /* NOOP */ break;
    }
  }

  private void dispatchChannelUpdate(final JsonObject json) {
    switch(Json.needInt(json, "type")) {
      case ChannelTypes.GUILD_CATEGORY:
      case ChannelTypes.GUILD_TEXT:
      case ChannelTypes.GUILD_VOICE:
        this.shard.guild(Json.needLong(json, "guild_id"))
          .ifPresent(guild -> Optionals.cast(guild.channel(Json.needLong(json, "id")), Refreshable.class)
            .ifPresent(channel -> channel.refresh(json)));
        break;
      case ChannelTypes.DM: /* NOOP */ break;
      case ChannelTypes.GROUP_DM: /* NOOP */ break;
    }
  }

  private void dispatchGuildCreate(final WebSocket ws, final JsonObject json) {
    if(Json.getBoolean(json, "unavailable", false)) {
      return;
    }

    final GuildImpl guild = this.guildFactory.create(json);
    this.shard.putGuild(Json.needLong(json, "id"), guild);
    this.bus.post(new GuildCreateEvent() {
      @Override
      public @NonNull Guild guild() {
        return guild;
      }
    });

    final int expectedMembers = Json.getInt(json, "member_count", -1);
    if(guild.requiresMemberChunking(expectedMembers)) {
      LOGGER.info("Requesting member chunks for guild {}...", guild.id());
      ws.sendText(GatewayPayload.create(GatewayOpcode.REQUEST_GUILD_MEMBERS, d -> {
        d.addProperty("guild_id", guild.id());
        d.addProperty("query", ""); // empty = all
        d.addProperty("limit", 0); // 0 = all
      }));
    }
  }

  private void dispatchGuildDelete(final JsonObject json) {
    this.shard.removeGuild(Json.needLong(json, "id")).ifPresent(guild -> this.bus.post(new GuildDeleteEvent() {
      @Override
      public @NonNull Guild guild() {
        return guild;
      }
    }));
  }

  private void dispatchGuildEmojisUpdate(final JsonObject json) {
    Optionals.cast(this.shard.guild(Json.needLong(json, "guild_id")), GuildImpl.class)
      .ifPresent(guild -> guild.refreshEmojis(json.getAsJsonArray("emojis")));
  }

  private void dispatchGuildMemberAdd(final JsonObject json) {
    Optionals.cast(this.shard.guild(Json.needLong(json, "guild_id")), GuildImpl.class)
      .ifPresent(guild -> {
        final Member member = guild.putMember(json);
        this.bus.post(new GuildMemberAddEvent() {
          @Override
          public @NonNull Guild guild() {
            return guild;
          }

          @Override
          public @NonNull Member member() {
            return member;
          }
        });
      });
  }

  private void dispatchGuildMemberRemove(final JsonObject json) {
    Optionals.cast(this.shard.guild(Json.needLong(json, "guild_id")), GuildImpl.class)
      .ifPresent(guild -> {
        guild.removeMember(Json.needLong(json.getAsJsonObject("user"), "id"))
          .ifPresent(member -> this.bus.post(new GuildMemberRemoveEvent() {
            @Override
            public @NonNull Guild guild() {
              return guild;
            }

            @Override
            public @NonNull Member member() {
              return member;
            }
          }));
      });
  }

  private void dispatchGuildMemberUpdate(final JsonObject json) {
    this.shard.guild(Json.needLong(json, "guild_id"))
      .ifPresent(guild -> Optionals.cast(guild.member(Json.needLong(json.getAsJsonObject("user"), "id")), Refreshable.class)
        .ifPresent(member -> member.refresh(json)));
  }

  private void dispatchGuildMembersChunk(final JsonObject json) {
    Optionals.cast(this.shard.guild(Json.needLong(json, "guild_id")), GuildImpl.class)
      .ifPresent(guild -> {
        for(final JsonElement member : json.getAsJsonArray("members")) {
          guild.putMember(member.getAsJsonObject());
        }
      });
  }

  private void dispatchGuildRoleCreate(final JsonObject json) {
    Optionals.cast(this.shard.guild(Json.needLong(json, "guild_id")), GuildImpl.class)
      .ifPresent(guild -> {
        final Role role = guild.putRole(json.getAsJsonObject("role"));
        this.bus.post(new GuildRoleCreateEvent() {
          @Override
          public @NonNull Guild guild() {
            return guild;
          }

          @Override
          public @NonNull Role role() {
            return role;
          }
        });
      });
  }

  private void dispatchGuildRoleDelete(final JsonObject json) {
    Optionals.cast(this.shard.guild(Json.needLong(json, "guild_id")), GuildImpl.class)
      .ifPresent(guild -> guild.removeRole(Json.needLong(json, "role_id")).ifPresent(role -> {
        this.bus.post(new GuildRoleDeleteEvent() {
          @Override
          public @NonNull Guild guild() {
            return guild;
          }

          @Override
          public @NonNull Role role() {
            return role;
          }
        });
      }));
  }

  private void dispatchGuildRoleUpdate(final JsonObject json) {
    this.shard.guild(Json.needLong(json, "guild_id"))
      .ifPresent(guild -> {
        final JsonObject roleJson = json.getAsJsonObject("role");
        Optionals.cast(guild.role(Json.needLong(roleJson, "id")), Refreshable.class).ifPresent(role -> role.refresh(roleJson));
      });
  }

  private void dispatchGuildUpdate(final JsonObject json) {
    Optionals.cast(this.shard.guild(Json.needLong(json, "id")), Refreshable.class)
      .ifPresent(guild -> guild.refresh(json));
  }

  private void dispatchMessageCreate(final JsonObject json) {
    if(json.has("guild_id")) {
      this.shard.guild(Json.needLong(json, "guild_id"))
        .flatMap(guild -> Optionals.cast(guild.channel(Json.needLong(json, "channel_id")), GuildTextChannelImpl.class))
        .ifPresent(channel -> {
          final Message message = this.messageFactory.create(channel, json);
          channel.putMessage(message.id(), message);
          this.bus.post(new ChannelMessageCreateEvent() {
            @Override
            public @NonNull Channel channel() {
              return channel;
            }

            @Override
            public @NonNull Message message() {
              return message;
            }
          });
        });
    } else {
      LOGGER.warn("Encountered request to create non-guild message: {}", json);
    }
  }

  private void dispatchMessageDelete(final JsonObject json) {
    if(json.has("guild_id")) {
      this.shard.guild(Json.needLong(json, "guild_id"))
        .flatMap(guild -> Optionals.cast(guild.channel(Json.needLong(json, "channel_id")), GuildTextChannelImpl.class))
        .ifPresent(channel -> {
          final Snowflaked message = Optionals.cast(channel.removeMessage(Json.needLong(json, "id")), Snowflaked.class)
            .orElseGet(() -> new SnowflakedImpl(Json.needLong(json, "id")));
          this.bus.post(new ChannelMessageDeleteEvent() {
            @Override
            public @NonNull Channel channel() {
              return channel;
            }

            @Override
            public @NonNull Snowflaked message() {
              return message;
            }
          });
        });
    } else {
      LOGGER.warn("Encountered request to delete non-guild message: {}", json);
    }
  }

  private void dispatchMessageDeleteBulk(final JsonObject json) {
    if(json.has("guild_id")) {
      this.shard.guild(Json.needLong(json, "guild_id"))
        .flatMap(guild -> Optionals.cast(guild.channel(Json.needLong(json, "channel_id")), GuildTextChannelImpl.class))
        .ifPresent(channel -> {
          for(final JsonElement id : json.getAsJsonArray("ids")) {
            final Snowflaked message = Optionals.cast(channel.removeMessage(Json.needLong(id, "id")), Snowflaked.class)
              .orElseGet(() -> new SnowflakedImpl(Json.needLong(id, "id")));
            this.bus.post(new ChannelMessageDeleteEvent() {
              @Override
              public @NonNull Channel channel() {
                return channel;
              }

              @Override
              public @NonNull Snowflaked message() {
                return message;
              }
            });
          }
        });
    } else {
      LOGGER.warn("Encountered request to bulk delete non-guild messages: {}", json);
    }
  }

  private void dispatchMessageReactionAdd(final JsonObject json) {
    if(json.has("guild_id")) {
      this.shard.guild(Json.needLong(json, "guild_id"))
        .flatMap(guild -> Optionals.cast(guild.channel(Json.needLong(json, "channel_id")), GuildTextChannelImpl.class))
        .ifPresent(channel -> {
          final Snowflaked message = Optionals.cast(channel.message(Json.needLong(json, "message_id")), Snowflaked.class)
            .orElseGet(() -> new SnowflakedImpl(Json.needLong(json, "message_id")));
          final Snowflaked user = Optionals.cast(this.client.user(Json.needLong(json, "user_id")), Snowflaked.class)
            .orElseGet(() -> new SnowflakedImpl(Json.needLong(json, "user_id")));
          final Emoji emoji = Emojis.from(json.getAsJsonObject("emoji"));
          this.bus.post(new ChannelMessageReactionAddEvent() {
            @Override
            public @NonNull Channel channel() {
              return channel;
            }

            @Override
            public @NonNull Snowflaked message() {
              return message;
            }

            @Override
            public @NonNull Snowflaked user() {
              return user;
            }

            @Override
            public @NonNull Emoji emoji() {
              return emoji;
            }
          });
        });
    } else {
      LOGGER.warn("Encountered request to add reaction to non-guild message: {}", json);
    }
  }

  private void dispatchMessageReactionRemove(final JsonObject json) {
    if(json.has("guild_id")) {
      this.shard.guild(Json.needLong(json, "guild_id"))
        .flatMap(guild -> Optionals.cast(guild.channel(Json.needLong(json, "channel_id")), GuildTextChannelImpl.class))
        .ifPresent(channel -> {
          final Snowflaked message = Optionals.cast(channel.message(Json.needLong(json, "message_id")), Snowflaked.class)
            .orElseGet(() -> new SnowflakedImpl(Json.needLong(json, "message_id")));
          final Snowflaked user = Optionals.cast(this.client.user(Json.needLong(json, "user_id")), Snowflaked.class)
            .orElseGet(() -> new SnowflakedImpl(Json.needLong(json, "user_id")));
          final Emoji emoji = Emojis.from(json.getAsJsonObject("emoji"));
          this.bus.post(new ChannelMessageReactionRemoveEvent() {
            @Override
            public @NonNull Channel channel() {
              return channel;
            }

            @Override
            public @NonNull Snowflaked message() {
              return message;
            }

            @Override
            public @NonNull Snowflaked user() {
              return user;
            }

            @Override
            public @NonNull Emoji emoji() {
              return emoji;
            }
          });
        });
    } else {
      LOGGER.warn("Encountered request to remove reaction from non-guild message: {}", json);
    }
  }

  private void dispatchMessageReactionRemoveAll(final JsonObject json) {
    if(json.has("guild_id")) {
      this.shard.guild(Json.needLong(json, "guild_id"))
        .flatMap(guild -> Optionals.cast(guild.channel(Json.needLong(json, "channel_id")), GuildTextChannelImpl.class))
        .ifPresent(channel -> {
          final Snowflaked message = Optionals.cast(channel.message(Json.needLong(json, "message_id")), Snowflaked.class)
            .orElseGet(() -> new SnowflakedImpl(Json.needLong(json, "message_id")));
          this.bus.post(new ChannelMessageReactionClearEvent() {
            @Override
            public @NonNull Channel channel() {
              return channel;
            }

            @Override
            public @NonNull Snowflaked message() {
              return message;
            }
          });
        });
    } else {
      LOGGER.warn("Encountered request to remove all reactions from non-guild message: {}", json);
    }
  }

  private void dispatchMessageUpdate(final JsonObject json) {
    if(json.has("guild_id")) {
      this.shard.guild(Json.needLong(json, "guild_id"))
        .flatMap(guild -> Optionals.cast(guild.channel(Json.needLong(json, "channel_id")), TextChannel.class))
        .flatMap(channel -> Optionals.cast(channel.message(Json.needLong(json, "id")), Refreshable.class))
        .ifPresent(message -> message.refresh(json));
    } else {
      LOGGER.warn("Encountered request to remove all reactions from non-guild message: {}", json);
    }
  }

  private void dispatchReady(final JsonObject json) {
    this.sessionId = Json.needString(json, "session_id");
    this.bus.post((ShardConnectedEvent) () -> this.shard);
  }

  private void dispatchResumed() {
    this.state = State.RESUMED;
    LOGGER.info("Shard {} resumed", this.shard.id());
    this.bus.post((ShardResumedEvent) () -> this.shard);
  }

  /*
   * HEARTBEAT
   */

  private void heartbeat(final WebSocket ws) {
    ws.sendText(GatewayPayload.create(GatewayOpcode.HEARTBEAT, () -> {
      if(this.lastSequence != -1) {
        return new JsonPrimitive(this.lastSequence);
      }
      return JsonNull.INSTANCE;
    }));
  }

  private void resetHeartbeat() {
    this.heartbeat.updateAndGet(future -> {
      if(future != null) {
        future.cancel(false);
      }
      return null;
    });
    this.heartbeatAck.set(false);
  }

  /*
   * RECONNECT
   */

  private void reconnect(final WebSocket ws) {
    this.resetHeartbeat();
    ws.sendClose(4000, "reconnect");
  }

  /*
   * INVALID_SESSION
   */

  private void invalidSession(final WebSocket ws) {
    this.lastSequence = -1;
    this.sessionId = null;
    this.identify(ws);
  }

  /*
   * HELLO
   */

  private void hello(final WebSocket ws, final JsonObject json) {
    final int interval = Json.needInt(json, "heartbeat_interval");
    this.heartbeat.updateAndGet(future -> {
      if(future != null) {
        future.cancel(false);
      }

      this.heartbeatAck.set(true);
      return this.scheduler.scheduleWithFixedDelay(() -> {
        if(this.heartbeatAck.getAndSet(false)) {
          this.heartbeat(ws);
        }
      }, 0, interval, TimeUnit.MILLISECONDS);
    });

    if(this.sessionId == null) {
      this.identify(ws);
    }
  }

  private void identify(final WebSocket ws) {
    ws.sendText(GatewayPayload.create(GatewayOpcode.IDENTIFY, d -> {
      d.addProperty("compress", true);

      final JsonObject properties = new JsonObject();
      properties.addProperty("$browser", "kassel");
      properties.addProperty("$device", "kassel");
      properties.addProperty("$os", System.getProperty("os.name"));
      d.add("properties", properties);

      final int shards = this.configuration.shards();
      if(shards > 1) {
        final JsonArray shard = new JsonArray(2);
        shard.add(this.shard.id());
        shard.add(shards);
        d.add("shard", shard);
      }

      d.addProperty("token", this.configuration.token());
    }));
  }

  private void resume(final WebSocket ws) {
    ws.sendText(GatewayPayload.create(GatewayOpcode.RESUME, d -> {
      d.addProperty("seq", this.lastSequence);
      d.addProperty("session_id", this.sessionId);
      d.addProperty("token", this.configuration.token());
    }));
  }

  /*
   * HEARTBEAT_ACK
   */

  private void heartbeatAck() {
    this.heartbeatAck.set(true);
  }

  public interface Factory {
    Gateway create(final Shard shard);
  }

  private enum State {
    CONNECTING,
    CONNECTED,
    RESUMING,
    RESUMED,
    DISCONNECTING,
    DISCONNECTED;
  }
}
