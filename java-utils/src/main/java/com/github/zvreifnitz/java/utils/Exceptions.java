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

package com.github.zvreifnitz.java.utils;

public final class Exceptions {

  public static <R, T extends Throwable> R throwExc(final T exc) throws T {
    throw exc;
  }

  public static <R> R throwExc(final Error error) {
    throw error;
  }

  public static <R> R throwExc(final RuntimeException runtimeException) {
    throw runtimeException;
  }

  public static <R> R throwExcUnchecked(final Throwable throwable) {
    if (throwable instanceof Error) {
      throw (Error) throwable;
    }
    if (throwable instanceof RuntimeException) {
      throw (RuntimeException) throwable;
    }
    throw new RuntimeException(throwable);
  }

}
