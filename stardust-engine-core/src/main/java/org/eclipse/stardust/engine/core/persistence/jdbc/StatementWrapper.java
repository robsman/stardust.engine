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
 * @author ubirkemeyer
 * @version $Revision$
 */
public class StatementWrapper
{
   private PreparedStatement statement;
   private String statementString;

   public StatementWrapper(String statementString, PreparedStatement statement)
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
