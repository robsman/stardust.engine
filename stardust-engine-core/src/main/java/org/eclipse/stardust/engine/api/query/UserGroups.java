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

import org.eclipse.stardust.engine.api.runtime.UserGroup;

/**
 * Result of a {@link UserGroupQuery} execution. Retrieved items are instances of
 * {@link org.eclipse.stardust.engine.api.runtime.UserGroup}.
 *
 * @author rsauer
 * @version $Revision$
 */
public class UserGroups extends AbstractQueryResult<UserGroup>
{
   UserGroups(UserGroupQuery query, RawQueryResult<UserGroup> result)
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
   public UserGroupQuery getQuery()
   {
      return (UserGroupQuery) query;
   }
}