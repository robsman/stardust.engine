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
package org.eclipse.stardust.engine.core.query.statistics.api;

import org.eclipse.stardust.engine.core.spi.query.CustomUserQuery;

/**
 * @author rsauer
 * @version $Revision$
 */
public class UserLoginStatisticsQuery extends CustomUserQuery
{
   static final long serialVersionUID = 2412420510572507685L;
   
   public static final String ID = UserLoginStatisticsQuery.class.getName();

   public static UserLoginStatisticsQuery forAllUsers()
   {
      return new UserLoginStatisticsQuery();
   }

   protected UserLoginStatisticsQuery()
   {
      super(ID);
   }
}
