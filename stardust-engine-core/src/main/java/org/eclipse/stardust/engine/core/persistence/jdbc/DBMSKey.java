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

import org.eclipse.stardust.common.StringKey;

/**
 * DBMSKey identifies the supported RDBMS.
 *  
 * @author rsauer
 * @version $Revision$
 */
public final class DBMSKey extends StringKey
{
   /**
    *  
    */
   private static final long serialVersionUID = 1L;
   
   public static final DBMSKey GENERIC_JDBC = new DBMSKey("other", "Generic JDBC");
   public static final DBMSKey ORACLE = new DBMSKey("oracle", "Oracle RDBMS");
   public static final DBMSKey ORACLE9i = new DBMSKey("oracle9i", "Oracle RDBMS (9i or higher)");
   public static final DBMSKey DB2_UDB = new DBMSKey("db2", "IBM DB2 UDB");
   public static final DBMSKey DERBY = new DBMSKey("derby", "Apache Derby");
   public static final DBMSKey MSSQL8 = new DBMSKey("mssql8", "Microsoft SQL Server 2000");
   public static final DBMSKey MYSQL = new DBMSKey("mysql", "MySQL Server");
   public static final DBMSKey POSTGRESQL = new DBMSKey("postgresql", "PostgreSQL ORDBMS");
   public static final DBMSKey SYBASE = new DBMSKey("sybase", "Sybase");

   private DBMSKey(String id, String defaultName)
   {
      super(id, defaultName);
   }
}
