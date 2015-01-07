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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.stardust.common.StringUtils;


/**
 * Restricts the resulting items to the ones related to a specific activity. 
 * The search can be further restricted to certain models by passing in a collection of model oids.
 *
 * @author rsauer
 * @version $Revision$
 */
public class ActivityFilter implements FilterCriterion
{
   private final String activityID;
   private final String processID;
   private final boolean includingSubProcesses;
   private final Set/*<Long>*/ modelOids;
   
   /**
    * Creates a filter matching the activity identified by <code>activityID</code>.
    *
    * @param activityID The ID of the activity to filter for.
    */
   public static ActivityFilter forAnyProcess(String activityID)
   {
      //TODO this should ideally be (ActivityFilter) forProcess(activityID, null, null, false); but without the cast
      //this will require a change in the signature of this method.
      return new ActivityFilter(activityID, null, null, false);
   }
   
   /**
    * Creates a filter matching the activity identified by <code>activityID</code>
    * and present in the given models <code>modelOids</code>.
    *
    * @param activityID The ID of the activity to filter for.
    * @param modelOids  The IDs of the models to restrict the search to. 
    */
   public static ActivityFilter forAnyProcess(String activityID, Collection/*<Long>*/ modelOids)
   {
      //TODO this should ideally be (ActivityFilter) forProcess(activityID, null, modelOids, false); but without the cast
      //this will require a change in the signature of this method.
      return new ActivityFilter(activityID, null, modelOids, false);
   }

   /**
    * Creates a filter matching the activity identified by <code>activityID</code> 
    * within scope of process definition identified by <code>processID</code>.
    * The created filter will include subprocesses.
    * 
    * @param activityID The ID of the activity to filter for. When <code>processID</code>
    *                   is set to <code>null</code> then activities are filtered for the whole model,
    *                   otherwise for the given process definition.
    * @param processID  The ID of the process definition to filter for. Can be <code>null</code>.
    * 
    * @see #forProcess(String, String, Collection, boolean)
    */
   public static FilterCriterion forProcess(String activityID, String processID)
   {
      return forProcess(activityID, processID, null, true);
   }

   /**
    * Creates a filter matching the activity identified by <code>activityID</code> 
    * within scope of process definition identified by <code>processID</code>.
    * 
    * @param activityID The ID of the activity to filter for. When <code>processID</code>
    *                   is set to <code>null</code> then activities are filtered for the whole model,
    *                   otherwise for the given process definition.
    * @param processID  The ID of the process definition to filter for. Can be <code>null</code>.
    * @param includingSubProcesses Flag indicating if subprocesses should be included. 
    */
   public static FilterCriterion forProcess(String activityID, String processID,
         boolean includingSubProcesses)
   {
      return forProcess(activityID, processID, null, includingSubProcesses);
   }

   
   /**
    * Creates a filter matching the activity identified by <code>activityID</code> 
    * within scope of process definition identified by <code>processID</code>.
    * 
    * @param activityID The ID of the activity to filter for. When <code>processID</code>
    *                   is set to <code>null</code> then activities are filtered for the whole model,
    *                   otherwise for the given process definition.
    * @param processID  The ID of the process definition to filter for. Can be <code>null</code>.
    * @param includingSubProcesses Flag indicating if subprocesses should be included. 
    */
   public static FilterCriterion forProcess(String activityID, String processID,
         Collection/*<Long>*/modelOids, boolean includingSubProcesses)
   {
      FilterCriterion filterCriterion = new ActivityFilter(activityID, processID,
            modelOids, includingSubProcesses);

      if ( !StringUtils.isEmpty(processID))
      {
         BlacklistFilterVerifyer LENIENT_VERIFYER = new BlacklistFilterVerifyer(
               new Class[] {});
         filterCriterion = new FilterAndTerm(LENIENT_VERIFYER)//
               .and(filterCriterion);

         if (includingSubProcesses)
         {
            ((FilterAndTerm) filterCriterion).and(new ProcessDefinitionFilter(processID,
                  includingSubProcesses));
         }
      }

      return filterCriterion;
   }
   /**
    * Creates a filter matching the activity identified by <code>activityID</code>.
    *
    * @param activityID The ID of the activity to filter for.
    * 
    * @deprecated Use {@link #forAnyProcess(String)} instead.
    */
   public ActivityFilter(String activityID)
   {
      this(activityID, null, null, false);
   }
   
   /**
    * Creates a filter matching the activity identified by <code>activityID</code> 
    * within scope of process definition identified by <code>processID</code>.
    * 
    * @param activityID The ID of the activity to filter for. When <code>processID</code>
    *                   is set to <code>null</code> then activities are filtered for the whole model,
    *                   otherwise for the given process definition.
    * @param processID  The ID of the process definition to filter for. Can be <code>null</code>.
    * @param modelOids  Collection of model oids to restrict the search to or <code>null</code> 
    *                   if no such restriction is to be placed. The constructor makes a deep copy of 
    *                   the collection
    * @param includingSubProcesses Flag indicating if subprocesses should be included.
    */
   private ActivityFilter(final String activityID, final String processID, final Collection/*<Long>*/ modelOids,
         boolean includingSubProcesses)
   {
      this.activityID = activityID;
      this.processID = processID;
      if (null != processID)
      {
         this.includingSubProcesses = includingSubProcesses;
      }
      else
      {
         this.includingSubProcesses = false;
      }
      if (modelOids != null && !modelOids.isEmpty())
      {
         this.modelOids = Collections.unmodifiableSet(new HashSet(modelOids));
      }
      else
      {
         this.modelOids = Collections.EMPTY_SET;
      }
   }

   /**
    * The ID of the activity to filter for.
    *
    * @return The activity ID.
    */
   public String getActivityID()
   {
      return activityID;
   }
   
   /**
    * The ID of the process definition to filter for.
    *
    * @return The process definition ID.
    */
   public String getProcessID()
   {
      return processID;
   }
   
   /**
    * State whether this filter will be applied to subprocesses as well.
    * 
    * @return <code>true</code> when subprocesses shall be filtered as well, otherwise <code>false</code>.
    */
   public boolean isIncludingSubProcesses()
   {
      return includingSubProcesses;
   }
   
   /**
    * {@inheritDoc}
    */
   public Object accept(FilterEvaluationVisitor visitor, Object context)
   {
      return visitor.visit(this, context);
   }
   
   /**
    * Gets the model oids to which the search is restricted to.
    * @return unmodifiable collection of model oids to restrict the search to or an empty collection if there is no such restriction.
    */
   public Collection/*<Long>*/ getModelOids()
   {
      return modelOids;
   }
}
