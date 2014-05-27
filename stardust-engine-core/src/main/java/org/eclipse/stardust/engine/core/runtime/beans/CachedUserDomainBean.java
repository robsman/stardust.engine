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

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.ConcurrencyException;
import org.eclipse.stardust.engine.api.runtime.LoginUtils;
import org.eclipse.stardust.engine.core.persistence.PersistenceController;


/**
 * @author rsauer
 * @version $Revision$
 */
public class CachedUserDomainBean implements IUserDomain
{
   private final long oid;

   private final String id;

   private final String description;

   private final IAuditTrailPartition partition;

   private final String superDomainId;

   public CachedUserDomainBean(IUserDomain delegate,
         CachedAuditTrailPartitionBean partition)
   {
      this.oid = delegate.getOID();
      this.id = delegate.getId();
      this.description = delegate.getDescription();

      this.partition = partition;
      this.superDomainId = (null != delegate.getSuperDomain())
            ? delegate.getSuperDomain().getId()
            : null;
   }

   public long getOID()
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

   public IAuditTrailPartition getPartition()
   {
      return partition;
   }

   public IUserDomain getSuperDomain()
   {
      return (null != superDomainId) //
            ? LoginUtils.findUserDomain(Parameters.instance(), getPartition(),
                  superDomainId)
            : null;
   }

   public void setOID(long oid)
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

   public void lock(int timeout) throws ConcurrencyException
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
      return "Domain: " + getId();
   }

}
