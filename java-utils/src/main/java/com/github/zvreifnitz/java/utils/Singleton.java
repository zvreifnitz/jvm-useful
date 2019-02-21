/*
 * Copyright 2019 zvreifnitz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.zvreifnitz.java.utils;

import java.util.function.Supplier;

public final class Singleton<T> implements Supplier<T> {

  private final Supplier<T> supplier;
  private T value = null;

  public Singleton() {
    this(null);
  }

  public Singleton(final Supplier<T> supplier) {
    this.supplier = supplier;
  }

  @Override
  public final T get() {
    final T existingValue = this.value;
    return (existingValue != null) ? existingValue : this.getSync();
  }

  public final void set(final T value) {
    this.setSync(value);
  }

  private synchronized T getSync() {
    final T existingValue = this.value;
    return (existingValue != null) ? existingValue : this.createAndSetInstance();
  }

  private synchronized void setSync(final T value) {
    final T createdValue = (value == null) ? this.createInstance() : value;
    this.value = VisibilityBarrier.makeVisible(createdValue);
  }

  private T createAndSetInstance() {
    final T createdValue = this.createInstance();
    this.value = VisibilityBarrier.makeVisible(createdValue);
    return createdValue;
  }

  private T createInstance() {
    final Supplier<T> supplierLocal = Preconditions
        .checkNotNull(this.supplier, "Singleton.supplier");
    return Preconditions.checkNotNull(supplierLocal.get(), "Singleton.supplier.get()");
  }
}
