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

import java.util.List;

public class BulkDeleteStatement
{

   private final Class type;
   private final String statementString;
   private final List bindValueList;

   public BulkDeleteStatement(Class type, String statementString, List bindValueList)
   {
      this.type = type;
      this.statementString = statementString;
      this.bindValueList = bindValueList;
   }

   public Class getType()
   {
      return type;
   }

   public String getStatementString()
   {
      return statementString;
   }

   public List getBindValueList()
   {
      return bindValueList;
   }

}
