package boilerhate;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Gathers all @Extra annotated fields and setter methods and creates a
 * fluent-api builder for creating an instance of the annotated type.
 * A no-arg constructor is a requirement.
 * <p/>
 * Besides methods for the extras, it exposes the following methods:
 * <ul>
 * <li>Activity|Service - toIntent(Context), toBundle().</li>
 * <li>-Fragment - toFragment(), toBundle().</li>
 * </ul>
 */
@Retention(CLASS)
@Target(TYPE)
public @interface ExtrasBuilder {

  /**
   * Name of the generated class, defaults to the name of the
   * annotated type with an added suffix.
   */
  String className() default "";

  /**
   * The generated class copies the visibility modifier of the annotated type
   * by default. With this the visibility modifier of the generated class can
   * be forced to be public whatever the visibility of the annotated type is.
   */
  boolean forcePublicVisibility() default false;
}
