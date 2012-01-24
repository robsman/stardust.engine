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
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;


/**
 * @author rsauer
 * @version $Revision$
 */
public class AbstractResource
{

   private final String repositoryId;
   
   private final String id;

   private final String path;

   private String name;

   private String description;

   private String owner;

   private final Date dateCreated;

   private final Date dateLastModified;

   private Map metaData;

   public AbstractResource(String repositoryId, String id, String path, Date dateCreated,
         Date dateLastModified)
   {
      this.repositoryId = repositoryId;
      
      this.id = id;

      this.path = path;

      this.dateCreated = dateCreated;
      this.dateLastModified = dateLastModified;
   }

   public String getRepositoryId()
   {
      return repositoryId;
   }

   public String getId()
   {
      return id;
   }

   public String getPath()
   {
      return path;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public String getDescription()
   {
      return description;
   }

   public void setDescription(String description)
   {
      this.description = description;
   }

   public String getOwner()
   {
      return owner;
   }

   public void setOwner(String owner)
   {
      this.owner = owner;
   }

   public Date getDateCreated()
   {
      return new Date(dateCreated.getTime());
   }

   public Date getDateLastModified()
   {
      return new Date(dateLastModified.getTime());
   }

   public Map getProperties()
   {
      return (null != metaData)
            ? Collections.unmodifiableMap(metaData)
            : Collections.EMPTY_MAP;
   }

   public Serializable getProperty(String name)
   {
      return (Serializable) ((null != metaData) ? metaData.get(name) : null);
   }

   public Serializable setProperty(String name, Serializable value)
   {
      if (null != value)
      {
         if (null == metaData)
         {
            this.metaData = CollectionUtils.createMap();
         }

         return (Serializable) metaData.put(name, value);
      }
      else
      {
         return (null != metaData) ? (Serializable) metaData.remove(name) : null;
      }
   }

}
