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
package org.eclipse.stardust.engine.core.spi.extensions.runtime;

import java.util.Map;

import org.eclipse.stardust.common.error.ConcurrencyException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IEventHandler;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.EventBindingBean;
import org.eclipse.stardust.engine.core.runtime.beans.EventUtils;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;



/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class DefaultEventBinder implements EventBinder
{
   private static final Logger trace = LogManager.getLogger(DefaultEventBinder.class);
   
   public void bind(int objectType, long oid, IEventHandler handler, Map attributes)
   {
      EventBindingBean binding = EventBindingBean.find(objectType, oid, handler,
            SecurityProperties.getPartitionOid());
      if (binding == null)
      {
         binding = new EventBindingBean(objectType, oid, handler, SecurityProperties
               .getPartitionOid());
      }
   }

   public EventBindingBean lock(int objectType, long oid, IEventHandler handler)
   {
      EventBindingBean binding = EventBindingBean.find(objectType, oid, handler,
              SecurityProperties.getPartitionOid());
      if (binding != null)
      {
         binding.lock();
      }
      else
      {
         throw new ConcurrencyException(
               BpmRuntimeError.BPMRT_LOCK_CONFLICT_FOR_HANDLE.raise(oid, objectType,
                     handler));
      }
      return binding;
   }
   
   public void unbind(int objectType, long oid, IEventHandler handler)
   {
      final BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();
      short partitionOid = SecurityProperties.getPartitionOid();
      EventBindingBean binding = EventBindingBean.find(objectType, oid, handler, partitionOid);
      if (binding != null)
      {
         binding.delete();
         // marks the binding as successfully deleted
         rtEnv.getEventBindingRecords().markDeleted(objectType, oid, handler, partitionOid);
      }
      // only logs if the binding was not already deleted in this transaction
      else if (!rtEnv.getEventBindingRecords().isDeleted(objectType, oid, handler, partitionOid))
      {
         rtEnv.getEventBindingRecords().markDeleted(objectType, oid, handler, partitionOid);
         trace.info("Cannot unbind handler with object oid " + oid + ", type '"
               + objectType + "' for handler with oid " + handler + ".");
      }
   }

   public void deactivate(int objectType, long oid, IEventHandler handler)
   {
      EventBindingBean binding = EventBindingBean.find(objectType, oid, handler,
            SecurityProperties.getPartitionOid());
      if (binding != null)
      {
         binding.lock();
         binding.setType(binding.getType() + EventUtils.DEACTIVE_TYPE);
      } 
   }
}