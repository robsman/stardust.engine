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
package org.eclipse.stardust.engine.core.model.convert.aris;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author fherinean
 * @version $Revision$
 */
public class ObjectDefinition extends ArisElement
{
   public static final String TAG_NAME = "ObjDef";
   public static final String TYPE_NUM_ATT = "TypeNum";
   public static final String SYMBOL_NUM_ATT = "SymbolNum";

   public static final int ORGANIZATION = 1;
   public static final int ROLE = 2;
   public static final int ACTIVITY = 3;
   public static final int EVENT = 4;
   public static final int DATA = 5;
   public static final int APPLICATION = 6;
   public static final int RULE = 7;
   public static final int PERSON = 8;
   public static final int TECHNICAL_TERM = 9;
   public static final int GROUP = 10;

   public static final int XOR_SPLIT = 1;
   public static final int AND_SPLIT = 2;
   public static final int OR_SPLIT = 3;

   private static final String DEFINITION_ID_ATT = TAG_NAME + "." + ID_ATT;
   private static final String LINKED_MODELS_ATT = "LinkedModels." + REFS_ATT;
   private static final String CONNECTIONS_ATT = "ToCxnDefs." + REFS_ATT;

   private static final String[] types = {
      "Unknown", "Organization", "Role", "Activity", "Event", "Data", "Application",
      "Rule", "Person", "Technical Term", "Group"
   };

   private static final String[] arisTypes = {
      "OT_ORG_UNIT", "OT_POS", "OT_FUNC", "OT_EVT", "OT_INFO_CARR", "OT_APPL_SYS_TYPE",
      "OT_RULE", "OT_PERS", "OT_TECH_TERM", "OT_GRP"
   };

   private static final String[] splitType = {
      "ST_OPR_XOR_1", "ST_OPR_AND_1", "ST_OPR_OR_1"
   };

   private int type = UNKNOWN;
   private String typeString;
   private ArrayList models;
   private String modelsString;
   private ArrayList connections;
   private String connectionsString;
   private int symbolType = UNKNOWN;
   private String symbolString;

   public ObjectDefinition(AML root, Element group)
   {
      super(root, group.getAttribute(DEFINITION_ID_ATT));
      typeString = group.getAttribute(TYPE_NUM_ATT);
      type = getType(typeString, arisTypes);
      symbolString = group.getAttribute(SYMBOL_NUM_ATT);
      switch (type)
      {
         case RULE:
            symbolType = getType(symbolString, splitType);
            break;
      }
      modelsString = group.getAttribute(LINKED_MODELS_ATT);
      connectionsString = group.getAttribute(CONNECTIONS_ATT);
      NodeList nodes = group.getChildNodes();
      for (int i = 0; i < nodes.getLength(); i++)
      {
         Node child = nodes.item(i);
         if (child instanceof Element)
         {
            Element element = (Element) child;
            if (ATTRIBUTE_TAG_NAME.equals(element.getTagName()))
            {
               addAttribute(element);
            }
            else if (ConnectionDefinition.TAG_NAME.equals(element.getTagName()))
            {
               addArisElement(new ConnectionDefinition(root, element));
            }
            // ExtCxnDef ?
         }
      }
   }

   public String toString()
   {
      return (type == 0 ? "Object " + typeString : types[type]) + ": " + getName();
   }

   public void resolveReferences()
   {
      if (modelsString != null)
      {
         models = new ArrayList();
         StringTokenizer st = new StringTokenizer(modelsString);
         while (st.hasMoreTokens())
         {
            models.add(getArisElement(st.nextToken()));
         }
      }
      if (connectionsString != null)
      {
         connections = new ArrayList();
         StringTokenizer st = new StringTokenizer(connectionsString);
         while (st.hasMoreTokens())
         {
            connections.add(getArisElement(st.nextToken()));
         }
      }
   }

   public int getType()
   {
      return type;
   }

   public int getSymbolType()
   {
      return symbolType;
   }

   public Iterator models()
   {
      return models.iterator();
   }

   public Iterator connections()
   {
      return connections.iterator();
   }

   public String getTypeString()
   {
      return typeString;
   }
}
