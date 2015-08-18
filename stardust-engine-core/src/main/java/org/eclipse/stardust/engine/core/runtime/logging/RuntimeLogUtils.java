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
package org.eclipse.stardust.engine.core.runtime.logging;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.core.runtime.beans.IAuditTrailPartition;
import org.eclipse.stardust.engine.core.runtime.beans.IUserDomain;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;


/**
 * @author sborn
 * @version $Revision$
 */
public final class RuntimeLogUtils
{
   private static final ISqlTimeRecorder defaultSqlTimeRecorder = new NullSqlTimeRecorder();
   
   public static void logSecurityContext()
   {
      if (RuntimeLog.SECURITY.isInfoEnabled())
      {
         IAuditTrailPartition partition = SecurityProperties.getPartition();

         if (null != partition)
         {
               IUserDomain domain = SecurityProperties.getUserDomain();

               RuntimeLog.SECURITY.info(partition + ", " + domain + ".");
         }
         else
         {
            Short partitionOid = new Short(SecurityProperties.getPartitionOid());
            Long domainOid = new Long(SecurityProperties.getUserDomainOid());

            RuntimeLog.SECURITY.info("PartitionOid: " + partitionOid + ", DomainOid: "
                  + domainOid + ".");
         }
      }
   }
   
   public static ISqlTimeRecorder getSqlTimeRecorder(Parameters params)
   {
      if (null == params)
      {
         params = Parameters.instance();
      }

      ISqlTimeRecorder recorder = (ISqlTimeRecorder) params
            .get(ISqlTimeRecorder.PRP_SQL_TIME_RECORDER);
      if (null == recorder)
      {
         // if no recorder is set then use and set default SQL recorder which is doing nothing
         recorder = defaultSqlTimeRecorder;
         params.set(ISqlTimeRecorder.PRP_SQL_TIME_RECORDER, recorder);
      }

      return recorder;
   }

   private RuntimeLogUtils()
   {
      // Utility class with static methods only.
   }
}
