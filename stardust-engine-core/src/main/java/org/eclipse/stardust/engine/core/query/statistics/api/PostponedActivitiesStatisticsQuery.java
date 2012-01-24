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
public class PostponedActivitiesStatisticsQuery extends CustomUserQuery
{
   static final long serialVersionUID = -3202612048722669003L;
   
   public static final String ID = PostponedActivitiesStatisticsQuery.class.getName();

   public static PostponedActivitiesStatisticsQuery forAllUsers()
   {
      return new PostponedActivitiesStatisticsQuery();
   }

   protected PostponedActivitiesStatisticsQuery()
   {
      super(ID);
   }
}
