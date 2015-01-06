/**********************************************************************************
 * Copyright (c) 2013 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.engine.runtime.utils;

import java.util.Set;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class HazelcastUtils
{
   /**
    * @return the one and only running Hazelcast instance
    */
   public static HazelcastInstance getHazelcastInstance()
   {
      final Set<HazelcastInstance> instances = Hazelcast.getAllHazelcastInstances();
      if (instances.isEmpty())
      {
         throw new IllegalStateException("No running Hazelcast instance found.");
      }
      if (instances.size() > 1)
      {
         throw new IllegalStateException("More than one Hazelcast instance is running on this JVM: " + instances);
      }
      
      return instances.iterator().next();
   }
}
