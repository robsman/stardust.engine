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
package org.eclipse.stardust.engine.core.runtime.utils;

import javax.xml.namespace.QName;

import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.QualifiedModelParticipantInfo;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;


public class ParticipantInfoUtil
{
   private ParticipantInfoUtil()
   {
      // Utility
   }
   
   /**
    * Creates a new ModelParticipantInfo object from the qualified id of a participant.
    * 
    * @param qualifiedParticipantId in the format '{' + modelId + '}' + participantId.
    * 
    * @return the corresponding ModelParticipantInfo.
    */
   public static ModelParticipantInfo newModelParticipantInfo(String qualifiedParticipantId)
   {
      return newModelParticipantInfo(qualifiedParticipantId, (DepartmentInfo) null);
   }
   
   /**
    * Creates a new ModelParticipantInfo object from the qualified id of a participant.
    * 
    * @param qualifiedParticipantId in the format '{' + modelId + '}' + participantId.
    * 
    * @return the corresponding ModelParticipantInfo.
    */
   public static ModelParticipantInfo newModelParticipantInfo(String qualifiedParticipantId, DepartmentInfo department)
   {
      QName qname = qualifiedParticipantId == null ? null : QName.valueOf(qualifiedParticipantId);
      return newModelParticipantInfo(qname.getNamespaceURI(), qname.getLocalPart(), department);
   }
   
   /**
    * Creates a new ModelParticipantInfo object from the model id and the id of a participant.
    * 
    * @param modelId
    * 
    * @param participantId
    * 
    * @return the created ModelParticipantInfo
    */
   public static ModelParticipantInfo newModelParticipantInfo(String modelId, final String participantId)
   {
      return newModelParticipantInfo(modelId, participantId, null);
   }
   
   /**
    * Creates a new ModelParticipantInfo object from the model id and the id of a participant.
    * 
    * @param modelId
    * 
    * @param participantId
    * 
    * @param department
    * 
    * @return the created ModelParticipantInfo
    */
   public static ModelParticipantInfo newModelParticipantInfo(String modelId, final String participantId,
         final DepartmentInfo department)
   {
      if (modelId == null || modelId.length() == 0)
      {
         if (PredefinedConstants.ADMINISTRATOR_ROLE.equals(participantId))
         {
            return ModelParticipantInfo.ADMINISTRATOR;
         }
         return new ModelParticipantInfo()
         {
            private static final long serialVersionUID = 1L;

            public boolean definesDepartmentScope()
            {
               return false;
            }

            public DepartmentInfo getDepartment()
            {
               return department;
            }

            public long getRuntimeElementOID()
            {
               return 0;
            }

            public boolean isDepartmentScoped()
            {
               return department != null;
            }

            public String getId()
            {
               return participantId;
            }

            public String getName()
            {
               return participantId;
            }

            public String toString()
            {
               return department == null || department == Department.DEFAULT
                  ? "ModelParticipant: " + participantId
                  : "ModelParticipant: " + participantId + " [" + department + ']';  
            }

            public String getQualifiedId()
            {
               return getId();
            }
         };
      }
      
      final String qualifiedId = '{' + modelId + '}' + participantId;
      return new QualifiedModelParticipantInfo()
      {
         private static final long serialVersionUID = 1L;

         public String getQualifiedId()
         {
            return qualifiedId;
         }

         public boolean definesDepartmentScope()
         {
            return false;
         }

         public DepartmentInfo getDepartment()
         {
            return department;
         }

         public long getRuntimeElementOID()
         {
            return 0;
         }

         public boolean isDepartmentScoped()
         {
            return department != null;
         }

         public String getId()
         {
            return participantId;
         }

         public String getName()
         {
            return participantId;
         }

         public String toString()
         {
            return department == null || department == Department.DEFAULT
               ? "ModelParticipant: " + qualifiedId
               : "ModelParticipant: " + qualifiedId + " [" + department + ']';  
         }
      };
   }
}