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
 * <p>
 * A {@link DBDescriptor} for <i>MySQL</i> databases in order to allow for
 * database sequences support (which <i>MySQL</i> does not provide out of the box).
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class MySqlSeqDbDescriptor extends SequenceDbDriver
{
   private static final String GLOBAL_PK_SEQUENCE_TABLE_NAME = "sequence";
   private static final String GLOBAL_PK_SEQUENCE_TABLE_FIELD_NAME_NAME = "name";
   private static final String GLOBAL_PK_SEQUENCE_TABLE_FIELD_VALUE_NAME = "value";
   
   private static final String SEQUENCE_STORED_PROCEDURE_NAME = "next_sequence_value_for";
   
   private final MySqlDbDescriptor delegate;
   
   public MySqlSeqDbDescriptor()
   {
      delegate = new MySqlDbDescriptor();
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.DBDescriptor#getDbmsKey()
    */
   @Override
   public DBMSKey getDbmsKey()
   {
      return DBMSKey.MYSQL_SEQ;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.DBDescriptor#getCreatePKSequenceStatementString(java.lang.String, java.lang.String, java.lang.String)
    */
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

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.DBDescriptor#getDropPKSequenceStatementString(java.lang.String, java.lang.String)
    */
   @Override
   public String getDropPKSequenceStatementString(final String schemaName, final String pkSequence)
   {
      /* nothing to do */
      return null;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.DBDescriptor#getCreatePKStatement(java.lang.String, java.lang.String, int)
    */
   @Override
   public String getCreatePKStatement(final String schemaName, final String pkSequence, final int sequenceCount)
   {
      if (sequenceCount < 1)
      {
         throw new IllegalArgumentException("Sequence count must be greater than 0.");
      }
      
      final StringBuffer sb = new StringBuffer(sequenceCount * 100);
      
      sb.append("SELECT ");
      writeSequenceStoredProcedureCall(sb, schemaName, pkSequence);
      
      if (sequenceCount > 1)
      {
         sb.append(" FROM (");
         for (int i=0; i<sequenceCount; i++)
         {
            sb.append("SELECT ").append(i);
            if (i+1 < sequenceCount)
            {
               sb.append(" UNION ");
            }
         }
         sb.append(") AS seq");
      }
      
      return sb.toString();
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.DBDescriptor#getCreatePKStatement(java.lang.String, java.lang.String)
    */
   @Override
   public String getCreatePKStatement(final String schemaName, final String pkSequence)
   {
      return getCreatePKStatement(schemaName, pkSequence, 1);
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.DBDescriptor#getNextValForSeqString(java.lang.String, java.lang.String)
    */
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
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.DBDescriptor#getSQLType(java.lang.Class, long)
    */
   @Override
   public String getSQLType(final Class type, final long length)
   {
      return delegate.getSQLType(type, length);
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.DBDescriptor#useQueryTimeout()
    */
   @Override
   public boolean useQueryTimeout()
   {
      return delegate.useQueryTimeout();
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.DBDescriptor#useAnsiJoins()
    */
   @Override
   public boolean useAnsiJoins()
   {
      return delegate.useAnsiJoins();
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.DBDescriptor#isLockRowStatementSQLQuery()
    */
   @Override
   public boolean isLockRowStatementSQLQuery()
   {
      return delegate.isLockRowStatementSQLQuery();
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.DBDescriptor#getLockRowStatementString(org.eclipse.stardust.engine.core.persistence.jdbc.SqlUtils, org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor, boolean, java.lang.String)
    */
   @Override
   public String getLockRowStatementString(final SqlUtils sqlUtils, final TypeDescriptor type, final boolean tryToUseDistinctLockTable, final String predicate)
   {
      return delegate.getLockRowStatementString(sqlUtils, type, tryToUseDistinctLockTable, predicate);
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.DBDescriptor#supportsSubselects()
    */
   @Override
   public boolean supportsSubselects()
   {
      return delegate.supportsSubselects();
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.DBDescriptor#getCreateIndexStatement(java.lang.String, java.lang.String, org.eclipse.stardust.engine.core.persistence.jdbc.IndexDescriptor)
    */
   @Override
   public String getCreateIndexStatement(final String schemaName, final String tableName, final IndexDescriptor indexDescriptor)
   {
      return delegate.getCreateIndexStatement(schemaName, tableName, indexDescriptor);
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.DBDescriptor#getDropIndexStatement(java.lang.String, java.lang.String, java.lang.String)
    */
   @Override
   public String getDropIndexStatement(final String schemaName, final String tableName, final String indexName)
   {
      return delegate.getDropIndexStatement(schemaName, tableName, indexName);
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.DBDescriptor#supportsColumnDeletion()
    */
   @Override
   public boolean supportsColumnDeletion()
   {
      return delegate.supportsColumnDeletion();
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.DBDescriptor#getCreateTableOptions()
    */
   @Override
   public String getCreateTableOptions()
   {
      return delegate.getCreateTableOptions();
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.DBDescriptor#quoteIdentifier(java.lang.String)
    */
   @Override
   public String quoteIdentifier(final String identifier)
   {
      return delegate.quoteIdentifier(identifier);
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.DBDescriptor#getCreateSequenceStoredProcedureStatementString(java.lang.String)
    */
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
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.DBDescriptor#getDropSequenceStoredProcedureStatementString(java.lang.String)
    */
   @Override
   public String getDropSequenceStoredProcedureStatementString(String schemaName)
   {
      final StringBuffer sb = new StringBuffer(100);
      
      final String fqStoredProcedureName = schemaName != null ? (schemaName + "." + SEQUENCE_STORED_PROCEDURE_NAME) : SEQUENCE_STORED_PROCEDURE_NAME;
      
      sb.append("DROP FUNCTION ").append(fqStoredProcedureName);
      
      return sb.toString();
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.DBDescriptor#getCreateGlobalPKSequenceStatementString(java.lang.String)
    */
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
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.DBDescriptor#getDropGlobalPKSequenceStatementString(java.lang.String)
    */
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
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.persistence.jdbc.SequenceDbDriver#getIdentityColumnQualifier()
    */
   @Override
   public final String getIdentityColumnQualifier()
   {
      return delegate.getIdentityColumnQualifier();
   }
}
