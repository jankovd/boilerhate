package boilerhate;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Does not allow:
 * <ul>
 * <li>multiple fields with the same key.</li>
 * <li>both a setter method and a field.</li>
 * <li>multiple getters.</li>
 * <li>annotated method without a key for extra.</li>
 * </ul>
 */
@Retention(CLASS)
@Target({FIELD, METHOD})
public @interface Extra {

  /**
   * Key for extra, defaults to field name. This would also be used as the
   * the name of the method generated for the builder if the enclosing type
   * is annotated with {@link ExtrasBuilder @ExtrasBuilder}.
   */
  String value() default "";
}
