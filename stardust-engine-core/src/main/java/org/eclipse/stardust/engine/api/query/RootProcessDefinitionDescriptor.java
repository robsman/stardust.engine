/*******************************************************************************
* Copyright (c) 2016 SunGard CSA LLC and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Barry.Grotjahn (SunGard CSA LLC) - initial API and implementation and/or initial documentation
*******************************************************************************/

package org.eclipse.stardust.engine.api.query;

/**
 * @author Barry.Grotjahn
 */
public class RootProcessDefinitionDescriptor
      implements IRootProcessDefinitionDescriptor, OrderCriterion

{
   private static final long serialVersionUID = 1L;
   private boolean ascending = false;   
   
   private FilterableAttribute attribute;
   
   public RootProcessDefinitionDescriptor(FilterableAttribute attribute)
   {
      super();
      this.attribute = attribute;
   }
   
   public RootProcessDefinitionDescriptor ascendig(boolean ascending)
   {
      this.ascending = ascending;
      return this;
   }
   
   public String getAttributeName()
   {
      return attribute.getAttributeName();
   }

   public Object accept(OrderEvaluationVisitor visitor, Object context)
   {
      return visitor.visit(this, context);
   }

   public boolean isAscending()
   {
      return ascending;
   }
}