package boilerhate;

import boilerhate.internal.RuntimeErrors;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;

class BindingsGroup {
  private static final String BUNDLE_PARAM_NAME = "args";
  private static final String TARGET_NAME = "target";
  private static final String BUNDLE_CLASS_NAME = "android.os.Bundle";

  private final TypeMirror classType;
  private final String packageName;
  private final String className;
  private final Environment env;
  private final Element classElement;
  private boolean hasMandatoryExtras;
  private Map<String, BundleBinding> keyToExtraBindings;
  private Map<String, BundleBinding> keyToStateBinding;
  private ExtrasBuilderBindings extrasBuilder;

  private TypeName resolvedTypeBundle;

  public BindingsGroup(TypeMirror classType, String packageName, String className,
      Environment env) {
    this.classType = classType;
    this.packageName = packageName;
    this.className = className;
    this.env = env;
    this.classElement = env.types.asElement(classType);
    this.keyToExtraBindings = new HashMap<String, BundleBinding>(8);
    this.keyToStateBinding= new HashMap<String, BundleBinding>(8);
    resolveTypes(env);
  }

  private void resolveTypes(Environment env) {
    resolvedTypeBundle = TypeName.get(env.elements.getTypeElement(BUNDLE_CLASS_NAME).asType());
  }

  public void addExtra(String bundleKey, Element element) {
    BundleBinding binding = keyToExtraBindings.get(bundleKey);
    if (binding == null) {
      binding = new BundleBinding.ExtraBundleBinding(env, bundleKey);
      keyToExtraBindings.put(bundleKey, binding);
    }
    binding.addElementProperties(element);
    hasMandatoryExtras |= binding.isMandatory();
  }

  public void addState(String bundleKey, Element element) {
    BundleBinding binding = keyToStateBinding.get(bundleKey);
    if (binding == null) {
      binding = new BundleBinding.StateBundleBinding(env, bundleKey);
      keyToStateBinding.put(bundleKey, binding);
    }
    binding.addElementProperties(element);
  }

  public void writeSourceToFile(Filer filer) {
    writeExtrasToFile(filer);
    writeStateToFile(filer);
  }

  // Will need to wrap this method along with keyToExtraBindings in a
  // class specific for the extras. the generated unpackExtras method would
  // have a variable signature depending on the target class:
  // unpack(Fragment) where the arguments would be retrieved from fr.getArguments(),
  // unpack(Intent) where the arguments would be read from the intent,
  // and unpack(Bundle) for every other class. This is a todo.
  // A static builder() method would also be defined in this class if there is
  // an arguments builder defined for it. Another todo.
  private void writeExtrasToFile(Filer filer) {
    if (keyToExtraBindings.isEmpty()) { return; }
    final String bundleParam = BUNDLE_PARAM_NAME;
    final String target = TARGET_NAME;
    final CodeBlock.Builder unpackExtrasMethodCode = CodeBlock.builder();
    if (hasMandatoryExtras) {
      unpackExtrasMethodCode.addStatement("$T.checkAndThrowIfExtrasBundleIsEmpty($L)",
          RuntimeErrors.class, bundleParam);
    } else {
      unpackExtrasMethodCode.beginControlFlow("if ($L == null)", bundleParam)
          .addStatement("return")
          .endControlFlow();
    }
    for (final BundleBinding extraBinding : keyToExtraBindings.values()) {
      extraBinding.writeGetToCodeBlock(unpackExtrasMethodCode, target, bundleParam);
    }
    final MethodSpec unpackExtrasMethod = MethodSpec.methodBuilder("unpackExtras")
        .addModifiers(Modifier.STATIC)
        .addParameter(TypeName.get(classType), target)
        .addParameter(resolvedTypeBundle, bundleParam)
        .addCode(unpackExtrasMethodCode.build())
        .build();
    final String extrasClassName = className + "_Extra";
    final TypeSpec extrasTypeSpec = TypeSpec.classBuilder(extrasClassName)
        .addAnnotation(AnnotationSpec.builder(Generated.class)
            .addMember("value", "$S", BoilerhateProcessor.class.getName())
            .build())
        .addMethod(unpackExtrasMethod)
        .build();
    try {
      final String qualifiedExtrasClassName = packageName + "." + extrasClassName;
      env.logger.warning("Writing %s originating from %s ", qualifiedExtrasClassName,
          classElement.getSimpleName().toString());
      JavaFileObject sourceFile = filer.createSourceFile(qualifiedExtrasClassName, classElement);
      Writer out = sourceFile.openWriter();
      JavaFile.builder(packageName, extrasTypeSpec)
          .skipJavaLangImports(true)
          .build()
          .writeTo(out);
      out.flush();
      out.close();
    } catch (IOException e) {
      env.logger.error(classElement, "Failure while writing to file: %s", e.getMessage());
    }
  }

  // parent state for views.
  private void writeStateToFile(Filer filer) {
    if (keyToStateBinding.isEmpty()) { return; }
    final String bundleParam = BUNDLE_PARAM_NAME;
    final String target = TARGET_NAME;
    final CodeBlock.Builder saveStateMethodCode = CodeBlock.builder()
        .addStatement("$T $L = new Bundle($L)", resolvedTypeBundle, bundleParam,
            keyToStateBinding.size());
    for (final BundleBinding stateBinding : keyToStateBinding.values()) {
      stateBinding.writePutToCodeBlock(saveStateMethodCode, target, bundleParam);
    }
    saveStateMethodCode.addStatement("return $L", bundleParam);
    final MethodSpec saveStateMethod = MethodSpec.methodBuilder("saveState")
        .addModifiers(Modifier.STATIC)
        .addParameter(TypeName.get(classType), target)
        .returns(resolvedTypeBundle)
        .addCode(saveStateMethodCode.build())
        .build();

    final CodeBlock.Builder restoreStateMethodCode = CodeBlock.builder()
        .beginControlFlow("if ($L == null)", bundleParam)
        .addStatement("return")
        .endControlFlow();
    for (final BundleBinding extraBinding : keyToStateBinding.values()) {
      extraBinding.writeGetToCodeBlock(restoreStateMethodCode, target, bundleParam);
    }
    final MethodSpec restoreStateMethod = MethodSpec.methodBuilder("restoreState")
        .addModifiers(Modifier.STATIC)
        .addParameter(TypeName.get(classType), target)
        .addParameter(resolvedTypeBundle, bundleParam)
        .addCode(restoreStateMethodCode.build())
        .build();

    final String stateClassName = className + "_State";
    final TypeSpec stateTypeSpec = TypeSpec.classBuilder(stateClassName)
        .addAnnotation(AnnotationSpec.builder(Generated.class)
            .addMember("value", "$S", BoilerhateProcessor.class.getName())
            .build())
        .addMethod(saveStateMethod)
        .addMethod(restoreStateMethod)
        .build();
    try {
      final String qualifiedStateClassName = packageName + "." + stateClassName;
      env.logger.warning("Writing %s originating from %s ", qualifiedStateClassName,
          classElement.getSimpleName().toString());
      JavaFileObject sourceFile = filer.createSourceFile(qualifiedStateClassName, classElement);
      Writer out = sourceFile.openWriter();
      JavaFile.builder(packageName, stateTypeSpec)
          .skipJavaLangImports(true)
          .build()
          .writeTo(out);
      out.flush();
      out.close();
    } catch (IOException e) {
      env.logger.error(classElement, "Failure while writing to file: %s", e.getMessage());
    }
  }
}
