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
import java.util.Iterator;
import java.util.Set;

import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.ProcessDefinitionFilter;
import org.eclipse.stardust.engine.api.runtime.BusinessObject;
import org.eclipse.stardust.engine.core.spi.query.CustomProcessInstanceQuery;


/**
 * @author roland.stamm
 * @version $Revision$
 */
public class BenchmarkProcessStatisticsQuery extends CustomProcessInstanceQuery
{
   static final long serialVersionUID = -4101345299290353466L;

   public static final String ID = BenchmarkProcessStatisticsQuery.class.getName();

   public static BenchmarkProcessStatisticsQuery forAllProcesses()
   {
      return new BenchmarkProcessStatisticsQuery();
   }

   public static BenchmarkProcessStatisticsQuery forProcesses(
         Set<ProcessDefinition> processes)
   {
      BenchmarkProcessStatisticsQuery query = new BenchmarkProcessStatisticsQuery();

      if(processes != null && !processes.isEmpty())
      {
         FilterOrTerm processFilter = query.getFilter().addOrTerm();
   
         for (Iterator<ProcessDefinition> i = processes.iterator(); i.hasNext();)
         {
            ProcessDefinition process = i.next();
   
            processFilter.add(PROCESS_DEFINITION_OID.isEqual(process.getRuntimeElementOID()));
         }
      }

      return query;
   }

   public static BenchmarkProcessStatisticsQuery forProcessIds(Set<String> processes)
   {
      BenchmarkProcessStatisticsQuery query = new BenchmarkProcessStatisticsQuery();

      if(processes != null && !processes.isEmpty())
      {
         FilterOrTerm processFilter = query.getFilter().addOrTerm();
   
         for (Iterator<String> i = processes.iterator(); i.hasNext();)
         {
            String processId = (String) i.next();
   
            processFilter.add(new ProcessDefinitionFilter(processId, false));
         }
      }

      return query;
   }
   
   /**
    * Creates a <code>BenchmarkProcessStatisticsQuery</code> where only process instances
    * should be considered which are:
    * <ul>
    *   <li>Of a type which is included in the <code>processes</code> set or of any type
    *     if this argument is an empty set or null 
    *   <li>Using a business object instance that are of the same type as 
    *     <code>businessObject</code>. <strong>Please note</strong> that this parameter
    *     is mandantory and cannot be null</li>
    * </ul>
    * Furthermore if the set from <code>businessObjectPrimaryKeys</code> contains  
    * entries then the result set is additionally restricted to the given primary keys.
    * @param processes Set of process definitions, an empty set or null
    * @param businessObject Business object type which is used as a filter argument
    * @param businessObjectPrimaryKeys Primary key values of the business object if the
    *    business object instances should be restricted to these values or an
    *    empty set resp. null if no restrictions to the business object should be applied.
    */
   public static BenchmarkProcessStatisticsQuery forProcessesAndBusinessObject(
         Set<ProcessDefinition> processes,
         BusinessObject businessObject, Set<Serializable> businessObjectPrimaryKeys)
   {
      return forProcessesAndBusinessObject(processes, 
            businessObject, businessObjectPrimaryKeys, null, null);
   }
   
   /**
    * Creates a <code>BenchmarkProcessStatisticsQuery</code> where only process instances
    * should be considered which are:
    * <ul>
    *   <li>Of a type which is included in the <code>processes</code> set or of any type
    *     if this argument is an empty set or null 
    *   <li>Using a business object instance that are of the same type as 
    *     <code>businessObject</code>. <strong>Please note</strong> that this parameter
    *     is mandatory and cannot be null</li>
    *   <li>Using a business object instance which have a relation to the business
    *     object which is given by <code>businessObjectGroup</code></li>
    * </ul>
    * @param processes Set of process definitions, an empty set or null
    * @param businessObject Business object type which is used as a filter argument
    * @param businessObjectGroup Business object type which is used as a groupBy argument
    *   which means that the instances of <code>businessObject</code> must have a 
    *   relation to instances of <code>businessObjectGroup</code>
    */
   public static BenchmarkProcessStatisticsQuery forProcessesAndBusinessObject(
         Set<ProcessDefinition> processes,
         BusinessObject businessObject, BusinessObject businessObjectGroup)
   {
      return forProcessesAndBusinessObject(processes, 
            businessObject, null, businessObjectGroup, null);
   }
   
   /**
    * Creates a <code>BenchmarkProcessStatisticsQuery</code> where only process instances
    * should be considered which are:
    * <ul>
    *   <li>Of a type which is included in the <code>processes</code> set or of any type
    *     if this argument is an empty set or null 
    *   <li>Using a business object instance that are of the same type as 
    *     <code>businessObject</code>. <strong>Please note</strong> that this parameter
    *     is mandatory and cannot be null</li>
    *   <li>Using a business object instance which have a relation to the business
    *     object which is given by <code>businessObjectGroup</code></li>
    * </ul>
    * Furthermore if the set from <code>businessObjectPrimaryKeys</code> and/or
    * <code>businessObjectGroupPrimaryKeys</code> contains entries then the result set 
    * is additionally restricted to the given primary keys.
    * @param processes Set of process definitions, an empty set or null
    * @param businessObject Business object type which is used as a filter argument
    * @param businessObjectPrimaryKeys Primary key values of the business object if the
    *    business object instances should be restricted to these values or an
    *    empty set resp. null if no restrictions to the business object should be applied.
    * @param businessObjectGroup Business object type which is used as a groupBy argument
    *   which means that the instances of <code>businessObject</code> must have a 
    *   relation to instances of <code>businessObjectGroup</code>
    * @param businessObjectGroupPrimaryKeys Primary key values of the groupBy business 
    *    object if the groupBy business object instances should be restricted to these 
    *    values or an empty set resp. null if no restrictions to the groupBy business 
    *    object should be applied.
    */
   public static BenchmarkProcessStatisticsQuery forProcessesAndBusinessObject(
         Set<ProcessDefinition> processes,
         BusinessObject businessObject, Set<Serializable> businessObjectPrimaryKeys,
         BusinessObject businessObjectGroup, Set<Serializable> businessObjectGroupPrimaryKeys)
   {
      BenchmarkProcessStatisticsQuery query = forProcesses(processes);

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
   

   protected BenchmarkProcessStatisticsQuery()
   {
      super(ID);
   }

}
