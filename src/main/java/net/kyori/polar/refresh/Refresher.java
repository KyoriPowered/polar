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
package net.kyori.polar.refresh;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.inject.Inject;
import net.kyori.event.EventBus;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class Refresher<T, C extends RefreshContext<T>> {
  private final List<Entry<?>> entries = new ArrayList<>();
  protected @Inject EventBus<Object> bus;

  protected Refresher() {
    this.register();
  }

  protected abstract void register();

  protected final <V> void complexField(final Function<T, V> oldValue, final Function<JsonObject, V> newValue, final BiConsumer<T, V> applier, final RefreshConsumer<C, V> apply) {
    this.entries.add(new ComplexEntry<>(oldValue, newValue, applier, apply));
  }

  protected final <V> void field(final Function<T, V> oldValue, final Function<JsonObject, V> newValue, final BiConsumer<T, V> applier, final RefreshEventFactory<C, V> event) {
    this.field(oldValue, null, newValue, applier, event);
  }

  protected final <V> void field(final Function<T, V> oldValue, final Predicate<JsonObject> newValueAvailable, final Function<JsonObject, V> newValue, final BiConsumer<T, V> applier, final RefreshEventFactory<C, V> event) {
    this.entries.add(new SimpleEntry<>(oldValue, newValueAvailable, newValue, applier, event));
  }

  public final void refresh(final C context, final JsonElement json) {
    if(json.isJsonObject()) {
      this.refresh(context, json.getAsJsonObject());
    } else {
      throw new UnsupportedOperationException(json.getClass().getSimpleName());
    }
  }

  public final void refresh(final C context, final JsonObject json) {
    for(final Entry<?> entry : this.entries) {
      entry.refresh(context, json);
    }
  }

  abstract class Entry<V> {
    final Function<T, V> oldValue;
    final Function<JsonObject, V> newValue;
    final BiConsumer<T, V> applier;

    Entry(final Function<T, V> oldValue, final Function<JsonObject, V> newValue, final BiConsumer<T, V> applier) {
      this.oldValue = oldValue;
      this.newValue = newValue;
      this.applier = applier;
    }

    void refresh(final C context, final JsonObject json) {
      final T thing = context.target();
      final V oldValue = this.oldValue.apply(thing);
      final V newValue = this.newValue.apply(json);
      if(!Objects.equals(oldValue, newValue)) {
        this.applier.accept(context.target(), newValue);
        this.refresh(context, oldValue, newValue);
      }
    }

    abstract void refresh(final C context, final V oldValue, final V newValue);
  }

  public class SimpleEntry<V> extends Entry<V> {
    final @Nullable Predicate<JsonObject> newValueAvailable;
    final RefreshEventFactory<C, V> event;

    SimpleEntry(final Function<T, V> oldValue, final @Nullable Predicate<JsonObject> newValueAvailable, final Function<JsonObject, V> newValue, final BiConsumer<T, V> applier, final RefreshEventFactory<C, V> event) {
      super(oldValue, newValue, applier);
      this.newValueAvailable = newValueAvailable;
      this.event = event;
    }

    @Override
    void refresh(final C context, final JsonObject json) {
      if(this.newValueAvailable != null && !this.newValueAvailable.test(json)) {
        return;
      }
      super.refresh(context, json);
    }

    @Override
    void refresh(final C context, final V oldValue, final V newValue) {
      Refresher.this.bus.post(this.event.apply(context, oldValue, newValue));
    }
  }

  public class ComplexEntry<V> extends Entry<V> {
    final RefreshConsumer<C, V> apply;

    ComplexEntry(final Function<T, V> oldValue, final Function<JsonObject, V> newValue, final BiConsumer<T, V> applier, final RefreshConsumer<C, V> apply) {
      super(oldValue, newValue, applier);
      this.apply = apply;
    }

    @Override
    void refresh(final C context, final V oldValue, final V newValue) {
      this.apply.accept(context, oldValue, newValue);
    }
  }
}
