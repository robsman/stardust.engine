/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
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
import java.util.*;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.ResourceInfo;
import org.eclipse.stardust.engine.core.struct.IXPathMap;
import org.eclipse.stardust.engine.core.struct.StructuredTypeRtUtils;
import org.eclipse.stardust.engine.core.struct.TypedXPath;
import org.eclipse.stardust.engine.extensions.dms.data.DmsConstants;
import org.eclipse.stardust.engine.extensions.dms.data.DmsDocumentBean;

class DataCopyResult
{
   Map<String, Serializable> result = CollectionUtils.newMap();
   Set<String> ignoreDataIds = CollectionUtils.newSet();
   
   private final IModel target;

   DataCopyResult(IModel target)
   {
      this.target = target;
   }

   void addValue(IData source, String dataId, Serializable value)
   {
      boolean ignored = true;
      IData data = target.findData(dataId);
      if (data != null)
      {
         if (source != null)
         {
            String typeId = data.getType().getId();
            String sourceTypeId = source.getType().getId();
            if (!CompareHelper.areEqual(sourceTypeId, typeId))
            {
               // we accept conversion from struct to dms types
               if (!isStruct(typeId) || !isStruct(sourceTypeId))
               {
                  // data type changed.
                  return;
               }
            }
            
            boolean isPrimitive = isPrimitive(typeId);
            boolean isStruct = isStruct(typeId);

            // accepted conversions from struct to dms and within primitive types (i.e. int to long)
            if (!isPrimitive && !isStruct &&
                !filter(data.getAllAttributes()).equals(filter(source.getAllAttributes())))
            {
               // data definition changed.
               return;
            }
            if (isStruct)
            {
               if (!typeId.equals(sourceTypeId))
               {
                  if (DocumentTypeUtils.isDmsDocumentData(sourceTypeId))
                  {
                     if (value instanceof ResourceInfo)
                     {
                        value = (Serializable) ((ResourceInfo) value).getProperties();
                     }
                     ignored = !CompareHelper.areEqual(source.getId(), dataId);
                  }
               }
               // traverse and fix
               IXPathMap srcMap = StructuredTypeRtUtils.getXPathMap(source);
               TypedXPath srcPath = !typeId.equals(sourceTypeId) && StructuredTypeRtUtils.isDmsType(sourceTypeId)
                     ? srcMap.getXPath("properties") : srcMap.getRootXPath();
               IXPathMap tgtMap = StructuredTypeRtUtils.getXPathMap(data);
               TypedXPath tgtPath = !typeId.equals(sourceTypeId) && StructuredTypeRtUtils.isDmsType(typeId)
                     ? tgtMap.getXPath("properties") : tgtMap.getRootXPath();
               Map<String, Serializable> map = CollectionUtils.newMap();
               map.put(srcPath.getId(), value);
               repair(tgtPath, srcPath, map);
               value = map.get(srcPath.getId());
               if (value == null)
               {
                  // incompatible.
                  return;
               }
               if (!typeId.equals(sourceTypeId) && DocumentTypeUtils.isDmsDocumentData(typeId) && value instanceof Map)
               {
                  DmsDocumentBean document = new DmsDocumentBean();
                  document.setProperties((Map) value);
                  if (DmsConstants.DATA_TYPE_DMS_DOCUMENT.equals(typeId))
                  {
                     value = document;
                  }
                  else
                  {
                     value = (Serializable) Collections.singletonList(document);
                  }
               }
            }
         }
         result.put(dataId, value);
         if (ignored)
         {
            ignoreDataIds.add(dataId);
         }
      }
   }

   private boolean isPrimitive(String typeId)
   {
      return PredefinedConstants.PRIMITIVE_DATA.equals(typeId);
   }

   private boolean isStruct(String typeId)
   {
      return StructuredTypeRtUtils.isStructuredType(typeId) || DocumentTypeUtils.isDmsDocumentData(typeId);
   }

   private Map<String, Object> filter(Map<String, Object> allAttributes)
   {
      Map<String, Object> filtered = CollectionUtils.newMap();
      if (allAttributes != null)
      {
         for (Map.Entry<String, Object> entry : allAttributes.entrySet())
         {
            String key = entry.getKey();
            if (key.startsWith(PredefinedConstants.ENGINE_SCOPE)
                  && !key.equals(PredefinedConstants.MODELELEMENT_VISIBILITY))
            {
               filtered.put(key, entry.getValue());
            }
         }
      }
      return filtered;
   }

   /**
    * Transformations:
    * <ul>
    * <li>from attribute to element or from element to attribute</li>
    * <li>from list to single item or from single item to list</li>
    * <li>empty collections are removed</li>
    * <li>namespace change is accepted</li>
    * <li>values for removed attributes or elements are dropped</li>
    * <li>values for attributes or elements with a different type are dropped (no type conversion)</li>
    * </ul>
    */
   private void repair(TypedXPath tgtPath, TypedXPath srcPath, Map<String, Serializable> map)
   {
      if (compatible(tgtPath, srcPath))
      {
         Serializable o = map.get(srcPath.getId());
         if (tgtPath.isList())
         {
            if (!srcPath.isList())
            {
               ArrayList<Serializable> list = new ArrayList<Serializable>();
               list.add(o);
               o = list;
               map.put(tgtPath.getId(), o);
            }
         }
         else if (srcPath.isList())
         {
            if (((List) o).isEmpty())
            {
               map.remove(srcPath.getId());
               return;
            }
            else
            {
               o = ((List<Serializable>) o).get(0);
               map.put(tgtPath.getId(), o);
            }
         }
         if (tgtPath.getType() == -1)
         {
            if (tgtPath.isList())
            {
               for (Serializable item : (List<Serializable>) o)
               {
                  repairChildren(tgtPath, srcPath, item);
               }
            }
            else
            {
               repairChildren(tgtPath, srcPath, o);
            }
         }
         if (o instanceof Collection)
         {
            if (((Collection) o).isEmpty())
            {
               map.remove(srcPath.getId());
            }
         }
      }
      else
      {
         map.remove(srcPath.getId());
      }
   }

   private void repairChildren(TypedXPath tgtPath, TypedXPath srcPath, Serializable o)
   {
      if (o instanceof Map)
      {
         Map<String, Serializable> m = (Map) o;
         Set<String> keys = CollectionUtils.newSet(); 
         keys.addAll(m.keySet());
         for (String key : keys)
         {
            TypedXPath tgtChild = tgtPath.getChildXPath(key);
            if (tgtChild == null)
            {
               m.remove(key);
            }
            else
            {
               TypedXPath srcChild = srcPath.getChildXPath(key);
               repair(tgtChild, srcChild, m);
            }
         }
      }
   }

   private boolean compatible(TypedXPath tgtPath, TypedXPath srcPath)
   {
      // accepted assignment of child xpath to root xpath if they have the same type.
      if (srcPath.getParentXPath() != null &&
          tgtPath.getParentXPath() != null &&
          !CompareHelper.areEqual(tgtPath.getId(), srcPath.getId()))
      {
         return false;
      }
      return tgtPath.getType() == srcPath.getType();
   }
}