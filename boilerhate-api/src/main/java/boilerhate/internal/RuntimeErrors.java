package boilerhate.internal;

import android.os.Bundle;

public class RuntimeErrors {
  private RuntimeErrors() {}

  public static void missingExtra(String className, String extraName) {
    throw new IllegalArgumentException(
        String.format("Missing extra with key '%s' in arguments for '%s'", extraName, className)
    );
  }

  public static void checkAndThrowIfExtrasBundleIsEmpty(Bundle args) {
    if (args == null || args.isEmpty()) {
      throw new IllegalArgumentException("Missing mandatory extras.");
    }
  }
}
