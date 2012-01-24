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

import org.eclipse.stardust.engine.api.dto.UserDetails;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.runtime.beans.UserBean;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;


/**
 * @author rsauer
 * @version $Revision$
 */
public class QueryServiceUtils
{

   public static EvaluationContext getDefaultEvaluationContext()
   {
      return new EvaluationContext(ModelManagerFactory.getCurrent(),
            SecurityProperties.getUser());
   }
   
   public static Users evaluateUserQuery(UserQuery query)
   {
      /* Without <User, UserDetails> antit would result in an incompatible types error
       * The strange thing is that it is compiling in eclipse without 
       * any explicit declerations */
      RawQueryResult<User> result = GenericQueryEvaluator.<User, UserDetails>
         evaluate(query, UserBean.class,
            IUser.class, UserDetails.class, getDefaultEvaluationContext());

      return QueryResultFactory.createUserQueryResult(query, result);
   }

   private QueryServiceUtils()
   {
      // utility class
   }

}
