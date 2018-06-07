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
import net.kyori.kassel.guild.Guild;
import net.kyori.kassel.guild.member.Member;
import net.kyori.kassel.guild.role.Role;
import net.kyori.kassel.user.User;
import net.kyori.lunar.EvenMoreObjects;
import net.kyori.peppermint.Json;
import net.kyori.polar.client.ClientImpl;
import net.kyori.polar.refresh.Refreshable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

public final class MemberImpl implements Member, Refreshable {
  final LongSet roles = new LongArraySet();
  private final MemberRefresher refresher;
  private final Guild guild;
  private final User user;
  private @Nullable String nick;

  @Inject
  private MemberImpl(final ClientImpl client, final MemberRefresher refresher, final @Assisted Guild guild, final @Assisted JsonObject json) {
    this.refresher = refresher;
    this.guild = guild;
    this.user = client.userOrCreate(json.getAsJsonObject("user"));
    this.nick = Json.getString(json, "nick", null);

    if(Json.isArray(json, "roles")) {
      for(final JsonElement role : json.getAsJsonArray("roles")) {
        this.roles.add(Json.needLong(role, "id"));
      }
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
      public MemberImpl target() {
        return MemberImpl.this;
      }
    }, json);
  }

  @Override
  public @NonNull User user() {
    return this.user;
  }

  @Override
  public @NonNull Optional<String> nick() {
    return Optional.ofNullable(this.nick);
  }

  @Override
  public @NonNull Stream<Role> roles() {
    return this.roles.stream()
      .map(this.guild::role)
      .filter(Optional::isPresent)
      .map(Optional::get);
  }

  void roles(final @NonNull LongSet roles) {
    this.roles.clear();
    this.roles.addAll(roles);
  }

  void nick(final @NonNull Optional<String> nick) {
    this.nick = nick.orElse(null);
  }

  @Override
  public boolean equals(final Object other) {
    return EvenMoreObjects.equals(this, other, that -> this.user.id() == that.user.id());
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
      .add("roles", this.roles().collect(Collectors.toSet()))
      .toString();
  }

  public interface Factory {
    MemberImpl create(final Guild guild, final JsonObject json);
  }
}
