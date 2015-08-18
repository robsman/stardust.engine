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
 * Filter criterion for matching specific process instances.
 *
 * @author roland.stamm
 */
public final class ProcessInstanceHierarchyFilter implements FilterCriterion
{
   public enum HierarchyMode {
      ROOT_PROCESS, SUB_PROCESS
   }

   /**
    * Filter to limit results to root process instances only.
    */
   public static final ProcessInstanceHierarchyFilter ROOT_PROCESS = new ProcessInstanceHierarchyFilter(
         HierarchyMode.ROOT_PROCESS);

   /**
    * Filter to limit results to sub process instances only.
    */
   public static final ProcessInstanceHierarchyFilter SUB_PROCESS = new ProcessInstanceHierarchyFilter(
         HierarchyMode.SUB_PROCESS);

   private HierarchyMode mode;

   private ProcessInstanceHierarchyFilter(HierarchyMode mode)
   {
      this.mode = mode;
   }

   public HierarchyMode getMode()
   {
      return mode;
   }

   public Object accept(FilterEvaluationVisitor visitor, Object context)
   {
      return visitor.visit(this, context);
   }

}
