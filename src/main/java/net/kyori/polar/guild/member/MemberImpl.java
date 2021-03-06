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
package net.kyori.polar.guild.member;

import com.google.common.base.MoreObjects;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.assistedinject.Assisted;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import net.kyori.kassel.guild.Guild;
import net.kyori.kassel.guild.member.Member;
import net.kyori.kassel.guild.role.Role;
import net.kyori.kassel.user.User;
import net.kyori.mu.Maybe;
import net.kyori.peppermint.Json;
import net.kyori.polar.client.ClientImpl;
import net.kyori.polar.guild.role.RoleImpl;
import net.kyori.polar.http.endpoint.Endpoints;
import net.kyori.polar.refresh.Refreshable;
import net.kyori.polar.util.Equality;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class MemberImpl implements Member, Refreshable {
  final RolesImpl roles = new RolesImpl();
  private final ClientImpl client;
  private final MemberRefresher refresher;
  private final Guild guild;
  private final User user;
  private @Nullable String nick;

  @Inject
  private MemberImpl(final ClientImpl client, final MemberRefresher refresher, final @Assisted Guild guild, final @Assisted JsonObject json) {
    this.client = client;
    this.refresher = refresher;
    this.guild = guild;
    this.user = client.userOrCreate(json.getAsJsonObject("user"));
    this.nick = Json.getString(json, "nick", null);

    if(Json.isArray(json, "roles")) {
      this.roles.set(RoleImpl.roles(json.getAsJsonArray("roles")));
    }
  }

  @Override
  public void refresh(final JsonElement json) {
    this.refresher.refresh(new MemberRefresher.Context() {
      @Override
      public @NonNull Guild guild() {
        return MemberImpl.this.guild;
      }

      @Override
      public @NonNull MemberImpl target() {
        return MemberImpl.this;
      }
    }, json);
  }

  @Override
  public @NonNull Guild guild() {
    return this.guild;
  }

  @Override
  public @NonNull User user() {
    return this.user;
  }

  @Override
  public @NonNull Maybe<String> nick() {
    return Maybe.maybe(this.nick);
  }

  void nick(final @NonNull Maybe<String> nick) {
    this.nick = nick.orDefault(null);
  }

  @Override
  public @NonNull Roles roles() {
    return this.roles;
  }

  void roles(final @NonNull LongSet roles) {
    this.roles.set(roles);
  }

  @Override
  public boolean equals(final Object other) {
    return Equality.equals(this, other, that -> this.guild.id() == that.guild.id() && this.user.id() == that.user.id());
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.user.id());
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("user", this.user)
      .add("nick", this.nick)
      .add("roles", this.roles.all().collect(Collectors.toSet()))
      .toString();
  }

  final class RolesImpl implements Roles {
    final LongSet roles = new LongArraySet();

    @Override
    public @NonNull Stream<Role> all() {
      return this.roles.stream()
        .map(MemberImpl.this.guild::role)
        .filter(Maybe::isJust)
        .map(Maybe::orThrow);
    }

    @Override
    public void add(final @NonNull Role role) {
      MemberImpl.this.client.executor().submit(() -> {
        MemberImpl.this.client.httpClient().json(Endpoints.guildMemberRole(MemberImpl.this.guild.id(), MemberImpl.this.user.id(), role.id()).request(builder -> builder.put(RequestBody.create(null, new byte[0]))));
      });
    }

    @Override
    public void remove(final @NonNull Role role) {
      MemberImpl.this.client.executor().submit(() -> {
        MemberImpl.this.client.httpClient().json(Endpoints.guildMemberRole(MemberImpl.this.guild.id(), MemberImpl.this.user.id(), role.id()).request(Request.Builder::delete));
      });
    }

    void set(final @NonNull LongSet roles) {
      this.roles.clear();
      this.roles.addAll(roles);
    }
  }

  public interface Factory {
    MemberImpl create(final Guild guild, final JsonObject json);
  }
}
