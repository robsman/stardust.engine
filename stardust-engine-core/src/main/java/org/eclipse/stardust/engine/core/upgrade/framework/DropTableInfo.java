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
package org.eclipse.stardust.engine.core.upgrade.framework;

import java.sql.SQLException;

/**
 * @author rsauer
 * @version $Revision$
 */
public class DropTableInfo extends AbstractTableInfo
{
   private final String sequenceName;

   public String getSequenceName()
   {
      return sequenceName;
   }

   public DropTableInfo(String tableName, String sequenceName)
   {
      super(tableName);

      this.sequenceName = sequenceName;
   }

   public void doCreate(RuntimeItem item) throws SQLException
   {
      // TODO Auto-generated method stub
      
   }

   public void drop(RuntimeItem item) throws SQLException
   {
      // TODO Auto-generated method stub
      
   }
}
