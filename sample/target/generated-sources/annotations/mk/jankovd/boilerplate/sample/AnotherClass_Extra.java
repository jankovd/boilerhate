package mk.jankovd.boilerplate.sample;

import android.os.Bundle;
import javax.annotation.Generated;

@Generated("boilerhate.BoilerhateProcessor")
class AnotherClass_Extra {
  static void unpackExtras(AnotherClass target, Bundle args) {
    if (args.containsKey("user_id")) {
      target.userId = args.getLong("user_id");
    }
    if (args.containsKey("someCounter")) {
      target.someCounter = args.getInt("someCounter");
    }
  }
}
