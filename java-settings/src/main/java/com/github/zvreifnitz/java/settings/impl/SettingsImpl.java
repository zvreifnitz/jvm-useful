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

package com.github.zvreifnitz.java.settings.impl;

import com.github.zvreifnitz.java.deps.AbstractOpenableResource;
import com.github.zvreifnitz.java.settings.Setting;
import com.github.zvreifnitz.java.settings.SettingChanged;
import com.github.zvreifnitz.java.settings.Settings;
import com.github.zvreifnitz.java.utils.Preconditions;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class SettingsImpl extends AbstractOpenableResource<SettingsProvider>
    implements SettingsContainer, Settings {

  private final Set<Listener<?>> listeners = new HashSet<>();

  public SettingsImpl() {
    super(null);
  }

  @Override
  public final <T> Setting<T> getObject(
      final Class<T> clazz, final String key, final SettingChanged<? super T> listener) {
    Preconditions.checkNotNull(clazz, "clazz");
    Preconditions.checkNotNull(key, "key");
    this.checkOpen();
    if (listener != null) {
      synchronized (this.listeners) {
        this.listeners.add(new ObjectListener<>(clazz, key, listener));
      }
    }
    final Setting<T> result = this.getResource().getObject(clazz, key);
    return ((result == null) ? new Setting<>(key) : result);
  }

  @Override
  public final <T> Setting<List<T>> getList(
      final Class<T> clazz, final String key,
      final SettingChanged<? super List<? super T>> listener) {
    Preconditions.checkNotNull(clazz, "clazz");
    Preconditions.checkNotNull(key, "key");
    this.checkOpen();
    if (listener != null) {
      synchronized (this.listeners) {
        this.listeners.add(new ListListener<>(clazz, key, listener));
      }
    }
    final Setting<List<T>> result = this.getResource().getList(clazz, key);
    return ((result == null) ? new Setting<>(key) : result);
  }

  @Override
  public final <T> Setting<Map<String, T>> getMap(
      final Class<T> clazz, final String key,
      final SettingChanged<? super Map<String, ? super T>> listener) {
    Preconditions.checkNotNull(clazz, "clazz");
    Preconditions.checkNotNull(key, "key");
    this.checkOpen();
    if (listener != null) {
      synchronized (this.listeners) {
        this.listeners.add(new MapListener<>(clazz, key, listener));
      }
    }
    final Setting<Map<String, T>> result = this.getResource().getMap(clazz, key);
    return ((result == null) ? new Setting<>(key) : result);
  }

  @Override
  public final <T> void removeListener(
      final Class<T> clazz, final String key, final SettingChanged<?> listener) {
    Preconditions.checkNotNull(clazz, "clazz");
    Preconditions.checkNotNull(key, "key");
    Preconditions.checkNotNull(listener, "listener");
    synchronized (this.listeners) {
      this.listeners.removeIf(
          l -> clazz.equals(l.clazz) && key.equals(l.key) && listener.equals(l.getListener()));
    }
  }

  @Override
  public final void setSettingsProvider(final SettingsProvider settingsProvider) {
    this.setResource(settingsProvider);
  }

  @Override
  protected final void performCloseResource(final SettingsProvider oldSettingsProvider) {
    final SettingsProvider newSettingsProvider = this.getResource();
    this.performUpdate(oldSettingsProvider, newSettingsProvider);
  }

  private void performUpdate(
      final SettingsProvider oldSettingsProvider,
      final SettingsProvider newSettingsProvider) {
    if ((oldSettingsProvider == null) && (newSettingsProvider == null)) {
      return;
    }
    synchronized (this.listeners) {
      for (final Listener<?> listener : this.listeners) {
        listener.triggerIfDifferent(oldSettingsProvider, newSettingsProvider);
      }
    }
  }

  private static <I> boolean settingsEqual(
      final Setting<I> oldSetting,
      final Setting<I> newSetting) {
    return ((oldSetting != null) && (newSetting != null)
        && Objects.equals(oldSetting.getOrDefault(null), newSetting.getOrDefault(null)));
  }

  private abstract static class Listener<I> {

    final Class<I> clazz;
    final String key;

    private Listener(final Class<I> clazz, final String key) {
      this.clazz = clazz;
      this.key = key;
    }

    private void triggerIfDifferent(
        final SettingsProvider oldSettingsProvider,
        final SettingsProvider newSettingsProvider) {
      final boolean isEqual = ((oldSettingsProvider != null)
          && (newSettingsProvider != null)
          && newSettingsProvider.isValueIdentical(this.key, oldSettingsProvider));
      if (isEqual) {
        return;
      }
      this.trigger(oldSettingsProvider, newSettingsProvider);
    }

    protected abstract void trigger(
        final SettingsProvider oldSettingsProvider,
        final SettingsProvider newSettingsProvider);

    protected abstract SettingChanged<?> getListener();

    @Override
    public final boolean equals(final Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Listener)) {
        return false;
      }
      final Listener<?> listener = (Listener<?>) o;
      return (clazz.equals(listener.clazz) && key.equals(listener.key)
          && getListener().equals(listener.getListener()));
    }

    @Override
    public final int hashCode() {
      int result = clazz.hashCode();
      result = 31 * result + key.hashCode();
      result = 31 * result + getListener().hashCode();
      return result;
    }
  }

  private final static class ObjectListener<I> extends Listener<I> {

    private final SettingChanged<? super I> listener;

    private ObjectListener(
        final Class<I> clazz, final String key,
        final SettingChanged<? super I> listener) {
      super(clazz, key);
      this.listener = listener;
    }

    @Override
    protected final SettingChanged<?> getListener() {
      return this.listener;
    }

    @Override
    protected final void trigger(
        final SettingsProvider oldSettingsProvider,
        final SettingsProvider newSettingsProvider) {
      final Setting<I> oldSetting = (oldSettingsProvider == null)
          ? null
          : oldSettingsProvider.getObject(this.clazz, this.key);
      final Setting<I> newSetting = (newSettingsProvider == null)
          ? null
          : newSettingsProvider.getObject(this.clazz, this.key);
      if (settingsEqual(oldSetting, newSetting)) {
        return;
      }
      this.listener.settingChanged(
          oldSetting == null ? new Setting<>(this.key) : oldSetting,
          newSetting == null ? new Setting<>(this.key) : newSetting);
    }
  }

  private final static class ListListener<I> extends Listener<I> {

    private final SettingChanged<? super List<? super I>> listener;

    private ListListener(
        final Class<I> clazz, final String key,
        final SettingChanged<? super List<? super I>> listener) {
      super(clazz, key);
      this.listener = listener;
    }

    @Override
    protected final SettingChanged<?> getListener() {
      return this.listener;
    }

    @Override
    protected final void trigger(
        final SettingsProvider oldSettingsProvider,
        final SettingsProvider newSettingsProvider) {
      final Setting<List<I>> oldSetting = (oldSettingsProvider == null)
          ? null
          : oldSettingsProvider.getList(this.clazz, this.key);
      final Setting<List<I>> newSetting = (newSettingsProvider == null)
          ? null
          : newSettingsProvider.getList(this.clazz, this.key);
      if (settingsEqual(oldSetting, newSetting)) {
        return;
      }
      this.listener.settingChanged(
          oldSetting == null ? new Setting<>(this.key) : oldSetting,
          newSetting == null ? new Setting<>(this.key) : newSetting);
    }
  }

  private final static class MapListener<I> extends Listener<I> {

    private final SettingChanged<? super Map<String, ? super I>> listener;

    private MapListener(
        final Class<I> clazz, final String key,
        final SettingChanged<? super Map<String, ? super I>> listener) {
      super(clazz, key);
      this.listener = listener;
    }

    @Override
    protected final SettingChanged<?> getListener() {
      return this.listener;
    }

    @Override
    protected final void trigger(
        final SettingsProvider oldSettingsProvider,
        final SettingsProvider newSettingsProvider) {
      final Setting<Map<String, I>> oldSetting = (oldSettingsProvider == null)
          ? null
          : oldSettingsProvider.getMap(this.clazz, this.key);
      final Setting<Map<String, I>> newSetting = (newSettingsProvider == null)
          ? null
          : newSettingsProvider.getMap(this.clazz, this.key);
      if (settingsEqual(oldSetting, newSetting)) {
        return;
      }
      this.listener.settingChanged(
          oldSetting == null ? new Setting<>(this.key) : oldSetting,
          newSetting == null ? new Setting<>(this.key) : newSetting);
    }
  }
}
