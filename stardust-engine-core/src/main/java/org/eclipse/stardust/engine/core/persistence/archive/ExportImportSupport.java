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
    * @param filter
    *           Filter to use when importing processes. Null filter will import all
    *           processes.
    */
   public static int loadProcessInstanceGraph(byte[] rawData, final Session session,
         ImportFilter filter)
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
               .loadProcessInstanceGraph(blob, session, null, filter);
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

   private static int prepareObjectsForImport(final Set<Persistent> persistents, final Session session)
   {
      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("Loaded " + persistents.size() + " persistents:");
      }
      int count = 0;
      for (final Persistent p : persistents)
      {
         if (LOGGER.isDebugEnabled())
         {
            LOGGER.debug("Persistent loaded: " + p.getClass().getSimpleName() + ". "
                  + p.toString());
         }
         if (p instanceof ProcessInstanceBean)
         {
            ProcessInstanceBean processInstance = (ProcessInstanceBean) p;
            processInstance.prepareForImportFromArchive();
            count++;
         }
         else if (p instanceof ActivityInstanceBean)
         {
            ActivityInstanceBean activity = (ActivityInstanceBean) p;
            // initialized the initial performer attribute which is necessary upon
            // session flushing
            activity.prepareForImportFromArchive();
         }
      }

      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("Loaded " + count + " ProcessInstanceBeans");
      }
      return count;
   }
}
