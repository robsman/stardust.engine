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
import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.common.config.ValueProvider;
import org.eclipse.stardust.engine.core.runtime.beans.Constants;


/**
 * @author sauer
 * @version $Revision$
 */
public class TypeDescriptorRegistry
{

   private static final String KEY_GLOBAL_TYPE_DESCRIPTOR_CACHE = TypeDescriptor.class.getName()
         + ".GlobalCache";

   private final Map descriptorsByType = CollectionUtils.newMap();

   private final Map descriptorsByTable = CollectionUtils.newMap();

   public static TypeDescriptorRegistry current()
   {
      final GlobalParameters globals = GlobalParameters.globals();
      
      TypeDescriptorRegistry registry = (TypeDescriptorRegistry) globals.get(KEY_GLOBAL_TYPE_DESCRIPTOR_CACHE);
      if (null == registry)
      {
         registry = (TypeDescriptorRegistry) globals.initializeIfAbsent(
               KEY_GLOBAL_TYPE_DESCRIPTOR_CACHE, new ValueProvider()
               {
                  public Object getValue()
                  {
                     TypeDescriptorRegistry registry = new TypeDescriptorRegistry();

                     registry.registerDefaultRuntimeClasses();
                     
                     return registry;
                  }
               });
      }

      return registry;
   }
   
   public TypeDescriptor getDescriptor(Class type)
   {
      return (TypeDescriptor) descriptorsByType.get(type);
   }

   public TypeDescriptor getDescriptorForTable(String tableName)
   {
      return (TypeDescriptor) descriptorsByTable.get(tableName);
   }

   public void registerDescriptor(TypeDescriptor descriptor)
   {
      descriptorsByType.put(descriptor.getType(), descriptor);
      descriptorsByTable.put(descriptor.getTableName(), descriptor);
   }

   public void registerDefaultRuntimeClasses()
   {
      for (int i = 0; i < Constants.PERSISTENT_RUNTIME_CLASSES.length; i++ )
      {
         Class clazz = Constants.PERSISTENT_RUNTIME_CLASSES[i];

         registerDescriptor(new TypeDescriptor(clazz));
      }

      for (int i = 0; i < Constants.PERSISTENT_MODELING_CLASSES.length; i++ )
      {
         Class clazz = Constants.PERSISTENT_MODELING_CLASSES[i];

         registerDescriptor(new TypeDescriptor(clazz));
      }
   }
   
}
