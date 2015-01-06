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
package org.eclipse.stardust.engine.core.runtime.beans;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IModelParticipant;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.model.utils.Identifiable;
import org.eclipse.stardust.engine.core.model.utils.ModelElement;
import org.eclipse.stardust.engine.core.persistence.IdentifiablePersistent;
import org.eclipse.stardust.engine.core.persistence.PersistentModelElement;
import org.eclipse.stardust.engine.core.struct.beans.StructuredDataBean;


/**
 * @author rsauer
 * @version $Revision$
 */
public class RuntimeOidUtils
{
   public static String internalizeFqId(String[] fqId)
   {
      // TODO quote embedded separator tokens
      StringBuffer buffer = new StringBuffer(50 * fqId.length);
      for (int i = 0; i < fqId.length; i++ )
      {
         buffer.append("::[").append(fqId[i]).append("]");
      }
      return buffer.toString();
   }
   
   public static String[] getFqId(Identifiable identifiable)
   {
      return getFqId(identifiable, null, null);
   }
   
   public static String[] getFqId(Identifiable identifiable, String path)
   {
      return getFqId(identifiable, path, null);
   }
   
   public static String[] getFqId(Identifiable persistent, IdCache cache)
   {
      String path = null;
      if (persistent instanceof StructuredDataBean)
      {
         path = ((StructuredDataBean) persistent).getXPath();
         persistent = cache.getParent(persistent);
      }
      return getFqId(persistent, path, cache);
   }
   
   private static String[] getFqId(Identifiable identifiable, String path, IdCache cache)
   {
      List<String> ids = CollectionUtils.newList(5);
      if (path != null)
      {
         ids.add(path);
      }
      while (identifiable != null)
      {
         ids.add(identifiable.getId());
         Identifiable current = identifiable;
         if (cache != null)
         {
            identifiable = cache.getParent(identifiable);
         }
         else if (identifiable instanceof ModelElement)
         {
            ModelElement parent = ((ModelElement) identifiable).getParent();
            identifiable = parent != identifiable && parent instanceof Identifiable
               ? (Identifiable) parent : null;               
         }
         if((current instanceof IModelParticipant || current instanceof AuditTrailParticipantBean)
               && org.eclipse.stardust.engine.api.query.QueryUtils.isPredefinedParticipant(current.getId()))
         {
            identifiable = null;
         }         
         if((current instanceof IData || current instanceof AuditTrailDataBean)
               && PredefinedConstants.META_DATA_IDS.contains(current.getId()))
         {
            identifiable = null;
         }         
      }
      Collections.reverse(ids);
      return ids.toArray(new String[ids.size()]);
   }
   
   public static class IdCache
   {
      private static final String ERR_MSG_MISSING_PARENT_ELEMENT = "Missing {0} for {1} with"
         + " ID {2} (OID {3, number,integer}, model OID {4,number,integer}).";
      
      private Map<Class<?>, Map<Long, Identifiable>> cache = CollectionUtils.newMap();
      
      private static Map<Class<?>, Class<?>> parentKind = CollectionUtils.newMap();
      static
      {
         parentKind.put(AuditTrailDataBean.class, ModelPersistorBean.class);
         parentKind.put(StructuredDataBean.class, AuditTrailDataBean.class);
         parentKind.put(AuditTrailParticipantBean.class, ModelPersistorBean.class);
         parentKind.put(AuditTrailProcessDefinitionBean.class, ModelPersistorBean.class);
         parentKind.put(AuditTrailTriggerBean.class, AuditTrailProcessDefinitionBean.class);
         parentKind.put(AuditTrailActivityBean.class, AuditTrailProcessDefinitionBean.class);
         parentKind.put(AuditTrailTransitionBean.class, AuditTrailProcessDefinitionBean.class);
         parentKind.put(AuditTrailEventHandlerBean.class, AuditTrailActivityBean.class);
      }

      private static Map<Class<?>, String> elementString = CollectionUtils.newMap();
      static
      {
         elementString.put(AuditTrailDataBean.class, "data");
         elementString.put(StructuredDataBean.class, "xpath");
         elementString.put(AuditTrailParticipantBean.class, "participant");
         elementString.put(AuditTrailProcessDefinitionBean.class, "process");
         elementString.put(AuditTrailTriggerBean.class, "trigger");
         elementString.put(AuditTrailActivityBean.class, "activity");
         elementString.put(AuditTrailTransitionBean.class, "transition");
         elementString.put(AuditTrailEventHandlerBean.class, "event handler");
      }

      public void register(Identifiable persistent)
      {
         if (persistent instanceof IdentifiablePersistent)
         {
            Class<?> key = persistent.getClass();
            Map<Long, Identifiable> map = cache.get(key);
            if (map == null)
            {
               map = CollectionUtils.newMap();
               cache.put(key, map);
            }
            map.put(((IdentifiablePersistent) persistent).getOID(), persistent);
         }
      }

      private Identifiable getParent(Identifiable persistent)
      {
         Identifiable parent = null;
         if (persistent instanceof PersistentModelElement)
         {
            PersistentModelElement pme = (PersistentModelElement) persistent;
            Class<?> key = persistent.getClass();
            Class<?> parentKey = parentKind.get(key);
            if (persistent instanceof AuditTrailEventHandlerBean
                  && ((AuditTrailEventHandlerBean) persistent).getActivity() == 0)
            {
               parentKey = parentKind.get(parentKey);
            }
            Map<Long, Identifiable> map = cache.get(parentKey);
            if (map != null)
            {
               parent = map.get(pme.getParent());
            }
            if (parent == null)
            {
               throw new InternalException(MessageFormat.format(
                     ERR_MSG_MISSING_PARENT_ELEMENT, new Object[] {
                           elementString.get(parentKey), elementString.get(key),
                           pme.getId(), pme.getOID(), pme.getModel()}));
            }
         }
         return parent;
      }
   }
}
