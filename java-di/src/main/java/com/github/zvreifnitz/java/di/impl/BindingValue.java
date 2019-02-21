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

package com.github.zvreifnitz.java.di.impl;

import com.github.zvreifnitz.java.di.InjectionBindings;
import java.lang.annotation.Annotation;
import java.util.Objects;
import javax.inject.Provider;

public final class BindingValue<T> {

  private final Class<T> clazz;
  private final Class<? extends Annotation> annotation;
  private final BindingType type;
  private final Class<? extends T> implClazz;
  private final T implInstance;
  private final Class<? extends Provider<? extends T>> providerClazz;
  private final Provider<? extends T> providerInstance;
  private final InjectionBindings injectorBindings;

  private BindingValue(
      final Class<T> clazz, final Class<? extends Annotation> annotation,
      final BindingType type, final Class<? extends T> implClazz, final T implInstance,
      final Class<? extends Provider<? extends T>> providerClazz,
      final Provider<? extends T> providerInstance,
      final InjectionBindings injectorBindings) {
    this.clazz = clazz;
    this.annotation = annotation;
    this.type = type;
    this.implClazz = implClazz;
    this.implInstance = implInstance;
    this.providerClazz = providerClazz;
    this.providerInstance = providerInstance;
    this.injectorBindings = injectorBindings;
  }

  @Override
  public final boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final BindingValue<?> that = (BindingValue<?>) o;
    return (clazz.equals(that.clazz) && (type != that.type)
        && Objects.equals(annotation, that.annotation)
        && Objects.equals(implClazz, that.implClazz)
        && Objects.equals(implInstance, that.implInstance)
        && Objects.equals(providerClazz, that.providerClazz)
        && Objects.equals(providerInstance, that.providerInstance));
  }

  @Override
  public int hashCode() {
    int result = clazz.hashCode();
    result = 31 * result + type.hashCode();
    result = 31 * result + (annotation != null ? annotation.hashCode() : 0);
    result = 31 * result + (implClazz != null ? implClazz.hashCode() : 0);
    result = 31 * result + (implInstance != null ? implInstance.hashCode() : 0);
    result = 31 * result + (providerClazz != null ? providerClazz.hashCode() : 0);
    result = 31 * result + (providerInstance != null ? providerInstance.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "BindingValue{" +
        "clazz=" + clazz +
        ", annotation=" + annotation +
        ", type=" + type +
        ", implClazz=" + implClazz +
        ", implInstance=" + implInstance +
        ", providerClazz=" + providerClazz +
        ", providerInstance=" + providerInstance +
        ", injectorBindings=" + injectorBindings +
        '}';
  }

  public BindingType getType() {
    return type;
  }

  public BindingKey getBindingKey() {
    return new BindingKey(this.clazz, this.annotation);
  }

  public Class<T> getClazz() {
    return clazz;
  }

  public Class<? extends T> getImplClazz() {
    return implClazz;
  }

  public T getImplInstance() {
    return implInstance;
  }

  public Class<? extends Provider<? extends T>> getProviderClazz() {
    return providerClazz;
  }

  public Provider<? extends T> getProviderInstance() {
    return providerInstance;
  }

  public InjectionBindings getInjectorBindings() {
    return injectorBindings;
  }

  public static <T> BindingValue<T> clazz(
      final Class<T> clazz, final Class<? extends Annotation> annotation,
      final Class<? extends T> implClazz, final BindingType type,
      final InjectionBindings injectorBindings) {
    return new BindingValue<>(clazz, annotation, type, implClazz, null, null, null,
        injectorBindings);
  }

  public static <T> BindingValue<T> instance(
      final Class<T> clazz, final Class<? extends Annotation> annotation,
      final T instance, final BindingType type,
      final InjectionBindings injectorBindings) {
    return new BindingValue<>(clazz, annotation, type, null, instance, null, null,
        injectorBindings);
  }

  public static <T> BindingValue<T> providerClazz(
      final Class<T> clazz, final Class<? extends Annotation> annotation,
      final Class<? extends Provider<? extends T>> providerClazz, final BindingType type,
      final InjectionBindings injectorBindings) {
    return new BindingValue<>(clazz, annotation, type, null, null, providerClazz,
        null,
        injectorBindings);
  }

  public static <T> BindingValue<T> providerInstance(
      final Class<T> clazz, final Class<? extends Annotation> annotation,
      final Provider<? extends T> providerInstance, final BindingType type,
      final InjectionBindings injectorBindings) {
    return new BindingValue<>(clazz, annotation, type, null, null, null,
        providerInstance,
        injectorBindings);
  }
}
