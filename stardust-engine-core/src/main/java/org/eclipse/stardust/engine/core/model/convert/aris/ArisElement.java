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

import java.util.*;

import org.eclipse.stardust.engine.core.model.convert.ConvertWarningException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;


/**
 * @author fherinean
 * @version $Revision$
 */
public abstract class ArisElement
{
   public static final String ID_ATT = "ID";
   public static final String TYPE_ATT = "Type";
   public static final String NAME_ATT = "AT_NAME";
   public static final String FULL_NAME_ATT = "AT_NAME_FULL";
   public static final String DESCRIPTION_ATT = "AT_DESC";
   public static final String ATTRIBUTE_TAG_NAME = "AttrDef";
   public static final String VALUE_TAG_NAME = "AttrValue";
   public static final String REFS_ATT = "IdRefs";

   public static final int UNKNOWN = 0;

   private static final String ATTRIBUTE_TYPE_ATT = ATTRIBUTE_TAG_NAME + "." + TYPE_ATT;

   private String id;
   private HashMap attributes;
   private ArrayList elements;
   private AML root;

   public ArisElement(AML parent, String id)
   {
      this.root = parent;
      this.id = id;
      parent.addGlobal(id, this);
   }

   public ArisElement getArisElement(String id)
   {
      return root.getElement(id);
   }

   public void addError(ConvertWarningException ex)
   {
      root.addError(ex);
   }

   public String getId()
   {
      return id;
   }

   public Map getAttributes()
   {
      return attributes;
   }

   public String getAttribute(String name)
   {
      return (String) (attributes == null ? null : attributes.get(name));
   }

   public void addAttribute(Element element)
   {
      String value = null;
      String name = element.getAttribute(ATTRIBUTE_TYPE_ATT);
      NodeList values = element.getElementsByTagName(VALUE_TAG_NAME);
      for (int i = 0; i < values.getLength(); i++)
      {
         Element attValue = (Element) values.item(i);
         Node text = attValue.getFirstChild();
         if (text != null)
         {
            value = text.getNodeValue().replace('\n', ' ');
         }
         // todo: handle the multilanguage case
         break;
      }
      if (attributes == null)
      {
         attributes = new HashMap();
      }
      attributes.put(name, value);
   }

   public void addArisElement(ArisElement element)
   {
      if (elements == null)
      {
         elements = new ArrayList();
      }
      if (elements.contains(element))
      {
         root.addError(new ConvertWarningException(
               "Duplicate element '" + element + "' in '" + this + "'"));
      }
      else
      {
         elements.add(element);
      }
   }

   public String toString()
   {
      return getId() + " " + attributes;
   }

   public boolean isPrototype()
   {
      return root.isPrototype();
   }

   public String indent()
   {
      return indent("");
   }

   public String indent(String indent)
   {
      StringBuffer b = new StringBuffer();
      b.append(indent).append(toString());
      if (attributes != null)
      {
         b.append(" ").append(attributes);
      }
      if (elements != null)
      {
         b.append("\r\n");
         String bi = indent + "  ";
         for (Iterator i = elements.iterator(); i.hasNext();)
         {
            ArisElement element = (ArisElement) i.next();
            b.append(element.indent(bi));
            if (i.hasNext())
            {
               b.append("\r\n");
            }
         }
      }
      return b.toString();
   }

   public String getName()
   {
      String name = getAttribute(FULL_NAME_ATT);
      if (name == null)
      {
         name = getAttribute(NAME_ATT);
      }
      return name == null ? id : name;
   }

   public String getDescription()
   {
      return getAttribute(DESCRIPTION_ATT);
   }

   public Iterator elements()
   {
      return elements == null ? Collections.EMPTY_LIST.iterator() : elements.iterator();
   }

   public int size()
   {
      return elements == null ? 0 : elements.size();
   }

   protected int getType(String type, String[] arisTypes)
   {
      for (int i = 0; i < arisTypes.length; i++)
      {
         if (arisTypes[i].equals(type))
         {
            return i + 1;
         }
      }
      return UNKNOWN;
   }

   public abstract void resolveReferences();
}
