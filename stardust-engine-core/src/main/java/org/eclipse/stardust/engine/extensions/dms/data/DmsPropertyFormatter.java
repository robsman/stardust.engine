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
package org.eclipse.stardust.engine.extensions.dms.data;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.stardust.common.StringUtils;


public class DmsPropertyFormatter
{

   public static final int AS_MAP = 1;
   public static final int AS_LIST = 2;

   private String excludeXPath;
   private int type;

   public DmsPropertyFormatter(int type, String excludeXPath)
   {
      this.type = type;
      this.excludeXPath = excludeXPath;
   }

   public void visit(Map m, String xPath)
   {
      if (m == null)
      {
         return;
      }

      for (Iterator i = m.entrySet().iterator(); i.hasNext(); )
      {
         Entry e = (Entry)i.next();
         String key = (String)e.getKey();
         if (e.getValue() instanceof List)
         {
            if (isUnstructuredProperty(key) && type == AS_MAP)
            {
               if ( !appendXPath(xPath, key).equals(this.excludeXPath))
               {
                  e.setValue(AuditTrailUtils.convertToPropertyMap((List) e.getValue()));
               }
            }
            else
            {
               visit((List)e.getValue(), appendXPath(xPath, key));
            }
         }
         else if (e.getValue() instanceof Map)
         {
            if (isUnstructuredProperty(key) && type == AS_LIST)
            {
               if ( !appendXPath(xPath, key).equals(this.excludeXPath))
               {
                  e.setValue(AuditTrailUtils.convertToPropertyList((Map) e.getValue()));
               }
            }
            else
            {
               visit((Map)e.getValue(), appendXPath(xPath, key));
            }
         }
      }
   }

   private boolean isUnstructuredProperty(String key)
   {
      return AuditTrailUtils.RES_PROPERTIES.equals(key) || AuditTrailUtils.FILE_ANNOTATIONS.equals(key);
   }

   public void visit(List l, String xPath)
   {
      if (l == null)
      {
         return;
      }

      for (int i=0; i<l.size(); i++)
      {
         Object o = l.get(i);

         if (o instanceof List)
         {
            visit((List)o, xPath);
         }
         else if (o instanceof Map)
         {
            visit((Map)o, xPath);
         }
      }
   }

   private String appendXPath(String parentXPath, String childName)
   {
      if (StringUtils.isEmpty(parentXPath))
      {
         return childName;
      }
      else
      {
         StringBuffer sb = new StringBuffer(parentXPath);
         sb.append("/");
         sb.append(childName);
         return sb.toString();
      }
   }
}
