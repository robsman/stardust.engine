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
package org.eclipse.stardust.engine.core.upgrade.jobs;

import java.util.Arrays;

import org.eclipse.stardust.common.Functor;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.TransformingIterator;
import org.eclipse.stardust.engine.core.persistence.jdbc.DBMSKey;
import org.eclipse.stardust.engine.core.upgrade.framework.RuntimeUpgradeJob;
import org.eclipse.stardust.engine.core.upgrade.framework.UpgradeException;


/**
 * @author rsauer
 * @version $Revision$
 */
public abstract class DbmsAwareRuntimeUpgradeJob extends RuntimeUpgradeJob
{
   private final DBMSKey[] supportedDbms;
   
   protected DbmsAwareRuntimeUpgradeJob(DBMSKey[] dbms)
   {
      this.supportedDbms = dbms;
   }

   protected void assertCompatibility() throws UpgradeException
   {
      boolean isSupported = false;
      for (int i = 0; i < supportedDbms.length; i++ )
      {
         isSupported |= supportedDbms[i].equals(item.getDbDescriptor().getDbmsKey());
      }
      
      if (!isSupported)
      {
         throw new UpgradeException("The runtime upgrade job for version "
               + getVersion()
               + " is only valid for the following DBMSs: "
               + StringUtils.join(new TransformingIterator(Arrays.asList(supportedDbms)
                     .iterator(), new Functor()
               {
                  public Object execute(Object source)
                  {
                     return ((DBMSKey) source).getName();
                  }
               })
               {
               }, ", ") + ".");
      }
   }
}
