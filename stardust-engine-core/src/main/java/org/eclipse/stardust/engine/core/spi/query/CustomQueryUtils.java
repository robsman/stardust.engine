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
package org.eclipse.stardust.engine.core.spi.query;

import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.common.config.FactoryFinder;


/**
 * @author rsauer
 * @version $Revision$
 */
public class CustomQueryUtils
{

   public static CustomUserQueryResult evaluateCustomQuery(CustomUserQuery query)
   {
      IUserQueryEvaluator evaluator = null;

      List qeFactories = FactoryFinder.findFactories(
            IQueryEvaluatorFactory.class.getName(), null, null);
      for (Iterator i = qeFactories.iterator(); i.hasNext();)
      {
         IQueryEvaluatorFactory factory = (IQueryEvaluatorFactory) i.next();

         evaluator = factory.getUserQueryEvaluator(query.getQueryId());
         if (null != evaluator)
         {
            break;
         }
      }

      CustomUserQueryResult result = null;
      if (null != evaluator)
      {
         result = evaluator.evaluateQuery(query);
      }
      return result;
   }

   public static CustomProcessInstanceQueryResult evaluateCustomQuery(
         CustomProcessInstanceQuery query)
   {
      IProcessInstanceQueryEvaluator evaluator = null;

      List qeFactories = FactoryFinder.findFactories(
            IQueryEvaluatorFactory.class.getName(), null, null);
      for (Iterator i = qeFactories.iterator(); i.hasNext();)
      {
         IQueryEvaluatorFactory factory = (IQueryEvaluatorFactory) i.next();

         evaluator = factory.getProcessInstanceQueryEvaluator(query.getQueryId());
         if (null != evaluator)
         {
            break;
         }
      }

      CustomProcessInstanceQueryResult result = null;
      if (null != evaluator)
      {
         result = evaluator.evaluateQuery(query);
      }
      return result;
   }

   public static CustomActivityInstanceQueryResult evaluateCustomQuery(
         CustomActivityInstanceQuery query)
   {
      IActivityInstanceQueryEvaluator evaluator = null;

      List qeFactories = FactoryFinder.findFactories(
            IQueryEvaluatorFactory.class.getName(), null, null);
      for (Iterator i = qeFactories.iterator(); i.hasNext();)
      {
         IQueryEvaluatorFactory factory = (IQueryEvaluatorFactory) i.next();

         evaluator = factory.getActivityInstanceQueryEvaluator(query.getQueryId());
         if (null != evaluator)
         {
            break;
         }
      }

      CustomActivityInstanceQueryResult result = null;
      if (null != evaluator)
      {
         result = evaluator.evaluateQuery(query);
      }
      return result;
   }

}
