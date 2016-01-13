/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.beans;

import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.extensions.dms.data.DmsConstants;

/**
 * @author Roland.Stamm
 */
public class RootPIUtils
{

   public static final String PROP_MODEL_PROCESS_ATTACHMENTS_BY_ROOT_PROCESS = "PROCESS_DEFINITIONS_WITH_PROCESS_ATTACHMENTS_BY_ROOT_PROCESS";

   public static boolean isRootProcessAttachmentAttributeEnabled(String dataId,
         IProcessInstance pi)
   {
      if (DmsConstants.DATA_ID_ATTACHMENTS.equals(dataId))
      {
         Map<Long, Boolean> byRefAttributeCache = (Map<Long, Boolean>) Parameters
               .instance().get(PROP_MODEL_PROCESS_ATTACHMENTS_BY_ROOT_PROCESS);
         if (byRefAttributeCache == null)
         {
            byRefAttributeCache = CollectionUtils.newHashMap();
         }

         IProcessDefinition pd = pi.getProcessDefinition();
         IModel model = (IModel) pd.getModel();
         Long modelOid = Long.valueOf(model.getOID());

         Boolean byRef = byRefAttributeCache.get(modelOid);
         if (byRef == null)
         {
            ModelElementList< ? > processDefinitions = model.getProcessDefinitions();
            byRef = Boolean.FALSE;
            for (int i = 0; i < processDefinitions.size(); i++)
            {
               IProcessDefinition innerPd = (IProcessDefinition) processDefinitions
                     .get(i);
               if (Boolean.TRUE
                     .equals(innerPd.getAttribute(DmsConstants.BY_REFERENCE_ATT)))
               {
                  byRef = Boolean.TRUE;
                  break;
               }
            }

            byRefAttributeCache.put(modelOid, byRef);
            Parameters.instance().set(PROP_MODEL_PROCESS_ATTACHMENTS_BY_ROOT_PROCESS,
                  byRefAttributeCache);
         }

         if (byRef)
         {
            IProcessInstance rootPI = pi.getRootProcessInstance();
            if (Boolean.TRUE.equals(rootPI.getProcessDefinition()
                  .getAttribute(DmsConstants.BY_REFERENCE_ATT)))
            {
               return true;
            }
         }
      }
      return false;
   }

}
