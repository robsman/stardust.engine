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

import org.eclipse.stardust.engine.core.query.statistics.api.WorklistProcessFilters;
import org.eclipse.stardust.engine.core.query.statistics.api.WorklistProcessFiltersQuery;

/**
 * @author florin.herinean
 * @version $Revision: $
 */
public class WorklistProcessFiltersResult extends WorklistProcessFilters
{
   private static final long serialVersionUID = 1L;

   public WorklistProcessFiltersResult(WorklistProcessFiltersQuery query)
   {
      super(query);
   }

   void update(String id, long count)
   {
      Long cumulated = processDefinitions.get(id);
      if (cumulated == null)
      {
         processDefinitions.put(id, count);
      }
      else
      {
         processDefinitions.put(id, cumulated + count);
      }
   }
}
 