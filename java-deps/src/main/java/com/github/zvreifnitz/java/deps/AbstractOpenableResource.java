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

package com.github.zvreifnitz.java.deps;

import com.github.zvreifnitz.java.utils.Exceptions;

public abstract class AbstractOpenableResource<TResource> extends AbstractOpenable {

  private volatile TResource underlyingResource = null;

  private boolean resourceInit = false;
  private boolean resourceOpen = false;

  protected AbstractOpenableResource(final TResource resource, final Openable... dependencies) {
    super(dependencies);
    this.underlyingResource = resource;
  }

  @Override
  protected final void performInit() {
    final TResource rsc = this.underlyingResource;
    this.underlyingResource = this.performInitResourceInternal(rsc);
    this.resourceInit = true;
  }

  @Override
  protected final void performOpen() {
    final TResource rsc = this.underlyingResource;
    this.underlyingResource = this.performOpenResourceInternal(rsc);
    this.resourceOpen = true;
  }

  @Override
  protected final void performClose() {
    this.resourceOpen = false;
    final TResource rsc = this.underlyingResource;
    this.underlyingResource = this.performCloseResourceInternal(rsc);
  }

  protected final TResource getResource() {
    return this.underlyingResource;
  }

  protected final synchronized void setResource(final TResource resource) {
    final TResource oldResource = this.underlyingResource;
    if (oldResource == resource) {
      return;
    }
    TResource disposeResource = resource;
    try {
      if (this.resourceInit) {
        this.performInitResourceInternal(resource);
      }
      if (this.resourceOpen) {
        this.performOpenResourceInternal(resource);
      }
      this.underlyingResource = resource;
      disposeResource = oldResource;
    } catch (final Exception exc) {
      Exceptions.throwExcUnchecked(exc);
    } finally {
      this.performCloseResourceInternal(disposeResource);
    }
  }

  private TResource performInitResourceInternal(final TResource resource) {
    this.performInitBy(resource);
    this.performInitResource(resource);
    return resource;
  }

  private TResource performOpenResourceInternal(final TResource resource) {
    this.performOpenBy(resource);
    this.performOpenResource(resource);
    return resource;
  }

  private TResource performCloseResourceInternal(final TResource resource) {
    this.performCloseResource(resource);
    this.performCloseBy(resource);
    return resource;
  }

  protected void performInitResource(final TResource resource) {
  }

  protected void performOpenResource(final TResource resource) {
  }

  protected void performCloseResource(final TResource resource) {
  }
}