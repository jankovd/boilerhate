package boilerhate;

import com.google.auto.common.Visibility;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

public class Boilerhate {
  public static final boolean DEBUG = true;

  private final Environment env;
  private final Map<String, BindingsGroup> typeToGroup;

  public Boilerhate(Environment env) {
    this.env = env;
    this.typeToGroup = new HashMap<String, BindingsGroup>(8);
  }

  public void addExtra(Element element) {
    final BindingsGroup typeGroup;
    final String bundleKey;
    if (!canWriteFromPackage(element)) {
      env.logger.error(element, "Cannot access '%s' from package, must not be private.",
          element.getSimpleName().toString());
    } else if ((bundleKey = BundleBinding.ExtraBundleBinding.getKey(element)) == null) {
      env.logger.error(element, "Missing value for key on @Extra annotated element.");
    } else if ((typeGroup = getBindingsGroupForElement(element)) == null) {
      env.logger.error(element, "Cannot create group: %s", element.getSimpleName().toString());
    } else {
      typeGroup.addExtra(bundleKey, element);
    }
  }

  public void addState(Element element) {
    if (!canWriteFromPackage(element)) {
      env.logger.error(element, "Cannot access '%s' from package, must not be private.",
          element.getSimpleName().toString());
      return;
    }
  }

  public void generateExtrasBuilder(Element element) {
    if (!canWriteFromPackage(element)) {
      env.logger.error(element, "%s must have at least package visibility.",
          element.getSimpleName().toString());
      return;
    }
  }

  private BindingsGroup getBindingsGroupForElement(Element element) {
    element = element.getEnclosingElement();
    if (element == null || element.getKind() != ElementKind.CLASS) { return null; }
    final TypeMirror classType = element.asType();
    StringBuilder classNameBuilder = new StringBuilder();
    while (element.getKind() != ElementKind.PACKAGE) {
      if (element.getKind() == ElementKind.CLASS) {
        if (classNameBuilder.length() != 0) { classNameBuilder.insert(0, "_"); }
        classNameBuilder.insert(0, element.getSimpleName().toString());
      }
      element = element.getEnclosingElement();
    }
    final String packageName = element.asType().toString();
    final String className = classNameBuilder.toString();
    final String identifier = packageName + "." + classType.toString();
    BindingsGroup bindingsGroup = typeToGroup.get(identifier);
    if (bindingsGroup == null) {
      bindingsGroup = new BindingsGroup(classType, packageName, className, env);
      typeToGroup.put(identifier, bindingsGroup);
    }
    return bindingsGroup;
  }

  private boolean canWriteFromPackage(Element element) {
    return canReadFromPackage(element)
        && (element.getKind() == ElementKind.METHOD
        || !element.getModifiers().contains(Modifier.FINAL));
  }

  private boolean canReadFromPackage(Element element) {
    return Visibility.effectiveVisibilityOfElement(element).ordinal()
        > Visibility.PRIVATE.ordinal();
  }

  public void writeSourceFiles(Filer filer) {
    for (final Map.Entry<String, BindingsGroup> entry : typeToGroup.entrySet()) {
      entry.getValue().writeSourceToFile(filer);
    }
  }
}
