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

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;


/**
 * @author Sebastian Woelk
 * @version $Revision$
 */
public class SynonymTableInfo extends AbstractTableInfo
{
   private static final Logger trace = LogManager.getLogger(SynonymTableInfo.class);
   
   /**
    * The name of the synonym for this table
    */
   private String synonymName = null;

   /**
    * Constructs a SynonymTableInfo object.
    *
    * @param tableName the name of the table this synonym belongs to
    * @param synonymName the name of the synonym which will be created or 
    * dropped
    */
   public SynonymTableInfo(String tableName, String synonymName)
   {
      super(tableName);

      if (synonymName == null)
      {
         throw new NullPointerException("synonymName may not be null");
      }

      this.synonymName = synonymName;
   }

   /**
    * Returns the name for the synonym of this table
    *
    * @return the name of the synonym
    */
   public String getSynonymName()
   {
      return synonymName;
   }

   /**
    *
    */
   public void doCreate(RuntimeItem item) throws SQLException
   {
      trace.info("Creating synonym " + getSynonymName());

      DatabaseHelper.createSynonym(item, this);
   }

   /**
    *
    */
   public void drop(RuntimeItem item) throws SQLException
   {
      trace.info("Dropping synonym " + getSynonymName());

      DatabaseHelper.dropSynonym(item, this);
   }
}
