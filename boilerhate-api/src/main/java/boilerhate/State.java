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
public @interface State {

  String value() default "";
}
