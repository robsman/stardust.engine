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
package org.eclipse.stardust.engine.core.query.statistics.utils;

import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;


/**
 * @author rsauer
 * @version $Revision$
 */
public class PkRegistry
{
   private final Map/*<Object, Set<Long>>*/ scopes = CollectionUtils.newMap();
   
   public boolean registerPk(Object scope, long pk)
   {
      Long wrappedPk = new Long(pk);
      
      Set/*<Long>*/ scopedRegistry = (Set) scopes.get(scope);
      if (null == scopedRegistry)
      {
         scopedRegistry = CollectionUtils.newSet();
         scopes.put(scope, scopedRegistry);
      }
      
      return scopedRegistry.add(wrappedPk);
   }
}
