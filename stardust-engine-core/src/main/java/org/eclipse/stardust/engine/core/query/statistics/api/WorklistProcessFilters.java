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

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.core.spi.query.CustomActivityInstanceQueryResult;


/**
 * @author florin.herinean
 * @version $Revision: $
 */
public abstract class WorklistProcessFilters extends CustomActivityInstanceQueryResult
{
   private static final long serialVersionUID = 1l;

   protected final Map<String, Long> processDefinitions;
   
   protected WorklistProcessFilters(WorklistProcessFiltersQuery query)
   {
      super(query);
      this.processDefinitions = CollectionUtils.newMap();
   }
   
   public Set<String> getProcessDefinitionIds()
   {
      return Collections.unmodifiableSet(processDefinitions.keySet());
   }
   
   public long getOpenActivitiesCount(String processId)
   {
      return processDefinitions.get(processId);
   }
}
