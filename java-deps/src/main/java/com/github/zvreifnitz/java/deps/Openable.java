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

public interface Openable extends AutoCloseable {

  boolean isInit();

  boolean isOpen();

  void init();

  void open();

  void close();

  boolean isInitBy(final Openable requester);

  boolean isOpenBy(final Openable requester);

  void initBy(final Openable requester);

  void openBy(final Openable requester);

  void closeBy(final Openable requester);
}
