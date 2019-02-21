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

import com.github.zvreifnitz.java.utils.Preconditions;
import com.github.zvreifnitz.java.utils.VisibilityBarrier;
import java.lang.annotation.Annotation;
import java.util.Set;

public final class StaticInjector {

  private static volatile boolean init = false;
  private static Injector injectorRef;

  public synchronized static void init(final Injector injector) {
    if (init) {
      throw new RuntimeException("StaticInjector already init");
    }
    final Injector injectorLocal = Preconditions.checkNotNull(injector, "injector");
    injectorRef = VisibilityBarrier.makeVisible(injectorLocal);
    init = true;
  }

  public synchronized static void init(final Set<InjectionModule> modules) {
    if (init) {
      throw new RuntimeException("StaticInjector already init");
    }
    final Injector injectorLocal = InjectorFactory.createInjector(modules);
    injectorRef = VisibilityBarrier.makeVisible(injectorLocal);
    init = true;
  }

  public static <T> T getInstance(final Class<T> clazz) {
    return getInjector().getInstance(clazz);
  }

  public static <T> T getInstance(
      final Class<T> clazz, final Class<? extends Annotation> annotation) {
    return getInjector().getInstance(clazz, annotation);
  }

  private static Injector getInjector() {
    final Injector injectorLocal = injectorRef;
    if (injectorLocal != null) {
      return injectorLocal;
    }
    if (init) {
      return injectorRef;
    }
    throw new RuntimeException("StaticInjector not init");
  }
}
