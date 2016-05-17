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
package org.eclipse.stardust.engine.core.model.beans;

import java.util.*;

import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.engine.api.model.IModelParticipant;
import org.eclipse.stardust.engine.api.model.IOrganization;
import org.eclipse.stardust.engine.api.model.IScopedModelParticipant;
import org.eclipse.stardust.engine.api.query.QueryUtils;
import org.eclipse.stardust.engine.core.model.utils.ModelElement;
import org.eclipse.stardust.engine.core.model.utils.RootElement;
import org.eclipse.stardust.engine.core.runtime.beans.IDepartment;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.IUserGroup;


/**
 * Class is a model participant with bound department.
 * 
 * @author stephan.born
 */
public class ScopedModelParticipant implements IScopedModelParticipant
{
   private static final long serialVersionUID = 1L;

   private final IModelParticipant modelParticipant;
   private final IDepartment department;
   
   public ScopedModelParticipant(IModelParticipant modelParticipant,
         IDepartment department)
   {
      this.modelParticipant = modelParticipant;
      this.department = department;
   }

   public String getQualifiedId()
   {
      return modelParticipant.getQualifiedId();
   }   
   
   public IModelParticipant getModelParticipant()
   {
      return modelParticipant;
   }
   
   public IDepartment getDepartment()
   {
      return department;
   }
   
   public void checkConsistency(List inconsistencies)
   {
      modelParticipant.checkConsistency(inconsistencies);
   }

   public void delete()
   {
      modelParticipant.delete();
   }

   public IOrganization findOrganization(String id)
   {
      return modelParticipant.findOrganization(id);
   }

   public Map<String, Object> getAllAttributes()
   {
      return modelParticipant.getAllAttributes();
   }

   public Iterator getAllOrganizations()
   {
      return modelParticipant.getAllOrganizations();
   }

   public Iterator getAllParticipants()
   {
      return modelParticipant.getAllParticipants();
   }

   public Iterator getAllTopLevelOrganizations()
   {
      return modelParticipant.getAllTopLevelOrganizations();
   }

   public Object getAttribute(String name)
   {
      return modelParticipant.getAttribute(name);
   }

   public boolean getBooleanAttribute(String name)
   {
      return modelParticipant.getBooleanAttribute(name);
   }

   public int getCardinality()
   {
      return modelParticipant.getCardinality();
   }

   public String getDescription()
   {
      return modelParticipant.getDescription();
   }

   public int getElementOID()
   {
      return modelParticipant.getElementOID();
   }

   public float getFloatAttribute(String name)
   {
      return modelParticipant.getFloatAttribute(name);
   }

   public String getId()
   {
      return modelParticipant.getId();
   }

   public int getIntegerAttribute(String name)
   {
      return modelParticipant.getIntegerAttribute(name);
   }

   public long getLongAttribute(String name)
   {
      return modelParticipant.getLongAttribute(name);
   }
   
   public boolean isPredefinedParticipant()
   {
      return QueryUtils.isPredefinedParticipant(modelParticipant.getId());
   }
   
   public RootElement getModel()
   {
      return modelParticipant.getModel();
   }

   public String getName()
   {
      return modelParticipant.getName();
   }

   public long getOID()
   {
      return modelParticipant.getOID();
   }

   public ModelElement getParent()
   {
      return modelParticipant.getParent();
   }

   public Object getRuntimeAttribute(String name)
   {
      return modelParticipant.getRuntimeAttribute(name);
   }

   public String getStringAttribute(String name)
   {
      return modelParticipant.getStringAttribute(name);
   }

   public String getUniqueId()
   {
      return modelParticipant.getUniqueId();
   }

   public boolean isAuthorized(IModelParticipant participant)
   {
      return modelParticipant.isAuthorized(participant);
   }

   public boolean isAuthorized(IUser user)
   {
      return modelParticipant.isAuthorized(user);
   }

   public boolean isAuthorized(IUserGroup userGroup)
   {
      return modelParticipant.isAuthorized(userGroup);
   }

   public boolean isPredefined()
   {
      return modelParticipant.isPredefined();
   }

   public boolean isTransient()
   {
      return modelParticipant.isTransient();
   }

   public void markModified()
   {
      modelParticipant.markModified();
   }

   public void register(int oid)
   {
      modelParticipant.register(oid);
   }

   public void removeAllAttributes()
   {
      modelParticipant.removeAllAttributes();
   }

   public void removeAttribute(String name)
   {
      modelParticipant.removeAttribute(name);
   }

   public <V> void setAllAttributes(Map<String, V> attributes)
   {
      modelParticipant.setAllAttributes(attributes);
   }

   public void setAttribute(String name, Object value)
   {
      modelParticipant.setAttribute(name, value);
   }

   public void setDescription(String description)
   {
      modelParticipant.setDescription(description);
   }

   public void setId(String id)
   {
      modelParticipant.setId(id);
   }

   public void setName(String name)
   {
      modelParticipant.setName(name);
   }

   public void setParent(ModelElement parent)
   {
      modelParticipant.setParent(parent);
   }

   public void setPredefined(boolean predefined)
   {
      modelParticipant.setPredefined(predefined);
   }

   public Object setRuntimeAttribute(String name, Object value)
   {
      return modelParticipant.setRuntimeAttribute(name, value);
   }

   @Override
   public String toString()
   {
      StringBuffer buffer = new StringBuffer(modelParticipant.toString());
      if (department != null)
      {
         buffer.append(", Department: ").append(department.getId());
      }
      return buffer.toString();
   }
   
   @Override
   public boolean equals(Object obj)
   {
      final boolean participantEquals = modelParticipant.equals(obj);
      if (participantEquals && obj instanceof IScopedModelParticipant)
      {
         IScopedModelParticipant thatParticipant = (IScopedModelParticipant) obj;
         if(QueryUtils.isPredefinedParticipant(thatParticipant.getId()))
         {
            return participantEquals;
         }      
         return CompareHelper.areEqual(department, thatParticipant.getDepartment());
      }

      return participantEquals;
   }
   
   @Override
   public int hashCode()
   {
      // TODO: use department for hash code, too.
      return modelParticipant.hashCode();
   }

   public void addToOrganizations(IOrganization org)
   {
      throw new UnsupportedOperationException();
   }
}