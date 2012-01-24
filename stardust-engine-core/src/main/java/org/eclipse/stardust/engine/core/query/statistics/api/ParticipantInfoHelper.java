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

import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.engine.api.dto.ModelParticipantInfoDetails;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.QualifiedModelParticipantInfo;
import org.eclipse.stardust.engine.api.runtime.PerformerType;
import org.eclipse.stardust.engine.core.runtime.utils.DepartmentUtils;


final class ParticipantInfoHelper
{
   public static boolean areEqual(ParticipantInfo pInfo1, ParticipantInfo pInfo2)
   {
      if (pInfo1 == pInfo2)
      {
         return true;
      }
      if (pInfo1 != null && pInfo2 != null)
      {
         if (pInfo1 instanceof ModelParticipantInfo && pInfo2 instanceof ModelParticipantInfo)
         {
            return areEqual((ModelParticipantInfo)pInfo1, (ModelParticipantInfo)pInfo2);
         }
         return CompareHelper.areEqual(pInfo1.getId(), pInfo2.getId());
      }
      return false;
   }
   
   public static boolean areEqual(ModelParticipantInfo pInfo1, ModelParticipantInfo pInfo2)
   {
      if (pInfo1 == pInfo2)
      {
         return true;
      }
      if (pInfo1 != null && pInfo2 != null)
      {
         if (pInfo1.getRuntimeElementOID() > 0 && pInfo2.getRuntimeElementOID() > 0)
         {
            if (pInfo1.getRuntimeElementOID() != pInfo2.getRuntimeElementOID())
            {
               return false;
            }
         }
         else
         {
            String id1 = pInfo1 instanceof QualifiedModelParticipantInfo
                  ? ((QualifiedModelParticipantInfo) pInfo1).getQualifiedId()
                  : pInfo1.getId();
            String id2 = pInfo2 instanceof QualifiedModelParticipantInfo
                  ? ((QualifiedModelParticipantInfo) pInfo2).getQualifiedId()
                  : pInfo2.getId();
            if (!CompareHelper.areEqual(id1, id2))
            {
               return false;
            }
         }
         return DepartmentUtils.areEqual(pInfo1.getDepartment(), pInfo2.getDepartment());
      }
      return false;
   }
   
   public static ParticipantInfo getLegacyParticipantInfo(
         PerformerType performerType, long participantOid)
   {
      ParticipantInfo result = null;
      if(PerformerType.ModelParticipant.equals(performerType))
      {
         return new LegacyModelParticipant(participantOid);
      }
      else
      {
         result = DepartmentUtils.getParticipantInfo(
               performerType, participantOid, 0, PredefinedConstants.ANY_MODEL);
      }
      return result;
   }
   
   private static final class LegacyModelParticipant extends ModelParticipantInfoDetails
   {
      private static final long serialVersionUID = 1L;

      public LegacyModelParticipant(long runtimeElementOID)
      {
         super(runtimeElementOID, null, null, false, false, null);
      }
   }
}
