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

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.core.compatibility.diagram.ConnectionSymbol;
import org.eclipse.stardust.engine.core.compatibility.diagram.Diagram;
import org.eclipse.stardust.engine.core.compatibility.diagram.NodeSymbol;
import org.eclipse.stardust.engine.core.model.gui.*;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * @author fherinean
 * @version $Revision$
 */
public class ConnectionOccurence extends ArisElement
{
   private static final Logger trace = LogManager.getLogger(ConnectionOccurence.class);

   public static final String TAG_NAME = "CxnOcc";

   private static final String CONNECTION_ID_ATT = TAG_NAME + "." + ID_ATT;
   private static final String CONNECTION_REF_ATT = "CxnDef.IdRef";
   private static final String CONNECTION_TARGET_ATT = "ToObjOcc.IdRef";

   private ObjectOccurence source;
   private String targetId;
   private ObjectOccurence target;
   private String referenceId;
   private ConnectionDefinition reference;
   private ArrayList positions;
   private boolean visible;
   private ConnectionOccurence cxTarget;
   private ConnectionSymbol symbol;

   public ConnectionOccurence(AML root, ObjectOccurence source, Element group)
   {
      super(root, group.getAttribute(CONNECTION_ID_ATT));
      this.source = source;
      targetId = group.getAttribute(CONNECTION_TARGET_ATT);
      referenceId = group.getAttribute(CONNECTION_REF_ATT);
      // if Visible attribute is missing, the default value is YES
      visible = !"NO".equals(group.getAttribute("Visible"));
      positions = new ArrayList();
      NodeList nodes = group.getChildNodes();
      for (int i = 0; i < nodes.getLength(); i++)
      {
         Node child = nodes.item(i);
         if (child instanceof Element)
         {
            Element element = (Element) child;
            if (Position.TAG_NAME.equals(element.getTagName()))
            {
               positions.add(new Position(element));
            }
            // ExtCxnOcc ?
         }
      }
   }

   public String toString()
   {
      return "ConnectionSymbol: " + (visible ? "visible " : "hidden ") + positions + " "
            + (reference == null ? "???" + referenceId : reference.toString());
   }

   public void resolveReferences()
   {
      target = (ObjectOccurence) getArisElement(targetId);
      target.addIncomming(this);
      reference = (ConnectionDefinition) getArisElement(referenceId);
   }

   public void connect(ObjectOccurence source, Diagram diagram)
   {
      switch (source.getReference().getType())
      {
         case ObjectDefinition.ACTIVITY:
            switch (reference.getType())
            {
               case ConnectionDefinition.OUTPUT:
                  dataMappingConnection(source, diagram);
                  break;
               case ConnectionDefinition.DIRECT_TRANSITION:
               case ConnectionDefinition.INDIRECT_TRANSITION:
               case ConnectionDefinition.OUT_RULE:
                  transitionConnection(source, diagram);
                  break;
               default:
                  trace.warn("Unregistered connection for " + source.toString() + " >>> " + toString());
                  transitionConnection(source, diagram);
            }
            break;
         case ObjectDefinition.EVENT:
            if (target != null)
            {
               switch (reference.getType())
               {
                  case ConnectionDefinition.DIRECT_EVENT:
                  case ConnectionDefinition.EVENT_JOIN:
                     transitionConnection(source, diagram);
                     break;
                  default:
                     trace.warn("Unregistered connection for " + source.toString() + " >>> " + toString());
                     transitionConnection(source, diagram);
               }
            }
            else
            {
               AnnotationSymbol ann = (AnnotationSymbol) source.getSymbol();
               symbol = new RefersToConnection(ann);
               symbol.setSecondSymbol(cxTarget.symbol);
               diagram.addToConnections(symbol, 0);
            }
            break;
         case ObjectDefinition.RULE:
            switch (reference.getType())
            {
               case ConnectionDefinition.COMBINED_RULE:
               case ConnectionDefinition.EVENT_SPLIT:
               case ConnectionDefinition.DIRECT_EVENT:
                  transitionConnection(source, diagram);
                  break;
               default:
                  trace.warn("Unregistered connection for " + source.toString() + " >>> " + toString());
                  transitionConnection(source, diagram);
            }
            break;
         case ObjectDefinition.DATA:
            switch (reference.getType())
            {
               case ConnectionDefinition.INPUT:
                  dataMappingConnection(source, diagram);
                  break;
               default:
                  trace.warn("DATA: " + source + " >>> " + toString());
            }
            break;
         case ObjectDefinition.APPLICATION:
            switch (reference.getType())
            {
               case ConnectionDefinition.EXECUTES:
                  executedByConnection(source, diagram);
                  break;
               default:
                  trace.warn("APPLICATION: " + source + " >>> " + toString());
            }
            break;
         case ObjectDefinition.ROLE:
            switch (reference.getType())
            {
               case ConnectionDefinition.PERFORMS:
                  performsConnection(source, diagram);
                  break;
               case ConnectionDefinition.TECHNICAL_RESPONSABLE:
                  performsConnection(source, diagram);
                  break;
               case ConnectionDefinition.MANAGES:
                  managesConnection(source, diagram);
                  break;
               default:
                  trace.warn(source.toString() + " >>> " + toString());
            }
            break;
         case ObjectDefinition.ORGANIZATION:
         case ObjectDefinition.GROUP:
            switch (reference.getType())
            {
               case ConnectionDefinition.PERFORMS:
                  performsConnection(source, diagram);
                  break;
               case ConnectionDefinition.TECHNICAL_RESPONSABLE:
                  performsConnection(source, diagram);
                  break;
               case ConnectionDefinition.WORKS_FOR:
                  worksForConnection(source, diagram);
                  break;
               case ConnectionDefinition.PART_OF:
                  partOfConnection(source, diagram);
                  break;
               default:
                  trace.warn("ORGANIZATION: " + source + " >>> " + toString());
            }
            break;
      }
   }

   public ObjectOccurence getTarget()
   {
      return target;
   }

   public ConnectionDefinition getReference()
   {
      return reference;
   }

   private void transitionConnection(ObjectOccurence source, Diagram diagram)
   {
      ActivitySymbol sourceActivitySymbol = (ActivitySymbol) source.getSymbol();
      ActivitySymbol targetActivitySymbol = (ActivitySymbol) target.getSymbol();
      if (sourceActivitySymbol != targetActivitySymbol)
      {
         createTransition(sourceActivitySymbol, targetActivitySymbol, diagram);
      }
   }

   private void createTransition(ActivitySymbol sourceActivitySymbol, ActivitySymbol targetActivitySymbol, Diagram diagram)
   {
      if (sourceActivitySymbol == null || targetActivitySymbol == null)
      {
         // todo: analyze this
         return;
      }
      IActivity sourceActivity = sourceActivitySymbol.getActivity();
      IActivity targetActivity = targetActivitySymbol.getActivity();
      IProcessDefinition pd = (IProcessDefinition) diagram.getParent();
      try
      {
         if (isPrototype())
         {
            // make sure the transition succeeds
            if (sourceActivity.getAllOutTransitions().hasNext() && sourceActivity.getSplitType().equals(JoinSplitType.None))
            {
               sourceActivity.setSplitType(JoinSplitType.And);
            }
            if (targetActivity.getAllInTransitions().hasNext() && targetActivity.getJoinType().equals(JoinSplitType.None))
            {
               targetActivity.setJoinType(JoinSplitType.And);
            }
         }
         ITransition transition = pd.createTransition(getId(), reference.getName(),
               reference.getDescription(), sourceActivity, targetActivity, 0);
         if (visible)
         {
            symbol = new TransitionConnection(sourceActivitySymbol,
                  targetActivitySymbol, transition);
            diagram.addToConnections(symbol, 0);
         }
      }
      catch (Exception ex)
      {
         trace.warn("Error in " + pd.toString() + " " + sourceActivity + " " + indent(""), ex);
      }
   }

   private void dataMappingConnection(ObjectOccurence source, Diagram diagram)
   {
      boolean input = false;
      DataSymbol dataSymbol = null;
      ActivitySymbol activitySymbol = null;
      if (source.getReference().getType() == ObjectDefinition.DATA)
      {
         dataSymbol = (DataSymbol) source.getSymbol();
         activitySymbol = (ActivitySymbol) target.getSymbol();
         input = true;
      }
      else
      {
         dataSymbol = (DataSymbol) target.getSymbol();
         activitySymbol = (ActivitySymbol) source.getSymbol();
      }
      IData data = dataSymbol.getData();
      IActivity activity = activitySymbol.getActivity();
      if (activity.getImplementationType().equals(ImplementationType.Route))
      {
         activity.setImplementationType(ImplementationType.Manual);
         if (!isPrototype())
         {
            trace.warn("Route activity with data mapping: " + source);
         }
      }
      activity.createDataMapping(reference.getId(), reference.getId(), data, input ? Direction.IN : Direction.OUT);
      if (visible)
      {
         DataMappingConnection connection = new DataMappingConnection(dataSymbol);
         connection.setSecondSymbol(activitySymbol, false);
         diagram.addToConnections(connection, 0);
      }
   }

   private void partOfConnection(ObjectOccurence source, Diagram diagram)
   {
      OrganizationSymbol organizationSymbol = (OrganizationSymbol) source.getSymbol();
      OrganizationSymbol subOrganizationSymbol = (OrganizationSymbol) target.getSymbol();
      IOrganization organization = organizationSymbol.getOrganization();
      IOrganization subOrganization = subOrganizationSymbol.getOrganization();
      organization.addToSubOrganizations(subOrganization);
      if (visible)
      {
         PartOfConnection connection = new PartOfConnection(subOrganizationSymbol);
         connection.setSecondSymbol(organizationSymbol, false);
         diagram.addToConnections(connection, 0);
      }
   }

   private void worksForConnection(ObjectOccurence source, Diagram diagram)
   {
      OrganizationSymbol organizationSymbol = (OrganizationSymbol) source.getSymbol();
      RoleSymbol roleSymbol = (RoleSymbol) target.getSymbol();
      IOrganization organization = organizationSymbol.getOrganization();
      IRole role = roleSymbol.getRole();
      organization.addToParticipants(role);
      if (visible)
      {
         WorksForConnection connection = new WorksForConnection(roleSymbol);
         connection.setSecondSymbol(organizationSymbol, false);
         diagram.addToConnections(connection, 0);
      }
   }

   private void managesConnection(ObjectOccurence source, Diagram diagram)
   {
      RoleSymbol roleSymbol = (RoleSymbol) source.getSymbol();
      OrganizationSymbol organizationSymbol = (OrganizationSymbol) target.getSymbol();
      IRole role = roleSymbol.getRole();
      IOrganization organization = organizationSymbol.getOrganization();
      organization.addToParticipants(role);
      if (visible)
      {
         WorksForConnection connection = new WorksForConnection(roleSymbol);
         connection.setSecondSymbol(organizationSymbol, false);
         diagram.addToConnections(connection, 0);
      }
   }

   private void executedByConnection(ObjectOccurence source, Diagram diagram)
   {
      ApplicationSymbol applicationSymbol = (ApplicationSymbol) source.getSymbol();
      ActivitySymbol activitySymbol = (ActivitySymbol) target.getSymbol();
      IApplication application = applicationSymbol.getApplication();
      IActivity activity = activitySymbol.getActivity();
      activity.setApplication(application);
      if (visible)
      {
         ExecutedByConnection connection = new ExecutedByConnection(applicationSymbol);
         connection.setSecondSymbol(activitySymbol, false);
         diagram.addToConnections(connection, 0);
      }
   }

   private void performsConnection(ObjectOccurence source, Diagram diagram)
   {
      NodeSymbol participantSymbol = source.getSymbol();
      ActivitySymbol activitySymbol = (ActivitySymbol) target.getSymbol();
      IModelParticipant role = source.getReference().getType() == ObjectDefinition.ROLE ?
            (IModelParticipant) ((RoleSymbol) participantSymbol).getRole() :
            (IModelParticipant) ((OrganizationSymbol) participantSymbol).getOrganization();
      IActivity activity = activitySymbol.getActivity();
      if (activity.getImplementationType().equals(ImplementationType.Route))
      {
         activity.setImplementationType(ImplementationType.Manual);
      }
      if (activity.getPerformer() != null)
      {
         IModelParticipant oldParticipant = activity.getPerformer();
         IOrganization pseudoPerformer = createPseudoPerformer(role, activity);
         OrganizationSymbol pseudoPerformerSymbol = (OrganizationSymbol) diagram.findSymbolForUserObject(pseudoPerformer);;
         if (pseudoPerformerSymbol == null)
         {
            NodeSymbol oldParticipantSymbol = (NodeSymbol) diagram.findSymbolForUserObject(oldParticipant);
            boolean visible = diagram.existConnectionBetween(oldParticipantSymbol, activitySymbol);
            activity.setPerformer(pseudoPerformer);
            pseudoPerformerSymbol = new OrganizationSymbol(pseudoPerformer);
            pseudoPerformerSymbol.setX((participantSymbol.getX() + activitySymbol.getX()) / 2);
            pseudoPerformerSymbol.setY((participantSymbol.getY() + activitySymbol.getY()) / 2);
            diagram.addToNodes(pseudoPerformerSymbol, 0);
            PerformsConnection connection = new PerformsConnection(pseudoPerformerSymbol);
            connection.setSecondSymbol(activitySymbol, false);
            diagram.addToConnections(connection, 0);
            if (visible)
            {
               connectToPseudoOrganization(oldParticipantSymbol, pseudoPerformerSymbol, diagram);
            }
         }
         if (visible)
         {
            connectToPseudoOrganization(participantSymbol, pseudoPerformerSymbol, diagram);
         }
      }
      else
      {
         activity.setPerformer(role);
         if (visible)
         {
            PerformsConnection connection = role instanceof IRole ?
                  new PerformsConnection((RoleSymbol) participantSymbol) :
                  new PerformsConnection((OrganizationSymbol) participantSymbol);
            connection.setSecondSymbol(activitySymbol, false);
            diagram.addToConnections(connection, 0);
         }
      }
   }

   private void connectToPseudoOrganization(NodeSymbol oldParticipantSymbol, OrganizationSymbol pseudoPerformerSymbol, Diagram diagram)
   {
      IModelParticipant oldParticipant = (IModelParticipant) oldParticipantSymbol.getUserObject();
      if (oldParticipant instanceof IOrganization)
      {
         PartOfConnection oldConnection = new PartOfConnection((OrganizationSymbol) oldParticipantSymbol);
         oldConnection.setSecondSymbol(pseudoPerformerSymbol, false);
         diagram.addToConnections(oldConnection, 0);
      }
      else
      {
         WorksForConnection oldConnection = new WorksForConnection((RoleSymbol) oldParticipantSymbol);
         oldConnection.setSecondSymbol(pseudoPerformerSymbol, false);
         diagram.addToConnections(oldConnection, 0);
      }
   }

   private IOrganization createPseudoPerformer(IModelParticipant newPerformer, IActivity activity)
   {
      IOrganization pseudoOrganization = null;
      IModelParticipant oldPerformer = activity.getPerformer();
      String oldPerformerId = oldPerformer.getId();
      if (oldPerformerId.startsWith("pseudo."))
      {
         pseudoOrganization = (IOrganization) oldPerformer;
      }
      else
      {
         String id = "pseudo." + activity.getId();
         String name = "Performers for " + activity.getName();
         String description = "Pseudo organization handling multiple performers of the activity " + activity.getName();
         IModel model = (IModel) activity.getModel();
         pseudoOrganization = model.createOrganization(id, name, description, 0);
         if (oldPerformer instanceof IOrganization)
         {
            pseudoOrganization.addToSubOrganizations((IOrganization) oldPerformer);
         }
         else
         {
            pseudoOrganization.addToParticipants(oldPerformer);
         }
      }
      if (newPerformer instanceof IOrganization)
      {
         pseudoOrganization.addToSubOrganizations((IOrganization) newPerformer);
      }
      else
      {
         pseudoOrganization.addToParticipants(newPerformer);
      }
      return pseudoOrganization;
   }

   public ObjectOccurence getSource()
   {
      return source;
   }

   public void setTarget(ObjectOccurence target)
   {
      this.target = target;
   }

   public void setConnectionTarget(ConnectionOccurence cxTarget)
   {
      target = null;
      this.cxTarget = cxTarget;
   }
}