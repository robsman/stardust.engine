/*******************************************************************************
 * Copyright (c) 2012, 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.cli.sysconsole.patch;

import java.util.*;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.query.QueryUtils;
import org.eclipse.stardust.engine.core.persistence.IdentifiablePersistent;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.runtime.beans.IRuntimeOidRegistry.ElementType;
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
      Map<Long, String> allAvailableModels = new HashMap<Long, String>();
      for (Iterator i = ModelPersistorBean.findAll(partitionOid); i.hasNext();)
      {
         ModelPersistorBean model = (ModelPersistorBean) i.next();
         allAvailableModels.put(model.getOID(), model.getId());
      }

      Map<Long, String> allAvailableData = new HashMap<Long, String>();

      // load data runtime OIDs
      for (Iterator i = collect(AuditTrailDataBean.findAll(partitionOid)); i.hasNext();)
      {
         AuditTrailDataBean atData = (AuditTrailDataBean) i.next();

         // test on non-predefined data
         // TODO: do predefined data need to be handled, too?
         if (!PredefinedConstants.META_DATA_IDS.contains(atData.getId()))
         {
            allAvailableData.put(atData.getOID(), atData.getId());

            String[] fqId = new String[] {
                  allAvailableModels.get(atData.getModel()), atData.getId()};
            registerRuntimeOid(atData.getModel(), rtOidRegistry,
                  IRuntimeOidRegistry.DATA, fqId, atData.getOID());
         }
      }

      // load all structured data xpath entries
      for (Iterator i = collect(StructuredDataBean.findAll(partitionOid)); i.hasNext();)
      {
         StructuredDataBean atStructuredData = (StructuredDataBean) i.next();
         String dataId = allAvailableData.get(atStructuredData.getData());

         String[] dataFqId = {
               allAvailableModels.get(atStructuredData.getModel()), dataId,
               atStructuredData.getXPath()};

         registerRuntimeOid(atStructuredData.getModel(), rtOidRegistry,
               IRuntimeOidRegistry.STRUCTURED_DATA_XPATH, dataFqId,
               atStructuredData.getOID());
      }

      // load model participant runtime OIDs
      for (Iterator i = collect(AuditTrailParticipantBean.findAll(partitionOid)); i
            .hasNext();)
      {
         AuditTrailParticipantBean atParticipant = (AuditTrailParticipantBean) i.next();

         boolean predefinedParticipant = QueryUtils.isPredefinedParticipant(atParticipant.getId());

         String[] fqId;
         if (predefinedParticipant)
         {
            fqId = new String[] {atParticipant.getId()};
         }
         else
         {
            fqId = new String[] {
                  allAvailableModels.get(atParticipant.getModel()), atParticipant.getId()};
         }
         registerRuntimeOid(atParticipant.getModel(), rtOidRegistry,
               IRuntimeOidRegistry.PARTICIPANT, fqId, atParticipant.getOID());
      }

      // load process definition runtime OIDs
      Map processIds = new HashMap();
      for (Iterator i = collect(AuditTrailProcessDefinitionBean.findAll(partitionOid)); i
            .hasNext();)
      {
         AuditTrailProcessDefinitionBean atProcess = (AuditTrailProcessDefinitionBean) i
               .next();

         String[] fqId = new String[] {
               allAvailableModels.get(atProcess.getModel()), atProcess.getId()};
         registerRuntimeOid(atProcess.getModel(), rtOidRegistry,
               IRuntimeOidRegistry.PROCESS, fqId, atProcess.getOID());
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

         String[] fqId = new String[] {
               allAvailableModels.get(atTrigger.getModel()), processId, atTrigger.getId()};
         registerRuntimeOid(atTrigger.getModel(), rtOidRegistry,
               IRuntimeOidRegistry.TRIGGER, fqId, atTrigger.getOID());
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

         String[] fqId = new String[] {
               allAvailableModels.get(atActivity.getModel()), processId,
               atActivity.getId()};
         registerRuntimeOid(atActivity.getModel(), rtOidRegistry,
               IRuntimeOidRegistry.ACTIVITY, fqId, atActivity.getOID());
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

         String[] fqId = new String[] {
               allAvailableModels.get(atTransition.getModel()), processId,
               atTransition.getId()};
         registerRuntimeOid(atTransition.getModel(), rtOidRegistry,
               IRuntimeOidRegistry.TRANSITION, fqId, atTransition.getOID());
      }

      // load event handler runtime OIDs
      for (Iterator j = collect(AuditTrailEventHandlerBean.findAll(partitionOid)); j
            .hasNext();)
      {
         AuditTrailEventHandlerBean atHandler = (AuditTrailEventHandlerBean) j.next();
         String fkProcess = numericKey(atHandler.getModel(),
               atHandler.getProcessDefinition());

         String processId = (String) processIds.get(fkProcess);

         String[] fqId;
         if (0 == atHandler.getActivity())
         {
            fqId = new String[] {
                  allAvailableModels.get(atHandler.getModel()), processId,
                  atHandler.getId()};
            registerRuntimeOid(atHandler.getModel(), rtOidRegistry,
                  IRuntimeOidRegistry.EVENT_HANDLER, fqId, atHandler.getOID());
         }
         else
         {
            String fkActivity = numericKey(atHandler.getModel(), atHandler.getActivity());
            String activityId = (String) activityIds.get(fkActivity);

            fqId = new String[] {
                  allAvailableModels.get(atHandler.getModel()), processId, activityId,
                  atHandler.getId()};
            registerRuntimeOid(atHandler.getModel(), rtOidRegistry,
                  IRuntimeOidRegistry.EVENT_HANDLER, fqId, atHandler.getOID());
         }
      }
   }

   private Iterator collect(Iterator itr)
   {
      List<IdentifiablePersistent> result = CollectionUtils.newArrayListFromIterator(itr);

      Collections.sort(result, new Comparator<IdentifiablePersistent>()
      {
         @Override
         public int compare(IdentifiablePersistent o1, IdentifiablePersistent o2)
         {
            if (useNewOid)
            {
               return CompareHelper.compare(o2.getOID(), o1.getOID());
            }
            else
            {
               return CompareHelper.compare(o1.getOID(), o2.getOID());
            }
         }
      });
      return result.iterator();
   }

   public Map<ElementType, List<RuntimeOidPatch>> getRuntimeOidPatches()
   {
      return runtimeOidPatches;
   }
}
