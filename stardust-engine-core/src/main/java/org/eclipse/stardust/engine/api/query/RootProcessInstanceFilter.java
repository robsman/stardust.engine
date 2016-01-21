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
 * Restricts the resulting items to the ones related to a specific root process.
 *
 * @author rsauer
 * @version $Revision$
 */
public class RootProcessInstanceFilter implements FilterCriterion
{
   private final String processID;
   private final String processName;
   
   /**
    * Creates a filter matching the root process identified by
    * <code>processID</code> and/or <code>processName</code>.
    *
    * @param processID The ID of the process to filter for (can also be null to exclude from filtering).
    * @param processName The name of the process to filter for (can also be null to exclude from filtering).
    */
   public RootProcessInstanceFilter(String processID, String processName)
   {
      this.processID = processID;         
      this.processName = processName;         
   }

   /**
    * Gets the ID of the root process to filter for.
    *
    * @return The root process definition ID.
    */
   public String getRootProcessID()
   {
      return processID;
   }

   /**
    * Gets the name of the root process to filter for.
    *
    * @return The root process definition name.
    */
   public String getRootProcessName()
   {
      return processName;
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
      sb.append(" ProcessName = ");
      sb.append(processName);
      
      return sb.toString();
   }
}