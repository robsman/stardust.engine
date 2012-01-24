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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.stardust.common.error.ConcurrencyException;
import org.eclipse.stardust.engine.core.persistence.PersistenceController;


/**
 * @author rsauer
 * @version $Revision$
 */
public class CachedUserRealmBean implements IUserRealm
{
   private final long oid;

   private final String id;

   private final String name;

   private final String description;

   private final IAuditTrailPartition partition;
   
   private final Map properties;

   public CachedUserRealmBean(IUserRealm delegate,
         CachedAuditTrailPartitionBean partition)
   {
      this.oid = delegate.getOID();
      this.id = delegate.getId();
      this.name = delegate.getName();
      this.description = delegate.getDescription();

      this.partition = partition;
      
      this.properties = Collections.unmodifiableMap(new HashMap(
            delegate.getAllProperties()));
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

   public String getDescription()
   {
      return description;
   }

   public IAuditTrailPartition getPartition()
   {
      return partition;
   }

   public Map getAllProperties()
   {
      return properties;
   }

   public Map getAllPropertyValues()
   {
      return new HashMap(properties);
   }

   public Serializable getPropertyValue(String name)
   {
      return (Serializable) properties.get(name);
   }

   public void setOID(long oid)
   {
      throw new UnsupportedOperationException();
   }

   public void setId(String id)
   {
      throw new UnsupportedOperationException();
   }

   public void setName(String name)
   {
      throw new UnsupportedOperationException();
   }

   public void setDescription(String description)
   {
      throw new UnsupportedOperationException();
   }

   public void addPropertyValues(Map map)
   {
      throw new UnsupportedOperationException();
   }

   public void setPropertyValue(String name, Serializable value, boolean force)
   {
      throw new UnsupportedOperationException();
   }   
   
   public void setPropertyValue(String name, Serializable value)
   {
      throw new UnsupportedOperationException();
   }
   
   public void removeProperty(String name)
   {
      throw new UnsupportedOperationException();
   }
   
   public void removeProperty(String name, Serializable value)
   {
      throw new UnsupportedOperationException();
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.runtime.beans.AttributedIdentifiablePersistent#createProperty(java.lang.String, java.io.Serializable)
    */
   public AbstractProperty createProperty(String name, Serializable value)
   {
      throw new UnsupportedOperationException();
   }

   public void lock() throws ConcurrencyException
   {
      throw new UnsupportedOperationException();
   }

   public void delete()
   {
      throw new UnsupportedOperationException();
   }

   public void delete(boolean writeThrough)
   {
      throw new UnsupportedOperationException();
   }

   public void disconnectPersistenceController()
   {
      throw new UnsupportedOperationException();
   }

   public void fetch()
   {
      // ignore
   }

   public void markModified()
   {
      throw new UnsupportedOperationException();
   }

   public void markModified(String fieldName)
   {
      throw new UnsupportedOperationException();
   }

   public void markCreated()
   {
      throw new UnsupportedOperationException();
   }
   
   public PersistenceController getPersistenceController()
   {
      throw new UnsupportedOperationException();
   }

   public void setPersistenceController(PersistenceController PersistenceController)
   {
      // ignore
   }

   public String toString()
   {
      return "User Realm: " + getId() + " (" + name + ")";
   }
}