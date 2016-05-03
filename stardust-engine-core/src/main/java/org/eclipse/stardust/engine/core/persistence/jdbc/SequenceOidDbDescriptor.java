/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Antje.Fuhrmann (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.persistence.jdbc;

public class SequenceOidDbDescriptor extends SequenceDbDriver
{
   private final DBDescriptor dbDescriptor;

   public SequenceOidDbDescriptor(DBDescriptor dbDescriptor)
   {
      this.dbDescriptor = dbDescriptor;
   }

   @Override
   public DBMSKey getDbmsKey()
   {
      return dbDescriptor.getDbmsKey();
   }

   @Override
   public String getCreatePKSequenceStatementString(String schemaName, String pkSequence,
         String initialValueExpr)
   {
      return dbDescriptor.getCreatePKSequenceStatementString(schemaName, pkSequence,
            initialValueExpr);
   }

   @Override
   public String getDropPKSequenceStatementString(String schemaName, String pkSequence)
   {
      return dbDescriptor.getDropPKSequenceStatementString(schemaName, pkSequence);
   }

   @Override
   public String getCreatePKStatement(String schemaName, String pkSequence,
         int sequenceCount)
   {
      return dbDescriptor.getCreatePKStatement(schemaName, pkSequence, sequenceCount);
   }

   @Override
   public String getCreatePKStatement(String schemaName, String pkSequence)
   {
      return dbDescriptor.getCreatePKStatement(schemaName, pkSequence);
   }

   @Override
   public String getNextValForSeqString(String schemaName, String sequenceName)
   {
      return dbDescriptor.getNextValForSeqString(schemaName, sequenceName);
   }

   @Override
   public String getSQLType(Class type, long length)
   {
      return dbDescriptor.getSQLType(type, length);
   }

   @Override
   public boolean useQueryTimeout()
   {
      return dbDescriptor.useQueryTimeout();
   }

   @Override
   public boolean useAnsiJoins()
   {
      return dbDescriptor.useAnsiJoins();
   }

   @Override
   public boolean isLockRowStatementSQLQuery()
   {
      return dbDescriptor.isLockRowStatementSQLQuery();
   }

   @Override
   public String getLockRowStatementString(SqlUtils sqlUtils, TypeDescriptor type,
         boolean tryToUseDistinctLockTable, String predicate)
   {
      return dbDescriptor.getLockRowStatementString(sqlUtils, type,
            tryToUseDistinctLockTable, predicate);
   }

   @Override
   public boolean supportsSubselects()
   {
      return dbDescriptor.supportsSubselects();
   }

}
