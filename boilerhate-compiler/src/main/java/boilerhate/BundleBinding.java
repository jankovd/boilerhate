package boilerhate;

import boilerhate.internal.RuntimeErrors;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

abstract class BundleBinding {
  private final Environment env;
  private boolean isMandatory;
  private String bundleKey;
  private BundleTypes.TypeMethods bundleTypeMethods;
  private TypeMirror setterType;
  private Element setterElement;
  private TypeMirror getterType;
  private Element getterElement;

  protected BundleBinding(Environment env, String bundleKey) {
    this.env = env;
    this.bundleKey = bundleKey;
  }

  protected static String getKey(Element element, String keyFromAnnotation) {
    final boolean isAnnotationKeyEmpty = "".equals(keyFromAnnotation);
    if (element.getKind() == ElementKind.METHOD && isAnnotationKeyEmpty) {
      return null;
    }
    return isAnnotationKeyEmpty ? element.getSimpleName().toString() : keyFromAnnotation;
  }

  public void addElementProperties(Element element) {
    final TypeMirror type = element.asType();
    if (type instanceof ExecutableType) {
      if (!addPropertiesFromExecutable(element, (ExecutableType) type)) {
        return;
      }
    } else if (setterType != null) {
      env.logger.error(element, "Duplicate field for key '%s'.", bundleKey);
      return;
    } else if (ensureBundleTypeIsAvailableForSetter(type)) {
      setterType = type;
      setterElement = element;
    } else {
      return;
    }
    isMandatory |= element.getAnnotation(Mandatory.class) != null;
  }

  private boolean addPropertiesFromExecutable(Element element, ExecutableType execType) {
    final int parametersCount = execType.getParameterTypes().size();
    if (parametersCount > 1) {
      env.logger.error(element,
          "Invalid parameter count on setter methods for %s, only methods with single parameter are allowed.",
          bundleKey);
      return false;
    }
    if (parametersCount == 1) {
      if (setterType != null) {
        env.logger.error(element, "Duplicate setter for key '%s'.", bundleKey);
        return false;
      }
      setterType = execType.getParameterTypes().get(0);
      setterElement = element;
      return ensureBundleTypeIsAvailableForSetter(setterType);
    } else if (parametersCount == 0
        && execType.getReturnType().getKind() == TypeKind.VOID) {
      env.logger.error(element,
          "Pointless method '%s' for key '%s', neither sets or provides a value.",
          element.getSimpleName().toString(), bundleKey);
    } else if (getterType != null) {
      env.logger.error(element, "Duplicate getter for key '%s'", bundleKey);
    } else {
      getterType = execType;
      getterElement = element;
      return true;
    }
    return false;
  }

  private boolean ensureBundleTypeIsAvailableForSetter(TypeMirror type) {
    if (bundleTypeMethods != null) { return true; }
    try {
      bundleTypeMethods = env.bundles.get(type);
      return true;
    } catch (BundleTypes.UnsupportedTypeException e) {
      env.logger.error(env.types.asElement(type), e.getMessage());
      return false;
    }
  }

  public void addToCodeBlock(CodeBlock.Builder codeBlock, String targetName, String bundleName) {
    if (setterType == null) { return; }
    final boolean isMethod = setterElement.getKind() == ElementKind.METHOD;
    final String setter = setterElement.getSimpleName().toString();
    final TypeName setterTypeName = TypeName.get(setterType);
    final String className = setterElement.getEnclosingElement().getSimpleName().toString();
    codeBlock.beginControlFlow("if ($L.containsKey($S))", bundleName, bundleKey);
    if (isMethod) {
      codeBlock.add("$L.$L(", targetName, setter);
      bundleTypeMethods.addGetMethod(codeBlock, setterTypeName, bundleName, bundleKey);
      codeBlock.add(");\n");
    } else {
      codeBlock.add("$L.$L = ", targetName, setter);
      bundleTypeMethods.addGetMethod(codeBlock, setterTypeName, bundleName, bundleKey);
      codeBlock.add(";\n");
    }
    if (isMandatory) {
      codeBlock.nextControlFlow("else ");
      addMissingExtraThrow(codeBlock, className, bundleKey);
      codeBlock.endControlFlow();
      return;
    } else if (getterType == null) {
      codeBlock.endControlFlow();
      return;
    }
    final String getter = getterElement.getSimpleName().toString();
    codeBlock.nextControlFlow("else ");
    if (isMethod) {
      codeBlock.addStatement("$L.$L($L.$L())", targetName, setter, targetName, getter);
    } else {
      codeBlock.addStatement("$L.$L = ($T) $L.$L()", targetName, setter, setterTypeName,
          targetName, getter);
    }
    codeBlock.endControlFlow();
  }

  private void addMissingExtraThrow(CodeBlock.Builder codeBlock, String className, String extra) {
    codeBlock.addStatement("$T.missingExtra($S, $S);", RuntimeErrors.class, className, extra);
  }

  public boolean isMandatory() {
    return isMandatory;
  }

  public static class ExtraBundleBinding extends BundleBinding {

    public ExtraBundleBinding(Environment env, String bundleKey) {
      super(env, bundleKey);
    }

    public static String getKey(Element element) {
      final Extra annotation = element.getAnnotation(Extra.class);
      if (annotation == null) { return null; }
      return getKey(element, annotation.value());
    }
  }
}
