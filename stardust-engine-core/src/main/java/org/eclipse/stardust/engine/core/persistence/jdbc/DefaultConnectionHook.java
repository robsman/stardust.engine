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

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;


/**
 * This class is only for demonstration purposes and its methods get called
 * on the following method calls:
 * - Session.getJDBCConnection (onGetConnection)
 * - Session.returnJDBCConnection (onCloseConnection)
 * - Session.getSequenceJDBCConnection (onGetConnection)
 * - Session.returnSequenceJDBCConnectionToPool (onCloseConnection)
 * 
 * In order to work the following property has to be set:
 * AuditTrail.ConnectionHookClassName = org.eclipse.stardust.engine.core.persistence.jdbc.DefaultConnectionHook
 * 
 * @author sborn
 * @version $Revision$
 */
public class DefaultConnectionHook implements ConnectionHook
{
   private static final transient Logger trace = LogManager.getLogger(Reflect.class);

   public void onGetConnection(Connection connection) throws SQLException
   {
      trace.debug("Called DefaultConnectionHook.onGetConnection for connection(hashCode): "
            + connection.hashCode());
   }

   public void onCloseConnection(Connection connection) throws SQLException
   {
      trace.debug("Called DefaultConnectionHook.onCloseConnection for connection(hashCode): "
            + connection.hashCode());
   }

}
