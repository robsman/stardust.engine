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
package org.eclipse.stardust.engine.core.persistence.jdbc;

import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.core.runtime.beans.Constants;


/**
 * @author sauer
 * @version $Revision$
 */
public class DmlManagerRegistry
{

   private final Map dmlManagers = CollectionUtils.newMap();

   public DmlManager getDmlManager(Class type)
   {
      return (DmlManager) dmlManagers.get(type);
   }

   public void registerDmlManager(Class type, DmlManager dmlManager)
   {
      dmlManagers.put(type, dmlManager);
   }

   public void registerDefaultRuntimeClasses(DBDescriptor dbDescriptor,
         SqlUtils sqlUtils, TypeDescriptorRegistry tdRegistry)
   {
      for (int i = 0; i < Constants.PERSISTENT_RUNTIME_CLASSES.length; i++ )
      {
         Class clazz = Constants.PERSISTENT_RUNTIME_CLASSES[i];

         registerDmlManager(clazz, new DmlManager(sqlUtils,
               tdRegistry.getDescriptor(clazz), dbDescriptor));
      }

      for (int i = 0; i < Constants.PERSISTENT_MODELING_CLASSES.length; i++ )
      {
         Class clazz = Constants.PERSISTENT_MODELING_CLASSES[i];

         registerDmlManager(clazz, new DmlManager(sqlUtils,
               tdRegistry.getDescriptor(clazz), dbDescriptor));
      }
   }

}
