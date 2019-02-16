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

package com.github.zvreifnitz.java.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public final class Reflection {

  public static void hasOnlyParamlessCTOR(final Class<?> clazz) {
    final Constructor<?>[] ctors = clazz.getDeclaredConstructors();
    if ((ctors.length != 1) || (ctors[0].getParameterCount() != 0)) {
      throw new RuntimeException("Class " + clazz.getName()
          + " must have just one public declared constructor without parameters");
    }
  }

  public static <T, I> T castOrNull(final Class<T> clazz, final I instance) {
    try {
      return (clazz == null || instance == null) ? null : clazz.cast(instance);
    } catch (final Exception exc) {
      return null;
    }
  }

  public static <T, I> T cast(final Class<T> clazz, final I instance) {
    Preconditions.checkNotNull(clazz, "clazz");
    try {
      return (instance == null) ? null : clazz.cast(instance);
    } catch (final Exception exc) {
      return Exceptions.throwExcUnchecked(exc);
    }
  }

  public static Set<Class<?>> getClasses(
      final String packageName, final boolean recursive) throws IOException {
    final ClassLoader loader = Thread.currentThread().getContextClassLoader();
    final HashSet<Class<?>> classes = new HashSet<>();
    final String path = packageName.replace('.', '/');
    final Enumeration resources = loader.getResources(path);

    while ((resources != null) && resources.hasMoreElements()) {
      final String filePath;
      {
        String filePathLcl = ((URL) resources.nextElement()).getFile();
        if (filePathLcl.indexOf("%20") > 0) {
          filePathLcl = filePathLcl.replaceAll("%20", " ");
        }
        if (filePathLcl.indexOf("%23") > 0) {
          filePathLcl = filePathLcl.replaceAll("%23", "#");
        }
        filePath = filePathLcl;
      }

      if ((filePath.indexOf("!") > 0) && (filePath.indexOf(".jar") > 0)) {
        final String jarPath;
        {
          String jarPathLcl = filePath.substring(0, filePath.indexOf("!"))
              .substring(filePath.indexOf(":") + 1);
          if (jarPathLcl.contains(":")) {
            jarPathLcl = jarPathLcl.substring(1);
          }
          jarPath = jarPathLcl;
        }
        classes.addAll(getFromJARFile(loader, jarPath, path, recursive));
      } else {
        classes.addAll(getFromDirectory(loader, new File(filePath), packageName, recursive));
      }
    }
    return classes;
  }

  private static Set<Class<?>> getFromDirectory(
      final ClassLoader loader, final File directory, final String packageName,
      final boolean recursive) {
    final HashSet<Class<?>> classes = new HashSet<>();
    if (directory.exists()) {
      final String[] fileNames = directory.list();
      final int numOfFiles = ((fileNames == null) ? 0 : fileNames.length);
      for (int i = 0; i < numOfFiles; ++i) {
        final String file = fileNames[i];
        if (file.endsWith(".class")) {
          final String name = packageName + '.' + stripFilenameExtension(file);
          final Class<?> clazz = createClass(name, loader);
          if (clazz != null) {
            classes.add(clazz);
          }
        } else if (recursive) {
          final File subDir = new File(directory + "/" + file);
          if (subDir.exists() && subDir.isDirectory()) {
            classes.addAll(getFromDirectory(loader, subDir, packageName + "." + file, true));
          }
        }
      }
    }
    return classes;
  }

  private static Set<Class<?>> getFromJARFile(
      final ClassLoader loader, final String jar, final String packageName, final boolean recursive)
      throws IOException {
    final HashSet<Class<?>> classes = new HashSet<>();
    try (final JarInputStream jarFile = new JarInputStream(new FileInputStream(jar))) {
      while (true) {
        final JarEntry jarEntry = jarFile.getNextJarEntry();
        if (jarEntry == null) {
          break;
        }
        final String className = jarEntry.getName();
        if (className.endsWith(".class") && (recursive ? getPackageName(className)
            .startsWith(packageName) : getPackageName(className).equals(packageName))) {
          final String cleanClassName = stripFilenameExtension(className);
          final Class<?> clazz = createClass(cleanClassName.replace('/', '.'), loader);
          if (clazz != null) {
            classes.add(clazz);
          }
        }
      }
    }
    return classes;
  }

  private static Class<?> createClass(final String name, final ClassLoader loader) {
    try {
      return Class.forName(name, true, loader);
    } catch (final Exception exc) {
      return null;
    }
  }

  private static String stripFilenameExtension(final String filename) {
    return filename.indexOf(46) != -1 ? filename.substring(0, filename.lastIndexOf(46)) : filename;
  }

  private static String getPackageName(final String filename) {
    return filename.contains("/") ? filename.substring(0, filename.lastIndexOf(47)) : filename;
  }
}
