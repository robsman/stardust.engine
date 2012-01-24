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

import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.engine.core.preferences.XmlPreferenceConstants.TAG_ID;
import static org.eclipse.stardust.engine.core.preferences.XmlPreferenceConstants.TAG_MODULE;
import static org.eclipse.stardust.engine.core.preferences.XmlPreferenceConstants.TAG_NAME;
import static org.eclipse.stardust.engine.core.preferences.XmlPreferenceConstants.TAG_PREFERENCE;
import static org.eclipse.stardust.engine.core.preferences.XmlPreferenceConstants.TAG_PREFERENCES;
import static org.eclipse.stardust.engine.core.preferences.XmlPreferenceConstants.TAG_TYPE;
import static org.eclipse.stardust.engine.core.preferences.XmlPreferenceConstants.TYPE_LIST;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.common.utils.xml.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;


public class XmlPreferenceReader implements IPreferencesReader
{

   public Map readPreferences(InputStream isPreferences) throws IOException
   {
      Document doc;
      Map preferences = null;

      try
      {
         doc = XmlUtils.parseStream(isPreferences);

         Element rootElement = doc.getDocumentElement();

         if (TAG_PREFERENCES.equals(rootElement.getLocalName()))
         {
            String moduleId = rootElement.getAttribute(TAG_MODULE);
            String preferencesId = rootElement.getAttribute(TAG_ID);
            // TODO verify IDs

            preferences = CollectionUtils.newMap();

            NodeList nodes = rootElement.getElementsByTagName(TAG_PREFERENCE);
            for (int i = 0, nNodes = nodes.getLength(); i < nNodes; ++i)
            {
               Element node = (Element) nodes.item(i);

               final String key = node.getAttribute(TAG_NAME);
               final String type = node.getAttribute(TAG_TYPE);

               if ( !StringUtils.isEmpty(key))
               {
                  Object pValue;
                  if ( !isEmpty(type)
                        && (TYPE_LIST.equals(type) || List.class.getName().equals(type)))
                     {
                        pValue = parseListValue(node);
                     }
                     else
                     {
                     pValue = parseSimpleValue(type, node);
                  }
                  preferences.put(key, pValue);
               }
            }
         }
      }
      catch (InternalException ie)
      {
         throw new PublicException(ie);
      }
      return preferences;
   }

   private Object parseListValue(Element node)
   {
      List<Object> list = new ArrayList<Object>();
      NodeList childElements = node.getElementsByTagName("*");
      for (int i = 0, nNodes = childElements.getLength(); i < nNodes; i++ )
      {
         Element childNode = (Element) childElements.item(i);
         final String type = childNode.getAttribute(TAG_TYPE);
         
         Object pValue = parseSimpleValue(type, childNode);
         list.add(pValue);
      }
      return list;
   }

   private Object parseSimpleValue(String type, Node pNode)
   {
      final StringBuilder value = new StringBuilder();
      NodeList valueNodes = pNode.getChildNodes();
      for (int i = 0, nValueNodes = valueNodes.getLength(); i < nValueNodes; i++ )
   {
         Node valueNode = valueNodes.item(i);
         if (valueNode instanceof Text)
         {
            value.append(((Text) valueNode).getNodeValue());
         }
      }

      Object pValue = value.toString();
      try
      {
         Class typeClass = Reflect.getClassFromAbbreviatedName(type);
         if (Boolean.class.equals(typeClass))
         {
            pValue = Boolean.valueOf(value.toString());
         }
         else if (Double.class.equals(typeClass))
         {
            pValue = Double.valueOf(value.toString());
         }
         else if (Float.class.equals(typeClass))
         {
            pValue = Float.valueOf(value.toString());
         }
         else if (Integer.class.equals(typeClass))
         {
            pValue = Integer.valueOf(value.toString());
         }
         else if (Long.class.equals(typeClass))
         {
            pValue = Long.valueOf(value.toString());
         }
      }
      catch (Exception e)
      {
         pValue = value;
      }
      return pValue;
   }
}
