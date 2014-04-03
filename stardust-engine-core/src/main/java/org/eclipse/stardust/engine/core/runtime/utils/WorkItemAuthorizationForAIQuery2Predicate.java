/*******************************************************************************
 * Copyright (c) 2013 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.utils;

import org.eclipse.stardust.engine.api.query.Query;


/**
 * Predicate class which is used to restrict access to work items based on its activity
 * declarative security permission.
 *
 * This is used for ActivityInstanceQueries which are configured to query from workitem table.
 * Also, as requirements for ExcludeUser handling is different for WorkilstQueries and ActivityInstanceQueries
 * this will be handled in method {{@link #queryRequiresExcludeUserHandling(Query)}.
 *
 * @author stephan.born
 * @version $Revision: 5162 $
 */
public class WorkItemAuthorizationForAIQuery2Predicate extends
      WorkItemAuthorization2Predicate
{

   public WorkItemAuthorizationForAIQuery2Predicate(AuthorizationContext context)
   {
      super(context);
   }

   @Override
   protected boolean queryRequiresExcludeUserHandling(Query query)
   {
      return isAiQueryWithExcludeUserPolicyApplied(query);
   }
}
