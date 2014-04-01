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
package org.eclipse.stardust.engine.extensions.dms.data;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.runtime.Resource;
import org.eclipse.stardust.engine.api.runtime.ResourceInfo;


/**
 * @author rsauer
 * @version $Revision$
 */
public abstract class DmsResourceBean implements Resource, ResourceInfo, Serializable
{

   private static final long serialVersionUID = 1L;

   private final Map legoResource;

   public Map vfsResource()
   {
      return legoResource;
   }

   protected DmsResourceBean(Map legoResource)
   {
      this.legoResource = legoResource;

      if ( !legoResource.containsKey(AuditTrailUtils.RES_PROPERTIES))
      {
         // initialize properties with an empty list
         this.setProperties(CollectionUtils.newHashMap());
      }
   }

   public String getRepositoryId()
   {
      return (String) vfsResource().get(AuditTrailUtils.RES_REPOSITORY_ID);
   }

   public String getId()
   {
      return (String) vfsResource().get(AuditTrailUtils.RES_ID);
   }
   
   public void setId(String id)
   {
     vfsResource().put(AuditTrailUtils.RES_ID, id);    
   }

   public String getPath()
   {
      return (String) vfsResource().get(AuditTrailUtils.RES_PATH);
   }
   
   public void setPath(String path)
   {
      vfsResource().put(AuditTrailUtils.RES_PATH, path);   
   }

   public String getName()
   {
      return (String) vfsResource().get(AuditTrailUtils.RES_NAME);
   }

   public void setName(String name)
   {
      vfsResource().put(AuditTrailUtils.RES_NAME, name);
   }

   public String getDescription()
   {
      return (String) vfsResource().get(AuditTrailUtils.RES_DESCRIPTION);
   }

   public void setDescription(String description)
   {
      vfsResource().put(AuditTrailUtils.RES_DESCRIPTION, description);
   }

   public String getOwner()
   {
      return (String) vfsResource().get(AuditTrailUtils.RES_OWNER);
   }

   public void setOwner(String owner)
   {
      vfsResource().put(AuditTrailUtils.RES_OWNER, owner);
   }

   public Date getDateCreated()
   {
      return (Date)vfsResource().get(AuditTrailUtils.RES_DATE_CREATED);
   }
   
   public void setDateCreated(Date date)
   {
      vfsResource().put(AuditTrailUtils.RES_DATE_CREATED, date);
   }

   public Date getDateLastModified()
   {
      return (Date)vfsResource().get(AuditTrailUtils.RES_DATE_LAST_MODIFIED);
   }
   
   public void setDateLastModified(Date date)
   {
      vfsResource().put(AuditTrailUtils.RES_DATE_LAST_MODIFIED, date);
   }


   public Map getProperties()
   {
      Map propertiesMap = null;
      Object rawProperties = vfsResource().get(AuditTrailUtils.RES_PROPERTIES);
      if (rawProperties instanceof Map)
      {
         propertiesMap = (Map) rawProperties;
      }

      if (propertiesMap == null)
      {
         propertiesMap = CollectionUtils.newHashMap();
         vfsResource().put(AuditTrailUtils.RES_PROPERTIES, propertiesMap);
      }
      return propertiesMap;
   }

   public void setProperties(Map properties)
   {
      vfsResource().put(AuditTrailUtils.RES_PROPERTIES, properties);
   }

   public Serializable getProperty(String propertyName)
   {
      Map propertiesMap = null;
      Object rawProperties = vfsResource().get(AuditTrailUtils.RES_PROPERTIES);
      if (rawProperties instanceof Map)
      {
         propertiesMap = (Map) rawProperties;
      }
      if (propertiesMap == null)
      {
         return null;
      }
      return (Serializable) propertiesMap.get(propertyName);
   }

   public void setProperty(String propertyName, Serializable propertyValue)
   {
      Map propertiesMap = null;
      Object rawProperties = vfsResource().get(AuditTrailUtils.RES_PROPERTIES);
      if (rawProperties instanceof Map)
      {
         propertiesMap = (Map) rawProperties;
      }
      if (propertiesMap == null)
      {
         propertiesMap = CollectionUtils.newHashMap();
         vfsResource().put(AuditTrailUtils.RES_PROPERTIES, propertiesMap);
      }
      propertiesMap.put(propertyName, propertyValue);
   }
   
   public boolean equals(Object obj)
   {
      if (obj instanceof DmsResourceBean)
      {
         return vfsResource().equals(((DmsResourceBean) obj).vfsResource());
      }
      else
      {
         return super.equals(obj);
      }
   }

}
