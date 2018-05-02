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

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;

import static com.google.common.base.Preconditions.checkState;

public final class BoundedLong2ObjectLinkedOpenHashMap<V> extends Long2ObjectLinkedOpenHashMap<V> {
  private final int maxSize;

  public static <V> Long2ObjectMap<V> sync(final int maxSize) {
    return Long2ObjectMaps.synchronize(new BoundedLong2ObjectLinkedOpenHashMap<>(maxSize));
  }

  public BoundedLong2ObjectLinkedOpenHashMap(final int maxSize) {
    checkState(maxSize > 0, "maxSize <= 0");
    this.maxSize = maxSize;
  }

  @Override
  public V put(final long key, final V value) {
    final V oldValue = super.put(key, value);
    this.shrink();
    return oldValue;
  }

  private void shrink() {
    if(this.size > this.maxSize) {
      this.removeFirst();
    }
  }
}
