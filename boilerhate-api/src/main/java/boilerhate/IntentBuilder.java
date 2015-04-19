package boilerhate;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

@Retention(CLASS)
@Target({ANNOTATION_TYPE, TYPE})
/*public*/ @interface IntentBuilder {

  String className() default "";

  String action() default "";

  int flags() default 0;

  String[] categories() default {};

  String data() default "";

  String mimeType() default "";
}
