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
package org.eclipse.stardust.engine.api.runtime;

import static java.util.Collections.singletonMap;

import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.error.ConcurrencyException;
import org.eclipse.stardust.engine.core.persistence.PersistenceController;
import org.eclipse.stardust.engine.core.runtime.beans.IAuditTrailPartition;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;


public class MultiPartitionTestSupport
{
   public static MockPartition newMockPartition(String partitionId)
   {
      MockPartition mockPartition = new MockPartition();
      mockPartition.setId(partitionId);

      return mockPartition;
   }

   public static void withinMockPartition(String partitionId, Runnable callback)
   {
      ParametersFacade.pushLayer(singletonMap(SecurityProperties.CURRENT_PARTITION,
            newMockPartition(partitionId)));
      try
      {
         callback.run();
      }
      finally
      {
         ParametersFacade.popLayer();
      }
   }

   public static class MockPartition implements IAuditTrailPartition
   {
      private String id;
      private short oid;
      private String description;

      public MockPartition()
      {
      }

      public String getId()
      {
         return id;
      }

      public void setId(String id)
      {
         this.id = id;
      }

      public short getOID()
      {
         return oid;
      }

      public void setOID(short oid)
      {
         this.oid = oid;
      }

      public String getDescription()
      {
         return description;
      }

      public void setDescription(String description)
      {
         this.description = description;
      }

      public void markModified(String fieldName)
      {
      }

      public void markModified()
      {
      }

      public void markCreated()
      {
      }

      public PersistenceController getPersistenceController()
      {
         return null;
      }

      public void setPersistenceController(
            PersistenceController PersistenceController)
      {
      }

      public void disconnectPersistenceController()
      {
      }

      public void fetch()
      {
      }

      public void delete(boolean writeThrough)
      {
      }

      public void delete()
      {
      }

      public void lock() throws ConcurrencyException
      {
      }
   }

}
