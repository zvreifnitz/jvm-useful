package com.github.zvreifnitz.java.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ReflectionTest {

  @Test
  void isPrimitive() {
    assertTrue(Reflection.isPrimitive(boolean.class), "boolean");
    assertTrue(Reflection.isPrimitive(byte.class), "byte");
    assertTrue(Reflection.isPrimitive(char.class), "char");
    assertTrue(Reflection.isPrimitive(short.class), "short");
    assertTrue(Reflection.isPrimitive(int.class), "int");
    assertTrue(Reflection.isPrimitive(long.class), "long");
    assertTrue(Reflection.isPrimitive(float.class), "float");
    assertTrue(Reflection.isPrimitive(double.class), "double");
    assertTrue(Reflection.isPrimitive(void.class), "void");

    assertFalse(Reflection.isPrimitive(Boolean.class), "Boolean");
    assertFalse(Reflection.isPrimitive(Byte.class), "Byte");
    assertFalse(Reflection.isPrimitive(Character.class), "Character");
    assertFalse(Reflection.isPrimitive(Short.class), "Short");
    assertFalse(Reflection.isPrimitive(Integer.class), "Integer");
    assertFalse(Reflection.isPrimitive(Long.class), "Long");
    assertFalse(Reflection.isPrimitive(Float.class), "Float");
    assertFalse(Reflection.isPrimitive(Double.class), "Double");
    assertFalse(Reflection.isPrimitive(Void.class), "Void");

    assertFalse(Reflection.isPrimitive(String.class), "String");
    assertFalse(Reflection.isPrimitive(Object.class), "Object");
  }

  @Test
  void isBoxedPrimitive() {
    assertFalse(Reflection.isBoxedPrimitive(boolean.class), "boolean");
    assertFalse(Reflection.isBoxedPrimitive(byte.class), "byte");
    assertFalse(Reflection.isBoxedPrimitive(char.class), "char");
    assertFalse(Reflection.isBoxedPrimitive(short.class), "short");
    assertFalse(Reflection.isBoxedPrimitive(int.class), "int");
    assertFalse(Reflection.isBoxedPrimitive(long.class), "long");
    assertFalse(Reflection.isBoxedPrimitive(float.class), "float");
    assertFalse(Reflection.isBoxedPrimitive(double.class), "double");
    assertFalse(Reflection.isBoxedPrimitive(void.class), "void");

    assertTrue(Reflection.isBoxedPrimitive(Boolean.class), "Boolean");
    assertTrue(Reflection.isBoxedPrimitive(Byte.class), "Byte");
    assertTrue(Reflection.isBoxedPrimitive(Character.class), "Character");
    assertTrue(Reflection.isBoxedPrimitive(Short.class), "Short");
    assertTrue(Reflection.isBoxedPrimitive(Integer.class), "Integer");
    assertTrue(Reflection.isBoxedPrimitive(Long.class), "Long");
    assertTrue(Reflection.isBoxedPrimitive(Float.class), "Float");
    assertTrue(Reflection.isBoxedPrimitive(Double.class), "Double");
    assertTrue(Reflection.isBoxedPrimitive(Void.class), "Void");

    assertFalse(Reflection.isBoxedPrimitive(String.class), "String");
    assertFalse(Reflection.isBoxedPrimitive(Object.class), "Object");
  }

  @Test
  void hasOnlyParamlessCTOR() {
  }

  @Test
  void castOrNull() {
    assertNull(Reflection.castOrNull(null, null), "Clazz = null");
    assertNull(Reflection.castOrNull(Object.class, null), "instance = null");
    assertNotNull(Reflection.castOrNull(Object.class, new Object()), "instance same class");
    assertNotNull(Reflection.castOrNull(Object.class, ""), "instance subclass");
    assertNull(Reflection.castOrNull(String.class, new Object()), "Clazz subclass");
  }

  @Test
  void cast() {
    assertThrows(NullPointerException.class, () -> Reflection.cast(null, null), "Clazz = null");
    assertNull(Reflection.cast(Object.class, null), "instance = null");
    assertNotNull(Reflection.cast(Object.class, new Object()), "instance same class");
    assertNotNull(Reflection.cast(Object.class, ""), "instance subclass");
    assertThrows(ClassCastException.class, () -> Reflection.cast(String.class, new Object()),
        "Clazz subclass");
  }

  @Test
  void getClasses() {
  }
}