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
