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
package org.eclipse.stardust.engine.api.query;

/**
 * For internal use only!
 * 
 * @author born
 * @version $Revision$
 */
public class CustomOrderCriterion extends AbstractSingleOrderCriterion
{
   private static final long serialVersionUID = 1L;

   private final Class type;

   private final String fieldName;

   CustomOrderCriterion(Class type, String fieldName)
   {
      this(type, fieldName, true);
   }

   CustomOrderCriterion(Class type, String fieldName, boolean ascending)
   {
      super(ascending);
      this.type = type;
      this.fieldName = fieldName;
   }

   public CustomOrderCriterion ascendig(boolean ascending)
   {
      if (ascending != this.isAscending())
      {
         return new CustomOrderCriterion(type, fieldName, ascending);
      }

      return this;
   }

   public Class getType()
   {
      return type;
   }

   public String getFieldName()
   {
      return fieldName;
   }

   public Object accept(OrderEvaluationVisitor visitor, Object context)
   {
      return visitor.visit(this, context);
   }
}
