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
    * Loads the process instance graph contained in the raw data and attaches all included {@link Persistent}s to the
    * given {@link Session}'s cache. 
    * </p>
    *
    * @param rawData the raw byte array that needs to be deserialized
    * @param session the session the {@link Persistent}s should be populated to
    */
   public static void loadProcessInstanceGraph(byte[] rawData, final Session session)
   {
      if (rawData == null)
      {
         return;
      }
      
      final ProcessInstanceGraphBlob blob = new ProcessInstanceGraphBlob(rawData);

      Set<Persistent> persistents = TransientProcessInstanceUtils.loadProcessInstanceGraph(blob, session, null);
      populateProcessInstanceScope(persistents, session);
   }
   
   private static void populateProcessInstanceScope(final Set<Persistent> persistents, final Session session)
   {

      for (final Persistent p : persistents)
      {
         if (p instanceof ProcessInstanceBean) {
            ProcessInstanceBean processInstance = (ProcessInstanceBean)p;
            if(processInstance.getScopeProcessInstance() != null) {
               new ProcessInstanceScopeBean(
                     processInstance, processInstance.getScopeProcessInstance(), processInstance.getRootProcessInstance());
            }
         }
         if (p instanceof ActivityInstanceBean) {
            ActivityInstanceBean activity = (ActivityInstanceBean) p;
            activity.prepareForImportFromArchive();
         }
      }
   }

}
