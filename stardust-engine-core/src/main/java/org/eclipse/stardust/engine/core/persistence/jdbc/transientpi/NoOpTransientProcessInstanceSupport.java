/**********************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.engine.core.persistence.jdbc.transientpi;

import java.util.List;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.core.persistence.Persistent;
import org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor;
import org.eclipse.stardust.engine.core.persistence.jms.BlobBuilder;

/**
 * <p>
 * TODO JavaDoc
 * </p>
 *
 * @author Nicolas.Werlein
 */
public class NoOpTransientProcessInstanceSupport extends AbstractTransientProcessInstanceSupport
{
   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.AbstractTransientProcessInstanceSupport#persistentsNeedToBeWrittenToBlob()
    */
   @Override
   public boolean persistentsNeedToBeWrittenToBlob()
   {
      return false;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.AbstractTransientProcessInstanceSupport#isCurrentSessionTransient()
    */
   @Override
   public boolean isCurrentSessionTransient()
   {
      return false;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.AbstractTransientProcessInstanceSupport#areAllPisCompleted()
    */
   @Override
   public boolean areAllPisCompleted()
   {
      return false;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.AbstractTransientProcessInstanceSupport#arePisTransientExecutionCandidates()
    */
   @Override
   public boolean arePisTransientExecutionCandidates()
   {
      return false;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.AbstractTransientProcessInstanceSupport#isTransientExecutionCancelled()
    */
   @Override
   public boolean isTransientExecutionCancelled()
   {
      return false;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.AbstractTransientProcessInstanceSupport#addPersistentToBeInserted(java.util.List)
    */
   @Override
   public void addPersistentToBeInserted(final List<Persistent> persistentsToBeInserted)
   {
      /* no-op */
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.AbstractTransientProcessInstanceSupport#writeToBlob(org.eclipse.stardust.engine.core.persistence.jms.BlobBuilder, org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor)
    */
   @Override
   public void writeToBlob(final BlobBuilder blobBuilder, final TypeDescriptor typeDesc)
   {
      /* no-op */
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.AbstractTransientProcessInstanceSupport#addPersistentToBeDeleted(org.eclipse.stardust.engine.core.persistence.Persistent)
    */
   @Override
   public void addPersistentToBeDeleted(final Persistent persistentToBeDeleted)
   {
      /* no-op */
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.AbstractTransientProcessInstanceSupport#cleanUpInMemStorage()
    */
   @Override
   public void cleanUpInMemStorage()
   {
      /* no-op */
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.AbstractTransientProcessInstanceSupport#writeToInMemStorage(org.eclipse.stardust.engine.core.persistence.jms.BlobBuilder)
    */
   @Override
   public void writeToInMemStorage(final BlobBuilder blobBuilder)
   {
      /* no-op */
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.AbstractTransientProcessInstanceSupport#isDeferredPersist()
    */
   @Override
   public boolean isDeferredPersist()
   {
      return false;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.AbstractTransientProcessInstanceSupport#newBlobBuilder()
    */
   @Override
   public BlobBuilder newBlobBuilder()
   {
      return new NoOpBlobBuilder();
   }

   /**
    * <p>
    * A {@link BlobBuilder} implementation that does nothing, i.e. all operations are no-ops.
    * </p>
    *
    * @author Nicolas.Werlein
    */
   private static final class NoOpBlobBuilder implements BlobBuilder
   {
      /* (non-Javadoc)
       * @see org.eclipse.stardust.engine.core.persistence.jms.BlobBuilder#init(org.eclipse.stardust.common.config.Parameters)
       */
      @Override
      public void init(final Parameters params)
      {
         /* no-op */
      }

      /* (non-Javadoc)
       * @see org.eclipse.stardust.engine.core.persistence.jms.BlobBuilder#persistAndClose()
       */
      @Override
      public void persistAndClose()
      {
         /* no-op */
      }

      /* (non-Javadoc)
       * @see org.eclipse.stardust.engine.core.persistence.jms.BlobBuilder#startInstancesSection(java.lang.String, int)
       */
      @Override
      public void startInstancesSection(final String tableName, final int nInstances)
      {
         /* no-op */
      }

      /* (non-Javadoc)
       * @see org.eclipse.stardust.engine.core.persistence.jms.BlobBuilder#writeBoolean(boolean)
       */
      @Override
      public void writeBoolean(final boolean value)
      {
         /* no-op */
      }

      /* (non-Javadoc)
       * @see org.eclipse.stardust.engine.core.persistence.jms.BlobBuilder#writeByte(byte)
       */
      @Override
      public void writeByte(final byte value)
      {
         /* no-op */
      }

      /* (non-Javadoc)
       * @see org.eclipse.stardust.engine.core.persistence.jms.BlobBuilder#writeChar(char)
       */
      @Override
      public void writeChar(final char value)
      {
         /* no-op */
      }

      /* (non-Javadoc)
       * @see org.eclipse.stardust.engine.core.persistence.jms.BlobBuilder#writeDouble(double)
       */
      @Override
      public void writeDouble(final double value)
      {
         /* no-op */
      }

      /* (non-Javadoc)
       * @see org.eclipse.stardust.engine.core.persistence.jms.BlobBuilder#writeFloat(float)
       */
      @Override
      public void writeFloat(final float value)
      {
         /* no-op */
      }

      /* (non-Javadoc)
       * @see org.eclipse.stardust.engine.core.persistence.jms.BlobBuilder#writeInt(int)
       */
      @Override
      public void writeInt(final int value)
      {
         /* no-op */
      }

      /* (non-Javadoc)
       * @see org.eclipse.stardust.engine.core.persistence.jms.BlobBuilder#writeLong(long)
       */
      @Override
      public void writeLong(final long value)
      {
         /* no-op */
      }

      /* (non-Javadoc)
       * @see org.eclipse.stardust.engine.core.persistence.jms.BlobBuilder#writeShort(short)
       */
      @Override
      public void writeShort(final short value)
      {
         /* no-op */
      }

      /* (non-Javadoc)
       * @see org.eclipse.stardust.engine.core.persistence.jms.BlobBuilder#writeString(java.lang.String)
       */
      @Override
      public void writeString(final String value)
      {
         /* no-op */
      }
   }
}
