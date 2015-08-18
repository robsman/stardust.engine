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

import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.engine.api.runtime.UserInfo;

/**
 * Specialization of a {@link org.eclipse.stardust.engine.api.query.Worklist Worklist} being owned by a
 * {@link org.eclipse.stardust.engine.api.runtime.User User}. User worklists usually contain
 * sub-worklists containing work items from granted roles or organizations.
 * 
 * @author rsauer
 * @version $Revision$
 */
public class UserWorklist extends Worklist
{
   private static final long serialVersionUID = 2L;

   private final UserInfo owner;
   private final List subDetails;

   UserWorklist(UserInfo owner, WorklistQuery query, SubsetPolicy subset, List items,
         boolean moreAvailable, List subDetails, Long totalCount)
   {
      this(owner, query, subset, items, moreAvailable, subDetails, totalCount,
            Long.MAX_VALUE);
   }
   
   UserWorklist(UserInfo owner, WorklistQuery query, SubsetPolicy subset, List items,
         boolean moreAvailable, List subDetails, Long totalCount, long totalCountThreshold)
   {
      super(query, subset, items, moreAvailable, totalCount, totalCountThreshold);

      this.owner = owner;
      this.subDetails = subDetails;
   }
   
   /**
    * Retrieves the owning user of this worklist.
    * 
    * @return The owning user.
    */
   public UserInfo getOwner()
   {
      return owner;
   }

   public boolean isUserWorklist()
   {
      return true;
   }

   public long getOwnerOID()
   {
      return owner.getOID();
   }

   public String getOwnerID()
   {
      return owner.getId();
   }

   public String getOwnerName()
   {
      return owner.getName();
   }

   public Iterator getSubWorklists()
   {
      return subDetails.iterator();
   }
   
   public List getSubDetails()
   {
      return subDetails;
   }
}
