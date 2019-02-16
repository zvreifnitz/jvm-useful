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

import com.github.zvreifnitz.java.di.impl.InjectorService;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.Set;

public final class InjectorFactory {

  private final static InjectorService InjectorService;

  static {
    final ServiceLoader<InjectorService> loader = ServiceLoader.load(InjectorService.class);
    final Iterator<InjectorService> iterator = loader.iterator();
    if (!iterator.hasNext()) {
      throw new Error("No 'InjectorService' found");
    }
    InjectorService = iterator.next();
  }

  public static Injector createInjector(final Set<InjectionModule> modules) {
    return InjectorService.createInjector(modules);
  }
}
