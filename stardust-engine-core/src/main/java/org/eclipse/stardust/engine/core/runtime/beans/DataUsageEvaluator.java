/*******************************************************************************
 * Copyright (c) 2011, 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.beans;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.core.runtime.utils.DataUtils;

public class DataUsageEvaluator implements Serializable
{

   private static final long serialVersionUID = -5376443808529675969L;

   private Map<Pair<String, String>, Set<String>> usedDataIdsCache;

   public DataUsageEvaluator()
   {
      usedDataIdsCache = new HashMap<Pair<String, String>, Set<String>>();
   }

   public boolean isUsedInProcess(IData data, IModel model, String processId)
   {
      QName qname = QName.valueOf(processId);
      String ns = qname.getNamespaceURI();
      String modelId = model.getId();
      if (!XMLConstants.NULL_NS_URI.equals(ns) && !ns.equals(modelId))
      {
         // process do not belong to this model
         return false;
      }

      String unqualifiedProcessId = DataUtils.getUnqualifiedProcessId(processId);
      Pair key = new Pair(modelId, unqualifiedProcessId);
      Set<String> usedDataIds = usedDataIdsCache.get(key);
      if (usedDataIds == null)
      {
         usedDataIds = DataUtils.getDataForProcess(unqualifiedProcessId, model);

         usedDataIdsCache.put(key, usedDataIds);
      }

      return usedDataIds.contains(data.getId());
   }

}
