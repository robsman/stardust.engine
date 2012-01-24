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

import java.io.Serializable;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.core.spi.query.CustomUserQueryResult;


/**
 * @author rsauer
 * @version $Revision$
 */
public abstract class WorklistStatistics extends CustomUserQueryResult
{
   private static final long serialVersionUID = 1l;

   protected final Map<Long, UserStatistics> userStatistics;
   
   protected final Map<ParticipantDepartmentPair, ParticipantStatistics> modelParticipantStatistics;

   protected WorklistStatistics(WorklistStatisticsQuery query, Users users)
   {
      super(query, users);
      
      this.userStatistics = CollectionUtils.newMap();
      this.modelParticipantStatistics = CollectionUtils.newMap();
   }

   public UserStatistics getUserStatistics(long userOid)
   {
      return userStatistics.get(Long.valueOf(userOid));
   }

   @Deprecated
   public ParticipantStatistics getModelParticipantStatistics(String participantId)
   {
      ParticipantDepartmentPair id = new ParticipantDepartmentPair(participantId, 0l);
      return modelParticipantStatistics.get(id);
   }

   public ParticipantStatistics getModelParticipantStatistics(ModelParticipantInfo participant)
   {
      return modelParticipantStatistics.get(
            ParticipantDepartmentPair.getParticipantDepartmentPair(participant));
   }

   public static class UserStatistics implements Serializable
   {
      private static final long serialVersionUID = 1l;

      public final long userOid;

      public long nPrivateWorkitems;

      public long nSharedWorkitems;

      public long nGrants;

      public boolean loggedIn;

      public UserStatistics(long userOid)
      {
         this.userOid = userOid;
      }
   }

   public static class ParticipantStatistics implements Serializable
   {
      private static final long serialVersionUID = 1l;

      public final String roleId;
      
      public final String qualifiedId;
      
      public final long departmentOid;

      public long nWorkitems;

      public long nUsers;

      public long nLoggedInUsers;

      public ParticipantStatistics(String roleId, String qualifiedId, long departmentOid)
      {
         this.roleId = roleId;
         this.qualifiedId = qualifiedId;
         this.departmentOid = departmentOid;
      }
   }

}
