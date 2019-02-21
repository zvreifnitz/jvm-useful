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

public abstract class AbstractClassIdentifiable {

  private final Class<? extends AbstractClassIdentifiable> clazz;

  protected AbstractClassIdentifiable() {
    this.clazz = this.getClass();
  }

  public final Class<?> getClazz() {
    return clazz;
  }

  @Override
  public final boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final AbstractClassIdentifiable that = (AbstractClassIdentifiable) o;
    return clazz.equals(that.clazz);
  }

  @Override
  public final int hashCode() {
    return clazz.hashCode();
  }
}
