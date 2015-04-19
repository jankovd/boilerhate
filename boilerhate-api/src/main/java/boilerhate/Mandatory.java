package boilerhate;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Whether an annotated field or method must be provided in the extras
 * bundle.
 * <p/>
 * Does not allow:
 * <ul>
 * <li>usage with an extra that has a method for a default value.</li>
 * </ul>
 */
@Retention(CLASS)
@Target({FIELD, METHOD, TYPE})
public @interface Mandatory {
}
