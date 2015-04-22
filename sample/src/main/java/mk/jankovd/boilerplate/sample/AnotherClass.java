package mk.jankovd.boilerplate.sample;

import boilerhate.Extra;
import boilerhate.State;

public class AnotherClass {

  @Extra public int someCounter;
  @Extra("user_id") public long userId;
  @State("modified_user_id") long modifiedUserId;
  @State String nullString;

  @State("modifiedCounter") void restoreCounter(int counter) {
    someCounter = counter;
  }

  @State("modifiedCounter") int getModifiedStateCounter() {
    return ++someCounter;
  }
}
