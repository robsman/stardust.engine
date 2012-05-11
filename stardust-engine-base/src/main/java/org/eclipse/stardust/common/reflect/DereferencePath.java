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
package org.eclipse.stardust.common.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;


/** Models a sequence of dereferetiation steps being able to transform a
starting object into Data, which can be used to populate GUI components like
Text Fields, Check Boxes or Tables. */
public class DereferencePath
{
   protected List steps;

   public void setVector(List steps)
   {
      this.steps = steps;
   }

   /** */
   public DereferencePath()
   {
      steps = CollectionUtils.newList();
   }

   /** */
   public void addAttributeStep(Field newField)
   {
      steps.add(new AttributeStep(newField));
   }

   /** */
   public void addMethodStep(Method newSetMethod, Method newGetMethod)
   {
      steps.add(new MethodStep(newSetMethod, newGetMethod));
   }

   /** */
   public void addAssociationStep(Field newField)
   {
      //	steps.addElement(new AttributeStep(newField));
   }

   /** */
   public Object getValue(Object startObject)
   {
      if (startObject == null)
      {
         return null;
      }

      Object tempObject;

      tempObject = startObject;
      for (int N = 0; N < steps.size(); N++)
      {
         tempObject = ((DereferenceStep) steps.get(N)).getValue(tempObject);
      }

      return tempObject;
   }

   /** get the full predicate path for AttributeSteps */
   public String getPredicatePath()
   {
      String delim = "";
      String path = "";

      for (int N = 0; N < steps.size(); N++)
      {
         path += delim + ((DereferenceStep) steps.get(N)).getName();
         delim = ".";
      }

      return path;
   }

   public void setValue(Object startObject,
         Object newValue)
   {
      if (startObject == null)
      {
         return;
      }

      Object tempObject;
      int N;

      tempObject = startObject;

      for (N = 0; N < steps.size() - 1; N++)
      {
         tempObject = ((DereferenceStep) steps.get(N)).getValue(tempObject);

         if (tempObject == null)
         {
            return;
         }
      }

      ((DereferenceStep) steps.get(N)).setValue(tempObject,
            newValue);

   }
}
