/*
 * (C) Copyright 2019 zvreifnitz
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
 *
 */

package com.github.zvreifnitz.java.concurrency;

import com.github.zvreifnitz.java.utils.Preconditions;
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
    if (existingValue != null) {
      return existingValue;
    }
    return this.getSync();
  }

  public final void set(final T value) {
    this.setSync(value);
  }

  private synchronized T getSync() {
    final T existingValue = this.value;
    if (existingValue != null) {
      return existingValue;
    }
    final T createdValue = this.createInstance();
    this.value = createdValue;
    return createdValue;
  }

  private synchronized void setSync(final T value) {
    if (value == null) {
      this.value = this.createInstance();
    } else {
      this.value = VisibilityBarrier.makeVisible(value);
    }
  }

  private T createInstance() {
    final Supplier<T> supplierLocal = this.supplier;
    Preconditions.nullCheck(supplierLocal, "Singleton.supplier");
    final T instance = supplierLocal.get();
    Preconditions.nullCheck(instance, "Singleton.supplier.get()");
    return VisibilityBarrier.makeVisible(instance);
  }
}
