package mk.jankovd.boilerplate.sample;

import android.os.Bundle;
import javax.annotation.Generated;

@Generated("boilerhate.BoilerhateProcessor")
class SampleActivity_SampleInnerClass_Extra {
  static void unpackExtras(SampleActivity.SampleInnerClass target, Bundle args) {
    if (args.containsKey("sampleInt")) {
      target.sampleInt = args.getInt("sampleInt");
    }
  }
}
