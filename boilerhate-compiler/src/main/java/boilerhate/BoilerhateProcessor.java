package boilerhate;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.Generated;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;

import static javax.tools.Diagnostic.Kind.ERROR;

public class BoilerhateProcessor extends AbstractProcessor {

  private Types typeUtils;
  private Elements elementUtils;
  private Filer filer;
  private Messager messager;

  @Override public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    typeUtils = processingEnv.getTypeUtils();
    elementUtils = processingEnv.getElementUtils();
    filer = processingEnv.getFiler();
    messager = processingEnv.getMessager();
  }

  @Override public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latest();
  }

  @Override public Set<String> getSupportedAnnotationTypes() {
    Set<String> supported = new LinkedHashSet<String>(6);
    supported.add(ArgumentExtra.class.getCanonicalName());
    supported.add(BundleExtra.class.getCanonicalName());
    supported.add(BundleExtrasBuilder.class.getCanonicalName());
    supported.add(Mandatory.class.getCanonicalName());
    supported.add(ParcelState.class.getCanonicalName());
    supported.add(StyledAttr.class.getCanonicalName());
    return supported;
  }

  @Override public boolean process(Set<? extends TypeElement> annotations,
      RoundEnvironment roundEnv) {
    Set<? extends Element> annotatedWith = roundEnv.getElementsAnnotatedWith(BundleExtrasBuilder.class);
    for (final Element element : annotatedWith) {
      final PackageElement packageOf = elementUtils.getPackageOf(element);
      try {
        final String name = element.getSimpleName() + "_Sample";
        final String packageName = packageOf.getQualifiedName().toString();
        TypeSpec sample = TypeSpec.classBuilder(name)
            .addAnnotation(AnnotationSpec.builder(Generated.class)
                .addMember("value", "$S", BoilerhateProcessor.class.getCanonicalName())
                .build())
            .addModifiers(Modifier.PUBLIC)
            .addMethod(MethodSpec.methodBuilder("log")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(String.class, "msg")
                .addStatement("$T.out.println($S)", System.class, packageName)
                .build())
            .addMethod(MethodSpec.methodBuilder("println")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(Object.class, "o")
                .addStatement("$T.out.println(String.valueOf($L))", System.class, "o")
                .build())
            .build();

        JavaFileObject sourceFile = filer.createSourceFile(packageName + "." + name,
            element.getEnclosingElement());
        JavaFile javaFile = JavaFile.builder(packageName, sample).build();
        Writer out = sourceFile.openWriter();
        javaFile.writeTo(out);
        out.flush();
        out.close();
      } catch (IOException e) {
        processingEnv.getMessager().printMessage(ERROR, "error writing to file", element);
      }
    }
    return false;
  }
}
