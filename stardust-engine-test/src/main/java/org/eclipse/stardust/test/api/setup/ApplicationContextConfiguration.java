/**********************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
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
 * All {@link ApplicationContextConfiguration} annotations in a class hierarchy will be respected, i.e. if the test class extends another class
 * and both classes have (different) {@link ApplicationContextConfiguration} annotations, the specified {@link #locations()} will be cumulated.
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
