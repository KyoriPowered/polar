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
package net.kyori.polar.util;

import java.util.function.Predicate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class Equality {
  /**
   * Tests if {@code you} equals {@code me}.
   *
   * @param me this
   * @param you that
   * @param predicate the predicate
   * @param <T> the type
   * @return {@code true} if {@code you} equals {@code me}
   */
  @SuppressWarnings("unchecked")
  public static <T> boolean equals(final @NonNull T me, final @Nullable Object you, final @NonNull Predicate<T> predicate) {
    final Class<T> type = (Class<T>) me.getClass();
    return equals(type, me, you, predicate);
  }

  /**
   * Tests if {@code you} equals {@code me}.
   *
   * @param type the type of {@code me}
   * @param me this
   * @param you that
   * @param predicate the predicate
   * @param <T> the type
   * @return {@code true} if {@code you} equals {@code me}
   */
  public static <T> boolean equals(final @NonNull Class<T> type, final @NonNull T me, final @Nullable Object you, final @NonNull Predicate<T> predicate) {
    return me == you || (you != null && type.isInstance(you) && predicate.test(type.cast(you)));
  }
}
