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

public enum BindingType {
  Free(false),
  FreeEager(true),
  FreeSingleton(true),
  Bound(false),
  BoundEager(true),
  BoundSingleton(true),
  BoundInstance(true),
  Provider(false),
  ProviderEager(true),
  ProviderSingleton(true),
  ProviderInstance(false),
  ProviderInstanceEager(true),
  ProviderInstanceSingleton(true);

  private final boolean singleton;

  BindingType(final boolean singleton) {
    this.singleton = singleton;
  }

  public boolean isSingleton() {
    return singleton;
  }
}
