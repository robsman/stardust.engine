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

import java.util.List;

import org.eclipse.stardust.engine.core.persistence.FetchPredicate;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.Join;
import org.eclipse.stardust.engine.core.persistence.OrderCriteria;
import org.eclipse.stardust.engine.core.persistence.PredicateTerm;


/**
 * @author rsauer
 * @version $Revision$
 */
public interface SqlBuilder
{
   ParsedQuery buildSql(Query query, Class type, EvaluationContext evaluationContext);

   public class ParsedQuery
   {
      private final List<FieldRef> selectExtension;

      private final PredicateTerm predicateTerm;
      private final List<Join> predicateJoins;

      private final OrderCriteria orderCriteria;
      private final List<Join> orderByJoins;

      private final FetchPredicate fetchPredicate;

      private final boolean useDistinct;

      private final String selectAlias;

      public ParsedQuery(List<FieldRef> selectExtension, PredicateTerm predicateTerm,
            List<Join> predicateJoins, OrderCriteria orderCriteria, List<Join> orderByJoins,
            FetchPredicate fetchPredicate, boolean useDistinct, String selectAlias)
      {
         this.selectExtension = selectExtension;

         this.predicateTerm = predicateTerm;
         this.predicateJoins = predicateJoins;

         this.orderCriteria = orderCriteria;
         this.orderByJoins = orderByJoins;

         this.fetchPredicate = fetchPredicate;

         this.useDistinct = useDistinct;
         this.selectAlias = selectAlias;
      }

      public List<FieldRef> getSelectExtension()
      {
         return selectExtension;
      }

      public PredicateTerm getPredicateTerm()
      {
         return predicateTerm;
      }

      public List<Join> getPredicateJoins()
      {
         return predicateJoins;
      }

      public OrderCriteria getOrderCriteria()
      {
         return orderCriteria;
      }

      public List<Join> getOrderByJoins()
      {
         return orderByJoins;
      }

      public FetchPredicate getFetchPredicate()
      {
         return fetchPredicate;
      }

      /**
       * @return Flag whether engine evaluated distinct is necessary.
       */
      public boolean useDistinct()
      {
         return useDistinct;
      }

      /**
       * @return Custom alias for SELECT clause.
       */
      public String getSelectAlias()
      {
         return selectAlias;
      }
   }
}
