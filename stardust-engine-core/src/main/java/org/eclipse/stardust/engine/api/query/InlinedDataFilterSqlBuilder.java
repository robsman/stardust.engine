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

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.core.persistence.*;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.DataValueBean;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceHierarchyBean;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceScopeBean;
import org.eclipse.stardust.engine.core.runtime.beans.WorkItemBean;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.DataFilterExtension;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.DataFilterExtensionContext;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.SpiUtils;
import org.eclipse.stardust.engine.core.struct.beans.StructuredDataValueBean;


/**
 * @author rsauer
 * @version $Revision$
 */
public class InlinedDataFilterSqlBuilder extends SqlBuilderBase
{
   /**
    * Has to be the same value as {@link ProcessInstanceBean#FIELD__ROOT_PROCESS_INSTANCE}
    * and {@link ProcessInstanceScopeBean#FIELD__ROOT_PROCESS_INSTANCE}
    */
   public static final String FIELD_GLUE_ROOT_PROCESS_INSTANCE = ProcessInstanceScopeBean.FIELD__ROOT_PROCESS_INSTANCE;

   /**
    * Has to be the same value as {@link ProcessInstanceBean#FIELD__SCOPE_PROCESS_INSTANCE}
    * and {@link ProcessInstanceScopeBean#FIELD__SCOPE_PROCESS_INSTANCE}
    */
   public static final String FIELD_GLUE_SCOPE_PROCESS_INSTANCE = ProcessInstanceScopeBean.FIELD__SCOPE_PROCESS_INSTANCE;

   protected ProcessHierarchyPreprocessor createQueryPreprocessor()
   {
      return new ProcessHierarchyPreprocessor();
   }

   public Object visit(AbstractDataFilter filter, Object rawContext)
   {
      VisitationContext context = (VisitationContext) rawContext;

      if (DataValueBean.isLargeValue(filter.getOperand()))
      {
         throw new InternalException(
               "Inlined data filter evaluation is not supported for big data values.");
      }

      final boolean isPrefetchHint = filter instanceof DataPrefetchHint;
      final boolean isFilterUsedInAndTerm = isAndTerm(context);
      final boolean isIsNullFilter = isIsNullFilter(filter);
      final boolean isNotAnyOfFilter = Operator.NOT_ANY_OF.equals(filter.getOperator());

      // join data_value table at most once for every dataID involved with the query, this
      // join will eventually be reused by successive DataFilters targeting the same
      // dataID (especially needed for ORed predicate to prevent combinatorial explosion)

      Pair joinKey = new Pair(Integer.valueOf(filter.getFilterMode()), new DataAttributeKey(filter));
      Join dvJoin = (Join) dataJoinMapping.get(joinKey);

      // collect qualifying data OIDs
      Map<Long, IData> dataMap = findAllDataRtOids(filter.getDataID(), context
            .getEvaluationContext().getModelManager());
      DataFilterExtension dataFilterExtension = SpiUtils.createDataFilterExtension(
            dataMap);

      final DataFilterExtensionContext dataFilterExtensionContext = context.getDataFilterExtensionContext();
      if (null == dvJoin)
      {
         // first use of this specific dataID, setup join
         // a dummy queryDescriptor needed here
         QueryDescriptor queryDescriptor = QueryDescriptor.from(ProcessInstanceBean.class)
               .select(ProcessInstanceBean.FIELD__OID,
                     ProcessInstanceBean.FIELD__SCOPE_PROCESS_INSTANCE);

         JoinFactory joinFactory = new JoinFactory(context);
         dvJoin = dataFilterExtension.createDvJoin(queryDescriptor, filter,
               dataJoinMapping.size() + 1, dataFilterExtensionContext,
               isFilterUsedInAndTerm, joinFactory);

         // use INNER JOIN if all predicates are true
         // otherwise use LEFT OUTER JOIN
         // @formatter:off
         dvJoin.setRequired(isFilterUsedInAndTerm
               && !isPrefetchHint
               && !isIsNullFilter
               && !isNotAnyOfFilter);

         // TODO: check if useDistinct has to be always set for isNotAnyOfFilter
         final boolean useDistinct = isNotAnyOfFilter || dataFilterExtensionContext.useDistinct();
         if (useDistinct)
         {
            context.useDistinct(useDistinct);
         }
         dataJoinMapping.put(joinKey, dvJoin);

         AndTerm andTerm = new AndTerm();
         dataFilterExtension.appendDataIdTerm(andTerm, dataMap, dvJoin, filter);
         if (andTerm.getParts().size() != 0)
         {
            dvJoin.where(andTerm);
         }
      }
      else
      {
         // if join already exists and all predicates are true
         // then force join to be an INNER JOIN
         // otherwise leave it as it is
         // @formatter:off
         if (isFilterUsedInAndTerm
               && !isPrefetchHint
               && !isIsNullFilter
               && !isNotAnyOfFilter)
         {
            dvJoin.setRequired(true);
         }
      }

      if (isPrefetchHint)
      {
         final List<FieldRef> selectExtension = context.getSelectExtension();
         selectExtension.addAll(dataFilterExtension.getPrefetchSelectExtension(dvJoin));

         return NOTHING;
      }
      else
      {
         PredicateTerm predicateTerm = dataFilterExtension.createPredicateTerm(dvJoin, filter, dataMap,
               dataFilterExtensionContext);
         if (isNotAnyOfFilter)
         {
            // Only add predicate once if join has been added and is not reused
            predicateTerm = null;
            MultiPartPredicateTerm collector = getTopLevelCollectorForNotAnyOf(dvJoin, isFilterUsedInAndTerm);
            if(collector == null || collector.getParts().size() <= 1)
            {
               // Currently only SDT is supported - hard coding of column is possible
               predicateTerm = Predicates.isNull(dvJoin.fieldRef(StructuredDataValueBean.FIELD__PROCESS_INSTANCE));
            }
         }

         return predicateTerm;
      }
   }

   // TODO (peekaboo): Refactor this and other classes with same name and semantic into common (base) class
   private class JoinFactory implements IJoinFactory
         {
      private final VisitationContext context;
      private final boolean isProcInstQuery;
      private final boolean isAiQueryUsingWorkItem;

      public JoinFactory(VisitationContext context)
      {
         this.context = context;
         this.isProcInstQuery = ProcessInstanceBean.class.isAssignableFrom(context
               .getType());
         this.isAiQueryUsingWorkItem = WorkItemBean.class.isAssignableFrom(context
               .getType());
      }

      public Join createDataFilterJoins(int dataFilterMode, int idx, Class dvClass, FieldRef dvProcessInstanceField)
      {
         final Join dvJoin;

         final String pisAlias;
         final String pihAlias = "PR_PIH" + idx;
         final String dvAlias = "PR_"+ dvProcessInstanceField.getType().getTableAlias() + idx;

         if (AbstractDataFilter.MODE_ALL_FROM_SCOPE == dataFilterMode)
         {
            if (isProcInstQuery)
            {
               dvJoin = new Join(dvClass, dvAlias)//
                     .on(ProcessInstanceBean.FR__SCOPE_PROCESS_INSTANCE,
                           dvProcessInstanceField.fieldName);
            }
            else
            {
               Join glue = getGlueJoin();

               dvJoin = new Join(dvClass, dvAlias)//
                     .on(glue.fieldRef(FIELD_GLUE_SCOPE_PROCESS_INSTANCE),
                           dvProcessInstanceField.fieldName);

               dvJoin.setDependency(glue);
            }
         }
         else if (AbstractDataFilter.MODE_SUBPROCESSES == dataFilterMode)
         {
            // TODO (peekaboo): Improve detection wether distinct is needed.
            //incSubProcModeCounter();
            context.useDistinct(true);

            FieldRef lhsFieldRef;
            if (isProcInstQuery)
            {
               lhsFieldRef = ProcessInstanceBean.FR__OID;
            }
            else if (isAiQueryUsingWorkItem)
            {
               lhsFieldRef = WorkItemBean.FR__PROCESS_INSTANCE;
            }
            else
            {
               lhsFieldRef = ActivityInstanceBean.FR__PROCESS_INSTANCE;
            }

            Join hierJoin = new Join(ProcessInstanceHierarchyBean.class, pihAlias)//
                  .on(lhsFieldRef,
                        ProcessInstanceHierarchyBean.FIELD__SUB_PROCESS_INSTANCE);

            dataJoinMapping.put(new Pair(
                  Integer.valueOf(AbstractDataFilter.MODE_SUBPROCESSES), pihAlias), hierJoin);

            dvJoin = new Join(dvClass, dvAlias)//
                  .on(hierJoin.fieldRef(ProcessInstanceHierarchyBean.FIELD__PROCESS_INSTANCE),
                        dvProcessInstanceField.fieldName);

            dvJoin.setDependency(hierJoin);
         }
         else if (AbstractDataFilter.MODE_ALL_FROM_HIERARCHY == dataFilterMode)
         {
            // TODO (peekaboo): Improve detection wether distinct is needed.
            //incAllFromHierModeCounter();
            context.useDistinct(true);

            Join pisJoin;
            pisAlias = "PR_PIS" + idx;
            if (isProcInstQuery)
            {
               pisJoin = new Join(ProcessInstanceScopeBean.class, pisAlias)//
                     .on(ProcessInstanceBean.FR__ROOT_PROCESS_INSTANCE,
                           ProcessInstanceScopeBean.FIELD__ROOT_PROCESS_INSTANCE);
            }
            else
            {
               Join glue = getGlueJoin();
               pisJoin = new Join(ProcessInstanceScopeBean.class, pisAlias)//
                     .on(glue.fieldRef(FIELD_GLUE_ROOT_PROCESS_INSTANCE),
                           ProcessInstanceScopeBean.FIELD__ROOT_PROCESS_INSTANCE);

               pisJoin.setDependency(glue);
            }

            dataJoinMapping.put(new Pair(Integer.valueOf(
                  AbstractDataFilter.MODE_ALL_FROM_HIERARCHY), pisAlias), pisJoin);

            dvJoin = new Join(dvClass, dvAlias)//
                  .on(pisJoin.fieldRef(ProcessInstanceScopeBean.FIELD__SCOPE_PROCESS_INSTANCE),
                        dvProcessInstanceField.fieldName);

            dvJoin.setDependency(pisJoin);
         }
         else
         {
            throw new InternalException(MessageFormat.format(
                  "Invalid DataFilter mode: {0}.", new Object[] { Integer.valueOf(dataFilterMode) }));
         }

         return dvJoin;
      }

      private Join getGlueJoin()
      {
         if (null == glueJoin)
         {
            glueJoin = (Join) context.getPredicateJoins().get(ProcessInstanceBean.class);

            if (null == glueJoin)
            {
               FieldRef frProcessInstance = ActivityInstanceBean.FR__PROCESS_INSTANCE;
               if (isAiQueryUsingWorkItem)
               {
                  frProcessInstance = WorkItemBean.FR__PROCESS_INSTANCE;
               }

               glueJoin = new Join(ProcessInstanceScopeBean.class, "PR_PIS")//
                     .on(frProcessInstance,
                           ProcessInstanceScopeBean.FIELD__PROCESS_INSTANCE);

               dataJoinMapping.put(ProcessInstanceScopeBean.class, glueJoin);
            }
         }

         return glueJoin;
      }

   }
}
