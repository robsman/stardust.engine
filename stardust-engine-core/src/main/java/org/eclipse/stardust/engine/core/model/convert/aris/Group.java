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

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

/**
 * @author fherinean
 * @version $Revision$
 */
public class Group extends ArisElement
{
   public static final String TAG_NAME = "Group";
   public static final String GROUP_ID_ATT = TAG_NAME + "." + ID_ATT;

   /**
    * An ARIS Group.
    *
    * The following items are processed in the modelling environment:
    * - Group (0..n): subgroups.
    * - ObjDef (0..n): object definitions. Contains activities, perticipants (roles and
    *      organizations), data and applications.
    * - Model (0..n): model definitions. Contains models and diagrams.
    * - AttrDef (0..n): dynamic attributes.
    *
    * The Group item contains 1 attribute, the Id.
    *
    * @param root the AML object
    * @param group the xml element containing the Group definition
    */
   public Group(AML root, Element group)
   {
      super(root, group.getAttribute(GROUP_ID_ATT));
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
            else if (TAG_NAME.equals(element.getTagName()))
            {
               addArisElement(new Group(root, element));
            }
            else if (ObjectDefinition.TAG_NAME.equals(element.getTagName()))
            {
               addArisElement(new ObjectDefinition(root, element));
            }
            else if (Model.TAG_NAME.equals(element.getTagName()))
            {
               addArisElement(new Model(root, element));
            }
         }
      }
   }

   public String toString()
   {
      return "Group: " + getName();
   }

   public void resolveReferences()
   {
      // no references !
   }
}
