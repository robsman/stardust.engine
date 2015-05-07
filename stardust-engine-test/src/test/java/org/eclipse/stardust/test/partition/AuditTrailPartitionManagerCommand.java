/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    roland.stamm (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.test.partition;

import java.io.Serializable;
import java.sql.SQLException;

import org.eclipse.stardust.common.error.ServiceCommandException;
import org.eclipse.stardust.engine.api.pojo.AuditTrailPartitionManager;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.core.runtime.command.ServiceCommand;

public class AuditTrailPartitionManagerCommand implements ServiceCommand
{

   private static final long serialVersionUID = -5628910006664132829L;

   @Override
   public Serializable execute(ServiceFactory sf)
   {
      try
      {
         AuditTrailPartitionManager.createAuditTrailPartition("test_partition", "sysop");
         AuditTrailPartitionManager.dropAuditTrailPartition("test_partition", "sysop");
      }
      catch (SQLException e)
      {
         throw new ServiceCommandException("SQL Error:", e);
      }

      return null;
   }

}
