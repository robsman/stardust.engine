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
import java.io.Serializable;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.persistence.*;
import org.eclipse.stardust.engine.core.persistence.Operator.Binary;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.DataFilterExtension;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.DataFilterExtensionContext;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.SpiUtils;

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
   protected static final String FIELD_GLUE_ROOT_PROCESS_INSTANCE = ProcessInstanceScopeBean.FIELD__ROOT_PROCESS_INSTANCE;

   /**
    * Has to be the same value as
    * {@link ProcessInstanceBean#FIELD__SCOPE_PROCESS_INSTANCE} and
    * {@link ProcessInstanceScopeBean#FIELD__SCOPE_PROCESS_INSTANCE}
    */
   protected static final String FIELD_GLUE_SCOPE_PROCESS_INSTANCE = ProcessInstanceScopeBean.FIELD__SCOPE_PROCESS_INSTANCE;

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

      Pair joinKey = new Pair(Integer.valueOf(filter.getFilterMode()),
            new DataAttributeKey(filter));
      Join dvJoin = (Join) dataJoinMapping.get(joinKey);

      // collect qualifying data OIDs
      Map<Long, IData> dataMap = findAllDataRtOids(filter.getDataID(), context
            .getEvaluationContext().getModelManager());
      DataFilterExtension dataFilterExtension = SpiUtils
            .createDataFilterExtension(dataMap);

      final DataFilterExtensionContext dataFilterExtensionContext = context
            .getDataFilterExtensionContext();
      if (null == dvJoin || isNotAnyOfFilter)
      {
         // first use of this specific dataID, setup join
         // a dummy queryDescriptor needed here
         QueryDescriptor queryDescriptor = QueryDescriptor
               .from(ProcessInstanceBean.class).select(ProcessInstanceBean.FIELD__OID,
                     ProcessInstanceBean.FIELD__SCOPE_PROCESS_INSTANCE);

         JoinFactory joinFactory = new JoinFactory(context);

         dvJoin = dataFilterExtension.createDvJoin(queryDescriptor, filter,
               dataJoinMapping.size() + 1, dataFilterExtensionContext,
               isFilterUsedInAndTerm, joinFactory);

         // use INNER JOIN if all predicates are true
         // otherwise use LEFT OUTER JOIN
         // @formatter:off
         dvJoin.setRequired(isFilterUsedInAndTerm && !isPrefetchHint && !isIsNullFilter
               && !isNotAnyOfFilter);

         // TODO: check if useDistinct has to be always set for isNotAnyOfFilter
         final boolean useDistinct = isNotAnyOfFilter
               || dataFilterExtensionContext.useDistinct();
         if (useDistinct)
         {
            context.useDistinct(useDistinct);
         }

         if (isNotAnyOfFilter)
         {
            // this shall never match with other key - use the join itself as key
            dataJoinMapping.put(dvJoin, dvJoin);
         }
         else
         {
            dataJoinMapping.put(joinKey, dvJoin);
         }

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
         if (isFilterUsedInAndTerm && !isPrefetchHint && !isIsNullFilter
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
         PredicateTerm predicateTerm = dataFilterExtension.createPredicateTerm(dvJoin,
               filter, dataMap, dataFilterExtensionContext);

         return predicateTerm;
      }
   }

   public Object visit(DescriptorFilter filter, Object rawContext)
   {
      VisitationContext context = (VisitationContext) rawContext;
      if (DataValueBean.isLargeValue(filter.getOperand()))
      {
         throw new InternalException(
               "Inlined data filter evaluation is not supported for big data values.");
      }

      String descriptorID = filter.getDescriptorID();
      OrTerm orTerm = new OrTerm();
      List<AbstractDataFilter> dataFilters = CollectionUtils.newList();
      if (filter.isCaseDescriptor())
      {
         if (PredefinedConstants.CASE_NAME_ELEMENT.equals(descriptorID))
         {
            dataFilters.add(new DataFilter(PredefinedConstants.QUALIFIED_CASE_DATA_ID,
                  PredefinedConstants.CASE_NAME_ELEMENT, (Binary) filter.getOperator(),
                  filter.getOperand(), filter.getFilterMode()));

         }
         else if (PredefinedConstants.CASE_DESCRIPTION_ELEMENT.equals(descriptorID))
         {
            dataFilters.add(new DataFilter(PredefinedConstants.QUALIFIED_CASE_DATA_ID,
                  PredefinedConstants.CASE_DESCRIPTION_ELEMENT, (Binary) filter
                        .getOperator(), filter.getOperand(), filter.getFilterMode()));
         }
         else
         {
            dataFilters.add(new DataFilter(PredefinedConstants.QUALIFIED_CASE_DATA_ID,
                  PredefinedConstants.CASE_DESCRIPTOR_VALUE_XPATH, (Binary) filter
                        .getOperator(), '{' + descriptorID + '}' + filter.getOperand(),
                  filter.getFilterMode()));
         }
      }
      else
      {
         Map<String, String> dataAccessPath = SqlBuilderBase
               .getDescriptorDataAccessPathMap(descriptorID, context
                     .getEvaluationContext().getModelManager());
         for (Map.Entry<String, String> entry : dataAccessPath.entrySet())
         {
            String dataID = entry.getKey();
            String attributeName = entry.getValue();
            if (filter.getOperator() instanceof Binary)
            {
               dataFilters.add(new DataFilter(dataID, attributeName,
                     (Operator.Binary) filter.getOperator(), filter.getOperand(), filter
                           .getFilterMode()));
            }
            else
            {
               dataFilters.add(new DataFilter(dataID, attributeName,
                     (Operator.Ternary) filter.getOperator(),
                     (Serializable) ((Pair) filter.getOperand()).getFirst(),
                     (Serializable) ((Pair) filter.getOperand()).getSecond(), filter
                           .getFilterMode()));
            }
         }
      }
      DataFilterExtensionContext ctx = new DataFilterExtensionContext(dataFilters);
      DataFilterExtensionContext dataFilterExtensionContext = context
            .getDataFilterExtensionContext();
      dataFilterExtensionContext.setContent(null);
      Map<String, List<AbstractDataFilter>> dataFiltersByDataId = ctx
            .getDataFiltersByDataId();
      for (Map.Entry<String, List<AbstractDataFilter>> entry : dataFiltersByDataId
            .entrySet())
      {
         dataFilterExtensionContext.getDataFiltersByDataId().put(entry.getKey(),
               entry.getValue());
      }
      for (AbstractDataFilter dataFilter : dataFilters)
      {
         if (context != null)
         {
            context.pushFilterKind(FilterTerm.OR);
         }
         PredicateTerm predicate = (PredicateTerm) visit(dataFilter, rawContext);
         orTerm.add(predicate);
      }
      return orTerm;
   }

   protected class JoinFactory implements IJoinFactory
   {
      private final VisitationContext context;

      protected final boolean isProcInstQuery;

      protected final boolean isAiQueryUsingWorkItem;

      public JoinFactory(VisitationContext context)
      {
         this.context = context;
         this.isProcInstQuery = ProcessInstanceBean.class.isAssignableFrom(context
               .getType());
         this.isAiQueryUsingWorkItem = WorkItemBean.class.isAssignableFrom(context
               .getType());
      }

      public Join createDataFilterJoins(int dataFilterMode, int idx, Class dvClass,
            FieldRef dvProcessInstanceField)
      {
         final Join dvJoin;

         final String pisAlias;
         final String pihAlias = "PR_PIH" + idx;
         final String dvAlias = "PR_" + dvProcessInstanceField.getType().getTableAlias()
               + idx;

         if (AbstractDataFilter.MODE_ALL_FROM_SCOPE == dataFilterMode)
         {
            dvJoin = new Join(dvClass, dvAlias)//
                  .on(getScopePiFieldRef(), dvProcessInstanceField.fieldName);

            Join scopePiGlueJoin = getGlueJoin();
            if (null != scopePiGlueJoin)
            {
               dvJoin.setDependency(scopePiGlueJoin);
            }
         }
         else if (AbstractDataFilter.MODE_SUBPROCESSES == dataFilterMode)
         {
            // TODO (peekaboo): Improve detection whether distinct is needed.
            // incSubProcModeCounter();
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

            dataJoinMapping.put(
                  new Pair(Integer.valueOf(AbstractDataFilter.MODE_SUBPROCESSES),
                        pihAlias), hierJoin);

            dvJoin = new Join(dvClass, dvAlias)//
                  .on(hierJoin
                        .fieldRef(ProcessInstanceHierarchyBean.FIELD__PROCESS_INSTANCE),
                        dvProcessInstanceField.fieldName);

            dvJoin.setDependency(hierJoin);
         }
         else if (AbstractDataFilter.MODE_ALL_FROM_HIERARCHY == dataFilterMode)
         {
            // TODO (peekaboo): Improve detection whether distinct is needed.
            // incAllFromHierModeCounter();
            context.useDistinct(true);

            Join pisJoin;
            pisAlias = "PR_PIS" + idx;

            pisJoin = new Join(ProcessInstanceScopeBean.class, pisAlias)//
                  .on(getRootPiFieldRef(),
                        ProcessInstanceScopeBean.FIELD__ROOT_PROCESS_INSTANCE);

            Join rootPiGlueJoin = getGlueJoin();
            if (null != rootPiGlueJoin)
            {
               pisJoin.setDependency(rootPiGlueJoin);
            }

            dataJoinMapping.put(
                  new Pair(Integer.valueOf(AbstractDataFilter.MODE_ALL_FROM_HIERARCHY),
                        pisAlias), pisJoin);

            dvJoin = new Join(dvClass, dvAlias)
                  //
                  .on(pisJoin
                        .fieldRef(ProcessInstanceScopeBean.FIELD__SCOPE_PROCESS_INSTANCE),
                        dvProcessInstanceField.fieldName);

            dvJoin.setDependency(pisJoin);
         }
         else
         {
            throw new InternalException(MessageFormat.format(
                  "Invalid DataFilter mode: {0}.",
                  new Object[] {Integer.valueOf(dataFilterMode)}));
         }

         return dvJoin;
      }

      protected FieldRef getScopePiFieldRef()
      {
         if (isProcInstQuery)
         {
            return ProcessInstanceBean.FR__SCOPE_PROCESS_INSTANCE;
         }
         else if (isAiQueryUsingWorkItem)
         {
            return WorkItemBean.FR__SCOPE_PROCESS_INSTANCE;
         }
         else
         {
            Join scopePiGlueJoin = getGlueJoin();
            return scopePiGlueJoin.fieldRef(FIELD_GLUE_SCOPE_PROCESS_INSTANCE);
         }
      }

      protected FieldRef getRootPiFieldRef()
      {
         if (isProcInstQuery)
         {
            return ProcessInstanceBean.FR__ROOT_PROCESS_INSTANCE;
         }
         else if (isAiQueryUsingWorkItem)
         {
            return WorkItemBean.FR__ROOT_PROCESS_INSTANCE;
         }
         else
         {
            Join scopePiGlueJoin = getGlueJoin();
            return scopePiGlueJoin.fieldRef(FIELD_GLUE_ROOT_PROCESS_INSTANCE);
         }
      }

      protected Join getGlueJoin()
      {
         if (isProcInstQuery || isAiQueryUsingWorkItem)
         {
            return null;
         }
         else if (null == glueJoin)
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
