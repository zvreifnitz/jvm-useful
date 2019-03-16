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
import com.github.zvreifnitz.java.settings.impl.SettingsContainer;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

final class PropertiesSettingsContainer extends AbstractOpenable {

  private final static String[] ResourceNames = new String[]{
      "reference", "config", "application", "translation"};

  private final SettingsContainer container;

  PropertiesSettingsContainer(final SettingsContainer container) {
    this.container = container;
  }

  void load() {
    this.reload();
  }

  private void reload() {
    final TreeMap<String, String> lines = new TreeMap<>();
    for (final String resourceName : ResourceNames) {
      lines.putAll(loadResource(resourceName));
    }
    final PropertyNode root = new PropertyNode(null);
    for (final Map.Entry<String, String> line : lines.entrySet()) {
      final String[] parts = PropertyNode.splitKey(line.getKey());
      PropertyNode node = root;
      for (int i = 0; i < parts.length - 1; i++) {
        node = node.getChildren().computeIfAbsent(parts[i], k -> new PropertyNode(null));
      }
      node.getChildren().put(parts[parts.length - 1], new PropertyNode(line.getValue()));
    }
    this.container.setSettingsProvider(new PropertiesSettingsProvider(root, this));
  }

  private Map<String, String> loadResource(final String resourceName) {
    try {
      final HashMap<String, String> result = new HashMap<>();
      final ResourceBundle bundle = ResourceBundle.getBundle(resourceName);
      for (final String key : bundle.keySet()) {
        result.put(key, bundle.getString(key));
      }
      return result;
    } catch (final Exception exc) {
      return new HashMap<>();
    }
  }
}
