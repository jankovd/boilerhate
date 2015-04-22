package boilerhate;

import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

class Environment {
  public final Elements elements;
  public final Types types;
  public final Logger logger;
  public final BundleTypes bundles;

  public Environment(Elements elements, Types types, Logger logger, BundleTypes bundles) {
    this.elements = elements;
    this.types = types;
    this.logger = logger;
    this.bundles = bundles;
  }
}
