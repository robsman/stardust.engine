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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.common.error.ConcurrencyException;
import org.eclipse.stardust.engine.core.persistence.PersistenceController;


/**
 * @author rsauer
 * @version $Revision$
 */
public class CachedAuditTrailPartitionBean implements IAuditTrailPartition
{
   public static final String PRP_PARTITION_CACHE = CachedAuditTrailPartitionBean.class.getName()
         + ".PartitionCache";

   private final short oid;

   private final String id;

   private final String description;
   
   private final Map cachedDomains;

   public CachedAuditTrailPartitionBean(IAuditTrailPartition delegate)
   {
      this.oid = delegate.getOID();
      this.id = delegate.getId();
      this.description = delegate.getDescription();
      
      this.cachedDomains = new HashMap();
   }

   public short getOID()
   {
      return oid;
   }

   public String getId()
   {
      return id;
   }

   public String getDescription()
   {
      return description;
   }
   
   public IUserDomain findCachedDomain(String domainId)
   {
      return (IUserDomain) cachedDomains.get(domainId);
   }

   public IUserDomain findCachedDomain(long domainOid)
   {
      IUserDomain domain = null;

      for (Iterator i = cachedDomains.values().iterator(); i.hasNext();)
      {
         IUserDomain candiate = (IUserDomain) i.next();
         if (candiate.getOID() == domainOid)
         {
            domain = candiate;
            break;
         }
      }
      
      return domain;
   }

   public void cachedDomain(IUserDomain domain)
   {
      cachedDomains.put(domain.getId(), new CachedUserDomainBean(domain, this));
   }

   public void setOID(short oid)
   {
      throw new UnsupportedOperationException();
   }

   public void setId(String id)
   {
      throw new UnsupportedOperationException();
   }

   public void setDescription(String description)
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

   public void markCreated()
   {
      throw new UnsupportedOperationException();
   }
   
   public void markModified(String fieldName)
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
      return "Partition: " + getId();
   }
}
