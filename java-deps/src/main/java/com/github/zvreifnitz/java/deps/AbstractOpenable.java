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

package com.github.zvreifnitz.java.deps;

import com.github.zvreifnitz.java.utils.Exceptions;
import com.github.zvreifnitz.java.utils.Preconditions;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class AbstractOpenable implements Openable {

  private final static String NotInit = "Openable is not initialised";
  private final static String NotOpen = "Openable is not open";

  private final Openable[] dependencies;
  private final Map<Openable, Boolean> requesters;
  private final SelfOpenable self;

  private boolean open;
  private boolean init;

  protected AbstractOpenable(final Openable... dependencies) {
    this.dependencies = compact(dependencies);
    this.requesters = new HashMap<>();
    this.self = new SelfOpenable();
  }

  @Override
  public final boolean isInit() {
    return this.isInitBySync(this.self);
  }

  @Override
  public final boolean isOpen() {
    return this.isOpenBySync(this.self);
  }

  @Override
  public final void init() {
    this.initBy(this.self);
  }

  @Override
  public final void open() {
    this.openBy(this.self);
  }

  @Override
  public final void close() {
    this.closeBy(this.self);
  }

  @Override
  public final boolean isInitBy(final Openable requester) {
    return this.isInitBySync(checkRequester(requester));
  }

  @Override
  public final boolean isOpenBy(final Openable requester) {
    return this.isOpenBySync(checkRequester(requester));
  }

  @Override
  public final void initBy(final Openable requester) {
    checkRequester(requester);
    this.initDeps(requester);
    this.initSync(requester);
  }

  @Override
  public final void openBy(final Openable requester) {
    checkRequester(requester);
    try {
      this.openDeps(requester);
      this.openSync(requester);
    } catch (final Exception exc) {
      this.closeDepsWhenExc(requester);
      Exceptions.throwExcUnchecked(exc);
    }
  }

  @Override
  public final void closeBy(final Openable requester) {
    checkRequester(requester);
    try {
      this.closeSync(requester);
      this.closeDeps(requester);
    } catch (final Exception exc) {
      this.closeDepsWhenExc(requester);
      Exceptions.throwExcUnchecked(exc);
    }
  }

  private synchronized boolean isInitBySync(final Openable requester) {
    return this.requesters.containsKey(requester);
  }

  private synchronized boolean isOpenBySync(final Openable requester) {
    return Boolean.TRUE.equals(this.requesters.get(requester));
  }

  private void initDeps(final Openable requester) {
    for (final Openable dependency : this.dependencies) {
      dependency.initBy(requester);
    }
  }

  private synchronized void initSync(final Openable requester) {
    if (this.requesters.containsKey(requester)) {
      return;
    }
    if (!this.init) {
      this.performInit();
      this.init = true;
    }
    this.requesters.put(requester, Boolean.FALSE);
  }

  private void openDeps(final Openable requester) {
    for (final Openable dependency : this.dependencies) {
      dependency.openBy(requester);
    }
  }

  private synchronized void openSync(final Openable requester) {
    final Boolean existing = this.requesters.get(requester);
    Preconditions.checkState(existing != null, NotInit);
    if (Boolean.TRUE.equals(existing)) {
      return;
    }
    if (!this.open) {
      this.performOpen();
      this.open = true;
    }
    this.requesters.put(requester, Boolean.TRUE);
  }

  private synchronized void closeSync(final Openable requester) {
    if (!Boolean.TRUE.equals(this.requesters.get(requester))) {
      return;
    }
    this.requesters.put(requester, Boolean.FALSE);
    if (this.open && this.noneOpen()) {
      this.open = false;
      this.performClose();
    }
  }

  private void closeDeps(final Openable requester) {
    for (final Openable dependency : this.dependencies) {
      dependency.closeBy(requester);
    }
  }

  private void closeDepsWhenExc(final Openable requester) {
    for (final Openable dependency : this.dependencies) {
      try {
        dependency.closeBy(requester);
      } catch (final Exception ignored) {
      }
    }
  }

  private boolean noneOpen() {
    return this.requesters.values().stream().noneMatch(Boolean.TRUE::equals);
  }

  protected final void throwNotOpen() {
    Preconditions.checkState(false, NotOpen);
  }

  protected final <T> void performInitBy(final T dependency) {
    if (dependency instanceof Openable) {
      ((Openable) dependency).initBy(this.self);
    }
  }

  protected final <T> void performOpenBy(final T dependency) {
    if (dependency instanceof Openable) {
      ((Openable) dependency).openBy(this.self);
    }
  }

  protected final <T> void performCloseBy(final T dependency) {
    if (dependency instanceof Openable) {
      ((Openable) dependency).closeBy(this.self);
    }
  }

  protected void performInit() {
  }

  protected void performOpen() {
  }

  protected void performClose() {
  }

  private static Openable checkRequester(final Openable requester) {
    return Preconditions.checkNotNull(requester, "requester");
  }

  private static Openable[] compact(final Openable[] input) {
    if ((input == null) || (input.length == 0)) {
      return new Openable[0];
    }
    return Arrays.stream(input).filter(Objects::nonNull).distinct().toArray(Openable[]::new);
  }

  private final static class SelfOpenable implements Openable {

    @Override
    public final boolean isInit() {
      return false;
    }

    @Override
    public final boolean isOpen() {
      return false;
    }

    @Override
    public final void init() {
    }

    @Override
    public final void open() {
    }

    @Override
    public final void close() {
    }

    @Override
    public final boolean isInitBy(final Openable requester) {
      return false;
    }

    @Override
    public final boolean isOpenBy(final Openable requester) {
      return false;
    }

    @Override
    public final void initBy(final Openable requester) {
    }

    @Override
    public final void openBy(final Openable requester) {
    }

    @Override
    public final void closeBy(final Openable requester) {
    }
  }
}
