/**********************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.engine.core.persistence.archive;

import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.persistence.Persistent;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.TransientProcessInstanceStorage.ProcessInstanceGraphBlob;
import org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.TransientProcessInstanceUtils;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceScopeBean;

/**
 * <p>
 * This class aims at facilitating import and export of process archived process instances
 * </p>
 *
 * @author jsaayman
 * @version $Revision$
 */
public class ExportImportSupport
{
   @SuppressWarnings("unused")
   private static final Logger LOGGER = LogManager.getLogger(ExportImportSupport.class);

   /**
    * <p>
    * Loads the process instance graph contained in the raw data and attaches all included
    * {@link Persistent}s to the given {@link Session}'s cache.
    * </p>
    *
    * @param rawData
    *           the raw byte array that needs to be deserialized
    * @param session
    *           the session the {@link Persistent}s should be populated to
    */
   public static int loadProcessInstanceGraph(byte[] rawData, final Session session)
   {
      int count;
      if (rawData == null)
      {
         count = 0;
      }
      else
      {
         final ProcessInstanceGraphBlob blob = new ProcessInstanceGraphBlob(rawData);
         Set<Persistent> persistents = TransientProcessInstanceUtils
               .loadProcessInstanceGraph(blob, session, null);
         if (CollectionUtils.isNotEmpty(persistents))
         {
            count = prepareObjectsForImport(persistents, session);
         }
         else
         {
            count = 0;
         }
      }
      return count;
   }

   private static int prepareObjectsForImport(final Set<Persistent> persistents,
         final Session session)
   {

      int count = 0;
      for (final Persistent p : persistents)
      {
         if (p instanceof ProcessInstanceBean)
         {
            ProcessInstanceBean processInstance = (ProcessInstanceBean) p;
            if (processInstance.getScopeProcessInstance() != null)
            {
               // we need to explicitly create the scope bean like this, it can't be
               // imported normally. this constructor calls session.cluster which is
               // necessary to be called only once.
               new ProcessInstanceScopeBean(processInstance,
                     processInstance.getScopeProcessInstance(),
                     processInstance.getRootProcessInstance());
            }
            count++;
         }
         if (p instanceof ActivityInstanceBean)
         {
            ActivityInstanceBean activity = (ActivityInstanceBean) p;
            // initialized the initial performer attribute which is necessary upon
            // session flushing
            activity.prepareForImportFromArchive();
         }
      }
      return count;
   }

}
