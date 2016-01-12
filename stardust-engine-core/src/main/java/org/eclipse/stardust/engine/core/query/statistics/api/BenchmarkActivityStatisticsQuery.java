/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.query.statistics.api;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.engine.api.runtime.BusinessObject;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.spi.query.CustomActivityInstanceQuery;


/**
 * @author roland.stamm
 * @version $Revision$
 */
public class BenchmarkActivityStatisticsQuery extends CustomActivityInstanceQuery
{
   static final long serialVersionUID = 8790093503685888481L;

   private static final String FIELD__PROCESS_INSTANCE_START_TIME = "process_instance_"
         + ProcessInstanceBean.FIELD__START_TIME;

   public static final String ID = BenchmarkActivityStatisticsQuery.class.getName();

   /**
    * The start time of the process instance the activity instance belongs to.
    *
    * @see org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean#getStartTime()
    */
   public static final FilterableAttribute PROCESS_INSTANCE_START_TIME = new ReferenceAttribute(
         new Attribute(FIELD__PROCESS_INSTANCE_START_TIME), ProcessInstanceBean.class,
         ActivityInstanceBean.FIELD__PROCESS_INSTANCE, ProcessInstanceBean.FIELD__OID,
         ProcessInstanceBean.FIELD__START_TIME);

   public static BenchmarkActivityStatisticsQuery forProcesses(
         ProcessDefinition process)
   {
      return forProcesses(Collections.singleton(process));
   }

   public static BenchmarkActivityStatisticsQuery forProcesses(
         Set<ProcessDefinition> processes)
   {
      Set<String> processIds = CollectionUtils.newSet();
      for (Iterator<ProcessDefinition> i = processes.iterator(); i.hasNext();)
      {
         ProcessDefinition process = i.next();
         processIds.add(process.getQualifiedId());
      }

      return forProcessIds(processIds);
   }

   public static BenchmarkActivityStatisticsQuery forProcessId(String processId)
   {
      return forProcessIds(Collections.singleton(processId));
   }

   public static BenchmarkActivityStatisticsQuery forProcessIds(Set<String> processIds)
   {
      BenchmarkActivityStatisticsQuery query = new BenchmarkActivityStatisticsQuery();

      FilterOrTerm processFilter = query.getFilter().addOrTerm();

      for (Iterator<String> i = processIds.iterator(); i.hasNext();)
      {
         String processId = i.next();

         processFilter.add(new ProcessDefinitionFilter(processId, false));
      }

      return query;
   }
   
   public static BenchmarkActivityStatisticsQuery forProcessesAndBusinessObject(
         Set<ProcessDefinition> processes,
         BusinessObject businessObjectId, Set<Serializable> businessObjectPrimaryKeys)
   {
      return forProcessesAndBusinessObject(processes, 
            businessObjectId, businessObjectPrimaryKeys, null, null);
   }
   
   public static BenchmarkActivityStatisticsQuery forProcessesAndBusinessObject(
         Set<ProcessDefinition> processes,
         BusinessObject businessObjectId, BusinessObject businessObjectGroup)
   {
      return forProcessesAndBusinessObject(processes, 
            businessObjectId, null, businessObjectGroup, null);
   }
   
   public static BenchmarkActivityStatisticsQuery forProcessesAndBusinessObject(
         Set<ProcessDefinition> processes,
         BusinessObject businessObject, Set<Serializable> businessObjectPrimaryKeys,
         BusinessObject businessObjectGroup, Set<Serializable> businessObjectGroupPrimaryKeys)
   {
      BenchmarkActivityStatisticsQuery query = forProcesses(processes);

      BusinessObjectPolicy boPolicy = BusinessObjectPolicy.filterFor(
            businessObject.getModelId(), businessObject.getId(),
            businessObjectPrimaryKeys);
      
      if(businessObjectGroup != null)
      {
         boPolicy.groupBy(businessObjectGroup.getModelId(),
               businessObjectGroup.getId(),
               businessObjectGroupPrimaryKeys);
      }
      
      query.setPolicy(boPolicy);

      return query;
   }

   protected BenchmarkActivityStatisticsQuery()
   {
      super(ID);
   }

   public static final class Attribute extends FilterableAttributeImpl
   {
      private Attribute(String name)
      {
         super(ActivityInstanceQuery.class, name);
      }
   }
}