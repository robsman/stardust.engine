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
package org.eclipse.stardust.engine.core.upgrade.jobs.m30;

import java.util.HashMap;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class Attribute
{
   private String name;
   // null means "String"
   private String className;
   private String value;
   private static HashMap transformer = new HashMap();
   private static HashMap valueTransformer = new HashMap();

   static
   {
      transformer.put("java.lang.String", null);
      transformer.put("java.lang.Long", "long");
      transformer.put("java.lang.Integer", "int");
      transformer.put("java.lang.Boolean", "boolean");
      transformer.put("java.lang.Double", "double");
      transformer.put("java.lang.Float", "float");
      transformer.put("java.lang.Character", "char");
      transformer.put("java.lang.Short", "short");
      transformer.put("java.lang.Byte", "byte");
      transformer.put("ag.carnot.workflow.TypeKey", "ag.carnot.workflow.spi.providers.data.java.Type");
      transformer.put("org.eclipse.stardust.common.Period", "Period");
      transformer.put("java.util.Date", "Timestamp");
      transformer.put("java.util.Calendar", "Calendar");

      valueTransformer.put("ag.carnot.workflow.User", "ag.carnot.workflow.runtime.User");
      valueTransformer.put("ag.carnot.workflow.UserHome", "ag.carnot.workflow.runtime.UserHome");
      valueTransformer.put("ag.carnot.workflow.UserPK", "ag.carnot.workflow.runtime.UserPK");
   }

   public Attribute(String name, String className, String value)
   {
      this.name = name;
      if (transformer.containsKey(className))
      {
         String shortClassName = (String) transformer.get(className);
         this.className = shortClassName;
      }
      else
      {
         this.className = className;
      }
      if (valueTransformer.containsKey(value))
      {
         this.value = (String) valueTransformer.get(value);
      }
      else
      {
         this.value = value;
      }
   }

   public Attribute(String name, String value)
   {
      this.name = name;
      this.value = value;
   }

   public String getName()
   {
      return name;
   }

   public String getValue()
   {
      return value;
   }

   public String getClassName()
   {
      return className;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public void setClassName(String name)
   {
      this.className = name;
   }

   public void setValue(String value)
   {
      this.value = value;
   }

}
