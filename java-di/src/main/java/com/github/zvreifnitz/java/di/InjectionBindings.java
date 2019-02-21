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

package com.github.zvreifnitz.java.di;

import com.github.zvreifnitz.java.di.impl.BindingKey;
import com.github.zvreifnitz.java.di.impl.BindingType;
import com.github.zvreifnitz.java.di.impl.BindingValue;
import com.github.zvreifnitz.java.utils.AbstractClassIdentifiable;
import com.github.zvreifnitz.java.utils.Preconditions;
import com.github.zvreifnitz.java.utils.Reflection;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.inject.Provider;

public abstract class InjectionBindings extends AbstractClassIdentifiable {

  private final Set<InjectionBindings> bindings;
  private final Map<BindingKey, BindingValue<?>> entries;

  int nestingLevel = 0;

  protected InjectionBindings() {
    Reflection.hasOnlyParamlessCTOR(this.getClazz());
    this.entries = new HashMap<>();
    this.bindings = new HashSet<>();
  }

  @Override
  public String toString() {
    return "InjectionBindings{}";
  }

  protected final void registerBindings(final InjectionBindings bindings) {
    Preconditions.checkNotNull(bindings, "bindings");
    bindings.nestingLevel = (this.nestingLevel + 1);
    this.bindings.add(bindings);
  }

  protected final <T> BindingBulder<T> register(final Class<T> clazz) {
    Preconditions.checkNotNull(clazz, "clazz");
    this.entries.put(
        new BindingKey(clazz, null),
        BindingValue.clazz(clazz, null, clazz, BindingType.Free, this));
    return new BindingBuilderImpl<>(clazz, null, this);
  }

  protected final <T> BindingBulder<T> registerAnnotated(
      final Class<T> clazz, final Class<? extends Annotation> annotation) {
    Preconditions.checkNotNull(clazz, "clazz");
    Preconditions.checkNotNull(annotation, "annotation");
    this.entries.put(
        new BindingKey(clazz, annotation),
        BindingValue.clazz(clazz, annotation, clazz, BindingType.Free, this));
    return new BindingBuilderImpl<>(clazz, null, this);
  }

  final void merge(final Map<BindingKey, BindingValue<?>> result) {
    for (final InjectionBindings binding : this.bindings) {
      binding.merge(result);
    }
    for (final Map.Entry<BindingKey, BindingValue<?>> entry : this.entries.entrySet()) {
      merge(result, entry.getKey(), entry.getValue());
    }
  }

  private static void merge(
      final Map<BindingKey, BindingValue<?>> result, final BindingKey key,
      final BindingValue<?> value) {
    final BindingValue<?> existingValue = result.get(key);
    if (existingValue == null) {
      result.put(key, value);
      return;
    }
    final int existNestLevel = existingValue.getInjectorBindings().nestingLevel;
    final int valueNestLevel = value.getInjectorBindings().nestingLevel;
    if (existNestLevel < valueNestLevel) {
      return;
    }
    if (existNestLevel > valueNestLevel) {
      result.put(key, value);
      return;
    }
    if (existingValue.equals(value)) {
      return;
    }
    throw new RuntimeException(
        "Different binding already exists for key " + key + " [existing: " + existingValue
            + ", new: " + value + "]");
  }

  protected interface BindingBulder<T> extends BindingLinker<T>, BindingScope {

  }

  protected interface BindingLinker<T> {

    BindingScope toClass(Class<? extends T> clazz);

    BindingScope toProviderClass(Class<? extends Provider<? extends T>> clazz);

    BindingScope toProviderInstance(Provider<? extends T> instance);

    void toInstance(T instance);
  }

  protected interface BindingScope {

    void asSingleton();

    void asEagerSingleton();
  }

  private final static class BindingBuilderImpl<T> implements BindingBulder<T> {

    private InjectionBindings parent;
    private Class<T> clazz;
    private Class<? extends Annotation> annotation;

    private BindingBuilderImpl(
        final Class<T> clazz, final Class<? extends Annotation> annotation,
        final InjectionBindings parent) {
      this.clazz = clazz;
      this.annotation = annotation;
      this.parent = parent;
    }

    @Override
    public final BindingScope toClass(final Class<? extends T> clazz) {
      if (clazz == null) {
        throw new NullPointerException("clazz");
      }
      this.parent.entries.put(
          new BindingKey(this.clazz, this.annotation),
          BindingValue.clazz(this.clazz, this.annotation, clazz,
              BindingType.Bound, this.parent));
      return new BindingScopeImpl<>(this.clazz, this.annotation, clazz, this.parent);
    }

    @Override
    public final BindingScope toProviderClass(final Class<? extends Provider<? extends T>> clazz) {
      if (clazz == null) {
        throw new NullPointerException("clazz");
      }
      this.parent.entries.put(
          new BindingKey(this.clazz, this.annotation),
          BindingValue.providerClazz(this.clazz, this.annotation, clazz,
              BindingType.Provider, this.parent));
      return new SupplierBindingScopeImpl<>(this.clazz, this.annotation, clazz,
          this.parent);
    }

    @Override
    public final BindingScope toProviderInstance(final Provider<? extends T> instance) {
      if (instance == null) {
        throw new NullPointerException("instance");
      }
      this.parent.entries.put(
          new BindingKey(this.clazz, this.annotation),
          BindingValue
              .providerInstance(this.clazz, this.annotation, instance,
                  BindingType.ProviderInstance, this.parent));
      return new SupplierInstanceBindingScopeImpl<>(this.clazz, this.annotation, instance,
          this.parent);
    }

    @Override
    public final void toInstance(final T instance) {
      if (instance == null) {
        throw new NullPointerException("instance");
      }
      this.parent.entries.put(
          new BindingKey(this.clazz, this.annotation),
          BindingValue.instance(this.clazz, this.annotation, instance,
              BindingType.BoundInstance, this.parent));
    }

    @Override
    public final void asSingleton() {
      this.parent.entries.put(
          new BindingKey(this.clazz, this.annotation),
          BindingValue.clazz(this.clazz, this.annotation, this.clazz,
              BindingType.FreeSingleton, this.parent));
    }

    @Override
    public final void asEagerSingleton() {
      this.parent.entries.put(
          new BindingKey(this.clazz, this.annotation),
          BindingValue
              .clazz(this.clazz, this.annotation, this.clazz,
                  BindingType.FreeEager, this.parent));
    }
  }

  private final static class BindingScopeImpl<T> implements BindingScope {

    private InjectionBindings parent;
    private Class<T> clazz;
    private Class<? extends T> clazzImpl;
    private Class<? extends Annotation> annotation;

    private BindingScopeImpl(
        final Class<T> clazz, final Class<? extends Annotation> annotation,
        final Class<? extends T> clazzImpl, final InjectionBindings parent) {
      this.clazz = clazz;
      this.clazzImpl = clazzImpl;
      this.annotation = annotation;
      this.parent = parent;
    }

    @Override
    public final void asSingleton() {
      this.parent.entries.put(
          new BindingKey(this.clazz, this.annotation),
          BindingValue
              .clazz(this.clazz, this.annotation, this.clazzImpl,
                  BindingType.BoundSingleton, this.parent));
    }

    @Override
    public final void asEagerSingleton() {
      this.parent.entries.put(
          new BindingKey(this.clazz, this.annotation),
          BindingValue.clazz(this.clazz, this.annotation, this.clazzImpl,
              BindingType.BoundEager, this.parent));
    }
  }

  private final static class SupplierBindingScopeImpl<T, S extends Provider<? extends T>>
      implements BindingScope {

    private InjectionBindings parent;
    private Class<T> clazz;
    private Class<? extends Annotation> annotation;
    private Class<S> supplierClazz;

    private SupplierBindingScopeImpl(
        final Class<T> clazz, final Class<? extends Annotation> annotation,
        final Class<S> supplierClazz, final InjectionBindings parent) {
      this.clazz = clazz;
      this.annotation = annotation;
      this.supplierClazz = supplierClazz;
      this.parent = parent;
    }

    @Override
    public final void asSingleton() {
      this.parent.entries.put(
          new BindingKey(this.clazz, this.annotation),
          BindingValue.providerClazz(this.clazz, this.annotation,
              this.supplierClazz, BindingType.ProviderSingleton, this.parent));
    }

    @Override
    public final void asEagerSingleton() {
      this.parent.entries.put(
          new BindingKey(this.clazz, this.annotation),
          BindingValue.providerClazz(this.clazz, this.annotation,
              this.supplierClazz, BindingType.ProviderEager, this.parent));
    }
  }

  private final static class SupplierInstanceBindingScopeImpl<T, S extends Provider<? extends T>>
      implements BindingScope {

    private InjectionBindings parent;
    private Class<T> clazz;
    private Class<? extends Annotation> annotation;
    private S supplierInstance;

    private SupplierInstanceBindingScopeImpl(
        final Class<T> clazz, final Class<? extends Annotation> annotation,
        final S supplierInstance, final InjectionBindings parent) {
      this.clazz = clazz;
      this.annotation = annotation;
      this.supplierInstance = supplierInstance;
      this.parent = parent;
    }

    @Override
    public final void asSingleton() {
      this.parent.entries.put(
          new BindingKey(this.clazz, this.annotation),
          BindingValue
              .providerInstance(this.clazz, this.annotation,
                  this.supplierInstance, BindingType.ProviderInstanceSingleton, this.parent));
    }

    @Override
    public final void asEagerSingleton() {
      this.parent.entries.put(
          new BindingKey(this.clazz, this.annotation),
          BindingValue
              .providerInstance(this.clazz, this.annotation,
                  this.supplierInstance, BindingType.ProviderInstanceEager, this.parent));
    }
  }
}
