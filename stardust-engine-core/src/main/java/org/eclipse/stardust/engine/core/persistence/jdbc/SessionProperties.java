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

public interface SessionProperties
{
   final String DS_NAME_AUDIT_TRAIL = "AuditTrail";
   final String DS_NAME_PWH = "PWH";
   
   final String DS_DATA_SOURCE_SUFFIX = ".DataSource";
   final String DS_SESSION_SUFFIX = ".Session";

   final String DS_DRIVER_CLASS_SUFFIX = ".DriverClass";
   final String DS_URL_SUFFIX = ".URL";
   final String DS_TYPE_SUFFIX = ".Type";
   final String DS_SCHEMA_SUFFIX = ".Schema";
   final String DS_USER_SUFFIX = ".User";
   final String DS_PASSWORD_SUFFIX = ".Password";
   final String DS_FIX_AUTO_COMMIT_SUFFIX = ".FixAutoCommit";
   final String DS_USE_PREPARED_STATEMENTS_SUFFIX = ".UsePreparedStatements";
   final String OPT_MIXED_PREPARED_STATEMENTS = "mixed";
   final String DS_USE_LOCK_TABLES_SUFFIX = ".UseLockTables";
   final String DS_USE_EAGER_LINK_FETCH_SUFFIX = ".EagerLinkFetching";
   final String DS_USE_JDBC_RETURN_GENERATED_KEYS = ".UseJdbcReturnGeneratedKeys";
   
   final String DS_INTERCEPT_JDBC_CALLS_SUFFIX = ".InterceptJdbcCalls";
   final String DS_MONITOR_ON_JDBC_INTERCEPTION_SUFFIX = ".MonitorOnJdbcInterception";

   final String DS_CONNECTION_HOOK_SUFFIX = ".ConnectionHookClassName";
   final String DS_ASSERT_READ_COMMITTED_SUFFIX = ".AssertReadCommitted";
}
