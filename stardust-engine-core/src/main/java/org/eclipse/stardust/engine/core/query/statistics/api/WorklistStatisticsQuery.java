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
public class WorklistStatisticsQuery extends CustomUserQuery
{
   static final long serialVersionUID = 5266310883567698278L;
   
   public static final String ID = WorklistStatisticsQuery.class.getName();

   public static WorklistStatisticsQuery forAllUsers()
   {
      return new WorklistStatisticsQuery();
   }

   protected WorklistStatisticsQuery()
   {
      super(ID);
   }
}
