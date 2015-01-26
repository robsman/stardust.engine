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

import java.sql.PreparedStatement;

/**
 * BatchStatementWrapper contains the same fields like {@link StatementWrapper}, the
 * class is only used to signal the user to call executeBatch instead of executeUpdate
 */
public class BatchStatementWrapper
{
   private PreparedStatement statement;
   private String statementString;

   public BatchStatementWrapper(String statementString, PreparedStatement statement)
   {
      this.statementString = statementString;
      this.statement = statement;
   }

   public PreparedStatement getStatement()
   {
      return statement;
   }

   public String getStatementString()
   {
      return statementString;
   }
}
