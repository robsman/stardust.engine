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
package org.eclipse.stardust.engine.core.query.statistics.evaluation;

import java.util.Map;

import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.core.query.statistics.api.UserLoginStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.UserLoginStatisticsQuery;


/**
 * @author rsauer
 * @version $Revision$
 */
public class UserLoginStatisticsResult extends UserLoginStatistics
{
   static final long serialVersionUID = -1276453335715463958L;

   public UserLoginStatisticsResult(UserLoginStatisticsQuery query, Users users,
         Map<Long, LoginStatistics> loginStatictics)
   {
      super(query, users);

      this.loginStatistics.putAll(loginStatictics);
   }
}
