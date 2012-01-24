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
public class ConnectionDefinition extends ArisElement
{
   public static final String TAG_NAME = "CxnDef";

   public static final int INPUT = 1;
   public static final int EXECUTES = 2;
   public static final int PERFORMS = 3;
   public static final int MANAGES = 4;
   public static final int WORKS_FOR = 5;
   public static final int PART_OF = 6;
   public static final int TECHNICAL_RESPONSABLE = 7;
   public static final int OUTPUT = 8;
   public static final int DIRECT_TRANSITION = 9;
   public static final int INDIRECT_TRANSITION = 10;
   public static final int DIRECT_EVENT = 11;
   public static final int OUT_RULE = 12;
   public static final int COMBINED_RULE = 13;
   public static final int EVENT_SPLIT = 14;
   public static final int EVENT_JOIN = 15;

   private static final String CONNECTION_ID_ATT = TAG_NAME + "." + ID_ATT;
   private static final String CONNECTION_TYPE_ATT = TAG_NAME + "." + TYPE_ATT;
   private static final String CONNECTION_REF_ATT = "ToObjDef.IdRef";

   private static final String[] types = {
      "Unknown", "Input", "Executes", "Performs", "Manages", "WorksFor", "PartOf",
      "TechnicalResponsable", "Output", "Transition", "Transition", "Transition",
      "OutRule", "CombinedRule", "EventSplit", "EventJoin"
   };

   private static final String[] arisTypes = {
      "CT_PROV_INP_FOR", // in datamapping, data to activity
      "CT_CAN_SUPP_1", // executes, applications to activity
      "CT_EXEC_1", // performs, participant to activity
      "CT_IS_ORG_RSPN", // organization manager, role to organization
      "CT_IS_CRT_BY", // (reverse) works for, organization to role
      "CT_IS_SUPERIOR_1", // (reverse) part of, organization to organization
      "CT_IS_TECH_RESP_1", // technical responsable, participant to activity
      "CT_CRT_OUT_TO", // out datamapping, activity to data
      "CT_IS_PREDEC_OF_1", // direct transition, activity to activity
      "CT_CRT_1", // indirect transition, activity to event
      "CT_ACTIV_1", // indirect transition, event to activity
      "CT_LEADS_TO_1", // rule transition, activity to rule
      "CT_LNK_2", // combined rule transition, rule to rule
      "CT_LEADS_TO_2", // event split transition, rule to event
      "CT_IS_EVAL_BY_1", // event join transition, event to rule
      // CT_MUST_BE_INFO_ABT_1, Roles only,
   };

   private int type;
   private String typeString;
   private String targetId;
   private ObjectDefinition target;

   public ConnectionDefinition(AML root, Element group)
   {
      super(root, group.getAttribute(CONNECTION_ID_ATT));
      typeString = group.getAttribute(CONNECTION_TYPE_ATT);
      type = getType(typeString, arisTypes);
      targetId = group.getAttribute(CONNECTION_REF_ATT);
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
            // ExtCxnDef ?
         }
      }
   }

   public String toString()
   {
      // todo: attributes ?
      return "Connection " + (type == 0 ? typeString : types[type]) + ": "
            + (target == null ? "???" + targetId : target.toString());
   }

   public void resolveReferences()
   {
      target = (ObjectDefinition) getArisElement(targetId);
   }

   public int getType()
   {
      return type;
   }

   public ObjectDefinition getTarget()
   {
      return target;
   }
}
