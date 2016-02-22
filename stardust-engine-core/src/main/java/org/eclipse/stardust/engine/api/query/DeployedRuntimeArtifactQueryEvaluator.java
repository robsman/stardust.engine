/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    roland.stamm (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.api.query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.engine.api.query.SqlBuilderBase.VisitationContext;
import org.eclipse.stardust.engine.core.persistence.*;
import org.eclipse.stardust.engine.core.persistence.Functions.BoundFunction;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor;
import org.eclipse.stardust.engine.core.runtime.beans.DetailsFactory;
import org.eclipse.stardust.engine.core.runtime.beans.RuntimeArtifactBean;

public class DeployedRuntimeArtifactQueryEvaluator
{

   public static <I, T extends I> RawQueryResult<I> evaluateActive(Query query,
         Class beanClass, Class interfaceClass, Class<T> detailsClass,
         EvaluationContext context)
   {
      QueryUtils.addCurrentPartitionFilter(query, beanClass);

      GenericQueryEvaluator evaluator = new GenericQueryEvaluator(query, beanClass,
            context);

      Map predicateJoins = new HashMap();
      VisitationContext visitationContext = new VisitationContext(query, beanClass,
            context, null);
      predicateJoins.put(VisitationContext.class, visitationContext);
      PredicateTerm whereTerm = evaluator.buildPredicate(predicateJoins);
      predicateJoins.remove(VisitationContext.class);

      SubsetPolicy subset = QueryUtils.getSubset(query);

      final boolean countAll = subset.isEvaluatingTotalCount();

      final boolean countImplicitly = countAll
            && SubsetPolicy.UNRESTRICTED.getMaxSize() == subset.getMaxSize();

      QueryExtension queryExtension = new QueryExtension();

      QueryDescriptor validFromSubQuery = QueryDescriptor.from(RuntimeArtifactBean.class, "ra_vf");

      BoundFunction maxValidFromFunction = Functions.constantExpression("MAX("
            + validFromSubQuery.getAlias() + "." + RuntimeArtifactBean.FIELD__VALID_FROM + ") AS MaxDate");

      validFromSubQuery.select(RuntimeArtifactBean.FR__ARTIFACT_TYPE_ID,
            RuntimeArtifactBean.FR__ARTIFACT_ID,
            maxValidFromFunction);

      validFromSubQuery.groupBy(RuntimeArtifactBean.FR__ARTIFACT_TYPE_ID,
            RuntimeArtifactBean.FR__ARTIFACT_ID);

      validFromSubQuery.where(whereTerm);

      Join validFromJoin = new Join(validFromSubQuery, "ra_vf");

      validFromJoin.on(RuntimeArtifactBean.FR__ARTIFACT_TYPE_ID, RuntimeArtifactBean.FIELD__ARTIFACT_TYPE_ID);
      validFromJoin.on(RuntimeArtifactBean.FR__ARTIFACT_ID, RuntimeArtifactBean.FIELD__ARTIFACT_ID);
      validFromJoin.on(RuntimeArtifactBean.FR__VALID_FROM, "MaxDate");

      queryExtension.addJoin(validFromJoin);

      QueryDescriptor maxOidSubQuery = QueryDescriptor.from(RuntimeArtifactBean.class, "ra_mo");

      BoundFunction maxOidFunction = Functions.constantExpression("MAX("
            + maxOidSubQuery.getAlias() + "." + RuntimeArtifactBean.FIELD__OID + ") AS MaxOid");
      maxOidSubQuery.select(RuntimeArtifactBean.FR__ARTIFACT_TYPE_ID,
            RuntimeArtifactBean.FR__ARTIFACT_ID, RuntimeArtifactBean.FR__VALID_FROM,
            maxOidFunction);

      maxOidSubQuery.groupBy(RuntimeArtifactBean.FR__ARTIFACT_TYPE_ID,
            RuntimeArtifactBean.FR__ARTIFACT_ID, RuntimeArtifactBean.FR__VALID_FROM);

      Join maxOidJoin = new Join(maxOidSubQuery, "ra_mo");

      maxOidJoin.on(validFromJoin.fieldRef(RuntimeArtifactBean.FIELD__ARTIFACT_TYPE_ID),
            RuntimeArtifactBean.FIELD__ARTIFACT_TYPE_ID);
      maxOidJoin.on(validFromJoin.fieldRef(RuntimeArtifactBean.FIELD__ARTIFACT_ID),
            RuntimeArtifactBean.FIELD__ARTIFACT_ID);
      maxOidJoin.on(validFromJoin.fieldRef("MaxDate"),
            RuntimeArtifactBean.FIELD__VALID_FROM);

      maxOidJoin.setDependency(validFromJoin);
      queryExtension.addJoin(maxOidJoin);

      queryExtension.setWhere(Predicates.andTerm(
            Predicates.isEqual(RuntimeArtifactBean.FR__VALID_FROM,
                  validFromJoin.fieldRef("MaxDate")),
            Predicates.isEqual(RuntimeArtifactBean.FR__OID,
                  maxOidJoin.fieldRef("MaxOid"))));

      TypeDescriptor type = TypeDescriptor.get(beanClass);
      OrderByClauseBuilder orderEvaluator = new OrderByClauseBuilder(type.getType(),
            context);
      orderEvaluator.evaluateOrder(query);
      queryExtension.setOrderCriteria(orderEvaluator.getOrderCriteria());

      Session session = (Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);

      final ResultIterator result = session.getIterator(beanClass, queryExtension,
            subset.getSkippedEntries(), subset.getMaxSize(), null, countImplicitly,
            QueryUtils.getTimeOut(query));

      /*
       * Without <I,T> antit would result in an incompatible types error The strange thing
       * is that it is compiling in eclipse without any explicit declerations
       */
      List<I> details = DetailsFactory.<I, T> createCollection(result, interfaceClass,
            detailsClass);

      queryExtension
            .setOrderCriteria(new org.eclipse.stardust.engine.core.persistence.OrderCriteria());

      // optionally issue explicit count call to avoid fetching whole record set
      final long totalCount = countImplicitly
            ? result.getTotalCount()
            : session.getCount(beanClass, queryExtension, null,
                  QueryUtils.getTimeOut(query));
      RawQueryResult<I> queryResult = new RawQueryResult<I>(details, subset,
            result.hasMore(), countAll ? totalCount : null);
      return queryResult;
   }

}
