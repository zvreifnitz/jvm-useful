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

public final class Preconditions {

  public static <T> T checkNotNull(final T instance, final String paramName) {
    return (instance != null)
        ? instance
        : Exceptions.throwNullPointerException(paramName);
  }

  public static <T extends Number> T checkPositive(final T input, final String paramName) {
    return (checkNotNull(input, paramName).doubleValue() > 0.0)
        ? input
        : Exceptions
            .throwIllegalArgumentException("Parameter '" + paramName + "' must be positive");
  }

  public static void checkState(final boolean condition, final String msg) {
    if (!condition) {
      Exceptions.throwIllegalStateException(msg);
    }
  }
}
