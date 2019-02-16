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

package com.github.zvreifnitz.java.di;

import com.github.zvreifnitz.java.di.InjectionBindings.BindingBulder;
import com.github.zvreifnitz.java.di.impl.BindingKey;
import com.github.zvreifnitz.java.di.impl.BindingValue;
import com.github.zvreifnitz.java.utils.AbstractClassIdentifiable;
import com.github.zvreifnitz.java.utils.Preconditions;
import com.github.zvreifnitz.java.utils.Reflection;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class InjectionModule extends AbstractClassIdentifiable {

  private final ModuleBindings bindings;
  private final Set<InjectionModule> modules;

  int nestingLevel = 0;

  protected InjectionModule() {
    Reflection.hasOnlyParamlessCTOR(this.getClazz());
    this.bindings = new ModuleBindings();
    this.modules = new HashSet<>();
  }

  protected final <T> BindingBulder<T> register(final Class<T> clazz) {
    this.bindings.nestingLevel = this.nestingLevel;
    return this.bindings.register(clazz);
  }

  protected final <T> BindingBulder<T> registerAnnotated(
      final Class<T> clazz, final Class<? extends Annotation> annotation) {
    this.bindings.nestingLevel = this.nestingLevel;
    return this.bindings.registerAnnotated(clazz, annotation);
  }

  protected final void registerBindings(final InjectionBindings bindings) {
    this.bindings.nestingLevel = this.nestingLevel;
    this.bindings.registerBindings(bindings);
  }

  protected final void registerModule(final InjectionModule module) {
    Preconditions.checkNotNull(module, "module");
    module.nestingLevel = (this.nestingLevel + 1);
    this.modules.add(module);
  }

  private void merge(final Map<BindingKey, BindingValue<?>> result) {
    for (final InjectionModule module : this.modules) {
      module.merge(result);
    }
    this.bindings.merge(result);
  }

  public static Map<BindingKey, BindingValue<?>> collectBindings(
      final Set<InjectionModule> modules) {
    final Map<BindingKey, BindingValue<?>> result = new HashMap<>();
    for (final InjectionModule module : modules) {
      module.merge(result);
    }
    return result;
  }

  private final static class ModuleBindings extends InjectionBindings {

    public ModuleBindings() {
    }
  }
}
