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
  public static final String BUNDLE_CLASS_NAME = "android.os.Bundle";

  private final TypeMirror classType;
  private final String packageName;
  private final String className;
  private final Environment env;
  private final Element classElement;
  private boolean hasMandatoryExtras;
  private Map<String, BundleBinding> keyToExtraBindings;
  private Map<String, BundleBinding> keyToStateBinding;
  private ExtrasBuilderBindings extrasBuilder;

  public BindingsGroup(TypeMirror classType, String packageName, String className,
      Environment env) {
    this.classType = classType;
    this.packageName = packageName;
    this.className = className;
    this.env = env;
    this.classElement = env.types.asElement(classType);
    this.keyToExtraBindings = new HashMap<String, BundleBinding>(8);
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

  public void writeSourceToFile(Filer filer) {
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
      extraBinding.addToCodeBlock(unpackExtrasMethodCode, target, bundleParam);
    }
    final MethodSpec unpackExtrasMethod = MethodSpec.methodBuilder("unpackExtras")
        .addModifiers(Modifier.STATIC)
        .addParameter(TypeName.get(classType), target)
        .addParameter(TypeName.get(env.elements.getTypeElement(BUNDLE_CLASS_NAME).asType()),
            bundleParam)
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
}
