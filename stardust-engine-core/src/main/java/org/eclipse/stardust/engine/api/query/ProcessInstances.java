/*******************************************************************************
 * Copyright (c) 2011, 2013 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.api.query;

import java.util.Collections;

import org.eclipse.stardust.engine.api.runtime.ProcessInstance;


/**
 * Result of an {@link ProcessInstanceQuery} execution. Retrieved items are instances of
 * {@link org.eclipse.stardust.engine.api.runtime.ProcessInstance}.
 *
 * @author rsauer
 * @version $Revision$
 */
public class ProcessInstances extends AbstractQueryResult<ProcessInstance>
{
   ProcessInstances(ProcessInstanceQuery query, RawQueryResult<ProcessInstance> result)
   {
      super(query, result.getItemList(), result.hasMore(), result.hasTotalCount()
            ? result.getTotalCount()
            : null, result.getTotalCountThreshold());
   }
   
   protected ProcessInstances(ProcessInstanceQuery query)
   {
      super(query, Collections.<ProcessInstance>emptyList(), false, null);
   }

   /**
    * Gets the query this result is based on.
    *
    * @return The query this result is based on.
    */
   public ProcessInstanceQuery getQuery()
   {
      return (ProcessInstanceQuery) query;
   }
}
