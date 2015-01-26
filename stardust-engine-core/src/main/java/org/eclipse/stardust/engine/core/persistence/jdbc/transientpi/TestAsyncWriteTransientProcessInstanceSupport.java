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
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor;
import org.eclipse.stardust.engine.core.persistence.jms.BlobBuilder;
import org.eclipse.stardust.engine.core.persistence.jms.ByteArrayBlobBuilder;

/**
 * <p>
 * An {@link AbstractTransientProcessInstanceSupport} implementation that's used for testing the <i>Write Behind</i>
 * functionality: it just writes the collected data into the db instead of sending it via <i>JMS</i>.
 * </p>
 *
 * @author Nicolas.Werlein
 */
public class TestAsyncWriteTransientProcessInstanceSupport extends AbstractTransientProcessInstanceSupport
{
   @Override
   public void addPersistentToBeInserted(final List<Persistent> persistentsToBeInserted)
   {
      /* no-op */
   }

   @Override
   public void addPersistentToBeDeleted(final Persistent persistentToBeDeleted)
   {
      /* no-op */
   }

   @Override
   public void writeToBlob(final BlobBuilder blobBuilder, final TypeDescriptor typeDesc)
   {
      /* no-op */
   }

   @Override
   public void cleanUpInMemStorage()
   {
      /* no-op */
   }

   @Override
   public void storeBlob(final BlobBuilder blobBuilder, final Session session, final Parameters parameters)
   {
      final ByteArrayBlobBuilder bb = castToByteArrayBlobBuilder(blobBuilder);
      writeOneBlobToAuditTrail(bb, session, parameters);
   }

   @Override
   public boolean arePisTransientExecutionCandidates()
   {
      return false;
   }

   @Override
   public boolean isCurrentSessionTransient()
   {
      return false;
   }

   @Override
   public boolean areAllPisCompleted()
   {
      return false;
   }

   @Override
   public boolean isTransientExecutionCancelled()
   {
      return false;
   }

   @Override
   public boolean persistentsNeedToBeWrittenToBlob()
   {
      return false;
   }

   @Override
   public BlobBuilder newBlobBuilder()
   {
      return new ByteArrayBlobBuilder();
   }
}
