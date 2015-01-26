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

import org.eclipse.stardust.engine.api.runtime.LogEntry;

/**
 * Result of an {@link LogEntryQuery} execution. Retrieved items are instances of
 * {@link org.eclipse.stardust.engine.api.runtime.LogEntry}.
 *
 * @author rsauer
 * @version $Revision$
 */
public class LogEntries extends AbstractQueryResult<LogEntry>
{
   LogEntries(LogEntryQuery query, RawQueryResult<LogEntry> result)
   {
      super(query, result.getItemList(), result.hasMore(), result.hasTotalCount()
            ? result.getTotalCount()
            : null);
   }

   /**
    * Gets the query this result is based on.
    *
    * @return The query this result is based on.
    */
   public LogEntryQuery getQuery()
   {
      return (LogEntryQuery) query;
   }
}
