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

import java.util.Collections;

import org.eclipse.stardust.engine.api.runtime.User;


/**
 * Result of an {@link UserQuery} execution. Retrieved items are instances of
 * {@link org.eclipse.stardust.engine.api.runtime.User}.
 *
 * @author rsauer
 * @version $Revision$
 */
public class Users extends AbstractQueryResult<User>
{
   Users(UserQuery query, RawQueryResult<User> result)
   {
      super(query, result.getItemList(), result.hasMore(), result.hasTotalCount()
            ? result.getTotalCount()
            : null);
   }

   protected Users(UserQuery query)
   {
      super(query, Collections.<User>emptyList(), false, null);
   }
   
   protected Users(UserQuery query, Users users)
   {
      super(query, users.items, users.hasMore, //
            users.hasTotalCount() //
                  ? users.getTotalCount()
                  : null);
   }
   
   /**
    * Gets the query this result is based on.
    * 
    * @return The query this result is based on.
    */
   public UserQuery getQuery()
   {
      return (UserQuery) query;
   }
}
