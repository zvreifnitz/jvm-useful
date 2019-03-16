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

package com.github.zvreifnitz.java.settings.impl.properties;

import com.github.zvreifnitz.java.deps.AbstractOpenable;
import com.github.zvreifnitz.java.settings.Setting;
import com.github.zvreifnitz.java.settings.impl.SettingsProvider;
import com.github.zvreifnitz.java.utils.Exceptions;
import com.github.zvreifnitz.java.utils.Reflection;
import com.github.zvreifnitz.java.utils.T2;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

final class PropertiesSettingsProvider extends AbstractOpenable implements SettingsProvider {

  private final PropertyNode root;

  PropertiesSettingsProvider(
      final PropertyNode root,
      final PropertiesSettingsContainer container) {
    super(container);
    this.root = root;
  }

  @Override
  public final <T> Setting<T> getObject(final Class<T> clazz, final String key) {
    final PropertyNode node = this.root.getSubNode(key);
    return (node == null)
        ? new Setting<>(key)
        : convertObject(clazz, key, node);
  }

  @Override
  public final <T> Setting<List<T>> getList(final Class<T> clazz, final String key) {
    final PropertyNode node = this.root.getSubNode(key);
    return (node == null)
        ? new Setting<>(key)
        : convertList(clazz, key, node.getChildren());
  }

  @Override
  public final <T> Setting<Map<String, T>> getMap(final Class<T> clazz, final String key) {
    final PropertyNode node = this.root.getSubNode(key);
    return (node == null)
        ? new Setting<>(key)
        : convertMap(clazz, key, node.getChildren());
  }

  @Override
  public final boolean isValueIdentical(final String key, final SettingsProvider otherProvider) {
    return (otherProvider instanceof PropertiesSettingsProvider)
        && this.isValueIdenticalInternal(key, (PropertiesSettingsProvider) otherProvider);
  }

  private boolean isValueIdenticalInternal(
      final String key, final PropertiesSettingsProvider otherProvider) {
    return Objects.equals(this.root.getSubNode(key), otherProvider.root.getSubNode(key));
  }

  private static int toIndex(final String input) {
    try {
      return (input == null) ? -1 : Integer.parseInt(input);
    } catch (final Exception ignored) {
      return -1;
    }
  }

  private static <T> Setting<Map<String, T>> convertMap(
      final Class<T> clazz, final String key, final HashMap<String, PropertyNode> children) {
    try {
      return new Setting<>(key, toMap(clazz, children));
    } catch (final Exception exc) {
      return new Setting<>(key, exc);
    }
  }

  private static <T> Map<String, T> toMap(
      final Class<T> clazz, final HashMap<String, PropertyNode> children) throws Exception {
    final Map<String, T> result = new HashMap<>(children.size());
    for (final Map.Entry<String, PropertyNode> child : children.entrySet()) {
      result.put(child.getKey(), toObject(clazz, child.getValue()));
    }
    return result;
  }

  private static <T> Setting<List<T>> convertList(
      final Class<T> clazz, final String key, final HashMap<String, PropertyNode> children) {
    try {
      return new Setting<>(key, toList(clazz, children));
    } catch (final Exception exc) {
      return new Setting<>(key, exc);
    }
  }

  private static <T> List<T> toList(
      final Class<T> clazz, final HashMap<String, PropertyNode> children) throws Exception {
    final List<T2<String, Integer>> itemIdx = children.keySet().stream()
        .map(k -> new T2<>(k, toIndex(k)))
        .filter(t -> t._2 != -1)
        .sorted(Comparator.comparingInt(t -> -t._2))
        .collect(Collectors.toList());
    final int size = itemIdx.isEmpty() ? 0 : itemIdx.get(0)._2 + 1;
    final List<T> result = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      result.add(null);
    }
    for (final T2<String, Integer> t : itemIdx) {
      result.set(t._2, toObject(clazz, children.get(t._1)));
    }
    return result;
  }

  private static <T> Setting<T> convertObject(
      final Class<T> clazz, final String key, final PropertyNode node) {
    try {
      return new Setting<>(key, toObject(clazz, node));
    } catch (final Exception exc) {
      return new Setting<>(key, exc);
    }
  }

  private static <T> T toObject(final Class<T> clazz, final PropertyNode node) throws Exception {
    final T simpleObject = toSimpleObject(clazz, node);
    return (simpleObject != null) ? simpleObject : toComplexObject(clazz, node);
  }

  private static <T> T toSimpleObject(
      final Class<T> clazz, final PropertyNode node) throws Exception {
    if (String.class.equals(clazz)) {
      return node.getValue() == null
          ? null
          : Reflection.cast(clazz, node.getValue());
    }
    if (int.class.equals(clazz)) {
      return Reflection.cast(clazz, Integer.parseInt(node.getValue()));
    }
    if (Integer.class.equals(clazz)) {
      return node.getValue() == null
          ? null
          : Reflection.cast(clazz, Integer.parseInt(node.getValue()));
    }
    if (boolean.class.equals(clazz)) {
      return Reflection.cast(clazz, Boolean.parseBoolean(node.getValue()));
    }
    if (Boolean.class.equals(clazz)) {
      return node.getValue() == null
          ? null
          : Reflection.cast(clazz, Boolean.parseBoolean(node.getValue()));
    }
    if (long.class.equals(clazz)) {
      return Reflection.cast(clazz, Long.parseLong(node.getValue()));
    }
    if (Long.class.equals(clazz)) {
      return node.getValue() == null
          ? null
          : Reflection.cast(clazz, Long.parseLong(node.getValue()));
    }
    if (double.class.equals(clazz)) {
      return Reflection.cast(clazz, Double.parseDouble(node.getValue()));
    }
    if (Double.class.equals(clazz)) {
      return node.getValue() == null
          ? null
          : Reflection.cast(clazz, Double.parseDouble(node.getValue()));
    }
    if (byte.class.equals(clazz)) {
      return Reflection.cast(clazz, Byte.parseByte(node.getValue()));
    }
    if (Byte.class.equals(clazz)) {
      return node.getValue() == null
          ? null
          : Reflection.cast(clazz, Byte.parseByte(node.getValue()));
    }
    if (char.class.equals(clazz)) {
      return Reflection.cast(clazz, node.getValue().charAt(0));
    }
    if (Character.class.equals(clazz)) {
      return node.getValue() == null
          ? null
          : Reflection.cast(clazz, node.getValue().charAt(0));
    }
    if (short.class.equals(clazz)) {
      return Reflection.cast(clazz, Short.parseShort(node.getValue()));
    }
    if (Short.class.equals(clazz)) {
      return node.getValue() == null
          ? null
          : Reflection.cast(clazz, Short.parseShort(node.getValue()));
    }
    if (float.class.equals(clazz)) {
      return Reflection.cast(clazz, Float.parseFloat(node.getValue()));
    }
    if (Float.class.equals(clazz)) {
      return node.getValue() == null
          ? null
          : Reflection.cast(clazz, Float.parseFloat(node.getValue()));
    }
    return null;
  }

  private static <T> T toComplexObject(
      final Class<T> clazz, final PropertyNode node) throws Exception {
    final T result = getInstance(clazz);
    for (final Map.Entry<String, PropertyNode> propertyEntry : node.getChildren().entrySet()) {
      setField(result, propertyEntry.getKey(), propertyEntry.getValue());
    }
    return result;
  }

  private static <T> T getInstance(final Class<T> clazz) throws Exception {
    try {
      final Constructor<T> ctor = clazz.getDeclaredConstructor();
      final boolean accessible = ctor.isAccessible();
      try {
        ctor.setAccessible(true);
        return ctor.newInstance();
      } finally {
        ctor.setAccessible(accessible);
      }
    } catch (final Exception exc) {
      return Exceptions
          .throwExc(new Exception("Invalid constructor for '" + clazz.getName() + "'", exc));
    }
  }

  private static <T> void setField(
      final T instance, final String fieldName, final PropertyNode node) throws Exception {
    final Field field = instance.getClass().getDeclaredField(fieldName);
    if (field == null) {
      return;
    }
    final boolean accessible = field.isAccessible();
    try {
      field.setAccessible(true);
      field.set(instance, toObject(field.getType(), node));
    } finally {
      field.setAccessible(accessible);
    }
  }
}
