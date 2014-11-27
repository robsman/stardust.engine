package org.eclipse.stardust.test.api.setup;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * <p>
 * Allows for defining one or more {@link org.springframework.context.ApplicationContext}s that will
 * be loaded by <i>Stardust Engine Test</i>. These are loaded in addition to the ones defined by default.
 * </p>
 *
 * <p>
 * Only those {@link #locations()} are allowed that could also be passed to {@link ClassPathXmlApplicationContext#ClassPathXmlApplicationContext(String...)}.
 * </p>
 *
 * @author Nicolas.Werlein
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ApplicationContextConfiguration
{
   /**
    * <p>
    * The {@link org.springframework.context.ApplicationContext} locations to use.
    * </p>
    */
   String[] locations() default {};
}
