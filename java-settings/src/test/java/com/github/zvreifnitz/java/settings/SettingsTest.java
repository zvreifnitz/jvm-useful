package com.github.zvreifnitz.java.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SettingsTest {

  static Settings settings = null;

  @BeforeAll
  static void setUpAll() {
    settings = SettingsFactory.createSettings();
    settings.init();
    assertTrue(settings.isInit(), "Init true");
    assertFalse(settings.isOpen(), "Open false");
  }

  @AfterAll
  static void tearDownAll() {
  }

  @BeforeEach
  void setUp() {
    assertTrue(settings.isInit(), "Init true");
    assertFalse(settings.isOpen(), "Open false");
    settings.open();
    assertTrue(settings.isOpen(), "Open true");
  }

  @AfterEach
  void tearDown() {
    assertTrue(settings.isOpen(), "Open true");
    settings.close();
    assertFalse(settings.isOpen(), "Open false");
    assertTrue(settings.isInit(), "Init true");
  }

  @Test
  void getBoolean() {
    final boolean val = settings.getBoolean("boolVal").get();
    assertTrue(val, "getBoolean (boolean)");
    final Boolean valBoolean = settings.getObject(Boolean.class, "boolVal").get();
    assertEquals(true, valBoolean, "getBoolean (Boolean)");
  }

  @Test
  void getInteger() {
    final int val = settings.getInteger("intVal").get();
    assertEquals(123, val, "getInteger (int)");
    final Integer valInteger = settings.getObject(Integer.class, "intVal").get();
    assertEquals(123, valInteger, "getInteger (Integer)");
  }

  @Test
  void getLong() {
    final long val = settings.getLong("intVal").get();
    assertEquals(123L, val, "getLong (long)");
    final Long valLong = settings.getObject(Long.class, "intVal").get();
    assertEquals(123L, valLong, "getLong (Long)");
  }

  @Test
  void getDouble() {
    final double val = settings.getDouble("intVal").get();
    assertEquals(123.0, val, "getDouble (double)");
    final Double valDouble = settings.getObject(Double.class, "intVal").get();
    assertEquals(123.0, valDouble, "getDouble (Double)");
  }

  @Test
  void getString() {
    final String val = settings.getString("stringVal").get();
    assertEquals("abc", val, "getString");
  }

  @Test
  void getObject() {
    final PropObj val = settings.getObject(PropObj.class, "objVal").get();
    assertNotNull(val, "getObject (not null)");
    assertEquals(34, val.age, "getObject (age)");
    assertEquals("cde", val.name, "getObject (name)");
  }

  @Test
  void getList() {
    final List<Integer> val = settings.getList(Integer.class, "array").get();
    assertEquals(5, val.size(), "getList");
    assertEquals(0, val.get(0), "getList (0)");
    assertEquals(1, val.get(1), "getList (1)");
    assertEquals(2, val.get(2), "getList (2)");
    assertNull(val.get(3), "getList (3)");
    assertEquals(4, val.get(4), "getList (4)");
  }

  @Test
  void getMap() {
    final Map<String, String> val = settings.getMap(String.class, "map").get();
    assertEquals(3, val.size(), "getMap");
    assertEquals("a", val.get("a"), "getMap (a)");
    assertEquals("b", val.get("b"), "getMap (b)");
    assertEquals("c", val.get("c"), "getMap (c)");
  }

  private final static class PropObj {

    private final int age;
    private final String name;

    private PropObj() {
      this(0, null);
    }

    private PropObj(final int age, final String name) {
      this.age = age;
      this.name = name;
    }
  }
}