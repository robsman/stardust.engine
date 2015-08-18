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
package org.eclipse.stardust.engine.core.model.utils;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class AddedElement
{
   private ModelElement modelElement;
   private String fieldName;
   private ModelElement target;

   public AddedElement(ModelElement modelElement, String fieldName)
   {
      this.modelElement = modelElement;
      this.fieldName = fieldName;
   }

   public ModelElement getModelElement()
   {
      return modelElement;
   }

   public String getFieldName()
   {
      return fieldName;
   }

   public String toString()
   {
      return modelElement.toString();
   }

   public void setTargetElement(ModelElement element)
   {
      this.target = element;
   }

   public ModelElement getTargetElement()
   {
      return target;
   }
}
