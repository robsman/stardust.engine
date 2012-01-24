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
package org.eclipse.stardust.engine.core.runtime.beans;

import java.io.Serializable;
import java.util.*;

import org.eclipse.stardust.common.Attribute;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.api.dto.UserGroupDetailsLevel;
import org.eclipse.stardust.engine.api.model.ModelParticipant;
import org.eclipse.stardust.engine.api.runtime.UserGroup;


/**
 * @author sborn
 * @version $Revision$
 */
public class UserGroupDetails implements UserGroup
{
   private static final long serialVersionUID = 2L;
   
   private long oid;
   private String id;
   private String name;
   private Date validFrom;
   private Date validTo;
   private String description;
   private Map properties = new HashMap();
   private UserGroupDetailsLevel detailsLevel;

   public UserGroupDetails(IUserGroup userGroup)
   {
      initDetailsLevel();
      init(userGroup);
   }

   private void init(IUserGroup userGroup)
   {
      this.oid = userGroup.getOID();
      this.id = userGroup.getId();
      this.name = userGroup.getName();
      this.validFrom = userGroup.getValidFrom();
      this.validTo = userGroup.getValidTo();
      this.description = userGroup.getDescription();

      if (UserGroupDetailsLevel.Full == detailsLevel)
      {
         for (Iterator i = userGroup.getAllProperties().values().iterator(); i.hasNext();)
         {
            Attribute property = (Attribute) i.next();

            if (property.getValue() != null)
            {
               properties.put(property.getName(), property.getValue());
            }
         }
      }
   }

   private void initDetailsLevel()
   {
      detailsLevel = (UserGroupDetailsLevel) Parameters.instance().get(
            UserGroupDetailsLevel.PRP_DETAILS_LEVEL);
      if (null == detailsLevel)
      {
         detailsLevel = UserGroupDetailsLevel.Full;
      }
   }
   
   public long getOID()
   {
      return oid;
   }

   public String getId()
   {
      return id;
   }

   public String getName()
   {
      return name;
   }

   public Date getValidFrom()
   {
      return validFrom;
   }

   public Date getValidTo()
   {
      return validTo;
   }

   public String getDescription()
   {
      return description;
   }

   public Object getAttribute(String name)
   {
      return properties.get(name);
   }

   public Map getAllAttributes()
   {
      return Collections.unmodifiableMap(properties);
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public void setValidFrom(Date validFrom)
   {
      this.validFrom = validFrom;
   }

   public void setValidTo(Date validTo)
   {
      this.validTo = validTo;
   }

   public void setDescription(String description)
   {
      this.description = description;
   }

   public void setAttribute(String name, Serializable value)
   {
      this.properties.put(name, value);
   }

   public void setAllProperties(Map properties)
   {
      this.properties = new HashMap();
      this.properties.putAll(properties);
   }

   /**
    * @deprecated This method has moved to {@link ModelParticipant#getPartitionOID()}
    */
   public short getPartitionOID()
   {
      return 0;
   }

   /**
    * @deprecated This method has moved to {@link ModelParticipant#getPartitionId()}
    */
   public String getPartitionId()
   {
      return null;
   }
   
   /**
    * @deprecated This method has moved to {@link ModelParticipant#getModelOID()}
    */
   public int getModelOID()
   {
      return 0;
   }

   /**
    * @deprecated This method has moved to {@link ModelParticipant#getElementOID()}
    */
   public int getElementOID()
   {
      return 0;
   }

   /**
    * @deprecated This method has moved to {@link ModelParticipant#getNamespace()}
    */
   public String getNamespace()
   {
      return null;
   }

   /**
    * @deprecated This method has moved to
    *             {@link ModelParticipant#getAllSuperOrganizations()}
    */
   public List getAllSuperOrganizations()
   {
      return Collections.EMPTY_LIST;
   }
   
   public UserGroupDetailsLevel getDetailsLevel()
   {
      return detailsLevel;
   }
   
   public boolean equals(Object rawRhs)
   {
      boolean isEqual = false;

      if (this == rawRhs)
      {
         isEqual = true;
      }
      else if (rawRhs instanceof UserGroupDetails)
      {
         final UserGroupDetails rhs = (UserGroupDetails) rawRhs;
         isEqual = (rhs.getOID() == oid);
      }

      return isEqual;
   }

   public String getQualifiedId()
   {
      // user groups have no namespace
      return null;
   }
}
