/**********************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.casepi;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.eclipse.stardust.test.util.TestConstants.MOTU;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.InvalidValueException;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetailsLevel;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetailsOptions;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.engine.core.runtime.beans.AbortScope;
import org.eclipse.stardust.engine.core.runtime.utils.ExecutionPermission;
import org.eclipse.stardust.engine.core.runtime.utils.Permissions;
import org.eclipse.stardust.test.api.junit.LocalJcrH2Test;
import org.eclipse.stardust.test.api.setup.ClientServiceFactory;
import org.eclipse.stardust.test.api.setup.RuntimeConfigurer;
import org.eclipse.stardust.test.api.util.ActivityInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.DepartmentHome;
import org.eclipse.stardust.test.api.util.ProcessInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.UserHome;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

/**
 * <p>
 * TODO javadoc
 * </p>
 * 
 * @author Roland.Stamm
 * @version $Revision$
 */
public class CaseProcessInstanceTest extends LocalJcrH2Test
{
   private static final String U1 = "u1";
   private static final String U2 = "u2";

   private static final String D1 = "d1";

   private static final String MODEL_NAME = "CaseModel";
   
   private final ClientServiceFactory sf = new ClientServiceFactory(MOTU, MOTU);
   private final RuntimeConfigurer rtConfigurer = new RuntimeConfigurer(sf, MODEL_NAME);
   
   @Rule
   public TestRule chain = RuleChain.outerRule(sf)
                                    .around(rtConfigurer);
   
   private WorkflowService wfService;
   
   @Before
   public void setUp()
   {
      wfService = sf.getWorkflowService();

      UserHome.create(sf, U1, "Org1");
      
      final Organization scopedOrg1 = getTestModel().getOrganization("ScopedOrg1");
      final Department dept = DepartmentHome.create(D1, "ScopedOrg1", null, sf);
      UserHome.create(sf, U2, dept.getScopedParticipant(scopedOrg1));
   }

   /**
    * Creating the case and adding a second process instance.
    */
   @Test
   public void testCreate()
   {
      ProcessInstance caseProcess1 = wfService.startProcess("{CaseModel}CaseProcess1", null,
            true);
      ProcessInstance caseProcess2 = wfService.startProcess("{CaseModel}CaseProcess2", null,
            true);

      long[] members = {caseProcess1.getOID()};
      ProcessInstance rootCaseProcess1 = wfService.createCase("Case1", null, members);
      assertNotNull(rootCaseProcess1);
      assertHierarchy(rootCaseProcess1, caseProcess1, true);

      long[] members2 = {caseProcess2.getOID()};
      ProcessInstance rootCaseProcess2 = wfService.joinCase(rootCaseProcess1.getOID(),
            members2);
      assertHierarchy(rootCaseProcess2, caseProcess2, true);

      assertSameProcessInstance(rootCaseProcess1, rootCaseProcess2);
      ProcessInstance updatedCaseProcess1 = getPiWithHierarchy(caseProcess1.getOID(),
            sf.getQueryService());
      ProcessInstance updatedCaseProcess2 = getPiWithHierarchy(caseProcess2.getOID(),
            sf.getQueryService());

      assertEquals(rootCaseProcess1.getOID(),
            updatedCaseProcess1.getParentProcessInstanceOid());
      assertEquals(rootCaseProcess1.getOID(),
            updatedCaseProcess2.getParentProcessInstanceOid());
   }

   /**
    * Removing CaseProcess1 and CaseProcess2 from the case in single steps. Case is
    * terminated because all members left.
    * @throws InterruptedException
    */
   @Test
   public void testRemove() throws Exception
   {
      ProcessInstance caseProcess1 = wfService.startProcess("{CaseModel}CaseProcess1", null,
            true);
      ProcessInstance caseProcess2 = wfService.startProcess("{CaseModel}CaseProcess2", null,
            true);

      long[] members = {caseProcess1.getOID(), caseProcess2.getOID()};
      ProcessInstance casePi = wfService.createCase("Case1", null, members);
      assertNotNull(casePi);
      assertHierarchy(casePi, caseProcess1, true);
      assertHierarchy(casePi, caseProcess2, true);

      long[] member1 = {caseProcess1.getOID()};
      ProcessInstance rootCaseProcess1 = wfService.leaveCase(casePi.getOID(), member1);
      assertHierarchy(rootCaseProcess1, caseProcess1, false);
      assertHierarchy(rootCaseProcess1, caseProcess2, true);

      long[] member2 = {caseProcess2.getOID()};
      ProcessInstance rootCaseProcess2 = wfService.leaveCase(casePi.getOID(), member2);
      assertHierarchy(rootCaseProcess2, caseProcess2, false);
      assertHierarchy(rootCaseProcess1, caseProcess1, false);

      assertSameProcessInstance(casePi, rootCaseProcess1);
      assertSameProcessInstance(rootCaseProcess1, rootCaseProcess2);

      ProcessInstanceStateBarrier.instance().await(casePi.getOID(), ProcessInstanceState.Aborted);
      
      ProcessInstance processInstance = wfService.getProcessInstance(casePi.getOID());
      assertEquals(ProcessInstanceState.Aborted, processInstance.getState());
   }

   /**
    * Case is terminated if all members leave or all processes are completed.
    *
    * @throws InterruptedException
    */
   @Test
   public void testTermination() throws Exception
   {
      ProcessInstance caseProcess1 = wfService.startProcess("{CaseModel}CaseProcess1", null,
            true);
      ProcessInstance caseProcess2 = wfService.startProcess("{CaseModel}CaseProcess2", null,
            true);

      long[] members = {caseProcess1.getOID(), caseProcess2.getOID()};
      ProcessInstance casePi = wfService.createCase("Case1", null, members);
      assertNotNull(casePi);
      assertHierarchy(casePi, caseProcess1, true);
      assertHierarchy(casePi, caseProcess2, true);

      wfService.abortProcessInstance(caseProcess1.getOID(), AbortScope.SubHierarchy);

      ActivityInstance ai = wfService.activateNextActivityInstanceForProcessInstance(caseProcess2.getOID());
      wfService.complete(ai.getOID(), null, null);

      ProcessInstanceStateBarrier.instance().await(casePi.getOID(), ProcessInstanceState.Completed);

      ProcessInstance processInstance = wfService.getProcessInstance(casePi.getOID());
      assertEquals(ProcessInstanceState.Completed, processInstance.getState());
   }

   /**
    * Querying for case.
    */
   @Test
   public void testQuery()
   {
      ProcessInstance caseProcess1 = wfService.startProcess("{CaseModel}CaseProcess1", null,
            true);
      long[] members = {caseProcess1.getOID()};
      ProcessInstance casePi = wfService.createCase("CaseForQuery1", null, members);
      assertNotNull(casePi);

      ProcessInstanceQuery query = ProcessInstanceQuery.findCaseByName("CaseForQuery1");

      ProcessInstances allProcessInstances = sf.getQueryService().getAllProcessInstances(
            query);

      assertEquals(1, allProcessInstances.size());
      assertEquals(casePi.getOID(), allProcessInstances.get(0).getOID());
   }

   /**
    * Querying for all groups.
    */
   @Test
   public void testQueryForAllGroups()
   {
      createCases(4);
      ProcessInstanceQuery query = ProcessInstanceQuery.findCases();

      ProcessInstances allProcessInstances = sf.getQueryService().getAllProcessInstances(
            query);

      assertEquals(4, allProcessInstances.size());
   }

   /**
    * Querying for all process instances.
    */
   @Test
   public void testQueryForAllPIs()
   {
      createCases(4);
      ProcessInstanceQuery query = ProcessInstanceQuery.findAll();

      ProcessInstances allProcessInstances = sf.getQueryService().getAllProcessInstances(
            query);

      int countGroups = 0;
      for (ProcessInstance processInstance : allProcessInstances)
      {
         if (PredefinedConstants.CASE_PROCESS_ID.equals(processInstance.getProcessID()))
         {
            countGroups++ ;
         }
      }
      assertEquals(4, countGroups);
   }

   /**
    * Querying for case members.
    */
   @Test
   public void testQueryMembers()
   {
      ProcessInstance caseProcess1 = wfService.startProcess("{CaseModel}CaseProcess1", null,
            true);
      long[] members = {caseProcess1.getOID()};
      ProcessInstance casePi = wfService.createCase("CaseForQuery1", null, members);
      assertNotNull(casePi);

      ProcessInstanceQuery query = ProcessInstanceQuery.findCaseMembers(casePi.getOID());

      ProcessInstances allProcessInstances = sf.getQueryService().getAllProcessInstances(
            query);

      assertEquals(1, allProcessInstances.size());
      assertEquals(caseProcess1.getOID(), allProcessInstances.get(0).getOID());
   }

   /**
    * Querying for case members.
    */
   @Test
   public void testQueryHierarchy()
   {
      QueryService queryService = sf.getQueryService();

      wfService.startProcess("{CaseModel}CaseProcess1", null, true);
      createCases(4);
      
      ProcessInstanceQuery query = ProcessInstanceQuery.findAll();
      ProcessInstances allProcessInstances = queryService.getAllProcessInstances(
            query);

      int all = allProcessInstances.size();

      ProcessInstanceQuery queryRoot = ProcessInstanceQuery.findAll();
      queryRoot.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstances rootProcessInstances = queryService
            .getAllProcessInstances(queryRoot);

      ProcessInstanceQuery querySub = ProcessInstanceQuery.findAll();
      querySub.where(ProcessInstanceHierarchyFilter.ROOT_PROCESS);
      ProcessInstances subProcessInstances = queryService.getAllProcessInstances(
            querySub);
      int root = rootProcessInstances.size();
      int sub = subProcessInstances.size();

      Assert.assertTrue(all > 0);
      Assert.assertTrue(sub > 0);
      Assert.assertTrue(root > 0);
      Assert.assertTrue(sub > root);
      assertEquals(all, root + sub);
   }

   /**
    * Querying for case members.
    */
   @Test
   public void testQueryWithoutCases()
   {
      QueryService queryService = sf.getQueryService();

      createCases(4);
      
      ProcessInstanceQuery query = ProcessInstanceQuery.findAll();
      ProcessInstances allPis = queryService.getAllProcessInstances(query);
      int all = allPis.size();

      ProcessDefinition processDefinition = queryService.getProcessDefinition(
            PredefinedConstants.ANY_MODEL,
            "{" + PredefinedConstants.PREDEFINED_MODEL_ID + "}"
                  + PredefinedConstants.CASE_PROCESS_ID);

      long caseDefOid = processDefinition.getRuntimeElementOID();

      ProcessInstanceQuery queryRoot = ProcessInstanceQuery.findAll();
      queryRoot.where(ProcessInstanceQuery.PROCESS_DEFINITION_OID.isEqual(caseDefOid));
      ProcessInstances noCaseInstances = queryService.getAllProcessInstances(
            queryRoot);

      Assert.assertTrue(all>noCaseInstances.size());
   }

   @Test
   public void testProcessInstanceJoin() throws Exception
   {
      ProcessInstance caseProcess1 = wfService.startProcess("{CaseModel}CaseProcess1", null,
            true);
      ProcessInstance caseProcess2 = wfService.startProcess("{CaseModel}CaseProcess2", null,
            true);

      long[] members = {caseProcess1.getOID(), caseProcess2.getOID()};
      ProcessInstance casePi = wfService.createCase("Case1", null, members);


      ProcessInstance caseProcess3 = wfService.startProcess("{CaseModel}CaseProcess1", null,
            true);
      ProcessInstance caseProcess4 = wfService.startProcess("{CaseModel}CaseProcess2", null,
            true);

      long[] members2 = {caseProcess3.getOID(), caseProcess4.getOID()};
      wfService.createCase("Case2", null, members2);


      ProcessInstance joinProcessInstance = wfService.joinProcessInstance(caseProcess1.getOID(), caseProcess4.getOID(), "joined by test case");
      assertEquals(joinProcessInstance.getOID(), caseProcess4.getOID());
      
      ProcessInstanceStateBarrier.instance().await(caseProcess1.getOID(), ProcessInstanceState.Aborted);
      
      assertEquals(ProcessInstanceState.Aborted, getPiWithHierarchy(caseProcess1.getOID(), sf.getQueryService()).getState());
      assertEquals(ProcessInstanceState.Active, getPiWithHierarchy(casePi.getOID(), sf.getQueryService()).getState());

      wfService.abortProcessInstance(caseProcess2.getOID(), AbortScope.SubHierarchy);
   }

   /**
    * Updating of the key descriptor "FirmId".
    */
   @Test
   public void testUpdateDescriptors()
   {
      Map<String, Object > data = CollectionUtils.newHashMap();
      data.put("FirmId", 126l);

      ProcessInstance caseProcess1 = wfService.startProcess("{CaseModel}CaseProcess1", data,
            true);
      ProcessInstance caseProcess2 = wfService.startProcess("{CaseModel}CaseProcess2", data,
            true);

      long[] members = {caseProcess1.getOID(), caseProcess2.getOID()};
      ProcessInstance casePi = wfService.createCase("Case1", "Description", members);
      assertNotNull(casePi);
      assertHierarchy(casePi, caseProcess1, true);
      assertHierarchy(casePi, caseProcess2, true);

      // set descriptors
      wfService.setOutDataPath(casePi.getOID(), "FirmId", Long.valueOf(126));
      wfService.setOutDataPath(casePi.getOID(), "FirmName", "Firm1");

      ActivityInstanceQuery query = ActivityInstanceQuery.findAll();
      query.where(new ProcessInstanceFilter(casePi.getOID(), false));
      ActivityInstances ais = sf.getQueryService().getAllActivityInstances(query);
      assertNotNull(ais);
      assertEquals(1, ais.size());
      ActivityInstance ai = ais.get(0);

      assertEquals(Long.valueOf(126), (Long) ai.getDescriptorValue("FirmId"));
      assertEquals(Long.valueOf(126), (Long) wfService.getInDataPath(casePi.getOID(), "FirmId"));

      assertEquals("Firm1", (String) ai.getDescriptorValue("FirmName"));
      assertEquals("Firm1", (String) wfService.getInDataPath(casePi.getOID(), "FirmName"));

      assertEquals("Case1",
            ai.getDescriptorValue(PredefinedConstants.CASE_NAME_ELEMENT));
      assertEquals("Case1", wfService.getInDataPath(casePi.getOID(),
            PredefinedConstants.CASE_NAME_ELEMENT));

      List<DataPath> descriptorDefinitions = ai.getDescriptorDefinitions();
      assertNotNull(descriptorDefinitions);
      assertEquals(4, descriptorDefinitions.size());

      assertEquals(PredefinedConstants.CASE_NAME_ELEMENT,
            descriptorDefinitions.get(0).getId());
      assertEquals(String.class, descriptorDefinitions.get(0).getMappedType());

      assertEquals(PredefinedConstants.CASE_DESCRIPTION_ELEMENT,
            descriptorDefinitions.get(1).getId());
      assertEquals(String.class, descriptorDefinitions.get(1).getMappedType());

      assertEquals("FirmName", descriptorDefinitions.get(2).getId());
      assertEquals(String.class, descriptorDefinitions.get(2).getMappedType());

      assertEquals("FirmId", descriptorDefinitions.get(3).getId());
      assertEquals(Long.class, descriptorDefinitions.get(3).getMappedType());
   }

   @Test
   public void testQueryDescriptorsOnAI()
   {      
      QueryService queryService = sf.getQueryService();
      
      createCases(4);
      ProcessInstance caseProcess = wfService.startProcess("{CaseModel}CaseProcess2", null, true);
      long[] members = {caseProcess.getOID()};
      ProcessInstance casePi = wfService.createCase("Case1", "Description", members);
      assertNotNull(casePi);
      wfService.setOutDataPath(casePi.getOID(), "FirmId", Long.valueOf(126));
      
      ActivityInstanceQuery query = ActivityInstanceQuery.findAll();

      String dataId = "FirmId";
      query.getFilter()
            .addOrTerm()
            .add(DataFilter.between(PredefinedConstants.QUALIFIED_CASE_DATA_ID,
                  PredefinedConstants.CASE_DESCRIPTOR_VALUE_XPATH, "{" + dataId + "}125",
                  "{" + dataId + "}127"))
            .add(DataFilter.between(dataId, Long.valueOf(125), Long.valueOf(127)));

      ActivityInstances ais = queryService.getAllActivityInstances(query);

      assertEquals(1, ais.size());
   }

   /**
    * Updating of the key descriptor "FirmId".
    */
   @Test(expected = InvalidValueException.class)
   public void testInvalidDescriptorValue()
   {
      ProcessInstance caseProcess1 = wfService.startProcess("{CaseModel}CaseProcess1", null,
            true);
      ProcessInstance caseProcess2 = wfService.startProcess("{CaseModel}CaseProcess2", null,
            true);

      long[] members = {caseProcess1.getOID(), caseProcess2.getOID()};
      ProcessInstance casePi = wfService.createCase("Case1", null, members);
      assertNotNull(casePi);
      assertHierarchy(casePi, caseProcess1, true);
      assertHierarchy(casePi, caseProcess2, true);

      // set descriptors
      wfService.setOutDataPath(casePi.getOID(), "InvalidValue", new Preferences(null, null, null, null));
      Assert.fail();
   }

   @Test
   public void testSpawnedProcessInstanceLeave() throws Exception
   {
      ProcessInstance caseProcess1 = wfService.startProcess("{CaseModel}CaseProcess1", null, true);

      long[] members = {caseProcess1.getOID()};
      ProcessInstance casePi = wfService.createCase("Case1", null, members);

      ProcessInstance spawnedPi = wfService.spawnSubprocessInstance(caseProcess1.getOID(), "{CaseModel}CaseProcess2", true, null);
      /* make sure that spawning is completed before moving on */
      ActivityInstanceStateBarrier.instance().awaitAliveActivityInstance(spawnedPi.getOID());
      
      wfService.leaveCase(casePi.getOID(), new long[]{caseProcess1.getOID()});

      ProcessInstanceStateBarrier.instance().await(casePi.getOID(), ProcessInstanceState.Aborted);
      ProcessInstance processInstance = wfService.getProcessInstance(casePi.getOID());
      assertEquals(ProcessInstanceState.Aborted, processInstance.getState());
   }

   @Test
   public void testDataFilter()
   {
      Map<String, ? > data1 = Collections.singletonMap("Department", "West");
      ProcessInstance caseProcess1 = wfService.startProcess("{CaseModel}CaseProcess1", data1,
            true);
      ProcessInstance caseProcess2 = wfService.spawnSubprocessInstance(caseProcess1.getOID(), "{CaseModel}CaseProcess2",true, data1);
      assertHierarchy(caseProcess1, caseProcess2, true);

      ProcessInstanceQuery queryRoot0 = ProcessInstanceQuery.findAll();
      queryRoot0.where(HierarchyDataFilter.isEqual("Department", "West"));
      ProcessInstances pis0 = sf.getQueryService().getAllProcessInstances(
            queryRoot0);
      assertEquals(2, pis0.size());

      long[] members = {caseProcess1.getOID()};
      ProcessInstance casePi = wfService.createCase("Case5", null, members);
      assertNotNull(casePi);
      assertHierarchy(casePi, caseProcess1, true);

      ProcessInstanceQuery queryRoot1 = ProcessInstanceQuery.findAll();
      queryRoot1.where(DataFilter.isEqual("Department", "West"));
      ProcessInstances pis1 = sf.getQueryService().getAllProcessInstances(
            queryRoot1);
      assertEquals(2, pis1.size());

      ProcessInstanceQuery queryRoot2 = ProcessInstanceQuery.findAll();
      queryRoot2.where(SubProcessDataFilter.isEqual("Department", "West"));
      ProcessInstances pis2 = sf.getQueryService().getAllProcessInstances(
            queryRoot2);
      assertEquals(2, pis2.size());

      ProcessInstanceQuery queryRoot3 = ProcessInstanceQuery.findAll();
      queryRoot3.where(HierarchyDataFilter.isEqual("Department", "West"));
      ProcessInstances pis3 = sf.getQueryService().getAllProcessInstances(
            queryRoot3);
      assertEquals(3, pis3.size());
   }

   @Test
   public void testCaseInclusionQuery() throws Exception
   {
      QueryService queryService = sf.getQueryService();

      ProcessInstance caseProcess = wfService.startProcess("{CaseModel}CaseProcess1", null, true);
      long[] members = {caseProcess.getOID()};
      ProcessInstance rootCaseProcess = wfService.createCase("Case1", null, members);
      assertNotNull(rootCaseProcess);
      wfService.abortProcessInstance(caseProcess.getOID(), AbortScope.SubHierarchy);
      
      ProcessInstanceStateBarrier.instance().await(caseProcess.getOID(), ProcessInstanceState.Aborted);
      
      ProcessInstanceQuery queryRoot0 = ProcessInstanceQuery.findInState(ProcessInstanceState.Aborted);
      queryRoot0.setPolicy(CasePolicy.INCLUDE_CASES);
      ProcessInstances pis0 = queryService.getAllProcessInstances(
            queryRoot0);
      assertEquals(2, pis0.size());
      assertEquals(2, pis0.getTotalCount());

      ProcessInstanceQuery queryRoot1 = ProcessInstanceQuery.findInState(ProcessInstanceState.Aborted);
      ProcessInstances pis1 = queryService.getAllProcessInstances(
            queryRoot1);
      assertEquals(1, pis1.size());
      assertEquals(1, pis1.getTotalCount());
   }

   @Test
   public void testCaseInclusionQueryCount() throws Exception
   {
      QueryService queryService = sf.getQueryService();

      ProcessInstance caseProcess = wfService.startProcess("{CaseModel}CaseProcess1", null, true);
      long[] members = {caseProcess.getOID()};
      ProcessInstance rootCaseProcess = wfService.createCase("Case1", null, members);
      assertNotNull(rootCaseProcess);
      wfService.abortProcessInstance(caseProcess.getOID(), AbortScope.SubHierarchy);
      ProcessInstanceStateBarrier.instance().await(caseProcess.getOID(), ProcessInstanceState.Aborted);
      
      ProcessInstanceQuery queryRoot0 = ProcessInstanceQuery.findInState(ProcessInstanceState.Aborted);
      queryRoot0.setPolicy(CasePolicy.INCLUDE_CASES);
      long pis0 = queryService.getProcessInstancesCount(
            queryRoot0);
      assertEquals(2, pis0);

      ProcessInstanceQuery queryRoot1 = ProcessInstanceQuery.findInState(ProcessInstanceState.Aborted);
      long pis1 = queryService.getProcessInstancesCount(queryRoot1);
      assertEquals(1, pis1);
   }

   /**
    * Delegation of a case to a non scoped organization.
    */
   @Test
   public void testDelegate()
   {
      ProcessInstance caseProcess1 = wfService.startProcess("{CaseModel}CaseProcess1", null,
            true);
      long[] members = {caseProcess1.getOID()};
      ProcessInstance casePi = wfService.createCase("Case1", null, members);
      assertNotNull(casePi);

      // DELEAGATE to Org1
      ParticipantInfo participantOrg1 = getTestModel().getParticipant("{CaseModel}Org1");
      ProcessInstance delegatedPi = wfService.delegateCase(casePi.getOID(), participantOrg1);
      assertNotNull(delegatedPi);

      ActivityInstance caseAi = null;
      ActivityInstances ais = sf.getQueryService().getAllActivityInstances(ActivityInstanceQuery.findForProcessInstance(delegatedPi.getOID()));
      for (ActivityInstance ai : ais)
      {
       if(ai.getModelElementID().equals("DefaultCaseActivity"))
       {
          caseAi = ai;
       }
      }
      assertNotNull(caseAi);
      assertEquals("For case, it should be default case activity","Default Case Activity",caseAi.getActivity().getName());
      assertEquals("Case activity should be delegated to org1 from Admin","Org1", caseAi.getParticipantPerformerID());
      assertEquals("Participant is changed, user of this activity is still same",0, caseAi.getUserPerformerOID());

      // check grants
      final String MANAGECASE = Permissions.PREFIX + "processDefinition" + '.' + ExecutionPermission.Id.modifyCase ;

      PermissionState permDelegatged = delegatedPi.getPermission(MANAGECASE);
      System.out.println("permDelegated from motu to Org1 as motu - " + permDelegatged.getValue() + "  "  + permDelegatged.getName());

      ServiceFactory sfU1 = ServiceFactoryLocator.get(U1, U1);
      ProcessInstance processInstanceU1 = sfU1.getWorkflowService().getProcessInstance(delegatedPi.getOID());

      PermissionState permDelegatged2 = processInstanceU1.getPermission(MANAGECASE);
      System.out.println("permDelegated from motu to Org1 as user1 - " + permDelegatged2.getValue() + "  "  + permDelegatged2.getName());

      // DELEGATE to ScopedOrg1
      Organization scopedOrg = getTestModel().getOrganization("{CaseModel}ScopedOrg1");
      Department department1 = sfU1.getQueryService().findDepartment(null, D1, scopedOrg);
      ParticipantInfo participant = department1.getScopedParticipant(scopedOrg);
      ProcessInstance delegatedPi2 = sfU1.getWorkflowService().delegateCase(delegatedPi.getOID(), participant);
      sfU1.close();

      PermissionState permDelegatged3 = delegatedPi2.getPermission(MANAGECASE);
      System.out.println("permDelegated from Org1 to ScopedOrg1 as user1 - " + permDelegatged3.getValue() + "  "  + permDelegatged3.getName());

      ServiceFactory sfU2 = ServiceFactoryLocator.get(U2, U2);
      ProcessInstance processInstanceU2 = sfU2.getWorkflowService().getProcessInstance(delegatedPi.getOID());

      PermissionState permDelegatged4 = processInstanceU2.getPermission(MANAGECASE);
      System.out.println("permDelegated from Org1 to ScopedOrg1 as user2 - " + permDelegatged4.getValue() + "  "  + permDelegatged4.getName());
      sfU2.close();
   }

   /**
    * Delegation of a case to the scoped organization ScopedOrg1 with department d1.
    */
   @Test
   public void testScopedDelegate()
   {
      QueryService queryService = sf.getQueryService();
      ProcessInstance caseProcess1 = wfService.startProcess("{CaseModel}CaseProcess1", null,
            true);
      long[] members = {caseProcess1.getOID()};
      ProcessInstance casePi = wfService.createCase("Case1", null, members);
      assertNotNull(casePi);

      Organization scopedOrg = getTestModel().getOrganization("{CaseModel}ScopedOrg1");

      Department department1 = queryService.findDepartment(null, D1, scopedOrg);

      ProcessInstance delegatedPi = wfService.delegateCase(casePi.getOID(), department1.getScopedParticipant(scopedOrg));
      assertNotNull(delegatedPi);

      ActivityInstances delegatedAis = queryService.getAllActivityInstances(ActivityInstanceQuery.findForProcessInstance(delegatedPi.getOID()));
      assertNotNull(delegatedAis);
      
      ActivityInstance delegatedAi = null;
      for (ActivityInstance ai : delegatedAis)
      {
         if (ai.getProcessInstanceOID() == casePi.getOID())
         {
            delegatedAi = ai;
         }
      }
      
      assertNotNull(delegatedAi);
      assertEquals("ScopedOrg1", delegatedAi.getParticipantPerformerID());
      assertEquals(0, delegatedAi.getUserPerformerOID());
   }

   /**
    * Tests merging members of groups.
    */
   @Test
   public void testMerge()
   {
      ProcessInstance caseProcess1 = wfService.startProcess("{CaseModel}CaseProcess1", null,
            true);
      ProcessInstance caseProcess2 = wfService.startProcess("{CaseModel}CaseProcess2", null,
            true);

      long[] members = {caseProcess1.getOID()};
      ProcessInstance rootCaseProcess1 = wfService.createCase("Case1", null, members);
      assertNotNull(rootCaseProcess1);
      assertHierarchy(rootCaseProcess1, caseProcess1, true);

      long[] members2 = {caseProcess2.getOID()};
      ProcessInstance rootCaseProcess2 = wfService.createCase("Case2", null, members2);
      assertHierarchy(rootCaseProcess2, caseProcess2, true);

      long[] srcGroups = {rootCaseProcess1.getOID()};
      ProcessInstance rootCaseProcess3 = wfService.mergeCases(rootCaseProcess2.getOID(),
            srcGroups, "");
      assertHierarchy(rootCaseProcess3, caseProcess1, true);
      assertHierarchy(rootCaseProcess3, caseProcess2, true);

      assertSameProcessInstance(rootCaseProcess2, rootCaseProcess3);
   }

   // ************************************************************************************
   // ************************************************************************************
   // ************************************** UTILS ***************************************
   // ************************************************************************************
   // ************************************************************************************

   private void createCases(int count)
   {
      for (int i=0; i<count; i++)
      {
         ProcessInstance caseProcess = wfService.startProcess("{CaseModel}CaseProcess1", null, true);
         long[] members = {caseProcess.getOID()};
         ProcessInstance rootCaseProcess = wfService.createCase("Case" + i, null, members);
         assertNotNull(rootCaseProcess);
      }
   }
   
   private DeployedModel getTestModel()
   {
      final QueryService qs = sf.getQueryService();
      final DeployedModelQuery query = DeployedModelQuery.findActiveForId(MODEL_NAME);
      final Models models = qs.getModels(query);
      return qs.getModel(((DeployedModelDescription) models.get(0)).getModelOID());
   }
   
   private void assertHierarchy(ProcessInstance rootPi, ProcessInstance subPi,
         boolean contained)
   {
      DeployedModel model = sf.getQueryService().getModel(rootPi.getModelOID());
      ProcessDefinition processDefinition = model.getProcessDefinition(rootPi.getProcessID());

      ProcessInstanceQuery query = ProcessInstanceQuery.findForProcess(
            processDefinition.getQualifiedId(), true);
      query.where(ProcessInstanceQuery.OID.isEqual(subPi.getOID()));
      query.where(ProcessInstanceQuery.ROOT_PROCESS_INSTANCE_OID.isEqual(rootPi.getOID()));

      ProcessInstances allProcessInstances = sf.getQueryService().getAllProcessInstances(
            query);

      boolean empty = CollectionUtils.isEmpty(allProcessInstances);
      Assert.assertTrue("Hierarchy condition failed", contained ? !empty : empty);
   }

   private void assertSameProcessInstance(ProcessInstance pi1, ProcessInstance pi2)
   {
      assertEquals(pi1.getOID(), pi2.getOID());
      assertEquals(pi1.getScopeProcessInstanceOID(), pi2.getScopeProcessInstanceOID());
      assertEquals(pi1.getRootProcessInstanceOID(), pi2.getRootProcessInstanceOID());
      assertEquals(pi1.getModelOID(), pi2.getModelOID());
   }

   private static ProcessInstance getPiWithHierarchy(long oid, QueryService qs)
   {
      ProcessInstanceQuery query = ProcessInstanceQuery.findAll();

      query.setPolicy(HistoricalStatesPolicy.WITH_HIST_STATES);

      ProcessInstanceDetailsPolicy pidp = new ProcessInstanceDetailsPolicy(
            ProcessInstanceDetailsLevel.Default);
      pidp.getOptions().add(ProcessInstanceDetailsOptions.WITH_HIERARCHY_INFO);
      query.setPolicy(pidp);
      query.where(ProcessInstanceQuery.OID.isEqual(oid));
      return qs.findFirstProcessInstance(query);
   }
}
