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

import java.sql.Connection;
import java.sql.SQLException;

/**
 * A connection hook provides the possibility of being called back
 * on several 'events' on connections
 * 
 * In order to work the following property has to be set:
 * AuditTrailDatabase.ConnectionHookClassName = [class name of implementation]
 * 
 * @see org.eclipse.stardust.engine.core.persistence.jdbc.DefaultConnectionHook
 * 
 * @author sborn
 * @version $Revision$
 */
public interface ConnectionHook
{
   /**
    * This method gets called back on
    * - Session.getJDBCConnection
    * - Session.getSequenceJDBCConnection
    * 
    * @param connection
    * @throws SQLException
    */
   void onGetConnection(Connection connection) throws SQLException;
   
   /**
    * This method gets called back on
    * - Session.returnJDBCConnection
    * - Session.returnSequenceJDBCConnectionToPool
    * 
    * @param connection
    * @throws SQLException
    */
   void onCloseConnection(Connection connection) throws SQLException;
}
