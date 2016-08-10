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
package org.eclipse.stardust.engine.core.persistence.jdbc.sequence;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor;

/**
 * This implementation uses the CachingSequenceGenerator, just with a different DataSource bean
 * "carnotAuditTrailSequenceDataSource".
 */
public class NonXADataSourceSequenceGenerator extends CachingSequenceGenerator
{
   private static final String AUDIT_TRAIL_SEQUENCE = "AuditTrail.Sequence";

   private static final Logger trace = LogManager.getLogger(NonXADataSourceSequenceGenerator.class);

   public NonXADataSourceSequenceGenerator()
   {}

   @Override
   public long getNextSequence(TypeDescriptor typeDescriptor, Session outerSession)
   {
      DataSource dataSource = SessionFactory.obtainDataSource(AUDIT_TRAIL_SEQUENCE, true);
      Session session = SessionFactory.createSession(AUDIT_TRAIL_SEQUENCE, dataSource);

      long nextSequence;
      try
      {
         if (session == null)
         {
            nextSequence = super.getNextSequence(typeDescriptor, outerSession);
         }
         else
         {
            nextSequence = super.getNextSequence(typeDescriptor, session);
         }
      }
      finally
      {
         try
         {
            if (session != null)
            {
               session.disconnect();
            }
         }
         catch (SQLException e)
         {
            trace.warn("Exception occurred trying to close the session: " + e.getMessage());
         }
      }

      return nextSequence;
   }

}
