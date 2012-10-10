/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.cli.sysconsole.patch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailActivityBean;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailDataBean;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailEventHandlerBean;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailParticipantBean;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailProcessDefinitionBean;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailTransitionBean;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailTriggerBean;
import org.eclipse.stardust.engine.core.runtime.beans.IRuntimeOidRegistry;
import org.eclipse.stardust.engine.core.runtime.beans.IRuntimeOidRegistry.ElementType;
import org.eclipse.stardust.engine.core.runtime.beans.RuntimeModelLoader;
import org.eclipse.stardust.engine.core.struct.beans.StructuredDataBean;

public class PatchAwareRuntimeModelLoader extends RuntimeModelLoader
{
   private Map<ElementType, List<RuntimeOidPatch>> runtimeOidPatches = new HashMap<ElementType, List<RuntimeOidPatch>>();

   private final short partitionOid;

   private boolean useNewOid;

   public PatchAwareRuntimeModelLoader(short partitionOid, boolean useNewOid)
   {
      super(partitionOid);
      this.partitionOid = partitionOid;
      this.useNewOid = useNewOid;
   }

   private static String numericKey(long p1, long p2)
   {
      return "::[" + p1 + "]::[" + p2 + "]";
   }

   private void registerRuntimeOid(long modelOid, IRuntimeOidRegistry rtOidRegistry,
         ElementType type, String[] fqId, long rtOid)
   {
      try
      {
         rtOidRegistry.registerRuntimeOid(type, fqId, rtOid);
      }
      catch (InternalException e)
      {
         long existingOid = rtOidRegistry.getRuntimeOid(type, fqId);

         List<RuntimeOidPatch> patchesForType = null;

         if (!runtimeOidPatches.containsKey(type))
         {
            patchesForType = new ArrayList<RuntimeOidPatch>();
            runtimeOidPatches.put(type, patchesForType);
         }
         else
         {
            patchesForType = runtimeOidPatches.get(type);
         }

         patchesForType.add(new RuntimeOidPatch(modelOid, rtOid, existingOid));
      }
   }

   @Override
   public void loadRuntimeOidRegistry(IRuntimeOidRegistry rtOidRegistry)
   {
      Map<Long, String> allAvailableData = new HashMap<Long, String>();

      // load data runtime OIDs
      for (Iterator i = collect(AuditTrailDataBean.findAll(partitionOid)); i.hasNext();)
      {
         AuditTrailDataBean atData = (AuditTrailDataBean) i.next();
         allAvailableData.put(atData.getOID(), atData.getId());

         registerRuntimeOid(atData.getModel(), rtOidRegistry, IRuntimeOidRegistry.DATA,
               new String[] {atData.getId()}, atData.getOID());
      }

      // load all structured data xpath entries
      for (Iterator i = collect(StructuredDataBean.findAll(partitionOid)); i.hasNext();)
      {
         StructuredDataBean atStructuredData = (StructuredDataBean) i.next();
         String dataId = allAvailableData.get(atStructuredData.getData());

         String[] dataFqId = {dataId, atStructuredData.getXPath()};

         registerRuntimeOid(atStructuredData.getModel(), rtOidRegistry,
               IRuntimeOidRegistry.STRUCTURED_DATA_XPATH, dataFqId,
               atStructuredData.getOID());
      }

      // load model participant runtime OIDs
      for (Iterator i = collect(AuditTrailParticipantBean.findAll(partitionOid)); i
            .hasNext();)
      {
         AuditTrailParticipantBean atParticipant = (AuditTrailParticipantBean) i.next();

         registerRuntimeOid(atParticipant.getModel(), rtOidRegistry,
               IRuntimeOidRegistry.PARTICIPANT, new String[] {atParticipant.getId()},
               atParticipant.getOID());
      }

      // load process definition runtime OIDs
      Map processIds = new HashMap();
      for (Iterator i = collect(AuditTrailProcessDefinitionBean.findAll(partitionOid)); i
            .hasNext();)
      {
         AuditTrailProcessDefinitionBean atProcess = (AuditTrailProcessDefinitionBean) i
               .next();

         registerRuntimeOid(atProcess.getModel(), rtOidRegistry,
               IRuntimeOidRegistry.PROCESS, new String[] {atProcess.getId()},
               atProcess.getOID());
         processIds.put(numericKey(atProcess.getModel(), atProcess.getOID()),
               atProcess.getId());
      }

      // load trigger runtime OIDs
      for (Iterator i = collect(AuditTrailTriggerBean.findAll(partitionOid)); i.hasNext();)
      {
         AuditTrailTriggerBean atTrigger = (AuditTrailTriggerBean) i.next();

         String fkProcess = numericKey(atTrigger.getModel(),
               atTrigger.getProcessDefinition());
         String processId = (String) processIds.get(fkProcess);

         registerRuntimeOid(atTrigger.getModel(), rtOidRegistry,
               IRuntimeOidRegistry.TRIGGER, new String[] {processId, atTrigger.getId()},
               atTrigger.getOID());
      }

      // load activity runtime OIDs
      Map activityIds = new HashMap();
      for (Iterator i = collect(AuditTrailActivityBean.findAll(partitionOid)); i
            .hasNext();)
      {
         AuditTrailActivityBean atActivity = (AuditTrailActivityBean) i.next();

         String fkProcess = numericKey(atActivity.getModel(),
               atActivity.getProcessDefinition());
         String processId = (String) processIds.get(fkProcess);

         registerRuntimeOid(atActivity.getModel(), rtOidRegistry,
               IRuntimeOidRegistry.ACTIVITY,
               new String[] {processId, atActivity.getId()}, atActivity.getOID());
         activityIds.put(numericKey(atActivity.getModel(), atActivity.getOID()),
               atActivity.getId());
      }

      // load transition runtime OIDs
      for (Iterator i = collect(AuditTrailTransitionBean.findAll(partitionOid)); i
            .hasNext();)
      {
         AuditTrailTransitionBean atTransition = (AuditTrailTransitionBean) i.next();

         String fkProcess = numericKey(atTransition.getModel(),
               atTransition.getProcessDefinition());
         String processId = (String) processIds.get(fkProcess);

         registerRuntimeOid(atTransition.getModel(), rtOidRegistry,
               IRuntimeOidRegistry.TRANSITION,
               new String[] {processId, atTransition.getId()}, atTransition.getOID());
      }

      // load event handler runtime OIDs
      for (Iterator j = collect(AuditTrailEventHandlerBean.findAll(partitionOid)); j
            .hasNext();)
      {
         AuditTrailEventHandlerBean atHandler = (AuditTrailEventHandlerBean) j.next();
         String fkProcess = numericKey(atHandler.getModel(),
               atHandler.getProcessDefinition());

         String processId = (String) processIds.get(fkProcess);

         if (0 == atHandler.getActivity())
         {
            registerRuntimeOid(atHandler.getModel(), rtOidRegistry,
                  IRuntimeOidRegistry.EVENT_HANDLER,
                  new String[] {processId, atHandler.getId()}, atHandler.getOID());
         }
         else
         {
            String fkActivity = numericKey(atHandler.getModel(), atHandler.getActivity());
            String activityId = (String) activityIds.get(fkActivity);

            registerRuntimeOid(atHandler.getModel(), rtOidRegistry,
                  IRuntimeOidRegistry.EVENT_HANDLER, new String[] {
                        processId, activityId, atHandler.getId()}, atHandler.getOID());
         }
      }
   }

   private Iterator collect(Iterator itr)
   {
      if (useNewOid)
      {
         List result = new ArrayList();
         while (itr.hasNext())
         {
            result.add(itr.next());
         }
         Collections.reverse(result);
         itr = result.iterator();
      }
      return itr;
   }

   public Map<ElementType, List<RuntimeOidPatch>> getRuntimeOidPatches()
   {
      return runtimeOidPatches;
   }
}
