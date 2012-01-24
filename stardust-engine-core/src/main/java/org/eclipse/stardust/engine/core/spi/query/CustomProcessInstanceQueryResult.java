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
package org.eclipse.stardust.engine.core.spi.query;

import java.io.Serializable;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.query.ProcessInstances;


/**
 * @author rsauer
 * @version $Revision$
 */
public class CustomProcessInstanceQueryResult extends ProcessInstances
{

   private Map extensions;

   public CustomProcessInstanceQueryResult(CustomProcessInstanceQuery query)
   {
      super(query);
   }

   public Serializable getExtendedResult(String key)
   {
      return (Serializable) ((null != extensions) ? extensions.get(key) : null);
   }

   public Serializable setExtendedResult(String key, Serializable value)
   {
      if (null == extensions)
      {
         this.extensions = CollectionUtils.createMap();
      }

      return (Serializable) extensions.put(key, value);
   }

}
