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
package net.kyori.polar.snowflake;

import com.google.common.base.MoreObjects;
import net.kyori.kassel.snowflake.Snowflake;
import net.kyori.kassel.snowflake.Snowflaked;

import java.util.Objects;
import net.kyori.polar.util.Equality;

public class SnowflakedImpl implements Snowflaked {
  protected final @Snowflake long id;

  public SnowflakedImpl(final @Snowflake long id) {
    this.id = id;
  }

  @Override
  public final @Snowflake long id() {
    return this.id;
  }

  @Override
  public String toString() {
    return this.toStringer().toString();
  }

  protected MoreObjects.ToStringHelper toStringer() {
    return MoreObjects.toStringHelper(this)
      .add("id", this.id);
  }

  @Override
  public boolean equals(final Object other) {
    return Equality.equals(this, other, that -> this.id == that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.id);
  }
}
