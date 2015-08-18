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

import org.eclipse.stardust.engine.core.persistence.PersistenceController;
import org.eclipse.stardust.engine.core.persistence.Persistent;
import org.eclipse.stardust.engine.core.persistence.PhantomException;

/**
 * The class persistent delegates all persistence management to its persistence
 * controller.
 */
public abstract class PersistentBean implements Persistent
{
   private PersistenceController persistenceController;

   public void delete()
   {
      delete(false);
   }
   
   public void delete(boolean writeThrough)
   {
      if (persistenceController != null)
      {
         persistenceController.markDeleted(writeThrough);
      }
   }

   public PersistenceController getPersistenceController()
   {
      return persistenceController;
   }

   /**
    *
    */
   public void setPersistenceController(PersistenceController persistenceController)
   {
      this.persistenceController = persistenceController;
   }

   public void disconnectPersistenceController()
   {
      this.persistenceController = null;
   }

   /**
    *
    */
   public void fetchLink(String linkName)
   {
      if (persistenceController != null)
      {
         persistenceController.fetchLink(linkName);
      }
   }

   public void fetchVector(String vectorName)
   {
      if (persistenceController != null)
      {
         persistenceController.fetchVector(vectorName);
      }
   }

   public void markModified()
   {
      if (persistenceController != null)
      {
         persistenceController.markModified();
      }
   }
   
   public void markCreated()
   {
      if (persistenceController != null)
      {
         persistenceController.markCreated();
      }
   }

   public void markModified(String fieldName)
   {
      if (persistenceController != null)
      {
         persistenceController.markModified(fieldName);
      }
   }
   
   public void fetch()
   {
      if (persistenceController != null)
      {
         persistenceController.fetch();
      }
   }

   public void reload() throws PhantomException
   {
      if (persistenceController != null)
      {
         persistenceController.reload();
      }
   }

   public void reloadAttribute(String attributeName) throws PhantomException
   {
      if (isPersistent())
      {
         persistenceController.reloadAttribute(attributeName);
      }
   }


   public boolean isPersistent()
   {
      return persistenceController != null;
   }

}


