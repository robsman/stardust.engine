/**********************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.engine.core.persistence.jdbc;

import org.eclipse.stardust.common.StringUtils;

/**
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class MySqlSeqDbDescriptor extends SequenceDbDriver
{
   private static final String GLOBAL_PK_SEQUENCE_TABLE_NAME = "sequence";
   private static final String GLOBAL_PK_SEQUENCE_TABLE_FIELD_NAME_NAME = "name";
   private static final String GLOBAL_PK_SEQUENCE_TABLE_FIELD_VALUE_NAME = "value";
   
   private static final String SEQUENCE_STORED_PROCEDURE_NAME = "next_sequence";
   
   private final MySqlDbDescriptor delegate;
   
   public MySqlSeqDbDescriptor()
   {
      delegate = new MySqlDbDescriptor();
   }
   
   @Override
   public DBMSKey getDbmsKey()
   {
      return DBMSKey.MYSQL_SEQ;
   }

   @Override
   public String getCreatePKSequenceStatementString(final String schemaName, final String pkSequence, final String initialValueExpr)
   {
      /* does not create a sequence, but initializes a row in the global sequence table */
      
      final StringBuffer sb = new StringBuffer(100);
      
      sb.append("INSERT INTO ");
      if ( !StringUtils.isEmpty(schemaName))
      {
         sb.append(schemaName).append(".");
      }      
      sb.append(GLOBAL_PK_SEQUENCE_TABLE_NAME).append(" VALUES ('");
      sb.append(pkSequence).append("', ");
      if ( !StringUtils.isEmpty(initialValueExpr))
      {
         sb.append(initialValueExpr);
      }
      else
      {
         sb.append("0");
      }
      sb.append(")");
      
      return sb.toString();
   }

   @Override
   public String getDropPKSequenceStatementString(final String schemaName, final String pkSequence)
   {
      /* nothing to do */
      return null;
   }

   @Override
   public String getCreatePKStatement(final String schemaName, final String pkSequence, final int sequenceCount)
   {
      // TODO (nw) support retrieving more than one sequence
      throw new UnsupportedOperationException("NYI");
   }

   @Override
   public String getCreatePKStatement(final String schemaName, final String pkSequence)
   {
      final StringBuffer sb = new StringBuffer(100);
      
      sb.append("SELECT ");
      writeSequenceStoredProcedureCall(sb, schemaName, pkSequence);
      
      return sb.toString();
   }

   @Override
   public String getNextValForSeqString(final String schemaName, final String sequenceName)
   {
      final StringBuffer sb = new StringBuffer(100);
      
      writeSequenceStoredProcedureCall(sb, schemaName, sequenceName);
      
      return sb.toString();
   }

   private void writeSequenceStoredProcedureCall(final StringBuffer sb, final String schemaName, final String sequenceName)
   {
      if ( !StringUtils.isEmpty(schemaName))
      {
         sb.append(schemaName).append(".");
      }
      sb.append(SEQUENCE_STORED_PROCEDURE_NAME).append("('").append(sequenceName).append("')");
   }
   
   @Override
   public String getSQLType(final Class type, final long length)
   {
      return delegate.getSQLType(type, length);
   }

   @Override
   public boolean useQueryTimeout()
   {
      return delegate.useQueryTimeout();
   }

   @Override
   public boolean useAnsiJoins()
   {
      return delegate.useAnsiJoins();
   }

   @Override
   public boolean isLockRowStatementSQLQuery()
   {
      return delegate.isLockRowStatementSQLQuery();
   }

   @Override
   public String getLockRowStatementString(final SqlUtils sqlUtils, final TypeDescriptor type, final boolean tryToUseDistinctLockTable, final String predicate)
   {
      return delegate.getLockRowStatementString(sqlUtils, type, tryToUseDistinctLockTable, predicate);
   }

   @Override
   public boolean supportsSubselects()
   {
      return delegate.supportsSubselects();
   }
   
   @Override
   public String getCreateIndexStatement(final String schemaName, final String tableName, final IndexDescriptor indexDescriptor)
   {
      return delegate.getCreateIndexStatement(schemaName, tableName, indexDescriptor);
   }
   
   @Override
   public String getDropIndexStatement(final String schemaName, final String tableName, final String indexName)
   {
      return delegate.getDropIndexStatement(schemaName, tableName, indexName);
   }
   
   @Override
   public boolean supportsColumnDeletion()
   {
      return delegate.supportsColumnDeletion();
   }
   
   @Override
   public String getCreateTableOptions()
   {
      return delegate.getCreateTableOptions();
   }
   
   @Override
   public String quoteIdentifier(final String identifier)
   {
      return delegate.quoteIdentifier(identifier);
   }
   
   @Override
   public String getCreateSequenceStoredProcedureStatementString(String schemaName)
   {
      final String seqNameParameter = "seq_name";
      final StringBuffer sb = new StringBuffer(200);
      
      final String fqStoredProcedureName = schemaName != null ? (schemaName + "." + SEQUENCE_STORED_PROCEDURE_NAME) : SEQUENCE_STORED_PROCEDURE_NAME; 
      
      sb.append("CREATE FUNCTION ").append(fqStoredProcedureName)
         .append("(").append(seqNameParameter).append(" char(30)) RETURNS BIGINT").append("\n");
      sb.append("BEGIN").append("\n");
      sb.append("UPDATE ").append(GLOBAL_PK_SEQUENCE_TABLE_NAME).append(" SET ").append(GLOBAL_PK_SEQUENCE_TABLE_FIELD_VALUE_NAME)
         .append("=last_insert_id(").append(GLOBAL_PK_SEQUENCE_TABLE_FIELD_VALUE_NAME).append("+1) WHERE ").append(GLOBAL_PK_SEQUENCE_TABLE_FIELD_NAME_NAME)
         .append("=").append(seqNameParameter).append(";").append("\n");
      sb.append("RETURN last_insert_id();").append("\n");
      sb.append("END").append("\n");
      
      return sb.toString();
   }
   
   @Override
   public String getDropSequenceStoredProcedureStatementString(String schemaName)
   {
      final StringBuffer sb = new StringBuffer(100);
      
      final String fqStoredProcedureName = schemaName != null ? (schemaName + "." + SEQUENCE_STORED_PROCEDURE_NAME) : SEQUENCE_STORED_PROCEDURE_NAME;
      
      sb.append("DROP FUNCTION ").append(fqStoredProcedureName);
      
      return sb.toString();
   }
   
   @Override
   public String getCreateGlobalPKSequenceStatementString(final String schemaName)
   {
      final StringBuffer sb = new StringBuffer(200);
      
      sb.append("CREATE TABLE ");
      if ( !StringUtils.isEmpty(schemaName))
      {
         sb.append(schemaName).append(".");
      }
      sb.append(GLOBAL_PK_SEQUENCE_TABLE_NAME);
      sb.append(" (").append(GLOBAL_PK_SEQUENCE_TABLE_FIELD_NAME_NAME).append(" VARCHAR(30) NOT NULL, ")
         .append(GLOBAL_PK_SEQUENCE_TABLE_FIELD_VALUE_NAME).append(" BIGINT NOT NULL, ")
         .append("PRIMARY KEY (").append(GLOBAL_PK_SEQUENCE_TABLE_FIELD_NAME_NAME).append("))");
      sb.append(" TYPE=MyISAM");
      
      return sb.toString();
   }
   
   @Override
   public String getDropGlobalPKSequenceStatementString(final String schemaName)
   {
      final StringBuffer sb = new StringBuffer(100);
      
      sb.append("DROP TABLE ");
      if ( !StringUtils.isEmpty(schemaName))
      {
         sb.append(schemaName).append(".");
      }
      sb.append(GLOBAL_PK_SEQUENCE_TABLE_NAME);
      
      return sb.toString();
   }
   
   @Override
   public final String getIdentityColumnQualifier()
   {
      return delegate.getIdentityColumnQualifier();
   }
}
