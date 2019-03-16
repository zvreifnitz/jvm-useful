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

import com.github.zvreifnitz.java.deps.Openable;
import java.util.List;
import java.util.Map;

public interface Settings extends Openable {

  <T> Setting<T> getObject(
      final Class<T> clazz, final String key, final SettingChanged<? super T> listener);

  <T> Setting<List<T>> getList(
      final Class<T> clazz, final String key,
      final SettingChanged<? super List<? super T>> listener);

  <T> Setting<Map<String, T>> getMap(
      final Class<T> clazz, final String key,
      final SettingChanged<? super Map<String, ? super T>> listener);

  <T> void removeListener(
      final Class<T> clazz, final String key, final SettingChanged<?> listener);

  default Setting<Boolean> getBoolean(final String key) {
    return this.getObject(boolean.class, key, null);
  }

  default Setting<Boolean> getBoolean(
      final String key, final SettingChanged<? super Boolean> listener) {
    return this.getObject(boolean.class, key, listener);
  }

  default Setting<Integer> getInteger(final String key) {
    return this.getObject(int.class, key, null);
  }

  default Setting<Integer> getInteger(
      final String key, final SettingChanged<? super Integer> listener) {
    return this.getObject(int.class, key, listener);
  }

  default Setting<Long> getLong(final String key) {
    return this.getObject(long.class, key, null);
  }

  default Setting<Long> getLong(final String key, final SettingChanged<? super Long> listener) {
    return this.getObject(long.class, key, listener);
  }

  default Setting<Double> getDouble(final String key) {
    return this.getObject(double.class, key, null);
  }

  default Setting<Double> getDouble(
      final String key, final SettingChanged<? super Double> listener) {
    return this.getObject(double.class, key, listener);
  }

  default Setting<String> getString(final String key) {
    return this.getObject(String.class, key, null);
  }

  default Setting<String> getString(
      final String key, final SettingChanged<? super String> listener) {
    return this.getObject(String.class, key, listener);
  }

  default <T> Setting<T> getObject(final Class<T> clazz, final String key) {
    return this.getObject(clazz, key, null);
  }

  default <T> Setting<List<T>> getList(final Class<T> clazz, final String key) {
    return this.getList(clazz, key, null);
  }

  default <T> Setting<Map<String, T>> getMap(final Class<T> clazz, final String key) {
    return this.getMap(clazz, key, null);
  }
}
