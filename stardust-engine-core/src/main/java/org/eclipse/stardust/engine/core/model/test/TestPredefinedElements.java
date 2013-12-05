/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.model.test;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;

import junit.framework.TestCase;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class TestPredefinedElements extends TestCase
{
   /**
    * Assuming that all constants in PredefinedElements ending with <code>_CLASS</code>
    * represent a valid class name contained in the CARNOT codebase the existence of
    * these classes is checked.  
    */
   public void testClassNames()
      throws Exception
   {
      List evilStuff = CollectionUtils.newList();
      Field[] fields = PredefinedConstants.class.getDeclaredFields();
      for (int i = 0; i < fields.length; i++)
      {
         Field field = fields[i];
         if (field.getName().endsWith("_CLASS"))
         {
            String className = (String) field.get(null);
            try
            {
               Reflect.getClassFromClassName(className);
            }
            catch (InternalException e)
            {
               evilStuff.add(className);
            }
         }
      }
      if (!evilStuff.isEmpty())
      {
         StringBuffer message = new StringBuffer("The following classes don't exist: \n");
         for (Iterator i = evilStuff.iterator(); i.hasNext();)
         {
            String s = (String) i.next();
            message.append("  ").append(s).append("\n");
         }
         fail(message.toString());
      }
   }
}
