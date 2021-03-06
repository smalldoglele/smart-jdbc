package org.smart.jdbc.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ java.lang.annotation.ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {

	String name() default "";

	UniqueConstraint[] uniqueConstraints() default {};
}
