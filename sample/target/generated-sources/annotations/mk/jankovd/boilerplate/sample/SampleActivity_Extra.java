package mk.jankovd.boilerplate.sample;

import android.os.Bundle;
import boilerhate.internal.RuntimeErrors;
import javax.annotation.Generated;

@Generated("boilerhate.BoilerhateProcessor")
class SampleActivity_Extra {
  static void unpackExtras(SampleActivity target, Bundle args) {
    RuntimeErrors.checkAndThrowIfExtrasBundleIsEmpty(args);
    if (args.containsKey("dateOfCreate")) {
      target.setDateOfCreate(args.getLong("dateOfCreate"));
    } else {
      RuntimeErrors.missingExtra("SampleActivity", "dateOfCreate");;
    }
    if (args.containsKey("parcelableArrayList")) {
      target.parcelableArrayList = args.getParcelableArrayList("parcelableArrayList");
    }
    if (args.containsKey("pint")) {
      target.pint = args.getInt("pint");
    }
    if (args.containsKey("bundleSparseArray")) {
      target.bundleSparseArray = args.getSparseParcelableArray("bundleSparseArray");
    }
    if (args.containsKey("bundles")) {
      target.bundleArray = (Bundle[]) args.getParcelableArray("bundles");
    }
    if (args.containsKey("intArray")) {
      target.intArray = args.getIntArray("intArray");
    }
    if (args.containsKey("string")) {
      target.string = args.getString("string");
    } else {
      target.string = (String) target.getDefaultSubtitle();
    }
    if (args.containsKey("stringArray")) {
      target.stringArray = args.getStringArray("stringArray");
    }
    if (args.containsKey("serializable")) {
      target.serializableClass = (SampleActivity.SerializableClass) args.getSerializable("serializable");
    }
    if (args.containsKey("serializableArray")) {
      target.integerArray = (Integer[]) args.getSerializable("serializableArray");
    }
    if (args.containsKey("array")) {
      target.bundleArrayList = args.getParcelableArrayList("array");
    }
  }
}
