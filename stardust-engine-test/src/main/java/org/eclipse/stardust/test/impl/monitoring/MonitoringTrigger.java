/**********************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.impl.monitoring;

import java.sql.Connection;
import java.sql.SQLException;

import org.h2.api.Trigger;

/**
 * <p>
 * This class represents the trigger for monitoring
 * database operations.
 * </p>
 *
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class MonitoringTrigger implements Trigger
{
   private TableOperation tableOp;

   /* (non-Javadoc)
    * @see org.h2.api.Trigger#init(java.sql.Connection, java.lang.String, java.lang.String, java.lang.String, boolean, int)
    */
   @Override
   public void init(final Connection connection, final String schemaName, final String triggerName, final String tableName, final boolean before, final int triggerType) throws SQLException
   {
      tableOp = new TableOperation(Operation.fromTriggerType(triggerType), tableName);
   }

   /* (non-Javadoc)
    * @see org.h2.api.Trigger#fire(java.sql.Connection, java.lang.Object[], java.lang.Object[])
    */
   @Override
   public void fire(final Connection connection, final Object[] oldValue, final Object[] newValue) throws SQLException
   {
      DatabaseOperationMonitoring.instance().addTableOperation(tableOp);
   }

   /* (non-Javadoc)
    * @see org.h2.api.Trigger#close()
    */
   @Override
   public void close() throws SQLException
   {
      /* nothing to do */
   }

   /* (non-Javadoc)
    * @see org.h2.api.Trigger#remove()
    */
   @Override
   public void remove() throws SQLException
   {
      /* nothing to do */
   }
}
