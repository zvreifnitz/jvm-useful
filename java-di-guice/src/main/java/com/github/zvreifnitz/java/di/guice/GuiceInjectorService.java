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

package com.github.zvreifnitz.java.di.guice;

import com.github.zvreifnitz.java.di.InjectionModule;
import com.github.zvreifnitz.java.di.Injector;
import com.github.zvreifnitz.java.di.impl.BindingKey;
import com.github.zvreifnitz.java.di.impl.BindingValue;
import com.github.zvreifnitz.java.di.impl.InjectorService;
import com.github.zvreifnitz.java.utils.Exceptions;
import com.github.zvreifnitz.java.utils.Preconditions;
import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Key;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;
import javax.inject.Singleton;

public final class GuiceInjectorService implements InjectorService {

  @Override
  public final Injector createInjector(final Set<InjectionModule> modules) {
    final Map<BindingKey, BindingValue<?>> bindings = InjectionModule.collectBindings(modules);
    final GuiceInjectorModule module = new GuiceInjectorModule(bindings);
    return new GuiceInjector(module);
  }

  private final static class GuiceInjectorModule extends AbstractModule {

    private final Map<BindingKey, BindingValue<?>> bindings;

    private GuiceInjectorModule(final Map<BindingKey, BindingValue<?>> bindings) {
      this.bindings = bindings;
    }

    @Override
    protected void configure() {
      final Binder binder = this.configureBinder();
      for (final Map.Entry<BindingKey, BindingValue<?>> binding : this.bindings.entrySet()) {
        processBinding(binder, binding.getKey(), binding.getValue());
      }
    }

    private Binder configureBinder() {
      final Binder binder = this.binder();
      binder.requireExplicitBindings();
      binder.requireExactBindingAnnotations();
      binder.disableCircularProxies();
      binder.requireAtInjectOnConstructors();
      return binder;
    }

    private static <T> void processBinding(
        final Binder binder, final BindingKey key, final BindingValue<T> value) {
      switch (value.getType()) {
        case Free: {
          if (key.getAnnotation() == null) {
            binder.bind(key.getClazz());
          } else {
            binder.bind(key.getClazz())
                .annotatedWith(key.getAnnotation());
          }
          break;
        }
        case FreeEager: {
          if (key.getAnnotation() == null) {
            binder.bind(key.getClazz())
                .asEagerSingleton();
          } else {
            binder.bind(key.getClazz())
                .annotatedWith(key.getAnnotation())
                .asEagerSingleton();
          }
          break;
        }
        case FreeSingleton: {
          if (key.getAnnotation() == null) {
            binder.bind(key.getClazz())
                .in(Singleton.class);
          } else {
            binder.bind(key.getClazz())
                .annotatedWith(key.getAnnotation())
                .in(Singleton.class);
          }
          break;
        }
        case Bound: {
          if (key.getAnnotation() == null) {
            binder.bind(value.getClazz())
                .to(value.getImplClazz());
          } else {
            binder.bind(value.getClazz())
                .annotatedWith(key.getAnnotation())
                .to(value.getImplClazz());
          }
          break;
        }
        case BoundEager: {
          if (key.getAnnotation() == null) {
            binder.bind(value.getClazz())
                .to(value.getImplClazz())
                .asEagerSingleton();
          } else {
            binder.bind(value.getClazz())
                .annotatedWith(key.getAnnotation())
                .to(value.getImplClazz())
                .asEagerSingleton();
          }
          break;
        }
        case BoundSingleton: {
          if (key.getAnnotation() == null) {
            binder.bind(value.getClazz())
                .to(value.getImplClazz())
                .in(Singleton.class);
          } else {
            binder.bind(value.getClazz())
                .annotatedWith(key.getAnnotation())
                .to(value.getImplClazz())
                .in(Singleton.class);
          }
          break;
        }
        case BoundInstance: {
          if (key.getAnnotation() == null) {
            binder.bind(value.getClazz())
                .toInstance(value.getImplInstance());
          } else {
            binder.bind(value.getClazz())
                .annotatedWith(key.getAnnotation())
                .toInstance(value.getImplInstance());
          }
          break;
        }
        case Provider: {
          if (key.getAnnotation() == null) {
            binder.bind(value.getClazz())
                .toProvider(value.getProviderClazz());
          } else {
            binder.bind(value.getClazz())
                .annotatedWith(key.getAnnotation())
                .toProvider(value.getProviderClazz());
          }
          break;
        }
        case ProviderEager: {
          if (key.getAnnotation() == null) {
            binder.bind(value.getClazz())
                .toProvider(value.getProviderClazz())
                .asEagerSingleton();
          } else {
            binder.bind(value.getClazz())
                .annotatedWith(key.getAnnotation())
                .toProvider(value.getProviderClazz())
                .asEagerSingleton();
          }
          break;
        }
        case ProviderSingleton: {
          if (key.getAnnotation() == null) {
            binder.bind(value.getClazz())
                .toProvider(value.getProviderClazz())
                .in(Singleton.class);
          } else {
            binder.bind(value.getClazz())
                .annotatedWith(key.getAnnotation())
                .toProvider(value.getProviderClazz())
                .in(Singleton.class);
          }
          break;
        }
        case ProviderInstance: {
          if (key.getAnnotation() == null) {
            binder.bind(value.getClazz())
                .toProvider(value.getProviderInstance());
          } else {
            binder.bind(value.getClazz())
                .annotatedWith(key.getAnnotation())
                .toProvider(value.getProviderInstance());
          }
          break;
        }
        case ProviderInstanceEager: {
          if (key.getAnnotation() == null) {
            binder.bind(value.getClazz())
                .toProvider(value.getProviderInstance())
                .asEagerSingleton();
          } else {
            binder.bind(value.getClazz())
                .annotatedWith(key.getAnnotation())
                .toProvider(value.getProviderInstance())
                .asEagerSingleton();
          }
          break;
        }
        case ProviderInstanceSingleton: {
          if (key.getAnnotation() == null) {
            binder.bind(value.getClazz())
                .toProvider(value.getProviderInstance())
                .in(Singleton.class);
          } else {
            binder.bind(value.getClazz())
                .annotatedWith(key.getAnnotation())
                .toProvider(value.getProviderInstance())
                .in(Singleton.class);
          }
          break;
        }
      }
    }
  }

  private final static class GuiceInjector implements Injector {

    private final com.google.inject.Injector underlyingInjector;

    private GuiceInjector(final GuiceInjectorModule module) {
      this.underlyingInjector = Guice.createInjector(module);
    }

    @Override
    public <T> T getInstance(final Class<T> clazz) {
      Preconditions.checkNotNull(clazz, "clazz");
      try {
        return this.underlyingInjector.getInstance(Key.get(clazz));
      } catch (final Throwable throwable) {
        return Exceptions.throwExcUnchecked(throwable);
      }
    }

    @Override
    public <T> T getInstance(final Class<T> clazz, final Class<? extends Annotation> annotation) {
      Preconditions.checkNotNull(clazz, "clazz");
      Preconditions.checkNotNull(annotation, "annotation");
      try {
        return this.underlyingInjector.getInstance(Key.get(clazz, annotation));
      } catch (final Throwable throwable) {
        return Exceptions.throwExcUnchecked(throwable);
      }
    }

  }
}
