package boilerhate;

import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public class BoilerhateProcessor extends AbstractProcessor {
  private Environment environment;

  @Override public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    final Elements elements = processingEnv.getElementUtils();
    final Types types = processingEnv.getTypeUtils();
    final Messager messager = processingEnv.getMessager();
    environment = new Environment(elements, types, new Logger(messager),
        new BundleTypes(elements, types));
  }

  @Override public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latest();
  }

  @Override public Set<String> getSupportedAnnotationTypes() {
    Set<String> supported = new LinkedHashSet<String>(6);
    supported.add(Extra.class.getCanonicalName());
    supported.add(State.class.getCanonicalName());
    supported.add(ExtrasBuilder.class.getCanonicalName());
    return supported;
  }

  @Override public boolean process(Set<? extends TypeElement> annotations,
      RoundEnvironment roundEnv) {
    Boilerhate boilerhate = new Boilerhate(environment);
    for (final Element element : roundEnv.getElementsAnnotatedWith(Extra.class)) {
      boilerhate.addExtra(element);
    }
    for (final Element element : roundEnv.getElementsAnnotatedWith(State.class)) {
      boilerhate.addState(element);
    }
    for (final Element element : roundEnv.getElementsAnnotatedWith(ExtrasBuilder.class)) {
      boilerhate.generateExtrasBuilder(element);
    }
    boilerhate.writeSourceFiles(processingEnv.getFiler());
    return false;
  }
}
