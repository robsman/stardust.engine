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
 * Restricts the resulting items to the ones related to a specific process definition.
 *
 * @author rsauer
 * @version $Revision$
 */
public class ProcessDefinitionFilter implements FilterCriterion
{
   private final String processID;
   private final boolean includingSubProcesses;

   /**
    * Creates a filter matching the process definition identified by
    * <code>processID</code> and all of its subprocesses.
    *
    * @param processID The ID of the process definition to filter for.
    *
    * @see #ProcessDefinitionFilter(String, boolean)
    */
   public ProcessDefinitionFilter(String processID)
   {
      this(processID, true);
   }

   /**
    * Creates a filter matching the process definition identified by
    * <code>processID</code>.
    *
    * @param processID The ID of the process definition to filter for.
    * @param includingSubProcesses Flag indicating if subprocesses should be included.
    *
    * @see #ProcessDefinitionFilter(String)
    */
   public ProcessDefinitionFilter(String processID, boolean includingSubprocesses)
   {
      this.processID = processID;
      this.includingSubProcesses = includingSubprocesses;
   }

   /**
    * Gets the ID of the process definition to filter for.
    *
    * @return The process definition ID.
    */
   public String getProcessID()
   {
      return processID;
   }

   /**
    * Retrieves the flag indicating if subprocesses should be included or not.
    *
    * @return <code>true</code> if subprocesses should be included, <code>false</code> if
    *         not.
    */
   public boolean isIncludingSubProcesses()
   {
      return includingSubProcesses;
   }

   public Object accept(FilterEvaluationVisitor visitor, Object context)
   {
      return visitor.visit(this, context);
   }

   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();
      sb.append("ProcessId = ");
      sb.append(processID);
      if (includingSubProcesses)
      {
         sb.append(" WITH SUBPROCESSES");
      }
      return sb.toString();
   }
}
