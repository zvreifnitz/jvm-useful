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

import com.github.zvreifnitz.java.utils.Preconditions;
import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Pattern;

final class PropertyNode {

  private final static Pattern KEY_SPLIT = Pattern.compile("\\.");

  private final String value;
  private final HashMap<String, PropertyNode> children;

  PropertyNode(final String value) {
    this.value = value;
    this.children = new HashMap<>();
  }

  String getValue() {
    return value;
  }

  HashMap<String, PropertyNode> getChildren() {
    return children;
  }

  PropertyNode getSubNode(final String key) {
    PropertyNode result = this;
    for (final String part : splitKey(key)) {
      result = (result == null)
          ? null
          : result.children.get(part);
    }
    return result;
  }

  @Override
  public final boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final PropertyNode other = (PropertyNode) o;
    return Objects.equals(this.value, other.value) && this.children.equals(other.children);
  }

  @Override
  public final int hashCode() {
    int result = value != null ? value.hashCode() : 0;
    result = 31 * result + children.hashCode();
    return result;
  }

  static String[] splitKey(final String key) {
    Preconditions.checkNotNull(key, "key");
    return KEY_SPLIT.split(key, 0);
  }
}
