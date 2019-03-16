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

import com.github.zvreifnitz.java.settings.impl.SettingsImpl;
import com.github.zvreifnitz.java.settings.impl.SettingsService;
import com.github.zvreifnitz.java.settings.impl.properties.PropertiesSettingsService;
import java.util.Iterator;
import java.util.ServiceLoader;

public final class SettingsFactory {

  private final static SettingsService SETTINGS_SERVICE;

  static {
    final ServiceLoader<SettingsService> loader = ServiceLoader.load(SettingsService.class);
    final Iterator<SettingsService> iterator = loader.iterator();
    SETTINGS_SERVICE = iterator.hasNext() ? iterator.next() : new PropertiesSettingsService();
  }

  public static Settings createSettings() {
    final SettingsImpl settings = new SettingsImpl();
    SETTINGS_SERVICE.registerSettingsContainer(settings);
    return settings;
  }
}
