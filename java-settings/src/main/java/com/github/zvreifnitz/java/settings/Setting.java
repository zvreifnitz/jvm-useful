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

package com.github.zvreifnitz.java.settings;

import com.github.zvreifnitz.java.utils.Exceptions;
import com.github.zvreifnitz.java.utils.Preconditions;

public final class Setting<T> {

  private final String key;
  private final T value;
  private final Exception exception;

  public Setting(final String key) {
    this(key, null, new RuntimeException("Setting for a key '" + key + "' is missing"));
  }

  public Setting(final String key, final Exception exception) {
    this(key, null, exception);
  }

  public Setting(final String key, final T value) {
    this(key, value, null);
  }

  private Setting(final String key, final T value, final Exception exception) {
    this.key = Preconditions.checkNotNull(key, "key");
    this.value = value;
    this.exception = exception;
  }

  public String getKey() {
    return this.key;
  }

  public T get() {
    return (this.exception == null)
        ? this.value
        : Exceptions.throwExcUnchecked(this.exception);
  }

  public boolean isPresent() {
    return (this.exception == null);
  }

  public T getOrDefault(final T defaultValue) {
    return (this.exception == null) ? this.value : defaultValue;
  }

  public Setting<T> getOrNext(final Setting<T> next) {
    return ((this.exception == null) || (next == null)) ? this : next;
  }
}
