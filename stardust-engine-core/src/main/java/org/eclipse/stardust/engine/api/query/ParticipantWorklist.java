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
import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;


/**
 * Specialization of a {@link org.eclipse.stardust.engine.api.query.Worklist Worklist} being owned by
 * either an {@link org.eclipse.stardust.engine.api.model.Organization Organization} or a
 * {@link org.eclipse.stardust.engine.api.model.Role Role}. Participant worklists don't contain
 * sub-worklists, any work items from associated organizations will be directly contained
 * in a user worklist.
 * 
 * @author rsauer
 * @version $Revision$
 */
public class ParticipantWorklist extends Worklist
{
   private static final long serialVersionUID = 2L;
   
   private final String ownerID;
   private final ParticipantInfo owner;

   ParticipantWorklist(ParticipantInfo owner, WorklistQuery query, SubsetPolicy subset,
         List items, boolean moreAvailable, Long totalCount)
   {
      this(owner, query, subset, items, moreAvailable, totalCount, Long.MAX_VALUE);
   }
   
   public ParticipantWorklist(ParticipantInfo owner, WorklistQuery query,
         SubsetPolicy subset, List items, boolean moreAvailable, Long totalCount,
         long totalCountThreshold)
   {
      super(query, subset, items, moreAvailable, totalCount, totalCountThreshold);

      this.ownerID = owner.getId();
      this.owner = owner;
   }

   ParticipantWorklist(String ownerID, WorklistQuery query, SubsetPolicy subset,
         List items, boolean moreAvailable, Long totalCount)
   {
      this(ownerID, query, subset, items, moreAvailable, totalCount, Long.MAX_VALUE);
   }

   ParticipantWorklist(String ownerID, WorklistQuery query, SubsetPolicy subset,
         List items, boolean moreAvailable, Long totalCount, long totalCountThreshold)
   {
      super(query, subset, items, moreAvailable, totalCount, totalCountThreshold);

      this.ownerID = ownerID;
      this.owner = null;
   }

   /**
    * Retrieves the owning participant of this worklist.
    * 
    * @return The owning participant, either an
    *         {@link org.eclipse.stardust.engine.api.runtime.UserGroupInfo UserGroupInfo} or a
    *         {@link org.eclipse.stardust.engine.api.model.OrganizationInfo Organization} or a
    *         {@link org.eclipse.stardust.engine.api.model.RoleInfo Role}. May be <code>null</code>.
    */
   public ParticipantInfo getOwner()
   {
      return owner;
   }

   public boolean isUserWorklist()
   {
      return false;
   }

   public long getOwnerOID()
   {
      return 0;
   }

   public String getOwnerID()
   {
      return ownerID;
   }

   public String getOwnerName()
   {
      String name = (null != owner) ? owner.getName() : null;
      if (StringUtils.isEmpty(name))
      {
         name = getOwnerID();
      }
      return name;
   }

   public Iterator getSubWorklists()
   {
      return Collections.EMPTY_LIST.iterator();
   }
}
