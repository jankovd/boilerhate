package boilerhate;

public class BundleBinding {

  private String key;
  private Class<?> valueType;
  private String defaultValueGetterMethodName;
  private String valueSetterMethodName;

  public boolean isBindingComplete() {
    return false;
  }
}
