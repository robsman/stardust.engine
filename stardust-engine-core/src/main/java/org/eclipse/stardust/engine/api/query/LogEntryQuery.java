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

import org.eclipse.stardust.engine.core.runtime.beans.LogEntryBean;

/**
 *
 * @author rsauer
 * @version $Revision$
 */
public class LogEntryQuery extends Query
{
   public static final Attribute OID = new Attribute(LogEntryBean.FIELD__OID);
   public static final Attribute TYPE = new Attribute(LogEntryBean.FIELD__TYPE);
   public static final Attribute CODE = new Attribute(LogEntryBean.FIELD__CODE);
   public static final Attribute SUBJECT = new Attribute(LogEntryBean.FIELD__SUBJECT);
   public static final Attribute STAMP = new Attribute(LogEntryBean.FIELD__STAMP);
   public static final Attribute PROCESS_INSTANCE_OID = new Attribute(LogEntryBean.FIELD__PROCESS_INSTANCE);
   public static final Attribute ACTIVITY_INSTANCE_OID = new Attribute(LogEntryBean.FIELD__ACTIVITY_INSTANCE);

   private static final FilterVerifier FILTER_VERIFIER = new FilterScopeVerifier(
         new WhitelistFilterVerifyer(new Class[]
         {
            FilterTerm.class,
            UnaryOperatorFilter.class,
            BinaryOperatorFilter.class,
            TernaryOperatorFilter.class,
            ProcessDefinitionFilter.class,
            ProcessInstanceFilter.class,
            ActivityFilter.class,
            ActivityInstanceFilter.class,
            CurrentPartitionFilter.class
         }),
         LogEntryQuery.class);

   /**
    * Creates a query for finding all log entries, ordering the result either by
    * descending or ascending timestamps.
    *
    * @param descending Flag indicating if found log entries will be ordered by descending
    *                   or ascending timestamps.
    *
    * @return The readily configured query.
    *
    * @see #findForProcessInstance
    * @see #orderBy(FilterableAttribute, boolean)
    * @see #STAMP
    */
   public static LogEntryQuery findAll(boolean descending)
   {
      LogEntryQuery query = new LogEntryQuery();

      query.orderBy(LogEntryQuery.STAMP, !descending);

      return query;
   }

   /**
    * Creates a query for finding a subset of all log entries.
    *
    * @param startIndex The number of log entries to be skipped.
    * @param maxSize The maximum number of log entries to be retrieved.
    *
    * @return The readily configured query.
    *
    * @see #setPolicy
    * @see SubsetPolicy
    */
   public static LogEntryQuery findAll(int startIndex, int maxSize)
   {
      LogEntryQuery query = new LogEntryQuery();

      query.setPolicy(new SubsetPolicy(maxSize, startIndex));

      return query;
   }

   public static LogEntryQuery findForProcessDefinition(String processDefinitionID)
   {
      LogEntryQuery query = new LogEntryQuery();
      query.getFilter().add(new ProcessDefinitionFilter(processDefinitionID));

      return query;
   }

   /**
    * Creates a query for finding log entries belonging to the process instance identified
    * by the given OID.
    *
    * @param processInstanceOID The OID of the process instance to find log entries for.
    * @return The readily configured query.
    *
    * @see #findForProcessInstance(long, boolean)
    */
   public static LogEntryQuery findForProcessInstance(long processInstanceOID)
   {
      LogEntryQuery query = new LogEntryQuery();
      query.getFilter().add(new ProcessInstanceFilter(processInstanceOID));

      return query;
   }

   /**
    * Creates a query for finding log entries belonging to the process instance identified
    * by the given OID, ordering the result either by descending or ascending timestamps.
    *
    * @param processInstanceOID The OID of the process instance to find log entries for.
    * @param descending Flag indicating if found log entries will be ordered by descending
    *                   or ascending timestamps.
    * @return The readily configured query.
    *
    * @see #findForProcessInstance(long)
    */
   public static LogEntryQuery findForProcessInstance(long processInstanceOID,
         boolean descending)
   {
      LogEntryQuery query = findForProcessInstance(processInstanceOID);

      query.orderBy(STAMP, !descending);

      return query;
   }

   public static LogEntryQuery findForActivity(String activityID)
   {
      LogEntryQuery query = new LogEntryQuery();
      query.getFilter().add(ActivityFilter.forAnyProcess(activityID));

      return query;
   }

   /**
    * Creates a query for finding log entries belonging to the activity instance
    * identified by the given OID.
    *
    * @param activityInstanceOID The OID of the activity instance to find log entries for.
    * @return The readily configured query.
    *
    * @see #findForActivityInstance(long, boolean)
    */
   public static LogEntryQuery findForActivityInstance(long activityInstanceOID)
   {
      LogEntryQuery query = new LogEntryQuery();
      query.getFilter().add(new ActivityInstanceFilter(activityInstanceOID));

      return query;
   }

   /**
    * Creates a query for finding log entries belonging to the activity instance
    * identified by the given OID, ordering the result either by descending or ascending
    * timestamps.
    *
    * @param activityInstanceOID The OID of the activity instance to find log entries for.
    * @param descending Flag indicating if found log entries will be ordered by descending
    *                   or ascending timestamps.
    * @return The readily configured query.
    *
    * @see #findForActivityInstance(long)
    */
   public static LogEntryQuery findForActivityInstance(long activityInstanceOID,
         boolean descending)
   {
      LogEntryQuery query = findForActivityInstance(activityInstanceOID);

      query.orderBy(STAMP, !descending);

      return query;
   }

   public LogEntryQuery()
   {
      super(FILTER_VERIFIER);
      setPolicy(new ModelVersionPolicy(false));
   }

   /**
    * Log entry attribute supporting filter operations.
    * <p />
    * Not for direct use.
    * 
    */
   public static final class Attribute extends FilterableAttributeImpl
   {
      private Attribute(String attribute)
      {
         super(LogEntryQuery.class, attribute);
      }
   }
}