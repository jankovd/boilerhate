package boilerhate;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

class Logger {
  public final Messager messager;

  public Logger(Messager messager) {
    this.messager = messager;
  }

  public void warning(String message, Object... formatArgs) {
    warning(null, message, formatArgs);
  }

  public void warning(Element element, String message, Object... formatArgs) {
    log(Diagnostic.Kind.WARNING, element, message, formatArgs);
  }

  public void error(String message, Object... formatArgs) {
    error(null, message, formatArgs);
  }

  public void error(Element element, String message, Object... formatArgs) {
    log(Diagnostic.Kind.ERROR, element, message, formatArgs);
  }

  private void log(Diagnostic.Kind kind, Element element, String message, Object... formatArgs) {
    if (formatArgs.length > 0) {
      messager.printMessage(kind, String.format(message, formatArgs), element);
    } else {
      messager.printMessage(kind, message, element);
    }
  }
}
