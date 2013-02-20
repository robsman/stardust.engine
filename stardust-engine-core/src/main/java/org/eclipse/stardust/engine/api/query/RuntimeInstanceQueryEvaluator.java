/*******************************************************************************
 * Copyright (c) 2011, 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.api.query;

import static org.eclipse.stardust.common.CollectionUtils.isEmpty;
import static org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties.QUERY_EVALUATION_PROFILE;
import static org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties.QUERY_EVALUATION_PROFILE_CLUSTERED;
import static org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties.QUERY_EVALUATION_PROFILE_INLINED;
import static org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties.QUERY_EVALUATION_PROFILE_LEGACY;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.query.SqlBuilder.ParsedQuery;
import org.eclipse.stardust.engine.api.runtime.IDescriptorProvider;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.core.persistence.EmptyResultSetIterator;
import org.eclipse.stardust.engine.core.persistence.FetchPredicate;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.Join;
import org.eclipse.stardust.engine.core.persistence.OrderCriteria;
import org.eclipse.stardust.engine.core.persistence.OrderCriterion;
import org.eclipse.stardust.engine.core.persistence.QueryExtension;
import org.eclipse.stardust.engine.core.persistence.ResultIterator;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.persistence.jdbc.SqlUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.setup.DataCluster;
import org.eclipse.stardust.engine.core.runtime.setup.RuntimeSetup;
import org.eclipse.stardust.engine.core.runtime.utils.AbstractAuthorization2Predicate;
import org.eclipse.stardust.engine.core.runtime.utils.ActivityInstanceAuthorization2Predicate;
import org.eclipse.stardust.engine.core.runtime.utils.Authorization2Predicate;
import org.eclipse.stardust.engine.core.runtime.utils.AuthorizationContext;
import org.eclipse.stardust.vfs.impl.utils.StringUtils;


/**
 * Filter evaluator generating SQL from a process or activity instance query.
 *
 * @author rsauer
 * @version $Revision$
 *
 * @see ProcessHierarchyPreprocessor
 */
public class RuntimeInstanceQueryEvaluator implements QueryEvaluator
{
   private static final Logger trace = LogManager
         .getLogger(RuntimeInstanceQueryEvaluator.class);

   protected final Query query;
   private final Class type;
   private final EvaluationContext evaluationContext;
   private final DataCluster[] dataClusterSetup;

   protected RuntimeInstanceQueryEvaluator(Query query, Class type,
         EvaluationContext context)
   {
      this.query = query;
      this.type = type;
      this.evaluationContext = context;
      dataClusterSetup = RuntimeSetup.instance().getDataClusterSetup();
   }

   public Class getQueriedType()
   {
      return type;
   }

   public EvaluationContext getEvaluationContext()
   {
      return evaluationContext;
   }

   public ParsedQuery parseQuery()
   {
      SqlBuilder sqlBuilder = createSqlBuilder();
      return sqlBuilder.buildSql(query, type, evaluationContext);
   }

   public long executeCount()
   {
      final BpmRuntimeEnvironment runtimeEnvironment = PropertyLayerProviderInterceptor.getCurrent();
      Authorization2Predicate authorizationPredicate = runtimeEnvironment.getAuthorizationPredicate();
      if (authorizationPredicate != null)
      {
         authorizationPredicate.addPrefetchDataHints(query);
      }

      SqlBuilder.ParsedQuery parsedQuery = parseQuery();

      FetchPredicate fetchPredicate = parsedQuery.getFetchPredicate();
      if (authorizationPredicate != null)
      {
         authorizationPredicate.setFetchPredicate(fetchPredicate);
         List<FieldRef> selectExtension = parsedQuery.getSelectExtension();
         if ( !isEmpty(selectExtension))
         {
            // set relative index to end of column list of result set
            int size = -selectExtension.size();
            authorizationPredicate.setSelectionExtension(size, selectExtension);
         }
         fetchPredicate = authorizationPredicate;
      }

      QueryExtension queryExtension = QueryExtension.where(parsedQuery.getPredicateTerm());
      queryExtension.setDistinct(parsedQuery.useDistinct());
      queryExtension.setSelectAlias(parsedQuery.getSelectAlias());
      for (Iterator i = parsedQuery.getPredicateJoins().iterator(); i.hasNext();)
      {
         queryExtension.addJoin((Join) i.next());
      }

      long count = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).getCount(type,
            queryExtension, fetchPredicate, QueryUtils.getTimeOut(query));

      SubsetPolicy subset = (SubsetPolicy) query.getPolicy(SubsetPolicy.class);
      if (null != subset)
      {
         count = Math.max(0, count - subset.getSkippedEntries());
         count = Math.min(count, subset.getMaxSize());
      }

      return count;
   }

   public ResultIterator executeFetch()
   {
      final BpmRuntimeEnvironment runtimeEnvironment = PropertyLayerProviderInterceptor.getCurrent();
      Authorization2Predicate authorizationPredicate = runtimeEnvironment.getAuthorizationPredicate();
      boolean excludedUserEnabled = Parameters.instance().getBoolean(
            KernelTweakingProperties.ENGINE_EXCLUDE_USER_EVALUATION, false);
      if (authorizationPredicate != null)
      {
         authorizationPredicate.addPrefetchDataHints(query);
      }
      else if (excludedUserEnabled && query.getPolicy(ExcludeUserPolicy.class) != null
            && SecurityProperties.getUser().hasRole(
                  PredefinedConstants.ADMINISTRATOR_ROLE))
      {
         authorizationPredicate = new ActivityInstanceAuthorization2Predicate(
               AuthorizationContext.create(QueryService.class, "getAllActivityInstances",
                     ActivityInstanceQuery.class))
         {
         };

         authorizationPredicate.addPrefetchDataHints(query);
      }

      SqlBuilder.ParsedQuery parsedQuery = parseQuery();
      List<FieldRef> selectExtension = parsedQuery.getSelectExtension();
      QueryExtension queryExtension = QueryExtension.where(parsedQuery.getPredicateTerm());

      // set the custom select alias
      queryExtension.setSelectAlias(parsedQuery.getSelectAlias());
      // set some hints for the query for later usage
      DescriptorPolicy descriptorPolicy = (DescriptorPolicy) query
            .getPolicy(DescriptorPolicy.class);
      if (null == descriptorPolicy)
      {
         // set default depending on the query type
         if (ProcessInstanceBean.class.isAssignableFrom(type))
         {
            descriptorPolicy = DescriptorPolicy.NO_DESCRIPTORS;
         }
         else
         {
            descriptorPolicy = DescriptorPolicy.WITH_DESCRIPTORS;
         }
      }
      queryExtension.getHints().put(IDescriptorProvider.PRP_PROPVIDE_DESCRIPTORS,
            Boolean.valueOf(descriptorPolicy.includeDescriptors()));

      // add hint if statement has a case policy
      CasePolicy casePolicy = (CasePolicy) query.getPolicy(CasePolicy.class);
      if (casePolicy != null)
      {
         queryExtension.getHints().put(CasePolicy.class.getName(), true);
      }

      FetchPredicate fetchPredicate = parsedQuery.getFetchPredicate();
      if (authorizationPredicate != null)
      {
         authorizationPredicate.setFetchPredicate(fetchPredicate);
         if (!isEmpty(selectExtension))
         {
            List<FieldRef> selection = SqlUtils.getDefaultSelectFieldList(TypeDescriptor.get(type));
            int size = selection.size();
            selection.addAll(selectExtension);
            queryExtension.setSelection(selection.toArray(new FieldRef[size]));
            authorizationPredicate.setSelectionExtension(size + 1, selectExtension);
         }
         fetchPredicate = authorizationPredicate;
      }

      List joins = new ArrayList(parsedQuery.getPredicateJoins());
      joins.addAll(parsedQuery.getOrderByJoins());
      for (Iterator i = joins.iterator(); i.hasNext();)
      {
         queryExtension.addJoin((Join) i.next());
      }

      SubsetPolicy subset = (SubsetPolicy) query.getPolicy(SubsetPolicy.class);
      if (null == subset)
      {
         subset = SubsetPolicy.UNRESTRICTED;
      }

      boolean countAll = subset.isEvaluatingTotalCount();

      if (countAll && (null == parsedQuery.getFetchPredicate()))
      {
         boolean countImplicitly = SubsetPolicy.UNRESTRICTED.getMaxSize() == subset
               .getMaxSize();

         applyDistinctOnQueryExtension(queryExtension, parsedQuery);

         queryExtension.setOrderCriteria(parsedQuery.getOrderCriteria());
         ResultIterator result;
         if (0 == (subset.getSkippedEntries() + subset.getMaxSize()))
         {
            // this is a count only query, not asking for any real items
            countImplicitly = false;
            result = EmptyResultSetIterator.INSTANCE;
         }
         else
         {
            result = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL)
               .getIterator(type, queryExtension, subset.getSkippedEntries(),
                     subset.getMaxSize(), fetchPredicate,
                     countImplicitly, QueryUtils.getTimeOut(query));
         }

         // optionally issue explicit count call to avoid fetching whole record set
         // use distinct evaluated by DB and not by engine as no ordering is applied.
         queryExtension.setDistinct(parsedQuery.useDistinct());
         queryExtension.setEngineDistinct(false);
         // set empty order criteria
         queryExtension.setOrderCriteria(new org.eclipse.stardust.engine.core.persistence.OrderCriteria());

         if ( !countImplicitly && fetchPredicate instanceof Authorization2Predicate)
         {
            Authorization2Predicate authPred = (Authorization2Predicate) fetchPredicate;
            // set relative index to end of column list of result set
            int size = isEmpty(selectExtension) ? 0 : -selectExtension.size();
            authPred.setSelectionExtension(size, selectExtension);
         }

         final long totalCount = countImplicitly
               ? result.getTotalCount()
               : getExplicitTotalCount(queryExtension, fetchPredicate, casePolicy != null);

         return new TotalCountDecorator(totalCount, result);
      }
      else
      {
         applyDistinctOnQueryExtension(queryExtension, parsedQuery);

         queryExtension.setOrderCriteria(parsedQuery.getOrderCriteria());

         return SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).getIterator(type,
               queryExtension, subset.getSkippedEntries(),
               subset.getMaxSize(), fetchPredicate, countAll,
               QueryUtils.getTimeOut(query));
      }
   }

   private long getExplicitTotalCount(QueryExtension queryExtension,
         FetchPredicate fetchPredicate, boolean useCasePolicy)
   {
      if (useCasePolicy || hasDataPrefetchHintFilter(fetchPredicate))
      {
         return Long.MAX_VALUE;
      }
      else
      {
         return SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).getCount(type,
               queryExtension, fetchPredicate, QueryUtils.getTimeOut(query));
      }
   }

   private boolean hasDataPrefetchHintFilter(FetchPredicate fetchPredicate)
   {
      boolean hasDataPrefetchHints = false;
      if ((fetchPredicate instanceof AbstractAuthorization2Predicate)
            && ((AbstractAuthorization2Predicate) fetchPredicate)
                  .hasDataPrefetchHintFilter())
      {
         hasDataPrefetchHints = true;
      }
      return hasDataPrefetchHints;
   }

   private static void applyDistinctOnQueryExtension(QueryExtension queryExtension,
         SqlBuilder.ParsedQuery parsedQuery)
   {
      // custom select alias needs distinct else the total count is calculated incorrectly.
      if (includesOrderOnJoinedTable(parsedQuery.getOrderCriteria()) && StringUtils.isEmpty(queryExtension.getSelectAlias()))
      {
         queryExtension.setDistinct(false);
         queryExtension.setEngineDistinct(parsedQuery.useDistinct());
      }
      else
         {
         queryExtension.setDistinct(parsedQuery.useDistinct());
         queryExtension.setEngineDistinct(false);
         }
   }

   private static boolean includesOrderOnJoinedTable(OrderCriteria criteria)
         {
      for (OrderCriterion criterion : criteria)
            {
         if (criterion.getFieldRef().getType() instanceof Join)
               {
                  return true;
               }
            }

      return false;
   }

   /**
    * Factory method allowing for varying SQL building strategies.
    *
    * @return A SqlBuilder instance according to the selected SQL building strategy
    */
   private SqlBuilder createSqlBuilder()
   {
      String profile = getEvaluationProfile(query);

      final SqlBuilder sqlBuilder;

      if (QUERY_EVALUATION_PROFILE_INLINED.equals(profile))
      {
         sqlBuilder = new InlinedDataFilterSqlBuilder();
      }
      else if (QUERY_EVALUATION_PROFILE_CLUSTERED.equals(profile))
      {
         sqlBuilder = new ClusterAwareInlinedDataFilterSqlBuilder();
      }
      else if (QUERY_EVALUATION_PROFILE_LEGACY.equals(profile))
      {
         sqlBuilder = new LegacySqlBuilder();
      }
      else
      {
         sqlBuilder = new LegacySqlBuilder();
         if (trace.isDebugEnabled())
         {
            trace.debug("Unknow evaluation profile '" + profile
                  + "'. Using profile '" + QUERY_EVALUATION_PROFILE_LEGACY
                  + "' as this supports DataFilter on big data.");
         }
      }

      return sqlBuilder;
   }

   /**
    * Method evaluating the query evaluation profile to be used by reading the
    * requested profile from properties and analyzing the query.
    *
    * @return Name of the query evaluation profile.
    */
   public static String getEvaluationProfile(Query query)
   {
      String profile = null;

      // TODO allow inlined/cluster strategy for BIG_ data filters
      boolean bigDataFilter = (Boolean) query.getFilter().accept(
            new BigDataFilterFinder(), null);

      // read requested profile
      String requestedProfile = Parameters.instance().getString(QUERY_EVALUATION_PROFILE);

      if (QUERY_EVALUATION_PROFILE_INLINED.equals(requestedProfile)
            || QUERY_EVALUATION_PROFILE_CLUSTERED.equals(requestedProfile))
      {
         if ( !bigDataFilter)
         {
            profile = requestedProfile;
         }
         else
         {
            if (trace.isDebugEnabled())
            {
               trace.debug("Not using query evaluation profile '"
                     + QUERY_EVALUATION_PROFILE_INLINED + "' or '"
                     + QUERY_EVALUATION_PROFILE_CLUSTERED
                     + "' as of DataFilter involving big data.");
            }
         }
      }
      else if (QUERY_EVALUATION_PROFILE_LEGACY.equals(requestedProfile))
      {
         profile = QUERY_EVALUATION_PROFILE_LEGACY;
      }

      if (null == profile)
      {
         if (bigDataFilter)
         {
            // as soon as InlinedDataFilterSqlBuilder can handle big data filters,
            // using LegacySqlBuilder as default can be removed.
            profile = QUERY_EVALUATION_PROFILE_LEGACY;
            if (trace.isDebugEnabled())
            {
               trace.debug("Using evaluation profile '" + QUERY_EVALUATION_PROFILE_LEGACY
                     + "' as of DataFilter involving big data.");
            }
         }
         else
         {
            profile = QUERY_EVALUATION_PROFILE_INLINED;
         }
      }

      return profile;
   }
}