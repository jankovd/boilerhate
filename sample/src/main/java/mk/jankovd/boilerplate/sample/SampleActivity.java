package mk.jankovd.boilerplate.sample;

import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.SparseArray;
import boilerhate.Extra;
import boilerhate.ExtrasBuilder;
import boilerhate.Mandatory;
import java.io.Serializable;
import java.util.ArrayList;

@ExtrasBuilder(
    className = "BundleExtrasUsageBuilder",
    forcePublicVisibility = true
)
public class SampleActivity extends Activity {

  @Extra int pint;
  @Extra int[] intArray;
  @Extra("string") String string;
  @Extra String[] stringArray;
  @Extra ArrayList<Parcelable> parcelableArrayList;
  @Extra("array") ArrayList<Bundle> bundleArrayList;
  @Extra("bundles") Bundle[] bundleArray;
  @Extra SparseArray<Bundle> bundleSparseArray;
  @Extra("serializableArray") Integer[] integerArray;
  @Extra("serializable") SerializableClass serializableClass;

  public static class SerializableClass implements Serializable {}

  public static class SampleInnerClass {
    @Extra int sampleInt;
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    SampleActivity_Extra.unpackExtras(this, getIntent().getExtras());
  }

  @Extra("string")
  protected String getDefaultSubtitle() {
    return "<subtitle-placeholder>";
  }

  @Mandatory @Extra("dateOfCreate")
  protected void setDateOfCreate(long dateOfCreate) {}
}
