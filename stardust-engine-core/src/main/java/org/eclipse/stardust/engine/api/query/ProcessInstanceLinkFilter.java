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

public class ProcessInstanceLinkFilter implements FilterCriterion
{
   private static final long serialVersionUID = 1L;

   private final long processInstanceOid;

   private final LinkDirection direction;

   private final String[] linkType;

   public ProcessInstanceLinkFilter(long processInstanceOid, LinkDirection direction,
         String[] linkType)
   {
      this.processInstanceOid = processInstanceOid;
      this.direction = direction;
      this.linkType = linkType;
   }

   public Object accept(FilterEvaluationVisitor visitor, Object context)
   {
      return visitor.visit(this, context);
   }

   public long getProcessInstanceOid()
   {
      return processInstanceOid;
   }

   public LinkDirection getDirection()
   {
      return direction;
   }

   public String[] getLinkType()
   {
      return linkType;
   }
}
