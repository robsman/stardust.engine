/*******************************************************************************
 * Copyright (c) 2011, 2013 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.upgrade.framework;

import java.io.StringReader;

import javax.xml.bind.JAXBException;

import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.xml.sax.SAXException;

/**
 * Wraps an xml CARNOT model as an upgradable item. The model is expected to
 * be in String form.
 *
 * @author kberberich, ubirkemeyer
 * @version $Revision$
 */
public class ModelItem implements UpgradableItem
{
   public static final Logger trace = LogManager.getLogger(ModelItem.class);

   private String model;

   private Version version;

   private long oid;

   private RuntimeItem runtimeItem;

   private ModelUpgradeInfo upgradeInfo;

   private boolean changed;

   public ModelItem(RuntimeItem runtimeItem, long oid, String model)
   {
      this.oid = oid;
      this.model = model;
      this.runtimeItem = runtimeItem;
   }

   public Version getVersion()
   {
      if (version == null)
      {
         try
         {
            String versionString = getUpgradeInfo().getVersion();
            if (versionString != null && !versionString.startsWith("9.9.9"))
            {
               version = Version.createModelVersion(versionString, getUpgradeInfo().getVendor());
            }
         }
         catch (Exception e)
         {
            e.printStackTrace();
            throw new UpgradeException("Unable to parse model with oid: " + oid);
         }
      }
      return version;
   }

   public void setVersion(Version version)
   {
      this.version = version;
   }

   public String getDescription()
   {
      try
      {
         return "'" + getUpgradeInfo().getName() + "' (" + getUpgradeInfo().getId() + ")";
      }
      catch (Exception e)
      {
         throw new UpgradeException("Unable to parse model with oid: " + oid);
      }
   }

   public String getModel()
   {
      return model;
   }

   public void setModel(String model)
   {
      this.model = model;
      changed = true;
   }

   public long getOid()
   {
      return oid;
   }

   public RuntimeItem getRuntimeItem()
   {
      return runtimeItem;
   }

   public ModelUpgradeInfo getUpgradeInfo() throws JAXBException, SAXException
   {
      if (upgradeInfo == null)
      {
         upgradeInfo = ModelUpgradeInfo.get(new StringReader(model));
      }
      return upgradeInfo;
   }

   public boolean isChanged()
   {
      return changed;
   }
}
