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
package org.eclipse.stardust.engine.core.model.beans;

import static org.eclipse.stardust.common.CollectionUtils.newHashSet;

import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.core.model.utils.ModelElement;
import org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariable;
import org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariableUtils;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



/**
 * Provides some additional methods for reading data from a DOM-Node.
 */
public class NodeReader
{
   private Node node = null;
   private final IConfigurationVariablesProvider confVarProvider;
   
   public NodeReader(IConfigurationVariablesProvider confVarProvider)
   {
      this.confVarProvider = confVarProvider;
   }

   public NodeReader(Node node, IConfigurationVariablesProvider confVarProvider)
   {
      setNode(node);
      this.confVarProvider = confVarProvider;
   }

   public String getAttribute(String name)
   {
      return getAttribute(name, true);
   }

   public String getAttribute(String name, boolean evalVariables)
   {
      String value = getRawAttribute(name, evalVariables);
      if (value != null)
      {
         value = value.intern();
      }
      return value;
   }
   
   public String getRawAttribute(String name)
   {
      return getRawAttribute(name, true);
   }
   
   public String getRawAttribute(String name, boolean evalVariables)
   {
      NamedNodeMap attributes = node.getAttributes();

      if (attributes != null)
      {
         Node attrNode = attributes.getNamedItem(name);

         if (attrNode != null)
         {
            final String value = attrNode.getNodeValue().trim();
            return evalVariables ? evaluteModelVariables(value) : value;
         }
      }

      return null;
   }
   
   public boolean getBooleanAttribute(String name, boolean defaultValue)
   {
      String value = getRawAttribute(name);

      if (value == null || value.length() == 0)
      {
         return defaultValue;
      }
      return value.compareToIgnoreCase("true") == 0;
   }

   public int getIntegerAttribute(String name, int defaultValue)
   {
      String value = getRawAttribute(name);

      if (value != null)
      {
         try
         {
            return Integer.parseInt(value);
         }
         catch (NumberFormatException e)
         {
            // @todo (france, ub): warn
         }
      }

      return defaultValue;
   }

   public void setNode(Node newNode)
   {
      node = newNode;
   }

   /**
    * Returns the value of the childnode from the DOM-Node or
    * null if there isn't such subnode.
    */
   public String getChildValue(String childName)
   {
      return getChildValue(childName, true);
   }
   
   public String getChildValue(String childName, boolean evalVariables)
   {
      String value = getRawChildValue(childName, evalVariables);
      if (value != null)
      {
         value = value.intern();
      }
      return value;
   }
   
   public String getRawChildValue(String childName)
   {
      return getRawChildValue(childName, true);
   }
   
   public String getRawChildValue(String childName, boolean evalVariables)
   {
      String text = null;

      try
      {
         Node childNode = node.getFirstChild();

         while ((childNode != null) && ( !childName.equals(childNode.getNodeName())))
         {
            childNode = childNode.getNextSibling();
         }
         if (null != childNode)
         {
            StringBuffer buffer = null;
            NodeList textNodes = childNode.getChildNodes();
            for (int i = 0, nTextNodes = textNodes.getLength(); i < nTextNodes; i++)
            {
               Node textNode = textNodes.item(i);
               if (null != textNode.getNodeValue())
               {
                  if (null == buffer)
                  {
                     buffer = new StringBuffer(400);
                  }
                  buffer.append(textNode.getNodeValue());
               }
            }
            text = (null != buffer) ? buffer.toString() : null;
         }
      }
      catch (NullPointerException _ex)
      {
         // Ignore exception because the searched value could be optional

         text = null;
      }
      
      if (text != null)
      {
         text = text.trim();
         text = evalVariables ? evaluteModelVariables(text) : text;
      }

      return text;
   }

   public ModelElement getReference(String name, IModel model)
   {
      int elementOID = getIntegerAttribute(name, 0);
      ModelElement result = model.lookupElement(elementOID);
      if (result == null)
      {
         // @todo (france, ub): warn
      }
      return result;
   }

   public List getListAttribute(String name)
   {
      List result = CollectionUtils.newList();
      String raw = getRawAttribute(name);
      if (!StringUtils.isEmpty(raw))
      {
         StringTokenizer tkr = new StringTokenizer(raw, ",");
         while (tkr.hasMoreTokens())
         {
            String value = tkr.nextToken().trim();
            result.add(value.intern());
         }
      }
      return result;
   }

   public List getRawListAttribute(String name)
   {
      List result = CollectionUtils.newList();
      String raw = getRawAttribute(name);
      if (!StringUtils.isEmpty(raw))
      {
         StringTokenizer tkr = new StringTokenizer(raw, ",");
         while (tkr.hasMoreTokens())
         {
            String value = tkr.nextToken().trim();
            result.add(value);
         }
      }
      return result;
   }

   private String evaluteModelVariables(String text)
   {
      // register names of potential variables
      Matcher varMatcher = ConfigurationVariableUtils
            .getConfigurationVariablesMatcher(text);
      
      Set<String> detectedCvNames = newHashSet();
      
      while (varMatcher.find())
      {
         String varCandidate = varMatcher.group(1);
         confVarProvider.registerCandidate(varCandidate);
         detectedCvNames.add(varCandidate);
      }
      
      // replace variables with values
      final List<ConfigurationVariable> configurationVariables = confVarProvider
            .getConfigurationVariables().getConfigurationVariables();
      
      if ( !detectedCvNames.isEmpty())
      {
         for (ConfigurationVariable var : configurationVariables)
         {
        	if (detectedCvNames.contains(var.getName())){
        		text = ConfigurationVariableUtils.replace(var, text);
        	}
         }
      }
      
      // replace escaped Variables: \${abc} -> ${abc}
      Matcher escMatcher = ConfigurationVariableUtils
            .getEscapedConfigurationVariablesMatcher(text);
      if (escMatcher.find())
      {
         text = escMatcher.replaceAll("$1");
      }

      return text;
   }
}

