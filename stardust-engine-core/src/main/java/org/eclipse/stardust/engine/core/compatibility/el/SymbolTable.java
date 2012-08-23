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
package org.eclipse.stardust.engine.core.compatibility.el;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.config.PropertyLayer;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.IDescriptorProvider;
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;

public interface SymbolTable
{
   public AccessPoint lookupSymbolType(String name);

   public Object lookupSymbol(String name);
   
   public static final class SymbolTableFactory
   {
      public static SymbolTable create(final String id, final Object value, final AccessPoint type)
      {
         return new SymbolTable()
         {
            public Object lookupSymbol(String name)
            {
               return name.equals(id) ? value : null;
            }
   
            public AccessPoint lookupSymbolType(String name)
            {
               return name.equals(id) ? type : null;
            }
         };
      }
      
      public static SymbolTable create(final IActivityInstance activityInstance)
      {
         return create(activityInstance, null);
      }
      
      public static SymbolTable create(final IActivityInstance activityInstance, final IActivity activity)
      {
         return new SymbolTable()
         {
            private IActivityInstance ai = activityInstance;
            private IProcessInstance pi;
            private IModel model;
            private IActivity a = activity;
   
            public AccessPoint lookupSymbolType(String name)
            {
               if (PredefinedConstants.ACTIVITY_INSTANCE_ACCESSPOINT.equals(name))
               {
                  return getActivity().getAccessPoint(
                        PredefinedConstants.ENGINE_CONTEXT,
                        PredefinedConstants.ACTIVITY_INSTANCE_ACCESSPOINT);
               }
               return getModel().findData(name);
            }
   
            public Object lookupSymbol(String name)
            {
               if (PredefinedConstants.ACTIVITY_INSTANCE_ACCESSPOINT.equals(name))
               {
                  PropertyLayer layer = null;
                  Object object = null;
                  
                  try
                  {         
                     Map<String, Object> props = new HashMap<String, Object>();
                     props.put(IDescriptorProvider.PRP_PROPVIDE_DESCRIPTORS, false);
                     layer = ParametersFacade.pushLayer(props);
                                       
                     object = ai.getIntrinsicOutAccessPointValues().get(
                           PredefinedConstants.ACTIVITY_INSTANCE_ACCESSPOINT);
                  }
                  finally
                  {                  
                     if (null != layer)
                     {
                        ParametersFacade.popLayer();
                     }
                  }
                  return object;
               }
               return getProcessInstance().lookupSymbol(name);
            }
   
            public IActivity getActivity()
            {
               if (a == null)
               {
                  a = ai.getActivity();
               }
               return a;
            }
   
            public IProcessInstance getProcessInstance()
            {
               if (pi == null)
               {
                  pi = ai.getProcessInstance();
               }
               return pi;
            }
   
            public IModel getModel()
            {
               if (model == null)
               {
                  model = (IModel) getProcessInstance().getProcessDefinition().getModel();
               }
               return model;
            }
         };
      }
      
      private SymbolTableFactory() {}
   }
}