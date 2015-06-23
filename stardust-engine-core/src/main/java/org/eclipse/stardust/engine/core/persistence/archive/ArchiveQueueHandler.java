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
package org.eclipse.stardust.engine.core.persistence.archive;

import javax.jms.ObjectMessage;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.LoginUtils;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;



/**
 * Sends messages containing ExportResults received from jms/CarnotArchiveQueue to ExportProcessesCommand for archiving
 * @author jsaayman
 * @version $Revision$
 */
public class ArchiveQueueHandler extends SecurityContextAwareAction
{
   private static final Logger trace = LogManager.getLogger(ArchiveQueueHandler.class);

   private final ObjectMessage message;

   private final String partitionId;

   public ArchiveQueueHandler(ArchiveQueueHandlerCarrier carrier)
   {
      super(carrier);

      this.partitionId = carrier.getPartitionId();
      this.message = carrier.getMessage();
   }

   private void configureRuntimeEnvironment(BpmRuntimeEnvironment rtEnv)
   {
      final Parameters params = Parameters.instance();

      if (trace.isDebugEnabled())
      {
         trace.debug("Received archive message for partition: " + partitionId);
      }
      IAuditTrailPartition partition = LoginUtils.findPartition(params, partitionId);
      setPartitionOid(partition.getOID());
      rtEnv.setProperty(SecurityProperties.CURRENT_PARTITION, partition);
      rtEnv.setProperty(SecurityProperties.CURRENT_PARTITION_OID, Short.valueOf(getPartitionOid()));

      UserRealmBean transientRealm = UserRealmBean.createTransientRealm(
            PredefinedConstants.SYSTEM_REALM, PredefinedConstants.SYSTEM_REALM, partition);
      IUser transientUser = UserBean.createTransientUser(PredefinedConstants.SYSTEM,
            PredefinedConstants.SYSTEM_FIRST_NAME, PredefinedConstants.SYSTEM_LAST_NAME,
            transientRealm);
      rtEnv.setProperty(SecurityProperties.CURRENT_USER, transientUser);

   }

   public Object execute()
   {
      final BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();

      configureRuntimeEnvironment(rtEnv);

      final ExportProcessesCommand command = new ExportProcessesCommand(message);
      return new WorkflowServiceImpl().execute(command);

   }

}