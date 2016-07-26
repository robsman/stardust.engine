/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Florin.Herinean (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.engine.core.upgrade.jobs;

import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.pojo.data.Type;
import org.eclipse.stardust.engine.core.upgrade.framework.*;

public class M9_2_0from9_0_0ModelJob extends ModelUpgradeJob
{
   @Override
   public UpgradableItem run(UpgradableItem item, boolean recover)
   {
      ModelItem modelItem = (ModelItem) item;

      try
      {
         ModelUpgradeInfo info = modelItem.getUpgradeInfo();
         if (!PredefinedConstants.PREDEFINED_MODEL_ID.equals(info.getId()))
         {
            changePrimitiveDataType(info, modelItem, PredefinedConstants.BUSINESS_DATE, Type.Calendar);
         }
      }
      catch (Exception e)
      {
         throw new UpgradeException(e);
      }

      return super.run(modelItem, recover);
   }

   @Override
   public Version getVersion()
   {
      return R9_2_0from9_0_0RuntimeJob.VERSION;
   }
}
