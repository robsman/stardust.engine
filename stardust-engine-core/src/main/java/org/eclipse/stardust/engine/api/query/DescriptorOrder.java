/*******************************************************************************
* Copyright (c) 2016 SunGard CSA LLC and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Antje.Fuhrmann (SunGard CSA LLC) - initial API and implementation and/or initial documentation
*******************************************************************************/
package org.eclipse.stardust.engine.api.query;

public class DescriptorOrder extends AbstractSingleOrderCriterion
{
   private static final long serialVersionUID = 1L;
   private final String descriptorId;

   public DescriptorOrder(String descriptorId, boolean ascending)
   {
      super(ascending);
      this.descriptorId = descriptorId;
   }

   @Override
   public Object accept(OrderEvaluationVisitor visitor, Object context)
   {
      return visitor.visit(this, context);
   }

   public String getDescriptorId()
   {
      return descriptorId;
   }
}
