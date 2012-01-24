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
import java.util.Date;
import java.util.Map;

import org.eclipse.stardust.engine.api.runtime.Resource;

import com.sungard.infinity.bpm.vfs.IResource;


/**
 * IResource-view of a DmsResource
 */
public class ResourceToIResourceAdapter implements IResource
{

   private final Resource resource;

   private String prefixedPath;

   public ResourceToIResourceAdapter(Resource resource, String prefixPath)
   {
      this.resource = resource;

      String res = resource.getPath();
      if (res.startsWith("/"))
      {
         res = prefixPath.concat(res);
      }
      this.prefixedPath = res;
   }

   public Date getDateCreated()
   {
      return resource.getDateCreated();
   }

   public Date getDateLastModified()
   {
      return resource.getDateLastModified();
   }

   public String getDescription()
   {
      return resource.getDescription();
   }

   public String getId()
   {
      return resource.getId();
   }

   public String getName()
   {
      return resource.getName();
   }

   public String getOwner()
   {
      return resource.getOwner();
   }

   public String getPath()
   {
      return prefixedPath;
   }

   public Map getProperties()
   {
      return resource.getProperties();
   }

   public Serializable getProperty(String propertyName)
   {
      return resource.getProperty(propertyName);
   }

   public String getRepositoryId()
   {
      return resource.getRepositoryId();
   }

   public void setDescription(String description)
   {
      throw new UnsupportedOperationException();
   }

   public void setName(String name)
   {
      throw new UnsupportedOperationException();
   }

   public void setOwner(String owner)
   {
      throw new UnsupportedOperationException();
   }

   public void setProperty(String propertyName, Serializable propertyValue)
   {
      throw new UnsupportedOperationException();
   }

   public String getParentId()
   {
      // CRNT-8785 (parentId and parentPath in jcr-vfs throw exception)
      return null;
   }

   public String getParentPath()
   {
      // CRNT-8785 (parentId and parentPath in jcr-vfs throw exception)
      return null;
   }

   public void setDateCreated(Date dateCreated)
   {
      throw new UnsupportedOperationException();
   }

   public void setDateLastModified(Date dateLastModified)
   {
      throw new UnsupportedOperationException();
   }

   public void setProperties(
         Map/* < ? extends String, ? extends Serializable> */properties)
   {
      throw new UnsupportedOperationException();
   }

   public String getPropertiesTypeId()
   {
      return null;
   }

   public String getPropertiesTypeSchemaLocation()
   {
      return null;
   }

   public void setPropertiesTypeId(String arg0)
   {
      throw new UnsupportedOperationException();
   }

   public void setPropertiesTypeSchemaLocation(String arg0)
   {
      throw new UnsupportedOperationException();
   }

}