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

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.persistence.PersistenceController;
import org.eclipse.stardust.engine.core.persistence.Persistent;
import org.eclipse.stardust.engine.core.persistence.PhantomException;


/**
 * Holds the information needed to manage the persistence of the
 * Java object, which is an instance of <tt>Persistent</tt>.
 * <p>
 * The link buffers for a default persistence controller are created
 * with the constructor depending on the type structure of the corresponding
 * persistent object. Their content might be changed later due to
 * closes and (re)opens, but their memory will never be disallocated.
 */
public class DefaultPersistenceController implements PersistenceController
{
   private static final Logger trace = LogManager.getLogger(DefaultPersistenceController.class);
   
   public static final Object[] NO_LINK_BUFFER = new Object[0];

   public static final boolean[] NO_LINK_FLAGS = new boolean[0];

   /**
    * A persistent object has the state <tt>CLOSED</tt>, if it is still in the
    * buffer, but a transaction has been recently committed. Accessing the object again
    * requires reload against the database table.
    */
   public static final byte CLOSED = 0;
   public static final byte OPENED = 1;
   public static final byte MODIFIED = 2;
   public static final byte CREATED = 3;
   public static final byte DELETED = 4;
   public static final byte DELETED_WAS_TRANSIENT = 5;

   private byte state;

   private final Session session;
   private final TypeDescriptor typeDescriptor;

   private Persistent persistent;
   
   private boolean locked;

   private Set<String> modifiedFields;

   private Object[] linkBuffer;

   /**
    * Indicates, whether a link is already loaded for the transaction.
    */
   private final boolean[] linkFlags;

   /**
    * Indicates, whether a vector is already loaded for the transaction.
    */
   private final boolean[] vectorFlags;

   /**
    * <tt>linkBuffer</tt> may be null.
    */
   public DefaultPersistenceController(Session session, TypeDescriptor typeDescriptor,
         Persistent persistent, Object[] linkBuffer)
   {
      this.session = session;
      this.typeDescriptor = typeDescriptor;
      
      this.persistent = persistent;
      this.linkBuffer = linkBuffer;
      this.linkFlags = (0 == linkBuffer.length)
            ? NO_LINK_FLAGS
            : new boolean[linkBuffer.length];
      List<PersistentVectorDescriptor> persistentVectors = typeDescriptor.getPersistentVectors();
      this.vectorFlags = (persistentVectors.isEmpty())
            ? NO_LINK_FLAGS
            : new boolean[persistentVectors.size()];

      // Newly created objects are not modified, because the are recently inserted.

      this.state = OPENED;

      persistent.setPersistenceController(this);
   }

   /**
    *
    */
   public org.eclipse.stardust.engine.core.persistence.Session getSession()
   {
      return session;
   }
   
   public TypeDescriptor getTypeDescriptor()
   {
      return typeDescriptor;
   }

   /**
    *
    */
   public Object[] getLinkBuffer()
   {
      return linkBuffer;
   }

   public Object getLinkFk(String linkName)
   {
      TypeDescriptor td = (null != session)
            ? session.getTypeDescriptor(persistent.getClass())
            : TypeDescriptor.get(persistent.getClass());
      
      return linkBuffer[td.getLinkIdx(linkName)];
   }

   /**
    *
    */
   public void fetch()
   {
      if (isClosed())
      {
         if (trace.isDebugEnabled())
         {
            trace.debug("Fetch required for " + persistent.getClass().getName());
         }

         open();
      }
   }

   /**
    *
    */
   public void fetchLink(String linkName)
   {
      fetch();
      session.fetchLink(persistent, linkName);

      if (trace.isDebugEnabled())
      {
         trace.debug("Link " + linkName + " fetched.");
      }
   }

   /**
    *
    */
   public void fetchVector(String vectorName)
   {
      fetch();
      if (session.fetchVector(persistent, vectorName) && trace.isDebugEnabled())
      {
         trace.debug("Vector " + vectorName + " fetched.");
      }
   }

   public void markAllLinksFetched()
   {
      for (int i = 0; i < linkFlags.length; i++ )
      {
         linkFlags[i] = true;
      }
   }

   public void markLinkFetched(int linkIdx)
   {
      linkFlags[linkIdx] = true;
   }

   public boolean isLinkFetched(int linkIdx)
   {
      return linkFlags[linkIdx];
   }

   public void markAllVectorsFetched()
   {
      for (int i = 0; i < vectorFlags.length; i++ )
      {
         vectorFlags[i] = true;
      }
   }

   public void markVectorFetched(int vectorIdx)
   {
      vectorFlags[vectorIdx] = true;
   }

   public boolean isVectorFetched(int vectorIdx)
   {
      return vectorFlags[vectorIdx];
   }

   public Persistent getPersistent()
   {
      return persistent;
   }

   /**
    *
    */
   public void markDeleted()
   {
      markDeleted(false);
   }
   
   public void markDeleted(boolean writeThrough)
   {
      session.markDeleted(this, writeThrough && !isCreated());
      
      this.state = isCreated() ? DELETED_WAS_TRANSIENT : DELETED;
      
      if (trace.isDebugEnabled())
      {
         trace.debug("Persistent object " + persistent.getClass().getName() + " marked deleted.");
      }
   }

   public boolean isLocked()
   {
      // created objects are not yet in the DBMS, so there is no need to lock
      return isCreated() || locked;
   }

   public void markLocked()
   {
      this.locked = true;
   }

   /**
    *
    */
   public void markModified()
   {
      fetch();
      
       
      // only mark modified if the object is not in CREATED state  
      if ( !isCreated())
      {
         this.state = MODIFIED;
         // set all fields to modified
         this.modifiedFields = null;
   
         session.markModified(this);
   
         if (trace.isDebugEnabled())
         {
            trace.debug("Persistent object " + persistent.getClass().getName()
                  + " marked modified.");
         }
      }
   }

   public void markModified(String fieldName)
   {
      fetch();

      // only mark modified if the object is not in CREATED state  
      if ( !isCreated())
      {
         if ( !isModified())
         {
            this.state = MODIFIED;
            this.modifiedFields = CollectionUtils.newSet();
         }
   
         if (isModified() && (null != modifiedFields))
         {
            modifiedFields.add(fieldName);
         }
         else
         {
            // fall back to old behaviour of updating all fields
            markModified();
         }
      }
   }

   /**
    *
    */
   public void markCreated()
   {
      state = CREATED;
   }

   public void open()
   {
      if (state == OPENED || state == MODIFIED)
      {
         return;
      }
      
      if (CLOSED == state)
      {
         for (int i = 0; i < linkFlags.length; i++ )
         {
            linkFlags[i] = false;
         }
      }

      Assert.isNotNull(session, "Session is not null.");
      Assert.isNotNull(persistent, "Reference to persistent object is not null.");

      state = OPENED;

      // Rebind the persistence controller with the session

      session.addToPersistenceControllers(
            session.getTypeDescriptor(persistent.getClass()).getIdentityKey(persistent),
            this);

      try
      {
         reload();
      }
      catch (PhantomException e)
      {
         throw new InternalException(e);
      }

      if (trace.isDebugEnabled())
      {
         trace.debug("Persistent object " + persistent.getClass().getName() + " opened.");
      }
   }

   public void close()
   {
      if (CLOSED != state)
      {
         this.state = CLOSED;
         this.locked = false;
         
         if (trace.isDebugEnabled())
         {
            trace.debug("Persistent object " + persistent.getClass().getName() + " closed.");
         }
      }
   }

   public boolean isClosed()
   {
      return state == CLOSED;
   }

   public boolean isModified()
   {
      return state == MODIFIED;
   }

   public Set<String> getModifiedFields()
   {
      return (null != modifiedFields)
            ? Collections.unmodifiableSet(modifiedFields)
            : null;
   }

   public boolean isDeleted()
   {
      return (DELETED == state) || (DELETED_WAS_TRANSIENT == state);
   }

   public boolean isTransientlyDeleted()
   {
      return state == DELETED_WAS_TRANSIENT;
   }

   public boolean isCreated()
   {
      return state == CREATED;
   }

   public void reload() throws PhantomException
   {
      // only reload if not in CREATED state (CREATED means not yet in DB)
      if ( !isCreated()) 
      {
         session.reloadObject(persistent);
      }
   }

   public void reloadAttribute(String attributeName) throws PhantomException
   {
      // only reload if not in CREATED state (CREATED means not yet in DB)
      if ( !isCreated()) 
      {
         session.reloadAttribute(persistent, attributeName);
      }
   }

   public void setLinkBuffer(Object[] linkBuffer)
   {
      this.linkBuffer = linkBuffer;
   }
}
