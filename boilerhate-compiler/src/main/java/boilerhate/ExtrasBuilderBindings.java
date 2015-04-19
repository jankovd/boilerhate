package boilerhate;

import javax.annotation.processing.Filer;

class ExtrasBuilderBindings {
  private final BindingsGroup bindings;
  private ExtrasBuilderBindings parentBuilder;

  public ExtrasBuilderBindings(BindingsGroup bindings) {
    this.bindings = bindings;
  }

  public void setParentBuilder(ExtrasBuilderBindings parent) {
    this.parentBuilder = parent;
  }

  public void writeSourceToFile(Filer filer) {

  }
}
