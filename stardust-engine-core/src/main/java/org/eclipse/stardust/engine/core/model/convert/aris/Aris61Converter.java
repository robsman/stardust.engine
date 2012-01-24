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

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.core.model.convert.ConvertWarningException;
import org.eclipse.stardust.engine.core.model.convert.Converter;
import org.eclipse.stardust.engine.core.pojo.data.JavaDataTypeUtils;
import org.eclipse.stardust.engine.core.pojo.data.Type;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * @author kberberich
 * @version $Revision$
 */
public class Aris61Converter extends Converter
{
   private static final Logger trace = LogManager.getLogger(Aris61Converter.class);

   static final String ROLE_TYPENUM = "OT_POS";
   static final String ORGANIZATION_TYPENUM = "OT_ORG_UNIT";
   static final String DATA_TYPENUM = "OT_INFO_CARR";
   static final String APPLICATION_TYPENUM = "OT_APPL_SYS_TYPE";
   static final String ACTIVITY_TYPENUM = "OT_FUNC";
   static final String EVENT_TYPENUM = "OT_EVT";
   static final String RULE_TYPENUM = "OT_RULE";

   static final String GROUP_NODE_NAME = "Group";
   static final String MODEL_NODE_NAME = "Model";
   static final String OBJDEF_NODE_NAME = "ObjDef";
   static final String OBJOCC_NODE_NAME = "ObjOcc";
   static final String CXNDEF_NODE_NAME = "CxnDef";
   static final String CXNOCC_NODE_NAME = "CxnOcc";

   static final String ATTRIBUTE_NODE_NAME = "AttrDef";
   static final String ATTRIBUTE_NODE_TYPE = "AttrDef.Type";
   static final String ATTRIBUTE_VALUE_NODE = "AttrValue";
   static final String ATTRIBUTE_TOOBJDEV_NODE = "ToObjDef.IdRef";
   static final String ATTRIBUTE_TOOBJOCC_NODE = "ToObjOcc.IdRef";

   static final String TYPENUM_ATTRIBUTE = "TypeNum";
   static final String OBJDEF_IDREF_ATTRIBUTE = "ObjDef.IdRef";
   static final String NAME_ATTRIBUTE = "AT_NAME";
   static final String ID_ATTRIBUTE = "ID";
   static final String SYMBOLNUM_ATTRIBUTE = "SymbolNum";
   static final String AND_SPLIT_TYPE = "ST_OPR_AND_1";
   static final String XOR_SPLIT_TYPE = "ST_OPR_XOR_1";
   static final String OR_SPLIT_TYPE = "ST_OPR_OR_1";

   static final int RETRYS = 20;
   static final String MANUAL_TRIGGER_NAME = "Standard Manual Trigger Participant";

   private HashMap objectMap;
   private HashMap objDefMap;
   private HashMap objOccMap;
   private HashMap activityPerformerMap;
   private HashMap activityApplicationMap;
   private HashMap predecessors;
   private HashMap occPres;
   private int mode;
   private IRole standardManualTrigger;
   private Node startingRouteNode;

   public Aris61Converter(int mode)
   {
      super();
      this.mode = mode;
      objectMap = new HashMap();
      objDefMap = new HashMap();
      objOccMap = new HashMap();
      activityPerformerMap = new HashMap();
      activityApplicationMap = new HashMap();
      predecessors = new HashMap();
      occPres = new HashMap();
   }

   public IModel convert(InputStream inputStream)
   {
      Vector modelRoots = new Vector();
      Vector roleNodes = new Vector();
      Vector orgNodes = new Vector();
      Vector dataNodes = new Vector();
      Vector appNodes = new Vector();

      try
      {
         Document document = getDocumentFromInputStream(inputStream);
         model = createModel();

         // code for reading ARIS model

//         Node arisRoot = findArisRoot(document);
         Element root = document.getDocumentElement();
         if ("AML".equals(root.getNodeName()))
         {
            List groups = SimpleTaskUtil.getElementsByName(root, GROUP_NODE_NAME);
            for (int i = 0; i < groups.size(); i++)
            {
               processGroup((Element) groups.get(i),
                     modelRoots, roleNodes, orgNodes, dataNodes, appNodes);
            }
         }
         else
         {
            // todo: specific exception
            throw new RuntimeException("Unexpected XML Structure!");
         }

         createAllRoles(model, roleNodes);
         createAllOrganizations(model, orgNodes);
         createAllData(model, dataNodes);
         createAllApplications(model, appNodes);
         createAllProcesses(model, modelRoots);

         // end of code for reading ARIS model

         trace.debug("Generate default diagrams.");
         if (mode == PROTOTYPE_MODE)
         {
            adjustForExecutability();
         }

         populateDefaultDiagrams();
      }
      catch (Exception e)
      {
         String message = "Could not convert model from ARIS 6.1 to CARNOT.";
         trace.warn(message, e);
         throw new PublicException(message, e);
      }

      return model;
   }

   private void processGroup(Element group, Vector modelRoots, Vector roleNodes, Vector orgNodes, Vector dataNodes, Vector appNodes)
   {
      NodeList allObjDefs = group.getChildNodes();
      trace.debug("Parsing 'Group' Children");

      for (int pos = 0; pos < allObjDefs.getLength(); pos++)
      {
         Node node = allObjDefs.item(pos);
         if (node instanceof Element)
         {
            Element element = (Element) node;
            if (GROUP_NODE_NAME.equals(element.getTagName()))
            {
               trace.debug(GROUP_NODE_NAME + " found.");
               processGroup(element, modelRoots, roleNodes, orgNodes, dataNodes, appNodes);
            }
            else if (MODEL_NODE_NAME.equals(element.getTagName()))
            {
               trace.debug(MODEL_NODE_NAME + " found.");
               modelRoots.addElement(element);
            }
            else if (OBJDEF_NODE_NAME.equals(element.getTagName()))
            {
               NamedNodeMap attributes;
               String[] idAndName;

               idAndName = SimpleTaskUtil.getIdAndName(element);
               trace.debug(OBJDEF_NODE_NAME + " found with id " + idAndName[0]
                     + " and name " + idAndName[1] + ".");
               attributes = element.getAttributes();

               if ((attributes != null) && (ROLE_TYPENUM.equals(attributes.getNamedItem(
                     TYPENUM_ATTRIBUTE).getNodeValue())))
               {
                  trace.debug("ObjDef is a role.");
                  roleNodes.addElement(element);
               }
               else if ((attributes != null) && (ORGANIZATION_TYPENUM.equals(
                     attributes.getNamedItem(TYPENUM_ATTRIBUTE).getNodeValue())))
               {
                  trace.debug("ObjDef is an organization.");
                  orgNodes.addElement(element);
               }
               else if ((attributes != null) && (DATA_TYPENUM.equals(
                     attributes.getNamedItem(TYPENUM_ATTRIBUTE).getNodeValue())))
               {
                  trace.debug("ObjDef is a Data.");
                  dataNodes.addElement(element);
               }
               else if ((attributes != null) && (APPLICATION_TYPENUM.equals(
                     attributes.getNamedItem(TYPENUM_ATTRIBUTE).getNodeValue())))
               {
                  trace.debug("ObjDef is a Application.");
                  appNodes.addElement(element);
               }
               else if ((attributes != null) && (EVENT_TYPENUM.equals(
                     attributes.getNamedItem(TYPENUM_ATTRIBUTE).getNodeValue())))
               {
                  trace.debug("ObjDef is an Event");
                  parseSuccessorReferences(element);
               }
               else if ((attributes != null) && (RULE_TYPENUM.equals(
                     attributes.getNamedItem(TYPENUM_ATTRIBUTE).getNodeValue())))
               {
                  trace.debug("ObjDef is a Rule");
                  parseSuccessorReferences(element);
               }

               objDefMap.put(idAndName[0], element);
            }
         }
      }
   }

   private void parseSuccessorReferences(Node node)
   {
      Vector references;

      references = SimpleTaskUtil.getChildrenByName(node, CXNDEF_NODE_NAME);

      for (int i = 0; i < references.size(); i++)
      {
         Node refNode;
         String id;

         refNode = (Node) references.elementAt(i);
         id = SimpleTaskUtil.getAttributeValue(refNode, ATTRIBUTE_TOOBJDEV_NODE);

         if (id != null)
         {
            Vector predecessorsForId = (Vector) predecessors.get(id);
            if (predecessorsForId == null)
            {
               predecessorsForId = new Vector();
            }
            predecessorsForId.addElement(node);
            predecessors.put(id, predecessorsForId);
         }
      }
   }

   private void createAllProcesses(IModel model, Vector modelRoots)
   {
      trace.info("Creating " + modelRoots.size() + " processes.");

      for (int i = 0; i < modelRoots.size(); i++)
      {
         Element modelNode = (Element) modelRoots.elementAt(i);
         createProcess(modelNode, model);
      }
   }

   private IProcessDefinition createProcess(Element modelNode, IModel model)
   {
      IProcessDefinition processDefinition;

      startingRouteNode = null;
      String[] idAndName = SimpleTaskUtil.getIdAndName(modelNode);
      trace.debug("Creating process with id " + idAndName[0] + " and name "
            + idAndName[1] + ".");
      processDefinition = model.createProcessDefinition(
            idAndName[0], idAndName[1], "");
      List objOccs = SimpleTaskUtil.getElementsByName(modelNode, OBJOCC_NODE_NAME);
      trace.debug("Parsing ObjOcc (reference) entries for predecessors");
      parseObjOccForPres(objOccs);
      trace.debug("Parsing all ObjOcc (reference) entries in process.");

      for (int j = 0; j < objOccs.size(); j++)
      {
         Node objOcc = (Node) objOccs.get(j);
         trace.debug("Reference found.");
         Element objDef = SimpleTaskUtil.getObjDefForObjOcc(objOcc, objDefMap);

         if (objDef != null)
         {
            trace.debug("Definition found.");
            String typeNum = SimpleTaskUtil.getAttributeValue(objDef, TYPENUM_ATTRIBUTE);

            try
            {
               if (ACTIVITY_TYPENUM.equals(typeNum))
               {
                  createActivity(objOcc, objDef, processDefinition);
               }
               else if (EVENT_TYPENUM.equals(typeNum))
               {
                  handleEvent(objOcc, processDefinition);
               }
               else
               {
                  trace.debug("Unhandled TypeNum " + typeNum);
               }
            }
            catch (ConvertWarningException e)
            {
               converterWarnings.addLast(e);
            }
            catch (Exception e)
            {
               trace.warn("", e);
               ConvertWarningException warning = new ConvertWarningException(
                     e.getMessage());
               warning.setProcessDefinition(processDefinition);
               converterWarnings.addLast(warning);
            }
         }
         else
         {
            String objOccId = SimpleTaskUtil.getIdForNode(objOcc);
            trace.warn("Couldn't find ObjDef for ObjOcc with id " + objOccId
                  + " attribute!");
         }
      }

      return processDefinition;
   }

   private void parseObjOccForPres(List objOccs)
   {
      for (int i = 0; i < objOccs.size(); i++)
      {
         Element node = (Element) objOccs.get(i);
         String nodeId = node.getAttribute(node.getNodeName()
               + "." + ID_ATTRIBUTE);
         objOccMap.put(nodeId, node);
         List references = SimpleTaskUtil.getElementsByName(node, CXNOCC_NODE_NAME);

         for (int j = 0; j < references.size(); j++)
         {
            Element refNode = (Element) references.get(j);
            String id = refNode.getAttribute(ATTRIBUTE_TOOBJOCC_NODE);

            if (id != null)
            {
               Vector predecessorsForId;

               predecessorsForId = (Vector) occPres.get(id);
               if (predecessorsForId == null)
               {
                  predecessorsForId = new Vector();
               }
               predecessorsForId.addElement(node);
               occPres.put(id, predecessorsForId);
            }
         }
      }
   }

   /**
    * This methods makes a transition out of an event. It is ensured that this
    * template is never called for an event that does not have a predecessor.
    * The from object of the transition is an ARIS function that has this event
    * as successor. The to object is referenced by this object.
    *
    * @param processDefinition the process in which to generate the transitions
    */
   private void handleEvent(Node objOcc, IProcessDefinition processDefinition)
         throws ConvertWarningException
   {
      String objOccId;
      Vector eventPredecessors;
      Node reference;
      Node preOcc;
      Node succOcc;

      reference = SimpleTaskUtil.getChildByName(objOcc, CXNOCC_NODE_NAME);

      if (reference != null)
      {
         String refId = SimpleTaskUtil.getAttributeValue(reference, ATTRIBUTE_TOOBJOCC_NODE);
         succOcc = (Node) objOccMap.get(refId);
      }
      else
      {
         // generate logical route as end activity
         getActivityForNode(objOcc, processDefinition);
         succOcc = objOcc;
      }

      objOccId = SimpleTaskUtil.getIdForNode(objOcc);
      eventPredecessors = (Vector) occPres.get(objOccId);

      // If the event has no predecessor (staring event), a manual trigger
      // and a logical starting route are created. if a starting route already
      // exists, only the trigger will be created.
      if ((eventPredecessors == null) || (eventPredecessors.size() < 1))
      {
         ITrigger trigger;

         String id = "trigger" + ++triggerId;
         String name = id.toUpperCase();

         trigger = processDefinition.createTrigger(id, name, model.findTriggerType(PredefinedConstants.MANUAL_TRIGGER), 0);
         trigger.setAttribute(PredefinedConstants.MANUAL_TRIGGER_PARTICIPANT_ATT,
               standardManualTrigger.getId());

         if (startingRouteNode == null)
         {
            startingRouteNode = objOcc;
            getActivityForNode(objOcc, processDefinition);
         }
         else
         {
            Node existingStartRef;
            Node newStartRef;
            String existingStartRefId;
            String newStartRefId;

            existingStartRef = SimpleTaskUtil.getChildByName(startingRouteNode,
                  CXNOCC_NODE_NAME);
            existingStartRefId = SimpleTaskUtil.getAttributeValue(existingStartRef,
                  ATTRIBUTE_TOOBJOCC_NODE);
            newStartRef = SimpleTaskUtil.getChildByName(objOcc,
                  CXNOCC_NODE_NAME);
            newStartRefId = SimpleTaskUtil.getAttributeValue(newStartRef,
                  ATTRIBUTE_TOOBJOCC_NODE);

            if (!existingStartRefId.equals(newStartRefId))
            {
               //               warning = new ConvertWarningException("Multiple start activities!");
               //               warning.setProcessDefinition(processDefinition);
               //               converterWarnings.addLast(warning);
               startingRouteNode = objOcc;
               getActivityForNode(objOcc, processDefinition);
            }
         }

         preOcc = startingRouteNode;
      }
      else if (eventPredecessors.size() > 1)
      {
         // an event should never have more than one predecessor (either a function
         // or a rule)
         ConvertWarningException warning = new ConvertWarningException(
               "An event can only have one predecessor!");
         warning.setProcessDefinition(processDefinition);
         throw warning;
      }
      else
      {
         preOcc = (Node) eventPredecessors.elementAt(0);
      }

      generateTransitions(objOcc, preOcc, succOcc, processDefinition);
   }

   private void generateTransitions(Node evtOcc, Node preOcc, Node succOcc,
         IProcessDefinition processDefinition)
         throws ConvertWarningException
   {
      String preType;
      String succType;
      int rules;
      Node preDef;
      Node succDef;
      Node evtDef;
      String evtName;

      preDef = SimpleTaskUtil.getObjDefForObjOcc(preOcc, objDefMap);
      succDef = SimpleTaskUtil.getObjDefForObjOcc(succOcc, objDefMap);
      evtDef = SimpleTaskUtil.getObjDefForObjOcc(evtOcc, objDefMap);
      preType = SimpleTaskUtil.getAttributeValue(preDef, TYPENUM_ATTRIBUTE);
      succType = SimpleTaskUtil.getAttributeValue(succDef, TYPENUM_ATTRIBUTE);
      evtName = SimpleTaskUtil.getAttrDef(evtDef, NAME_ATTRIBUTE);

      // @todo handle situations, where a rule is followed by another rule

      if (evtName != null)
      {
         evtName = evtName.replace('\n', ' ');
      }

      if (ACTIVITY_TYPENUM.equals(preType))
      {
         rules = 0;
      }
      else if (EVENT_TYPENUM.equals(preType))
      {
         // this happens, when the predecessor is the starting event that has been
         // transfered to a route activity. This is handled as though a function was
         // predecessor.
         rules = 0;
      }
      else if (RULE_TYPENUM.equals(preType))
      {
         rules = 1;
      }
      else
      {
         throw new RuntimeException("Predecessor of an event can only be a function or " +
               "a rule!");
      }

      if (ACTIVITY_TYPENUM.equals(succType))
      {
         rules += 0;
      }
      else if (EVENT_TYPENUM.equals(succType))
      {
         // this happens, when the successor is the finishing event that has been
         // transfered to a route activity. This is handled as though a function was
         // successor.
         rules += 0;
      }
      else if (RULE_TYPENUM.equals(succType))
      {
         rules += 2;
      }
      else
      {
         throw new RuntimeException("Successor of an event can only be a function or " +
               "a rule!");
      }

      switch (rules)
      {
         case 0:
            // pre and succ are functions
            generateTransition(preOcc, succOcc, evtName, processDefinition);
            break;
         case 1:
            // pre is rule succ is funtion
            possibleJoin(preOcc, succOcc, evtName, processDefinition);
            break;
         case 2:
            // pre is function succ is rule
            possibleSplit(preOcc, succOcc, evtName, processDefinition);
            break;
         case 3:
            // pre is rule succ is rule
            possibleSplitAndJoin(evtOcc, preOcc, succOcc, processDefinition);
            break;
      }
   }

   private Vector possibleSplitAndJoin(Node evtOcc, Node preRule, Node postRule,
         IProcessDefinition processDefinition)
         throws ConvertWarningException
   {
      Vector transitions = new Vector();
      Vector preFunctions;
      Vector postFunctions;
      String preRuleId;
      Node objDef;
      String id;
      String name;

      id = SimpleTaskUtil.getIdForNode(evtOcc);
      objDef = SimpleTaskUtil.getObjDefForObjOcc(evtOcc, objDefMap);
      name = SimpleTaskUtil.getAttrDef(objDef, NAME_ATTRIBUTE);
      preRuleId = SimpleTaskUtil.getIdForNode(preRule);
      preFunctions = (Vector) occPres.get(preRuleId);
      Assert.isNotNull(preFunctions, "Rule before event must have at least one " +
            "referencing ObjOcc element!");
      postFunctions = SimpleTaskUtil.getChildrenByName(postRule, CXNOCC_NODE_NAME);
      Assert.isNotNull(postFunctions, "Rule after event must habe at least one " +
            "referencing ObjOcc element!");

      if ((preFunctions.size() == 1) && (postFunctions.size() == 1))
      {
         // simply make a transition from prefuntion to postfunction
         Node preFunctionNode;
         Node postFunctionNode;
         Node postFunctionRef;
         ITransition t;
         String refId;

         preFunctionNode = (Node) preFunctions.elementAt(0);
         postFunctionRef = (Node) postFunctions.elementAt(0);
         refId = SimpleTaskUtil.getAttributeValue(postFunctionRef, ATTRIBUTE_TOOBJOCC_NODE);
         postFunctionNode = (Node) objOccMap.get(refId);
         t = generateTransition(preFunctionNode, postFunctionNode, name, processDefinition);
         transitions.addElement(t);
      }
      else
      {
         // create a logical route, with a join or a split or both.
         IActivity logicalRoute;

         logicalRoute = getActivityForNode(evtOcc, processDefinition);
         //logicalRoute = processDefinition.createActivity(id, name, "");

         if (preFunctions.size() > 1)
         {
            JoinSplitType joinType = SimpleTaskUtil.getCarnotRuleType(preRule);
            logicalRoute.setJoinType(joinType);
         }

         if (postFunctions.size() > 1)
         {
            JoinSplitType splitType = SimpleTaskUtil.getCarnotRuleType(postRule);
            logicalRoute.setSplitType(splitType);
         }
         objectMap.put(id, logicalRoute);

         transitions.addAll(possibleJoin(preRule, evtOcc, name, processDefinition));
         transitions.addAll(possibleSplit(evtOcc, postRule, name, processDefinition));
      }

      return transitions;
   }

   private Vector possibleJoin(Node rule, Node to, String name,
         IProcessDefinition processDefinition)
         throws ConvertWarningException
   {
      Vector transitions = new Vector();
      JoinSplitType ruleType;
      IActivity toActivity;
      Vector preObjOccs;
      String id;

      id = SimpleTaskUtil.getIdForNode(rule);
      toActivity = getActivityForNode(to, processDefinition);
      preObjOccs = (Vector) occPres.get(id);
      ruleType = SimpleTaskUtil.getCarnotRuleType(rule);

      if (preObjOccs.size() > 1)
      {
         // this must be a join
         toActivity.setJoinType(ruleType);

         for (int i = 0; i < preObjOccs.size(); i++)
         {
            Node preNode = (Node) preObjOccs.elementAt(i);
            ITransition t = generateTransition(preNode, to, name, processDefinition);
            transitions.addElement(t);
         }
      }
      else if (preObjOccs.size() == 1)
      {
         // this is most likely a split
         Node preNode = (Node) preObjOccs.elementAt(0);
         IActivity preActivity = getActivityForNode(preNode, processDefinition);
         preActivity.setSplitType(ruleType);
         ITransition t = generateTransition(preNode, to, name, processDefinition);
         transitions.addElement(t);
      }
      else
      {
         throw new PublicException("A possible split rule must have at least one predecessor.");
      }

      return transitions;
   }

   private Vector possibleSplit(Node from, Node rule, String evtName,
         IProcessDefinition processDefinition)
         throws ConvertWarningException
   {
      Vector transitions = new Vector();
      Vector references;
      IActivity fromActivity;
      JoinSplitType ruleType;

      fromActivity = getActivityForNode(from, processDefinition);
      ruleType = SimpleTaskUtil.getCarnotRuleType(rule);
      references = SimpleTaskUtil.getChildrenByName(rule, CXNOCC_NODE_NAME);

      if (references.size() > 1)
      {
         // if this is the case, the rule must be a split
         fromActivity.setSplitType(ruleType);

         for (int i = 0; i < references.size(); i++)
         {
            Node referenceNode = (Node) references.elementAt(i);
            String refId = SimpleTaskUtil.getAttributeValue(referenceNode, ATTRIBUTE_TOOBJOCC_NODE);
            Node target = (Node) objOccMap.get(refId);
            ITransition t = generateTransition(from, target, evtName, processDefinition);
            transitions.addElement(t);
         }
      }
      else if (references.size() == 1)
      {
         // if this is the case, the rule must be a join
         Node referenceNode = (Node) references.elementAt(0);
         String refId = SimpleTaskUtil.getAttributeValue(referenceNode, ATTRIBUTE_TOOBJOCC_NODE);
         Element targetOcc = (Element) objOccMap.get(refId);
         Node targetDef = SimpleTaskUtil.getObjDefForObjOcc(targetOcc, objDefMap);
         String typeNum = SimpleTaskUtil.getAttributeValue(targetDef, TYPENUM_ATTRIBUTE);

         if (ACTIVITY_TYPENUM.equals(typeNum))
         {
            IActivity toActivity = getActivityForNode(targetOcc, processDefinition);
            toActivity.setJoinType(ruleType);
            ITransition t = generateTransition(from, targetOcc, evtName, processDefinition);
            transitions.addElement(t);
         }
         else if (RULE_TYPENUM.equals(typeNum))
         {
            ITransition t;
            IActivity logicalRoute;

            logicalRoute = getActivityForNode(rule, processDefinition);
            logicalRoute.setName("Logical Route");
            logicalRoute.setJoinType(ruleType);
            t = generateTransition(from, rule, evtName, processDefinition);
            transitions.addElement(t);
            generateTransitionsForNextRule(targetOcc, logicalRoute, processDefinition);
            /*
                        Node targetRef = SimpleTaskUtil.getChildByName(targetOcc, CXNOCC_NODE_NAME);
                        String targetRefId = SimpleTaskUtil.getAttributeValue(targetRef, ATTRIBUTE_TOOBJOCC_NODE);
                        Node targetsTarget = (Node) objOccMap.get(targetRefId);
                        Activity afterTargetAct = getActivityForNode(targetsTarget, processDefinition);
                        afterTargetAct.setJoinType(SimpleTaskUtil.getCarnotRuleType(targetOcc));
                        t = generateTransition(rule, targetsTarget, evtName, processDefinition);
                        transitions.addElement(t);
            */
         }
         else
         {
            ConvertWarningException warning = new ConvertWarningException("A rule" +
                  " folling an event can only be followed by either a function or " +
                  "a rule.");
            warning.setProcessDefinition(processDefinition);
            throw warning;
         }
      }
      else
      {
         trace.warn("A possible join rule must reference at least one other element!");
         throw new PublicException("A possible join rule must reference at least one other element!");
      }

      return transitions;
   }

   private void generateTransitionsForNextRule(Element nextRule, IActivity previousRule,
         IProcessDefinition processDefinition)
         throws ConvertWarningException
   {
      Vector predecessors;
      String[] idAndName;
      IActivity nextActivity;

      idAndName = SimpleTaskUtil.getIdAndName(nextRule);
      nextActivity = (IActivity) objectMap.get(idAndName[0]);

      // we have run through this procedure before and simply have to make the transition
      if (nextActivity != null)
      {
         String id = processDefinition.getDefaultTransitionId();
         processDefinition.createTransition(id, id, "", previousRule, nextActivity);
      }
      else
      {
         JoinSplitType joinType = SimpleTaskUtil.getCarnotRuleType(nextRule);
         predecessors = (Vector) occPres.get(idAndName[0]);

         if ((predecessors == null) || (predecessors.size() == 0))
         {
            throw new PublicException("This method must only be called if there is a " +
                  "successor in form of a rule!");
         }

         if (predecessors.size() == 1)
         {
            IActivity logicalRoute;
            JoinSplitType splitType;
            Vector references;
            // this means that the next rule is a split
            // in this case we have to generate a logical route as split point.
            logicalRoute = getActivityForNode(nextRule, processDefinition);
            logicalRoute.setName("Logical Route");
            splitType = SimpleTaskUtil.getCarnotRuleType(nextRule);
            logicalRoute.setSplitType(splitType);
            references = SimpleTaskUtil.getChildrenByName(nextRule, CXNOCC_NODE_NAME);

            for (int i = 0; i < references.size(); i++)
            {
               Node reference = (Node) references.elementAt(i);
               String refId = SimpleTaskUtil.getAttributeValue(reference,
                     ATTRIBUTE_TOOBJOCC_NODE);
               Element targetOcc = (Element) objOccMap.get(refId);
               Node targetDef = SimpleTaskUtil.getObjDefForObjOcc(targetOcc, objDefMap);
               String typeNum = SimpleTaskUtil.getAttributeValue(targetDef,
                     TYPENUM_ATTRIBUTE);

               if (ACTIVITY_TYPENUM.equals(typeNum))
               {
                  nextActivity = getActivityForNode(targetOcc, processDefinition);
                  nextActivity.setJoinType(joinType);
                  String transitionId = processDefinition.getDefaultTransitionId();
                  String transitionName = SimpleTaskUtil.getIdForNode(targetOcc);
                  processDefinition.createTransition(transitionId, transitionName, "",
                        previousRule, nextActivity);
               }
               else if (RULE_TYPENUM.equals(typeNum))
               {
                  // simply delegate the task recursively
                  generateTransitionsForNextRule(targetOcc, previousRule, processDefinition);
               }
            }
         }
         else
         {
            // this means that the next rule is a join.
            // the successor of of the next rule is one function or one rule
            // no additional route activity is needed in this case
            Node reference = SimpleTaskUtil.getChildByName(nextRule, CXNOCC_NODE_NAME);
            String refId = SimpleTaskUtil.getAttributeValue(reference,
                  ATTRIBUTE_TOOBJOCC_NODE);
            Element successorOcc = (Element) objOccMap.get(refId);
            Node successorDef = SimpleTaskUtil.getObjDefForObjOcc(successorOcc, objDefMap);
            String typeNum = SimpleTaskUtil.getAttributeValue(successorDef, TYPENUM_ATTRIBUTE);

            if (ACTIVITY_TYPENUM.equals(typeNum))
            {
               nextActivity = getActivityForNode(successorOcc, processDefinition);
               nextActivity.setJoinType(joinType);
               String id = processDefinition.getDefaultTransitionId();
               String name = SimpleTaskUtil.getIdForNode(successorOcc);
               processDefinition.createTransition(id, name, "", previousRule, nextActivity);
            }
            else if (RULE_TYPENUM.equals(typeNum))
            {
               // simply delegate the task recursively
               generateTransitionsForNextRule(successorOcc, previousRule, processDefinition);
            }
         }
      }
   }

   private ITransition generateTransition(Node fromOccNode, Node toOccNode, String name,
         IProcessDefinition processDefinition)
         throws ConvertWarningException
   {
      IActivity fromAct;
      IActivity toAct;
      ITransition retVal;

      fromAct = getActivityForNode(fromOccNode, processDefinition);
      toAct = getActivityForNode(toOccNode, processDefinition);
      retVal = processDefinition.createTransition(processDefinition.getDefaultTransitionId(), name,
            "", fromAct, toAct);
      return retVal;
   }

   private IActivity getActivityForNode(Node fromOccNode, IProcessDefinition processDefinition)
         throws ConvertWarningException
   {
      String id;
      Object o;
      IActivity activity;

      id = SimpleTaskUtil.getAttributeValue(fromOccNode, fromOccNode.getNodeName()
            + "." + ID_ATTRIBUTE);
      o = objectMap.get(id);

      if (o == null)
      {
         activity = createActivity(fromOccNode, processDefinition);
         objectMap.put(id, activity);
      }
      else
      {
         Assert.condition(o instanceof IActivity, "Activity expected from objectMap, " +
               o.getClass().getName() + " retrieved for id " + id);
         activity = (IActivity) o;
      }

      return activity;
   }

   private IActivity createActivity(Node objOcc, IProcessDefinition processDefinition)
         throws ConvertWarningException
   {
      Element objDef = SimpleTaskUtil.getObjDefForObjOcc(objOcc, objDefMap);
      return createActivity(objOcc, objDef, processDefinition);
   }

   private IActivity createActivity(Node objOcc, Element objDef, IProcessDefinition processDefinition)
         throws ConvertWarningException
   {
      IActivity activity = null;
      String[] idAndName;
      String objOccId;
      IModelParticipant performer;
      IApplication application;
      ImplementationType implType;

      try
      {
         idAndName = SimpleTaskUtil.getIdAndName(objDef);
         objOccId = SimpleTaskUtil.getAttributeValue(objOcc, objOcc.getNodeName() + "."
               + ID_ATTRIBUTE);
         activity = (IActivity) objectMap.get(objOccId);

         if (activity != null)
         {
            trace.info("Activity with id " + objOccId + " already created.");
            return activity;
         }

         trace.info("Creating activity with id " + objOccId + " and name "
               + idAndName[1] + ".");
         performer = (IModelParticipant) activityPerformerMap.get(idAndName[0]);
         application = (IApplication) activityApplicationMap.get(idAndName[0]);
         activity = processDefinition.createActivity(objOccId, idAndName[1], "", 0);
         implType = ImplementationType.Route;

         if (performer != null)
         {
            trace.info("Setting activity's performer to " + performer.getName()
                  + " (" + performer.getId() + ").");
            activity.setPerformer(performer);
            implType = ImplementationType.Manual;
         }

         if (application != null)
         {
            trace.info("Settion activity's application to " + application.getName()
                  + " (" + application.getId() + ").");
            activity.setApplication(application);
            implType = ImplementationType.Application;
         }

         activity.setImplementationType(implType);
         objectMap.put(objOccId, activity);
      }
      catch (Exception e)
      {
         trace.warn("Exception while trying to create activity.", e);
         throw new ConvertWarningException(e.getMessage());
      }

      return activity;
   }

   private void createAllApplications(IModel model, Vector appNodes)
   {
      trace.info("Creating " + appNodes.size() + " application objects.");
      for (int i = 0; i < appNodes.size(); i++)
      {
         Element node = (Element) appNodes.elementAt(i);
         IApplication app = createApplication(node, model);
         objectMap.put(app.getId(), app);
      }
   }

   private IApplication createApplication(Element node, IModel model)
   {
      String[] idAndName = SimpleTaskUtil.getIdAndName(node);
      trace.info("Creating application with id " + idAndName[0] + " and name "
            + idAndName[1] + ".");
      IApplication app = model.createApplication(idAndName[0], idAndName[1], "", 0);
      scanActivityRelations(node, app, activityApplicationMap);
      return app;
   }

   private void createAllData(IModel model, Vector dataNodes)
   {
      trace.info("Creating " + dataNodes.size() + " data objects.");
      for (int nodePos = 0; nodePos < dataNodes.size(); nodePos++)
      {
         Element node = (Element) dataNodes.elementAt(nodePos);
         IData data = createData(node, model);
         objectMap.put(data.getId(), data);
      }
   }

   private IData createData(Element node, IModel model)
   {
      String[] idAndName = SimpleTaskUtil.getIdAndName(node);
      trace.info("Creating data with id " + idAndName[0]
            + " and name " + idAndName[1] + ".");
      IData data = model.createData(idAndName[0],
            model.findDataType(PredefinedConstants.PRIMITIVE_DATA),
            idAndName[1], "", false, 0,
            JavaDataTypeUtils.initPrimitiveAttributes(Type.String, ""));
      return data;
   }

   private void createAllOrganizations(IModel model, Vector orgNodes)
   {
      trace.info("Creating " + orgNodes.size() + " organization objects.");
      for (int i = 0; i < orgNodes.size(); i++)
      {
         IOrganization org = createOrganization((Element) orgNodes.elementAt(i), model);
         objectMap.put(org.getId(), org);
      }
   }

   private IOrganization createOrganization(Element node, IModel model)
   {
      String[] idAndName = SimpleTaskUtil.getIdAndName(node);
      trace.info("Creating organization with id " + idAndName[0]
            + " and name " + idAndName[1] + ".");
      IOrganization org = model.createOrganization(idAndName[0], idAndName[1], "", 0);
      scanActivityRelations(node, org, activityPerformerMap);
      return org;
   }

   private void createAllRoles(IModel model, Vector roleNodes)
   {
      trace.info("Creating standard Manual Trigger participant");
      standardManualTrigger = model.createRole(model.getDefaultRoleId(),
            MANUAL_TRIGGER_NAME, "", 0);
      trace.info("Creating " + roleNodes.size() + " role objects.");
      for (int i = 0; i < roleNodes.size(); i++)
      {
         Element node = (Element) roleNodes.elementAt(i);
         IRole role = createRole(node, model);
         objectMap.put(role.getId(), role);
      }
   }

   private IRole createRole(Element node, IModel model)
   {
      String[] idAndName = SimpleTaskUtil.getIdAndName(node);
      trace.info("Creating Role with id " + idAndName[0]
            + " and name " + idAndName[1] + ".");
      IRole role = model.createRole(idAndName[0], idAndName[1], "", 0);
      scanActivityRelations(node, role, activityPerformerMap);
      return role;
   }

   private void scanActivityRelations(Node node, Object modelElement,
         Map activityIdModelElementMap)
   {
      Vector references;
      trace.debug("scanning activity relations");
      references = SimpleTaskUtil.getChildrenByName(node, CXNDEF_NODE_NAME);

      for (int i = 0; i < references.size(); i++)
      {
         Node refNode;
         String refId = null;

         refNode = (Node) references.elementAt(i);
         refId = SimpleTaskUtil.getAttributeValue(refNode, ATTRIBUTE_TOOBJDEV_NODE);

         if (refId != null)
         {
            activityIdModelElementMap.put(refId, modelElement);
         }
      }
   }
}
