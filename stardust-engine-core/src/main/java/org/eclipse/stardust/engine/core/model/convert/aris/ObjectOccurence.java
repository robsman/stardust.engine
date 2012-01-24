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
import java.util.Collections;
import java.util.Iterator;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.core.compatibility.diagram.Diagram;
import org.eclipse.stardust.engine.core.compatibility.diagram.NodeSymbol;
import org.eclipse.stardust.engine.core.model.convert.ConvertWarningException;
import org.eclipse.stardust.engine.core.model.gui.*;
import org.eclipse.stardust.engine.core.pojo.data.JavaDataTypeUtils;
import org.eclipse.stardust.engine.core.pojo.data.Type;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * @author fherinean
 * @version $Revision$
 */
public class ObjectOccurence extends ArisElement
{
   private static final Logger trace = LogManager.getLogger(ObjectOccurence.class);

   public static final String TAG_NAME = "ObjOcc";

   private static final String OBJECT_ID_ATT = TAG_NAME + "." + ID_ATT;
   private static final String OBJECT_REF_ATT = "ObjDef.IdRef";

   private String referenceId;
   private ObjectDefinition reference;
   private boolean visible;
   private Position position;
   private NodeSymbol symbol;
   private ArrayList incomming;
   private boolean reverse = false;

   public ObjectOccurence(AML root, Element group)
   {
      super(root, group.getAttribute(OBJECT_ID_ATT));
      referenceId = group.getAttribute(OBJECT_REF_ATT);
      // if Visible attribute is missing, the default value is YES
      visible = !"NO".equals(group.getAttribute("Visible"));
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
            else if (Position.TAG_NAME.equals(element.getTagName()))
            {
               position = new Position(element);
            }
            else if (ConnectionOccurence.TAG_NAME.equals(element.getTagName()))
            {
               addArisElement(new ConnectionOccurence(root, this, element));
            }
         }
      }
   }

   public void resolveReferences()
   {
      reference = (ObjectDefinition) getArisElement(referenceId);
   }

   public String toString()
   {
      // todo: attributes ?
      return "NodeSymbol: " + (visible ? "visible (" : "hidden (") + position + ") "
            + (reference == null ? "???" + referenceId : reference.toString());
   }

   public void create(IModel model, Diagram diagram, int[] objectTypes)
   {
      // todo: visible ???
      if (isLegalSymbol(objectTypes))
      {
         switch (reference.getType())
         {
            case ObjectDefinition.DATA:
               symbol = new DataSymbol(getData(model));
               break;
            case ObjectDefinition.APPLICATION:
               symbol = new ApplicationSymbol(getApplication(model));
               break;
            case ObjectDefinition.ROLE:
               symbol = new RoleSymbol(getRole(model));
               break;
            case ObjectDefinition.ORGANIZATION:
               symbol = new OrganizationSymbol(getOrganization(model));
               break;
            case ObjectDefinition.GROUP:
               symbol = new OrganizationSymbol(getOrganization(model));
               break;
            case ObjectDefinition.ACTIVITY:
               symbol = new ActivitySymbol(getActivity((IProcessDefinition) diagram.getParent()));
               break;
            case ObjectDefinition.EVENT:
               if (mustRoute())
               {
                  symbol = new ActivitySymbol(getActivity((IProcessDefinition) diagram.getParent()));
               }
               break;
         }
         if (symbol != null)
         {
            placeSymbol(diagram);
         }
      }
   }

   private boolean mustRoute()
   {
      boolean mustRoute = true;
      if (incomming != null && incomming.size() == 1 && size() == 1)
      {
         ConnectionOccurence inCx = (ConnectionOccurence) incomming().next();
         ConnectionOccurence outCx = (ConnectionOccurence) elements().next();
         ObjectOccurence in = inCx.getSource();
         ObjectOccurence out = outCx.getTarget();
         if (in.getReference().getType() == ObjectDefinition.ACTIVITY ||
               out.getReference().getType() == ObjectDefinition.ACTIVITY)
         {
            symbol = new AnnotationSymbol(reference.getName(), null);
            out.incomming.remove(outCx);
            out.incomming.add(inCx);
            inCx.setTarget(outCx.getTarget());
            outCx.setConnectionTarget(inCx);
            mustRoute = false;
         }
      }
      return mustRoute;
   }

   private void placeSymbol(Diagram diagram)
   {
      if (reverse)
      {
         symbol.setX(position.y);
         symbol.setY(position.x / 3);
      }
      else
      {
         symbol.setX(position.x);
         symbol.setY(position.y / 3);
      }
      diagram.addToNodes(symbol, 0);
   }

   public void processRule(Diagram diagram)
   {
      // break condition for recursive use
      if (symbol != null)
      {
         return;
      }
      JoinSplitType jsType = getReference().getSymbolType() == ObjectDefinition.AND_SPLIT ?
            JoinSplitType.And : JoinSplitType.Xor;
      boolean hasOwn = false;
      if (size() > 1 && incomming.size() > 1)
      {
         // we need an extra activity here, with both split and join set to the same type.
         hasOwn = true;
      }
      else
      {
         for (Iterator i = elements(); i.hasNext();)
         {
            ConnectionOccurence conn = (ConnectionOccurence) i.next();
            if (conn.getReference().getType() == ConnectionDefinition.COMBINED_RULE)
            {
               // rule to rule combined case, so we add here also an extra activity
               hasOwn = true;
            }
         }
      }
      if (hasOwn)
      {
         IProcessDefinition pd = (IProcessDefinition) diagram.getParent();
         IActivity activity = pd.createActivity(getId(), reference.getName(),
               reference.getDescription(), 0);
         if (size() > 1)
         {
            activity.setSplitType(jsType);
         }
         if (incomming.size() > 1)
         {
            activity.setJoinType(jsType);
         }
         symbol = new ActivitySymbol(activity);
         placeSymbol(diagram);
      }
      else
      {
         if (size() == 1)
         {
            // join rule
            ConnectionOccurence connection = (ConnectionOccurence) elements().next();
            ObjectOccurence target = connection.getTarget();
            symbol = target.getSymbol();
            IActivity activity = ((ActivitySymbol)symbol).getActivity();
            activity.setJoinType(jsType);
         }
         else if (incomming.size() == 1)
         {
            // split rule
            ConnectionOccurence connection = (ConnectionOccurence) incomming().next();
            ObjectOccurence source = connection.getSource();
            if (source.getReference().getType() == ObjectDefinition.RULE)
            {
               source.processRule(diagram);
            }
            symbol = source.getSymbol();
            IActivity activity = ((ActivitySymbol)symbol).getActivity();
            activity.setSplitType(jsType);
         }
         else
         {
            trace.warn(indent());
         }
      }
   }

   public void connect(IModel model, Diagram diagram, int[] objectTypes)
   {
      if (isLegalSymbol(objectTypes))
      {
         for (Iterator i = elements(); i.hasNext();)
         {
            ((ConnectionOccurence) i.next()).connect(this, diagram);
         }
      }
   }

   private boolean isLegalSymbol(int[] objectTypes)
   {
      if (objectTypes == null)
      {
         // all symbols are valid
         return true;
      }
      for (int i = 0; i < objectTypes.length; i++)
      {
         if (reference.getType() == objectTypes[i])
         {
            return true;
         }
      }
      addError(new ConvertWarningException("Illegal symbol in diagram: " + reference.getTypeString()));
      return false;
   }

   private IActivity getActivity(IProcessDefinition pd)
   {
      // activities are never referenced multiple times in a model, so we create one at
      // each OT_FUNC or OT_EVT occurence.
      return pd.createActivity(getId(), reference.getName(),
               reference.getDescription(), 0);
   }

   private IData getData(IModel model)
   {
      IData data = model.findData(reference.getId());
      if (data == null)
      {
         data = model.createData(reference.getId(),
                  model.findDataType(PredefinedConstants.PRIMITIVE_DATA),
                  reference.getName(), reference.getDescription(), false, 0,
                  JavaDataTypeUtils.initPrimitiveAttributes(Type.String, ""));
      }
      return data;
   }

   private IApplication getApplication(IModel model)
   {
      IApplication application = model.findApplication(reference.getId());
      if (application == null)
      {
         application = model.createApplication(reference.getId(), reference.getName(),
               reference.getDescription(), 0);
      }
      return application;
   }

   private IOrganization getOrganization(IModel model)
   {
      IOrganization role = (IOrganization) model.findParticipant(reference.getId());
      if (role == null)
      {
         role = model.createOrganization(reference.getId(), reference.getName(),
               reference.getDescription(), 0);
      }
      return role;
   }

   private IRole getRole(IModel model)
   {
      IRole role = (IRole) model.findParticipant(reference.getId());
      if (role == null)
      {
         role = model.createRole(reference.getId(), reference.getName(),
               reference.getDescription(), 0);
      }
      return role;
   }

   public ObjectDefinition getReference()
   {
      return reference;
   }

   public NodeSymbol getSymbol()
   {
      return symbol;
   }

   public void addIncomming(ConnectionOccurence connectionOccurence)
   {
      if (incomming == null)
      {
         incomming = new ArrayList();
      }
      incomming.add(connectionOccurence);
   }

   public Iterator incomming()
   {
      return incomming == null ? Collections.EMPTY_LIST.iterator() : incomming.iterator();
   }

   public void setReverse(boolean reverse)
   {
      this.reverse = reverse;
   }
}
