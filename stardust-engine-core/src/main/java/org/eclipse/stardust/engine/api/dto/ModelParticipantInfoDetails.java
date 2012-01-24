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
package org.eclipse.stardust.engine.api.dto;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.engine.api.model.IModelParticipant;
import org.eclipse.stardust.engine.api.model.QualifiedModelParticipantInfo;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.engine.core.model.beans.ScopedModelParticipant;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.runtime.utils.DepartmentUtils;


public abstract class ModelParticipantInfoDetails extends ParticipantInfoDetails
      implements QualifiedModelParticipantInfo
{
   private static final long serialVersionUID = 2L;
   
   private long runtimeElementOID;
   private DepartmentInfo department;
   private boolean definesDepartmentScope;
   private String qualifiedId;
   private boolean isDepartmentScoped;
   
   protected ModelParticipantInfoDetails(Pair<? extends IModelParticipant, DepartmentInfo> pair)
   {
      this(getRtOid(pair.getFirst()), getQualifiedId(pair.getFirst()), pair.getFirst().getName(),
            DepartmentUtils.getFirstScopedOrganization(pair.getFirst()) != null,
            DepartmentUtils.isRestrictedModelParticipant(pair.getFirst()), pair
                  .getSecond());
   }

   public ModelParticipantInfoDetails(long runtimeElementOID, String qualifiedId, String name,
         boolean isDepartmentScoped, boolean definesDepartmentScope, DepartmentInfo department)
   {
      super(QName.valueOf(qualifiedId).getLocalPart(), name);
      this.qualifiedId = qualifiedId;
      this.runtimeElementOID = runtimeElementOID;
      this.qualifiedId = qualifiedId;
      this.isDepartmentScoped = isDepartmentScoped;
      this.definesDepartmentScope = definesDepartmentScope;
      this.department = department;
   }

   public String getQualifiedId()
   {
      return qualifiedId;
   }

   public long getRuntimeElementOID()
   {
      return runtimeElementOID;
   }

   public boolean isDepartmentScoped()
   {
      return isDepartmentScoped;
   }

   public boolean definesDepartmentScope()
   {
      return definesDepartmentScope;
   }

   public DepartmentInfo getDepartment()
   {
      return department;
   }
   
   private static long getRtOid(IModelParticipant modelParticipant)
   {
      return ModelManagerFactory.getCurrent().getRuntimeOid(modelParticipant);
   }

   private static String getQualifiedId(IModelParticipant participant)
   {
      if(participant instanceof ScopedModelParticipant
            && ((ScopedModelParticipant) participant).isPredefinedParticipant())
      {
         return participant.getId();
      }      
      return '{' + participant.getModel().getId() + '}' + participant.getId();
   }
}