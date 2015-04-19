package boilerhate;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Format to use with {@link String#format(String, Object...)} for
 */
@Retention(CLASS)
@Target(METHOD)
/*public*/ @interface MultiExtra {

  String format();

  String key();

  String[] names();

  Class<?>[] types();
}
