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
package org.eclipse.stardust.engine.core.preferences;

import static org.eclipse.stardust.engine.core.preferences.XmlPreferenceConstants.TAG_ID;
import static org.eclipse.stardust.engine.core.preferences.XmlPreferenceConstants.TAG_LIST_VALUE;
import static org.eclipse.stardust.engine.core.preferences.XmlPreferenceConstants.TAG_MODULE;
import static org.eclipse.stardust.engine.core.preferences.XmlPreferenceConstants.TAG_NAME;
import static org.eclipse.stardust.engine.core.preferences.XmlPreferenceConstants.TAG_PREFERENCE;
import static org.eclipse.stardust.engine.core.preferences.XmlPreferenceConstants.TAG_PREFERENCES;
import static org.eclipse.stardust.engine.core.preferences.XmlPreferenceConstants.TAG_TYPE;
import static org.eclipse.stardust.engine.core.preferences.XmlPreferenceConstants.TYPE_LIST;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.core.runtime.utils.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;


public class XmlPreferenceWriter implements IPreferencesWriter
{

   public void writePreferences(OutputStream osPreferences, String moduleId,
         String preferencesId, Map preferences) throws IOException
   {
      Document prefsDoc = XmlUtils.newDocument();

      Element ePreferences = prefsDoc.createElement(TAG_PREFERENCES);
      ePreferences.setAttribute(TAG_MODULE, moduleId);
      ePreferences.setAttribute(TAG_ID, preferencesId);

      prefsDoc.appendChild(ePreferences);

      for (Iterator i = preferences.entrySet().iterator(); i.hasNext();)
      {
         Map.Entry entry = (Map.Entry) i.next();

         if ( !StringUtils.isEmpty((String) entry.getKey()))
         {
            Element eEntry = prefsDoc.createElement(TAG_PREFERENCE);
            eEntry.setAttribute(TAG_NAME, (String) entry.getKey());
            Object prefValue = entry.getValue();
            if (null != prefValue)
            {
               
               if (prefValue instanceof List)
               {
                  appendListValue(eEntry, (List) prefValue);
               }
               else
               {
                  appendSimpleValue(eEntry, prefValue);
               }
            }
            ePreferences.appendChild(eEntry);
         }
      }

      XmlUtils.serialize(prefsDoc, osPreferences, 2);
      osPreferences.flush();
   }

   private void appendListValue(Element entry, List prefValue)
   {
      if ( !prefValue.isEmpty())
      {
         entry.setAttribute(TAG_TYPE, TYPE_LIST);
         for (Object listValue : prefValue)
         {
            if (listValue != null)
            {
               Element listNode = entry.getOwnerDocument().createElement(TAG_LIST_VALUE);
               appendSimpleValue(listNode, listValue);
               entry.appendChild(listNode);
            }
         }
      }
   }

   private void appendSimpleValue(Element eEntry, Object prefValue)
   {
      String type = Reflect.getAbbreviatedName(prefValue.getClass());
      if ( !StringUtils.isEmpty(type))
      {
         eEntry.setAttribute(TAG_TYPE, type);
      }
      Text nodeValue = eEntry.getOwnerDocument().createTextNode(prefValue.toString());
      eEntry.appendChild(nodeValue);
   }

}
