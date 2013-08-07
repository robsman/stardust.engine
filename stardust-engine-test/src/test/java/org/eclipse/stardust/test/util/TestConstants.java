/**********************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.util;

/**
 * <p>
 * This class contains constants common to all functional tests.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class TestConstants
{
   /**
    * the username and password of the predefined administrator user
    */
   public static final String MOTU = "motu";
   
   /**
    * prefix for all assertion messages: allows to print assertion messages on the console when running the JUnit test via Ant,
    * otherwise the complete message would be swallowed since it starts with <code>Caused by: java.lang.AssertionError</code>
    * (see {@link org.apache.tools.ant.taskdefs.optional.junit.JUnitTestRunner} of <code>org.apache.ant:ant-junit:1.7.x</code>)
    */
   public static final String NL = "\n";
   
   private TestConstants()
   {
      /* utility class; do not allow the creation of an instance */
   }
}
