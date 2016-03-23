/*******************************************************************************
 * Copyright (c) 2016 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Thomas.Wolfram (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.engine.core.runtime.beans;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean.DataValueChangeListener;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;

/**
 * 
 * @author Thomas.Wolfram
 *
 */
public class DataValueHistoryListener implements DataValueChangeListener
{
   private static final Logger trace = LogManager.getLogger(DataValueHistoryListener.class);   
   
   @Override
   public void onDataValueChanged(IDataValue dataValue, DataMappingContext mappingContext)
   {
      boolean isDataValueHistoryActive = Parameters.instance().getBoolean(
            KernelTweakingProperties.WRITE_HISTORICAL_DATA_TO_DB, false);

      if (isDataValueHistoryActive)
      {

         if (trace.isDebugEnabled())
         {
            trace.debug("Writing data value history entry for data <"
                  + dataValue.getData().getId() + "> with value <" + dataValue.getValue()
                  + ">");
         }

         IDataValue historyValue = new DataValueHistoryBean((DataValueBean) dataValue,
               mappingContext);
      }

   }

   public boolean isMappingContextStateForHistory(DataMappingContext mappingContext)
   {
      if (mappingContext != null)
      {
         if (mappingContext.getActivityInstance()
               .getState()
               .equals(ActivityInstanceState.COMPLETED))
         {
            return true;
         }
      }
      return false;
   }

}
