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
package org.eclipse.stardust.engine.core.persistence.jdbc;

import java.io.Serializable;

import org.eclipse.stardust.common.error.ConcurrencyException;
import org.eclipse.stardust.engine.core.persistence.IdentifiablePersistent;
import org.eclipse.stardust.engine.core.persistence.PersistenceController;


/**
 * Base class for all objects becoming persistent during workflow runtime.
 * <p>
 * Must proxy/overwrite superclass methods to reflect transient
 * data management.
 */
public class IdentifiablePersistentBean extends PersistentBean
      implements IdentifiablePersistent, Serializable
{
   public static final String FIELD__OID = "oid";

   protected Long oid;

   public long getOID()
   {
      fetch();
      return (null != oid) ? oid.longValue() : 0L;
   }

   public void setOID(long oid)
   {
      if ((null == this.oid) || (this.oid.longValue() != oid))
      {
         markModified(FIELD__OID);
         this.oid = new Long(oid);
      }
   }

   public void lock() throws ConcurrencyException
   {
      lockInternal(null);
   }

   public void lock(int timeout) throws ConcurrencyException
   {
      lockInternal(Integer.valueOf(timeout));
   }

   private void lockInternal(Integer timeout) throws ConcurrencyException
   {
      final PersistenceController pc = getPersistenceController();

      // in state CREATED, the entity is not yet in the DB
      if (isPersistent() && !pc.isCreated())
      {
         if ( !pc.isLocked())
         {
            if (timeout != null)
            {
               pc.getSession().lock(getClass(), getOID(), timeout.intValue());
            }
            else
            {
               pc.getSession().lock(getClass(), getOID());
            }

            // no concurrency exception, object is exclusively locked in this session now
            pc.markLocked();
         }
      }
   }
}
