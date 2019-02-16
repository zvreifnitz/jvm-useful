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

package com.github.zvreifnitz.java.di.impl;

import java.lang.annotation.Annotation;
import java.util.Objects;

public final class BindingKey {

  private final Class<?> clazz;
  private final Class<? extends Annotation> annotation;

  public BindingKey(final Class<?> clazz, final Class<? extends Annotation> annotation) {
    this.clazz = clazz;
    this.annotation = annotation;
  }

  @Override
  public int hashCode() {
    int result = clazz.hashCode();
    result = 31 * result + (annotation != null ? annotation.hashCode() : 0);
    return result;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if ((o == null) || (getClass() != o.getClass())) {
      return false;
    }
    final BindingKey that = (BindingKey) o;
    return (clazz.equals(that.clazz) && Objects.equals(annotation, that.annotation));
  }

  @Override
  public String toString() {
    return "BindingKey{" +
        "clazz=" + clazz +
        ", annotation=" + annotation +
        '}';
  }

  public Class<?> getClazz() {
    return clazz;
  }

  public Class<? extends Annotation> getAnnotation() {
    return annotation;
  }
}
