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
package org.eclipse.stardust.test.archive;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.concurrent.TimeoutException;

import javax.sql.DataSource;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.core.persistence.archive.ExportProcessesCommand;
import org.eclipse.stardust.engine.core.persistence.archive.ImportProcessesCommand;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.ProcessInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

public class ArchiveTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(
         MOTU, MOTU);

   private static final String ASSERTION_MSG_HAS_ENTRY_IN_DB = " - process instance entry in database";

   private static final String NL = "\n";

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(
         ADMIN_USER_PWD_PAIR, testClassSetup);

   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(
         ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING,
         ArchiveModelConstants.MODEL_ID);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup).around(sf);

   @Test
   public void importNull()
   {
      WorkflowService workflowService = sf.getWorkflowService();
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(null));
      assertEquals(0, count);
   }

   @Test
   public void invalidProcessInstanceOid()
   {
      WorkflowService workflowService = sf.getWorkflowService();
      List<Long> oids = Arrays.asList(-1L);
      byte[] rawData = (byte[]) workflowService.execute(new ExportProcessesCommand(oids));
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(rawData));
      assertEquals(0, count);
      assertNull(rawData);
   }

   @Test
   public void invalidProcessInstanceOidNull()
   {
      WorkflowService workflowService = sf.getWorkflowService();
      Long oid = null;
      List<Long> oids = Arrays.asList(oid);
      byte[] rawData = (byte[]) workflowService.execute(new ExportProcessesCommand(oids));
      assertNull(rawData);
   }

   @Test
   public void invalidProcessInstanceOidBlankList()
   {
      WorkflowService workflowService = sf.getWorkflowService();
      List<Long> oids = new ArrayList<Long>();
      byte[] rawData = (byte[]) workflowService.execute(new ExportProcessesCommand(oids));
      assertNull(rawData);
   }

   @Test
   public void testExportNotCompletedOrAborted() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();

      final ProcessInstance pi = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);

      List<Long> oids = Arrays.asList(pi.getOID());
      byte[] rawData = (byte[]) workflowService.execute(new ExportProcessesCommand(oids));
      assertNull(rawData);

      // ensure it was not purged
      ProcessInstanceQuery pQuery = new ProcessInstanceQuery();
      pQuery.where(ProcessInstanceQuery.OID.isEqual(pi.getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      assertNotNull(oldInstances);
      assertEquals(1, oldInstances.size());
   }

   @Test
   public void testExportAllNullDates() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance simpleManualA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualA);
      final ProcessInstance simpleManualB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualB);
      final ProcessInstance simpleA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleA);
      final ProcessInstance simpleB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleB);
      final ProcessInstance subProcessesInModel = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL, null, true);
      completeSubProcessesInModel(subProcessesInModel);

      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});
      ProcessInstanceQuery querySubSimple = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLE);
      querySubSimple.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstanceQuery querySubManual = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL);
      querySubManual.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstances oldSubProcessInstancesSimple = queryService
            .getAllProcessInstances(querySubSimple);
      ProcessInstances oldSubProcessInstancesManual = queryService
            .getAllProcessInstances(querySubManual);
      assertNotNull(oldSubProcessInstancesSimple);
      assertEquals(1, oldSubProcessInstancesSimple.size());
      assertNotNull(oldSubProcessInstancesManual);
      assertEquals(1, oldSubProcessInstancesManual.size());
      ProcessInstance subSimple = oldSubProcessInstancesSimple.iterator().next();
      ProcessInstance subManual = oldSubProcessInstancesManual.iterator().next();

      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subSimple.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subManual.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subProcessesInModel
            .getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(7, oldInstances.size());
      assertEquals(20, oldActivities.size());

      byte[] rawData = (byte[]) workflowService.execute(new ExportProcessesCommand(null,
            null));
      assertNotNull(rawData);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());

      int count = (Integer) workflowService.execute(new ImportProcessesCommand(rawData));
      assertEquals(7, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      assertProcessInstancesEquals(oldInstances, newInstances);
      assertActivityInstancesEquals(oldActivities, newActivities);
   }

   @Test
   public void testExportAllFromAndToDate() throws Exception
   {

      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance simpleManualA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualA);
      Thread.sleep(1000);
      Date fromDate = new Date();
      final ProcessInstance simpleManualB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualB);
      final ProcessInstance simpleA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleA);
      final ProcessInstance simpleB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleB);
      Thread.sleep(1000);
      Date toDate = new Date();
      final ProcessInstance subProcessesInModel = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL, null, true);
      completeSubProcessesInModel(subProcessesInModel);

      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});
      ProcessInstanceQuery querySubSimple = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLE);
      querySubSimple.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstanceQuery querySubManual = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL);
      querySubManual.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstances oldSubProcessInstancesSimple = queryService
            .getAllProcessInstances(querySubSimple);
      ProcessInstances oldSubProcessInstancesManual = queryService
            .getAllProcessInstances(querySubManual);
      assertNotNull(oldSubProcessInstancesSimple);
      assertEquals(1, oldSubProcessInstancesSimple.size());
      assertNotNull(oldSubProcessInstancesManual);
      assertEquals(1, oldSubProcessInstancesManual.size());
      ProcessInstance subSimple = oldSubProcessInstancesSimple.iterator().next();
      ProcessInstance subManual = oldSubProcessInstancesManual.iterator().next();

      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subSimple.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subManual.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subProcessesInModel
            .getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(7, oldInstances.size());
      assertEquals(20, oldActivities.size());

      byte[] rawData = (byte[]) workflowService.execute(new ExportProcessesCommand(
            fromDate, toDate));
      assertNotNull(rawData);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(4, clearedInstances.size());
      assertEquals(13, clearedActivities.size());

      int count = (Integer) workflowService.execute(new ImportProcessesCommand(rawData));
      assertEquals(3, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      assertProcessInstancesEquals(oldInstances, newInstances);
      assertActivityInstancesEquals(oldActivities, newActivities);
   }

   @Test
   public void testExportAllFilterImportFromAndToDate() throws Exception
   {

      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance simpleManualA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualA);
      Thread.sleep(1000);
      Date fromDate = new Date();
      final ProcessInstance simpleManualB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualB);
      final ProcessInstance simpleA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleA);
      final ProcessInstance simpleB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleB);
      Thread.sleep(1000);
      Date toDate = new Date();
      final ProcessInstance subProcessesInModel = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL, null, true);
      completeSubProcessesInModel(subProcessesInModel);

      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});
      ProcessInstanceQuery querySubSimple = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLE);
      querySubSimple.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstanceQuery querySubManual = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL);
      querySubManual.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstances oldSubProcessInstancesSimple = queryService
            .getAllProcessInstances(querySubSimple);
      ProcessInstances oldSubProcessInstancesManual = queryService
            .getAllProcessInstances(querySubManual);
      assertNotNull(oldSubProcessInstancesSimple);
      assertEquals(1, oldSubProcessInstancesSimple.size());
      assertNotNull(oldSubProcessInstancesManual);
      assertEquals(1, oldSubProcessInstancesManual.size());
      ProcessInstance subSimple = oldSubProcessInstancesSimple.iterator().next();
      ProcessInstance subManual = oldSubProcessInstancesManual.iterator().next();

      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subSimple.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subManual.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subProcessesInModel
            .getOID()));

      ActivityInstanceQuery aExpectedQuery = new ActivityInstanceQuery();
      FilterOrTerm orTermExpectedQuery = aExpectedQuery.getFilter().addOrTerm();
      orTermExpectedQuery.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleA
            .getOID()));
      orTermExpectedQuery.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleB
            .getOID()));
      orTermExpectedQuery.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID
            .isEqual(simpleManualB.getOID()));
      ProcessInstanceQuery pExpectedQuery = new ProcessInstanceQuery();
      FilterOrTerm orTermPExpectedQuery = pExpectedQuery.getFilter().addOrTerm();
      orTermPExpectedQuery.or(ProcessInstanceQuery.OID.isEqual(simpleA.getOID()));
      orTermPExpectedQuery.or(ProcessInstanceQuery.OID.isEqual(simpleB.getOID()));
      orTermPExpectedQuery.or(ProcessInstanceQuery.OID.isEqual(simpleManualB.getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ProcessInstances expectedInstances = queryService
            .getAllProcessInstances(pExpectedQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      ActivityInstances expectedActivities = queryService
            .getAllActivityInstances(aExpectedQuery);
      assertNotNull(oldInstances);
      assertNotNull(expectedInstances);
      assertNotNull(oldActivities);
      assertNotNull(expectedActivities);
      assertEquals(7, oldInstances.size());
      assertEquals(20, oldActivities.size());
      assertEquals(7, expectedActivities.size());
      assertEquals(3, expectedInstances.size());

      byte[] rawData = (byte[]) workflowService.execute(new ExportProcessesCommand());
      assertNotNull(rawData);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());

      int count = (Integer) workflowService.execute(new ImportProcessesCommand(rawData,
            fromDate, toDate));
      assertEquals(3, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService
            .getAllActivityInstances(aExpectedQuery);

      assertProcessInstancesEquals(expectedInstances, newInstances);
      assertActivityInstancesEquals(expectedActivities, newActivities);
   }

   @Test
   public void testExportAllFilterImportFromAndToDateBothNull() throws Exception
   {

      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance simpleManualA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualA);
      final ProcessInstance simpleManualB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualB);
      final ProcessInstance simpleA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleA);
      final ProcessInstance simpleB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleB);
      final ProcessInstance subProcessesInModel = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL, null, true);
      completeSubProcessesInModel(subProcessesInModel);

      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});
      ProcessInstanceQuery querySubSimple = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLE);
      querySubSimple.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstanceQuery querySubManual = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL);
      querySubManual.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstances oldSubProcessInstancesSimple = queryService
            .getAllProcessInstances(querySubSimple);
      ProcessInstances oldSubProcessInstancesManual = queryService
            .getAllProcessInstances(querySubManual);
      assertNotNull(oldSubProcessInstancesSimple);
      assertEquals(1, oldSubProcessInstancesSimple.size());
      assertNotNull(oldSubProcessInstancesManual);
      assertEquals(1, oldSubProcessInstancesManual.size());
      ProcessInstance subSimple = oldSubProcessInstancesSimple.iterator().next();
      ProcessInstance subManual = oldSubProcessInstancesManual.iterator().next();

      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subSimple.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subManual.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subProcessesInModel
            .getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(7, oldInstances.size());
      assertEquals(20, oldActivities.size());

      byte[] rawData = (byte[]) workflowService.execute(new ExportProcessesCommand());
      assertNotNull(rawData);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());

      int count = (Integer) workflowService.execute(new ImportProcessesCommand(rawData,
            null, null));
      assertEquals(7, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      assertProcessInstancesEquals(oldInstances, newInstances);
      assertActivityInstancesEquals(oldActivities, newActivities);
   }

   @Test
   public void testExportAllFilterImportFromNullAndToDate() throws Exception
   {

      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance simpleManualA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualA);
      Thread.sleep(1000);
      Date fromDate = null;
      final ProcessInstance simpleManualB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualB);
      final ProcessInstance simpleA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleA);
      final ProcessInstance simpleB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleB);
      Thread.sleep(1000);
      Date toDate = new Date();
      final ProcessInstance subProcessesInModel = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL, null, true);
      completeSubProcessesInModel(subProcessesInModel);

      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});
      ProcessInstanceQuery querySubSimple = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLE);
      querySubSimple.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstanceQuery querySubManual = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL);
      querySubManual.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstances oldSubProcessInstancesSimple = queryService
            .getAllProcessInstances(querySubSimple);
      ProcessInstances oldSubProcessInstancesManual = queryService
            .getAllProcessInstances(querySubManual);
      assertNotNull(oldSubProcessInstancesSimple);
      assertEquals(1, oldSubProcessInstancesSimple.size());
      assertNotNull(oldSubProcessInstancesManual);
      assertEquals(1, oldSubProcessInstancesManual.size());
      ProcessInstance subSimple = oldSubProcessInstancesSimple.iterator().next();
      ProcessInstance subManual = oldSubProcessInstancesManual.iterator().next();

      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subSimple.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subManual.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subProcessesInModel
            .getOID()));

      ActivityInstanceQuery aExpectedQuery = new ActivityInstanceQuery();
      FilterOrTerm orTermExpectedQuery = aExpectedQuery.getFilter().addOrTerm();
      orTermExpectedQuery.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleA
            .getOID()));
      orTermExpectedQuery.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleB
            .getOID()));
      orTermExpectedQuery.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID
            .isEqual(simpleManualA.getOID()));
      orTermExpectedQuery.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID
            .isEqual(simpleManualB.getOID()));
      ProcessInstanceQuery pExpectedQuery = new ProcessInstanceQuery();
      FilterOrTerm orTermPExpectedQuery = pExpectedQuery.getFilter().addOrTerm();
      orTermPExpectedQuery.or(ProcessInstanceQuery.OID.isEqual(simpleA.getOID()));
      orTermPExpectedQuery.or(ProcessInstanceQuery.OID.isEqual(simpleB.getOID()));
      orTermPExpectedQuery.or(ProcessInstanceQuery.OID.isEqual(simpleManualB.getOID()));
      orTermPExpectedQuery.or(ProcessInstanceQuery.OID.isEqual(simpleManualA.getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ProcessInstances expectedInstances = queryService
            .getAllProcessInstances(pExpectedQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      ActivityInstances expectedActivities = queryService
            .getAllActivityInstances(aExpectedQuery);
      assertNotNull(oldInstances);
      assertNotNull(expectedInstances);
      assertNotNull(oldActivities);
      assertNotNull(expectedActivities);
      assertEquals(7, oldInstances.size());
      assertEquals(20, oldActivities.size());
      assertEquals(10, expectedActivities.size());
      assertEquals(4, expectedInstances.size());

      byte[] rawData = (byte[]) workflowService.execute(new ExportProcessesCommand());
      assertNotNull(rawData);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());

      int count = (Integer) workflowService.execute(new ImportProcessesCommand(rawData,
            fromDate, toDate));
      assertEquals(4, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService
            .getAllActivityInstances(aExpectedQuery);

      assertProcessInstancesEquals(expectedInstances, newInstances);
      assertActivityInstancesEquals(expectedActivities, newActivities);
   }

   @Test
   public void testExportAllFilterImportFromAndToDateNull() throws Exception
   {

      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance simpleManualA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualA);
      Thread.sleep(1000);
      Date fromDate = new Date();
      final ProcessInstance simpleManualB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualB);
      final ProcessInstance simpleA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleA);
      final ProcessInstance simpleB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleB);
      Thread.sleep(1000);
      Date toDate = null;
      final ProcessInstance subProcessesInModel = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL, null, true);
      completeSubProcessesInModel(subProcessesInModel);

      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});
      ProcessInstanceQuery querySubSimple = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLE);
      querySubSimple.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstanceQuery querySubManual = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL);
      querySubManual.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstances oldSubProcessInstancesSimple = queryService
            .getAllProcessInstances(querySubSimple);
      ProcessInstances oldSubProcessInstancesManual = queryService
            .getAllProcessInstances(querySubManual);
      assertNotNull(oldSubProcessInstancesSimple);
      assertEquals(1, oldSubProcessInstancesSimple.size());
      assertNotNull(oldSubProcessInstancesManual);
      assertEquals(1, oldSubProcessInstancesManual.size());
      ProcessInstance subSimple = oldSubProcessInstancesSimple.iterator().next();
      ProcessInstance subManual = oldSubProcessInstancesManual.iterator().next();

      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subSimple.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subManual.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subProcessesInModel
            .getOID()));

      ActivityInstanceQuery aExpectedQuery = new ActivityInstanceQuery();
      FilterOrTerm orTermExpectedQuery = aExpectedQuery.getFilter().addOrTerm();
      orTermExpectedQuery.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleA
            .getOID()));
      orTermExpectedQuery.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleB
            .getOID()));
      orTermExpectedQuery.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID
            .isEqual(simpleManualB.getOID()));
      orTermExpectedQuery.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subSimple
            .getOID()));
      orTermExpectedQuery.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subManual
            .getOID()));
      orTermExpectedQuery.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID
            .isEqual(subProcessesInModel.getOID()));
      ProcessInstanceQuery pExpectedQuery = new ProcessInstanceQuery();
      FilterOrTerm orTermPExpectedQuery = pExpectedQuery.getFilter().addOrTerm();
      orTermPExpectedQuery.or(ProcessInstanceQuery.OID.isEqual(simpleA.getOID()));
      orTermPExpectedQuery.or(ProcessInstanceQuery.OID.isEqual(simpleB.getOID()));
      orTermPExpectedQuery.or(ProcessInstanceQuery.OID.isEqual(simpleManualB.getOID()));
      orTermPExpectedQuery.or(ProcessInstanceQuery.OID.isEqual(subSimple.getOID()));
      orTermPExpectedQuery.or(ProcessInstanceQuery.OID.isEqual(subManual.getOID()));
      orTermPExpectedQuery.or(ProcessInstanceQuery.OID.isEqual(subProcessesInModel
            .getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ProcessInstances expectedInstances = queryService
            .getAllProcessInstances(pExpectedQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      ActivityInstances expectedActivities = queryService
            .getAllActivityInstances(aExpectedQuery);
      assertNotNull(oldInstances);
      assertNotNull(expectedInstances);
      assertNotNull(oldActivities);
      assertNotNull(expectedActivities);
      assertEquals(7, oldInstances.size());
      assertEquals(20, oldActivities.size());
      assertEquals(17, expectedActivities.size());
      assertEquals(6, expectedInstances.size());

      byte[] rawData = (byte[]) workflowService.execute(new ExportProcessesCommand());
      assertNotNull(rawData);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());

      int count = (Integer) workflowService.execute(new ImportProcessesCommand(rawData,
            fromDate, toDate));
      assertEquals(6, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService
            .getAllActivityInstances(aExpectedQuery);

      assertProcessInstancesEquals(expectedInstances, newInstances);
      assertActivityInstancesEquals(expectedActivities, newActivities);
   }

   @Test
   public void testExportAllNullFromAndToDate() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance simpleManualA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualA);
      final ProcessInstance simpleManualB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualB);
      final ProcessInstance simpleA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleA);
      final ProcessInstance simpleB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleB);
      Thread.sleep(1000);
      Date toDate = new Date();
      final ProcessInstance subProcessesInModel = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL, null, true);
      completeSubProcessesInModel(subProcessesInModel);

      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});
      ProcessInstanceQuery querySubSimple = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLE);
      querySubSimple.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstanceQuery querySubManual = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL);
      querySubManual.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstances oldSubProcessInstancesSimple = queryService
            .getAllProcessInstances(querySubSimple);
      ProcessInstances oldSubProcessInstancesManual = queryService
            .getAllProcessInstances(querySubManual);
      assertNotNull(oldSubProcessInstancesSimple);
      assertEquals(1, oldSubProcessInstancesSimple.size());
      assertNotNull(oldSubProcessInstancesManual);
      assertEquals(1, oldSubProcessInstancesManual.size());
      ProcessInstance subSimple = oldSubProcessInstancesSimple.iterator().next();
      ProcessInstance subManual = oldSubProcessInstancesManual.iterator().next();

      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subSimple.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subManual.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subProcessesInModel
            .getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(7, oldInstances.size());
      assertEquals(20, oldActivities.size());

      Date fromDate = null;
      byte[] rawData = (byte[]) workflowService.execute(new ExportProcessesCommand(
            fromDate, toDate));
      assertNotNull(rawData);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(3, clearedInstances.size());
      assertEquals(10, clearedActivities.size());

      int count = (Integer) workflowService.execute(new ImportProcessesCommand(rawData));
      assertEquals(4, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      assertProcessInstancesEquals(oldInstances, newInstances);
      assertActivityInstancesEquals(oldActivities, newActivities);
   }

   @Test
   public void testExportAllFromAndNullToDate() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance simpleManualA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualA);
      Thread.sleep(1000);
      Date fromDate = new Date();
      final ProcessInstance simpleManualB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualB);
      final ProcessInstance simpleA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleA);
      final ProcessInstance simpleB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleB);
      final ProcessInstance subProcessesInModel = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL, null, true);
      completeSubProcessesInModel(subProcessesInModel);

      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});
      ProcessInstanceQuery querySubSimple = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLE);
      querySubSimple.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstanceQuery querySubManual = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL);
      querySubManual.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstances oldSubProcessInstancesSimple = queryService
            .getAllProcessInstances(querySubSimple);
      ProcessInstances oldSubProcessInstancesManual = queryService
            .getAllProcessInstances(querySubManual);
      assertNotNull(oldSubProcessInstancesSimple);
      assertEquals(1, oldSubProcessInstancesSimple.size());
      assertNotNull(oldSubProcessInstancesManual);
      assertEquals(1, oldSubProcessInstancesManual.size());
      ProcessInstance subSimple = oldSubProcessInstancesSimple.iterator().next();
      ProcessInstance subManual = oldSubProcessInstancesManual.iterator().next();

      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subSimple.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subManual.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subProcessesInModel
            .getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(7, oldInstances.size());
      assertEquals(20, oldActivities.size());

      Date toDate = null;
      byte[] rawData = (byte[]) workflowService.execute(new ExportProcessesCommand(
            fromDate, toDate));
      assertNotNull(rawData);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(1, clearedInstances.size());
      assertEquals(3, clearedActivities.size());

      int count = (Integer) workflowService.execute(new ImportProcessesCommand(rawData));
      assertEquals(6, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      assertProcessInstancesEquals(oldInstances, newInstances);
      assertActivityInstancesEquals(oldActivities, newActivities);
   }

   @Test
   public void testExportAll() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance simpleManualA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualA);
      final ProcessInstance simpleManualB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualB);
      final ProcessInstance simpleA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleA);
      final ProcessInstance simpleB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleB);
      final ProcessInstance subProcessesInModel = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL, null, true);
      completeSubProcessesInModel(subProcessesInModel);

      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});
      ProcessInstanceQuery querySubSimple = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLE);
      querySubSimple.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstanceQuery querySubManual = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL);
      querySubManual.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstances oldSubProcessInstancesSimple = queryService
            .getAllProcessInstances(querySubSimple);
      ProcessInstances oldSubProcessInstancesManual = queryService
            .getAllProcessInstances(querySubManual);
      assertNotNull(oldSubProcessInstancesSimple);
      assertEquals(1, oldSubProcessInstancesSimple.size());
      assertNotNull(oldSubProcessInstancesManual);
      assertEquals(1, oldSubProcessInstancesManual.size());
      ProcessInstance subSimple = oldSubProcessInstancesSimple.iterator().next();
      ProcessInstance subManual = oldSubProcessInstancesManual.iterator().next();

      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subSimple.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subManual.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subProcessesInModel
            .getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(7, oldInstances.size());
      assertEquals(20, oldActivities.size());

      byte[] rawData = (byte[]) workflowService.execute(new ExportProcessesCommand());
      assertNotNull(rawData);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());

      int count = (Integer) workflowService.execute(new ImportProcessesCommand(rawData));
      assertEquals(7, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      assertProcessInstancesEquals(oldInstances, newInstances);
      assertActivityInstancesEquals(oldActivities, newActivities);
   }

   @Test
   public void testExportAllFilterImportProcessIds() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance simpleManualA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualA);
      final ProcessInstance simpleManualB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualB);
      final ProcessInstance simpleA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleA);
      final ProcessInstance simpleB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleB);
      final ProcessInstance subProcessesInModel = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL, null, true);
      completeSubProcessesInModel(subProcessesInModel);

      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});
      ProcessInstanceQuery querySubSimple = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLE);
      querySubSimple.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstanceQuery querySubManual = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL);
      querySubManual.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstances oldSubProcessInstancesSimple = queryService
            .getAllProcessInstances(querySubSimple);
      ProcessInstances oldSubProcessInstancesManual = queryService
            .getAllProcessInstances(querySubManual);
      assertNotNull(oldSubProcessInstancesSimple);
      assertEquals(1, oldSubProcessInstancesSimple.size());
      assertNotNull(oldSubProcessInstancesManual);
      assertEquals(1, oldSubProcessInstancesManual.size());
      ProcessInstance subSimple = oldSubProcessInstancesSimple.iterator().next();
      ProcessInstance subManual = oldSubProcessInstancesManual.iterator().next();

      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subSimple.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subManual.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subProcessesInModel
            .getOID()));

      ActivityInstanceQuery aExpectedQuery = new ActivityInstanceQuery();
      FilterOrTerm orTermExpectedQuery = aExpectedQuery.getFilter().addOrTerm();
      orTermExpectedQuery.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleA
            .getOID()));
      orTermExpectedQuery.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID
            .isEqual(subProcessesInModel.getOID()));
      orTermExpectedQuery.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subSimple
            .getOID()));
      orTermExpectedQuery.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subManual
            .getOID()));
      ProcessInstanceQuery pExpectedQuery = new ProcessInstanceQuery();
      FilterOrTerm orTermPExpectedQuery = pExpectedQuery.getFilter().addOrTerm();
      orTermPExpectedQuery.or(ProcessInstanceQuery.OID.isEqual(simpleA.getOID()));
      orTermPExpectedQuery.or(ProcessInstanceQuery.OID.isEqual(subProcessesInModel
            .getOID()));
      orTermPExpectedQuery.or(ProcessInstanceQuery.OID.isEqual(subSimple.getOID()));
      orTermPExpectedQuery.or(ProcessInstanceQuery.OID.isEqual(subManual.getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ProcessInstances expectedInstances = queryService
            .getAllProcessInstances(pExpectedQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      ActivityInstances expectedActivities = queryService
            .getAllActivityInstances(aExpectedQuery);
      assertNotNull(oldInstances);
      assertNotNull(expectedInstances);
      assertNotNull(oldActivities);
      assertNotNull(expectedActivities);
      assertEquals(7, oldInstances.size());
      assertEquals(20, oldActivities.size());
      assertEquals(12, expectedActivities.size());
      assertEquals(4, expectedInstances.size());

      byte[] rawData = (byte[]) workflowService.execute(new ExportProcessesCommand());
      assertNotNull(rawData);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());

      List<Long> oids = Arrays.asList(simpleA.getOID(), subProcessesInModel.getOID());
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(rawData,
            oids));
      assertEquals(4, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService
            .getAllActivityInstances(aExpectedQuery);

      assertProcessInstancesEquals(expectedInstances, newInstances);
      assertActivityInstancesEquals(expectedActivities, newActivities);
   }

   @Test
   public void testExportAllFilterImportProcessIdsInvalid() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance simpleManualA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualA);
      final ProcessInstance simpleManualB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualB);
      final ProcessInstance simpleA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleA);
      final ProcessInstance simpleB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleB);
      final ProcessInstance subProcessesInModel = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL, null, true);
      completeSubProcessesInModel(subProcessesInModel);

      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});
      ProcessInstanceQuery querySubSimple = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLE);
      querySubSimple.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstanceQuery querySubManual = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL);
      querySubManual.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstances oldSubProcessInstancesSimple = queryService
            .getAllProcessInstances(querySubSimple);
      ProcessInstances oldSubProcessInstancesManual = queryService
            .getAllProcessInstances(querySubManual);
      assertNotNull(oldSubProcessInstancesSimple);
      assertEquals(1, oldSubProcessInstancesSimple.size());
      assertNotNull(oldSubProcessInstancesManual);
      assertEquals(1, oldSubProcessInstancesManual.size());
      ProcessInstance subSimple = oldSubProcessInstancesSimple.iterator().next();
      ProcessInstance subManual = oldSubProcessInstancesManual.iterator().next();

      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subSimple.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subManual.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subProcessesInModel
            .getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(7, oldInstances.size());
      assertEquals(20, oldActivities.size());

      byte[] rawData = (byte[]) workflowService.execute(new ExportProcessesCommand());
      assertNotNull(rawData);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());

      List<Long> oids = Arrays.asList(-1L, null);
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(rawData,
            oids));
      assertEquals(0, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      assertEquals(0, newInstances.size());
   }

   @Test
   public void testExportAllFilterImportProcessIdsBlank() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance simpleManualA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualA);
      final ProcessInstance simpleManualB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualB);
      final ProcessInstance simpleA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleA);
      final ProcessInstance simpleB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleB);
      final ProcessInstance subProcessesInModel = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL, null, true);
      completeSubProcessesInModel(subProcessesInModel);

      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});
      ProcessInstanceQuery querySubSimple = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLE);
      querySubSimple.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstanceQuery querySubManual = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL);
      querySubManual.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstances oldSubProcessInstancesSimple = queryService
            .getAllProcessInstances(querySubSimple);
      ProcessInstances oldSubProcessInstancesManual = queryService
            .getAllProcessInstances(querySubManual);
      assertNotNull(oldSubProcessInstancesSimple);
      assertEquals(1, oldSubProcessInstancesSimple.size());
      assertNotNull(oldSubProcessInstancesManual);
      assertEquals(1, oldSubProcessInstancesManual.size());
      ProcessInstance subSimple = oldSubProcessInstancesSimple.iterator().next();
      ProcessInstance subManual = oldSubProcessInstancesManual.iterator().next();

      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subSimple.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subManual.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subProcessesInModel
            .getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(7, oldInstances.size());
      assertEquals(20, oldActivities.size());

      byte[] rawData = (byte[]) workflowService.execute(new ExportProcessesCommand());
      assertNotNull(rawData);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());

      List<Long> oids = new ArrayList<Long>();
      int count = (Integer) workflowService.execute(new ImportProcessesCommand(rawData,
            oids));
      assertEquals(0, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      assertEquals(0, newInstances.size());
   }

   @Test
   public void testExport2OfMany() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      final ProcessInstance simpleManualA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualA);
      final ProcessInstance simpleManualB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);
      completeSimpleManual(simpleManualB);
      final ProcessInstance simpleA = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleA);
      final ProcessInstance simpleB = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);
      completeSimple(simpleB);
      final ProcessInstance subProcessesInModel = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL, null, true);
      completeSubProcessesInModel(subProcessesInModel);

      ProcessInstanceQuery pQuery = ProcessInstanceQuery
            .findInState(new ProcessInstanceState[] {
                  ProcessInstanceState.Aborted, ProcessInstanceState.Completed});
      ProcessInstanceQuery querySubSimple = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLE);
      querySubSimple.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstanceQuery querySubManual = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL);
      querySubManual.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstances oldSubProcessInstancesSimple = queryService
            .getAllProcessInstances(querySubSimple);
      ProcessInstances oldSubProcessInstancesManual = queryService
            .getAllProcessInstances(querySubManual);
      assertNotNull(oldSubProcessInstancesSimple);
      assertEquals(1, oldSubProcessInstancesSimple.size());
      assertNotNull(oldSubProcessInstancesManual);
      assertEquals(1, oldSubProcessInstancesManual.size());
      ProcessInstance subSimple = oldSubProcessInstancesSimple.iterator().next();
      ProcessInstance subManual = oldSubProcessInstancesManual.iterator().next();

      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      FilterOrTerm orTerm = aQuery.getFilter().addOrTerm();
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualA.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(simpleManualB.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subSimple.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subManual.getOID()));
      orTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subProcessesInModel
            .getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(7, oldInstances.size());
      assertEquals(20, oldActivities.size());

      List<Long> oids = Arrays.asList(simpleA.getOID(), simpleManualA.getOID());
      byte[] rawData = (byte[]) workflowService.execute(new ExportProcessesCommand(oids));
      assertNotNull(rawData);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(5, clearedInstances.size());
      assertEquals(15, clearedActivities.size());

      int count = (Integer) workflowService.execute(new ImportProcessesCommand(rawData));
      assertEquals(2, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      assertProcessInstancesEquals(oldInstances, newInstances);
      assertActivityInstancesEquals(oldActivities, newActivities);
   }

   @Test
   public void testExportImportSimple() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();

      final ProcessInstance pi = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLE, null, true);

      completeSimple(pi);

      ProcessInstanceQuery pQuery = new ProcessInstanceQuery();
      pQuery.where(ProcessInstanceQuery.OID.isEqual(pi.getOID()));
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      aQuery.where(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi.getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(1, oldInstances.size());
      assertEquals(2, oldActivities.size());
      assertEquals(pi.getOID(), oldInstances.get(0).getOID());
      assertNotNull(pi.getScopeProcessInstanceOID());
      assertNotNull(pi.getRootProcessInstanceOID());

      List<Long> oids = Arrays.asList(pi.getOID());
      byte[] rawData = (byte[]) workflowService.execute(new ExportProcessesCommand(oids));
      assertNotNull(rawData);

      ProcessInstances instances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances activitiesCleared = queryService.getAllActivityInstances(aQuery);
      assertNotNull(instances);
      assertNotNull(activitiesCleared);
      assertEquals(0, instances.size());
      assertEquals(0, activitiesCleared.size());

      int count = (Integer) workflowService.execute(new ImportProcessesCommand(rawData));
      assertEquals(1, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);
      assertProcessInstancesEquals(oldInstances, newInstances);
      assertActivityInstancesEquals(oldActivities, newActivities);
   }

   private ProcessInstance completeSimple(ProcessInstance pi) throws TimeoutException,
         InterruptedException
   {
      completeNextActivity(pi, null, null);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(),
            ProcessInstanceState.Completed);
      return pi;
   }

   @Test
   public void testExportImportSimpleManualAborted() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      AdministrationService adminService = sf.getAdministrationService();

      final ProcessInstance pi = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);

      final ActivityInstance writeActivity = completeNextActivity(pi,
            ArchiveModelConstants.DATA_ID_TEXTDATA, "my test data");

      adminService.abortProcessInstance(pi.getOID());

      ProcessInstanceStateBarrier.instance().await(pi.getOID(),
            ProcessInstanceState.Aborted);

      assertDataExists(pi.getOID(), writeActivity.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, "my test data");

      ProcessInstanceQuery pQuery = new ProcessInstanceQuery();
      pQuery.where(ProcessInstanceQuery.OID.isEqual(pi.getOID()));
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      aQuery.where(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi.getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(1, oldInstances.size());
      assertEquals(2, oldActivities.size());
      assertEquals(pi.getOID(), oldInstances.get(0).getOID());
      assertNotNull(pi.getScopeProcessInstanceOID());
      assertNotNull(pi.getRootProcessInstanceOID());

      List<Long> oids = Arrays.asList(pi.getOID());
      byte[] rawData = (byte[]) workflowService.execute(new ExportProcessesCommand(oids));
      assertNotNull(rawData);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());
      assertDataNotExists(pi.getOID(), writeActivity.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, "my test data");

      int count = (Integer) workflowService.execute(new ImportProcessesCommand(rawData));
      assertEquals(1, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      assertProcessInstancesEquals(oldInstances, newInstances);
      assertActivityInstancesEquals(oldActivities, newActivities);
      assertDataExists(pi.getOID(), writeActivity.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, "my test data");

   }

   @Test
   public void testExportImportSimpleManual() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();

      final ProcessInstance pi = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL, null, true);

      final ActivityInstance writeActivity = completeSimpleManual(pi);

      assertDataExists(pi.getOID(), writeActivity.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, "my test data");

      ProcessInstanceQuery pQuery = new ProcessInstanceQuery();
      pQuery.where(ProcessInstanceQuery.OID.isEqual(pi.getOID()));
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      aQuery.where(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi.getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(1, oldInstances.size());
      assertEquals(3, oldActivities.size());
      assertEquals(pi.getOID(), oldInstances.get(0).getOID());
      assertNotNull(pi.getScopeProcessInstanceOID());
      assertNotNull(pi.getRootProcessInstanceOID());

      List<Long> oids = Arrays.asList(pi.getOID());
      byte[] rawData = (byte[]) workflowService.execute(new ExportProcessesCommand(oids));
      assertNotNull(rawData);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());
      assertDataNotExists(pi.getOID(), writeActivity.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, "my test data");

      int count = (Integer) workflowService.execute(new ImportProcessesCommand(rawData));
      assertEquals(1, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      assertProcessInstancesEquals(oldInstances, newInstances);
      assertActivityInstancesEquals(oldActivities, newActivities);
      assertDataExists(pi.getOID(), writeActivity.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, "my test data");

   }

   @Test
   public void testExportImportScriptProcess() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();

      final ProcessInstance pi = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SCRIPTPROCESS, null, true);

      final ActivityInstance writeActivity = completeScriptProcess(pi,10);

      assertDataExists(pi.getOID(), writeActivity.getOID(),
            ArchiveModelConstants.PROCESS_DEF_CALL_SCRIPTPROCESS,
            ArchiveModelConstants.DATA_ID_NUMBERVALUE, 20);
      
      assertTrue(hasStructuredDateField(pi.getOID(), ArchiveModelConstants.DATA_ID_STRUCTUREDDATA,
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA_MYFIELDB, 20));

      ProcessInstanceQuery pQuery = new ProcessInstanceQuery();
      pQuery.where(ProcessInstanceQuery.OID.isEqual(pi.getOID()));
      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      aQuery.where(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi.getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(oldInstances);
      assertNotNull(oldActivities);
      assertEquals(1, oldInstances.size());
      assertEquals(8, oldActivities.size());
      assertEquals(pi.getOID(), oldInstances.get(0).getOID());
      assertNotNull(pi.getScopeProcessInstanceOID());
      assertNotNull(pi.getRootProcessInstanceOID());

      List<Long> oids = Arrays.asList(pi.getOID());
      byte[] rawData = (byte[]) workflowService.execute(new ExportProcessesCommand(oids));
      assertNotNull(rawData);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      assertNotNull(clearedInstances);
      assertNotNull(clearedActivities);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedActivities.size());
      assertDataNotExists(pi.getOID(), writeActivity.getOID(),
            ArchiveModelConstants.PROCESS_DEF_CALL_SCRIPTPROCESS,
            ArchiveModelConstants.DATA_ID_NUMBERVALUE, 20);

      assertFalse(hasStructuredDateField(pi.getOID(), ArchiveModelConstants.DATA_ID_STRUCTUREDDATA,
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA_MYFIELDB, 20));

      int count = (Integer) workflowService.execute(new ImportProcessesCommand(rawData));
      assertEquals(1, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQuery);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);

      assertProcessInstancesEquals(oldInstances, newInstances);
      assertActivityInstancesEquals(oldActivities, newActivities);
      assertDataExists(pi.getOID(), writeActivity.getOID(),
            ArchiveModelConstants.PROCESS_DEF_CALL_SCRIPTPROCESS,
            ArchiveModelConstants.DATA_ID_NUMBERVALUE, 20);

      assertTrue(hasStructuredDateField(pi.getOID(), ArchiveModelConstants.DATA_ID_STRUCTUREDDATA,
            ArchiveModelConstants.DATA_ID_STRUCTUREDDATA_MYFIELDB, 20));

   }

   private ActivityInstance completeScriptProcess(final ProcessInstance pi, int numberValue)
         throws TimeoutException, InterruptedException
   {
      final ActivityInstance writeActivity = completeNextActivity(pi,
            ArchiveModelConstants.DATA_ID_NUMBERVALUE, numberValue);

      completeNextActivity(pi, null, null);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(),
            ProcessInstanceState.Completed);
      return writeActivity;
   }
   
   private ActivityInstance completeSimpleManual(final ProcessInstance pi)
         throws TimeoutException, InterruptedException
   {
      final ActivityInstance writeActivity = completeNextActivity(pi,
            ArchiveModelConstants.DATA_ID_TEXTDATA, "my test data");

      completeNextActivity(pi, null, null);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(),
            ProcessInstanceState.Completed);
      return writeActivity;
   }

   private void completeSubProcessesInModel(final ProcessInstance pi) throws Exception
   {

      QueryService queryService = sf.getQueryService();
      String dataInput1 = "aaaa";
      String dataInput2 = "bbb";

      completeNextActivity(pi, ArchiveModelConstants.DATA_ID_TEXTDATA1, dataInput2);

      ProcessInstanceQuery querySubSimple = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLE);
      querySubSimple.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstances subProcessInstancesSimple = queryService
            .getAllProcessInstances(querySubSimple);
      assertNotNull(subProcessInstancesSimple);
      assertEquals(1, subProcessInstancesSimple.size());
      ProcessInstance subSimple = subProcessInstancesSimple.iterator().next();
      completeNextActivity(subSimple, null, null);

      ProcessInstanceQuery querySubManual = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL);
      querySubManual.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstances subProcessInstancesManual = queryService
            .getAllProcessInstances(querySubManual);
      assertNotNull(subProcessInstancesManual);
      assertEquals(1, subProcessInstancesManual.size());
      ProcessInstance subSimpleManual = subProcessInstancesManual.iterator().next();
      completeNextActivity(subSimpleManual, ArchiveModelConstants.DATA_ID_TEXTDATA,
            dataInput1);
      completeNextActivity(subSimpleManual, null, null);

      completeNextActivity(pi, null, null);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(),
            ProcessInstanceState.Completed);
   }

   private ActivityInstance completeNextActivity(final ProcessInstance pi, String dataId,
         Object data)
   {
      final ActivityInstance ai1 = sf.getQueryService().findFirstActivityInstance(
            ActivityInstanceQuery.findAlive(pi.getProcessID()));
      Map<String, Object> outData;
      if (dataId != null)
      {
         outData = new HashMap<String, Object>();
         outData.put(dataId, data);
      }
      else
      {
         outData = null;
      }
      final ActivityInstance writeActivity = sf.getWorkflowService().activateAndComplete(
            ai1.getOID(), null, outData);
      return writeActivity;

   }

   @Test
   public void testExportImportSubProcessesInModel() throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      String dataInput1 = "aaaa";
      String dataInput2 = "bbb";
      final ProcessInstance pi = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL, null, true);

      final ActivityInstance writeActivityOuter = completeNextActivity(pi,
            ArchiveModelConstants.DATA_ID_TEXTDATA1, dataInput2);

      ProcessInstanceQuery querySubSimple = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLE);
      querySubSimple.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstances subProcessInstancesSimple = queryService
            .getAllProcessInstances(querySubSimple);
      assertNotNull(subProcessInstancesSimple);
      assertEquals(1, subProcessInstancesSimple.size());
      ProcessInstance subSimple = subProcessInstancesSimple.iterator().next();
      completeNextActivity(subSimple, null, null);

      ProcessInstanceQuery querySubManual = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL);
      querySubManual.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstances subProcessInstancesManual = queryService
            .getAllProcessInstances(querySubManual);
      assertNotNull(subProcessInstancesManual);
      assertEquals(1, subProcessInstancesManual.size());
      ProcessInstance subSimpleManual = subProcessInstancesManual.iterator().next();
      ActivityInstance writeActivitySub = completeNextActivity(subSimpleManual,
            ArchiveModelConstants.DATA_ID_TEXTDATA, dataInput1);
      completeNextActivity(subSimpleManual, null, null);

      completeNextActivity(pi, null, null);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(),
            ProcessInstanceState.Completed);

      assertDataExists(subSimpleManual.getOID(), writeActivitySub.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, dataInput1);

      assertDataExists(pi.getOID(), writeActivityOuter.getOID(),
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL,
            ArchiveModelConstants.DATA_ID_TEXTDATA1, dataInput2);

      ProcessInstanceQuery pQueryRoot = new ProcessInstanceQuery();
      pQueryRoot.where(ProcessInstanceQuery.OID.isEqual(pi.getOID()));

      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      aQuery.where(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi.getOID()));
      ActivityInstanceQuery aQuerySubSimple = new ActivityInstanceQuery();
      aQuerySubSimple.where(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subSimple
            .getOID()));
      ActivityInstanceQuery aQuerySubSimpleManual = new ActivityInstanceQuery();
      aQuerySubSimpleManual.where(ActivityInstanceQuery.PROCESS_INSTANCE_OID
            .isEqual(subSimpleManual.getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQueryRoot);
      ProcessInstances oldInstancesSubSimple = queryService
            .getAllProcessInstances(querySubSimple);
      ProcessInstances oldInstancesSubManual = queryService
            .getAllProcessInstances(querySubManual);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      ActivityInstances oldActivitiesSubSimple = queryService
            .getAllActivityInstances(aQuerySubSimple);
      ActivityInstances oldActivitiesSubSimpleManual = queryService
            .getAllActivityInstances(aQuerySubSimpleManual);
      assertNotNull(oldInstances);
      assertNotNull(oldInstancesSubSimple);
      assertNotNull(oldInstancesSubManual);
      assertNotNull(oldActivities);
      assertNotNull(oldActivitiesSubSimple);
      assertNotNull(oldActivitiesSubSimpleManual);
      assertEquals(1, oldInstances.size());
      assertEquals(5, oldActivities.size());
      assertEquals(2, oldActivitiesSubSimple.size());
      assertEquals(3, oldActivitiesSubSimpleManual.size());
      assertEquals(pi.getOID(), oldInstances.get(0).getOID());
      assertNotNull(pi.getScopeProcessInstanceOID());
      assertNotNull(pi.getRootProcessInstanceOID());

      List<Long> oids = Arrays.asList(pi.getOID());
      byte[] rawData = (byte[]) workflowService.execute(new ExportProcessesCommand(oids));
      assertNotNull(rawData);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQueryRoot);
      ProcessInstances clearedInstancesSubSimple = queryService
            .getAllProcessInstances(querySubSimple);
      ProcessInstances clearedInstancesSubManual = queryService
            .getAllProcessInstances(querySubManual);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      ActivityInstances clearedActivitiesSubSimple = queryService
            .getAllActivityInstances(aQuerySubSimple);
      ActivityInstances clearedActivitiesSubSimpleManual = queryService
            .getAllActivityInstances(aQuerySubSimpleManual);
      assertNotNull(clearedInstances);
      assertNotNull(clearedInstancesSubSimple);
      assertNotNull(clearedInstancesSubManual);
      assertNotNull(clearedActivities);
      assertNotNull(clearedActivitiesSubSimple);
      assertNotNull(clearedActivitiesSubSimpleManual);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedInstancesSubSimple.size());
      assertEquals(0, clearedInstancesSubManual.size());
      assertEquals(0, clearedActivities.size());
      assertEquals(0, clearedActivitiesSubSimple.size());
      assertEquals(0, clearedActivitiesSubSimpleManual.size());
      assertDataNotExists(subSimpleManual.getOID(), writeActivitySub.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, dataInput1);

      assertDataNotExists(pi.getOID(), writeActivityOuter.getOID(),
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL,
            ArchiveModelConstants.DATA_ID_TEXTDATA1, dataInput2);

      int count = (Integer) workflowService.execute(new ImportProcessesCommand(rawData));
      assertEquals(3, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQueryRoot);
      ProcessInstances newInstancesSubSimple = queryService
            .getAllProcessInstances(querySubSimple);
      ProcessInstances newInstancesSubManual = queryService
            .getAllProcessInstances(querySubManual);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);
      ActivityInstances newActivitiesSubSimple = queryService
            .getAllActivityInstances(aQuerySubSimple);
      ActivityInstances newActivitiesSubSimpleManual = queryService
            .getAllActivityInstances(aQuerySubSimpleManual);

      assertProcessInstancesEquals(oldInstances, newInstances);
      assertProcessInstancesEquals(oldInstancesSubSimple, newInstancesSubSimple);
      assertProcessInstancesEquals(oldInstancesSubManual, newInstancesSubManual);
      assertActivityInstancesEquals(oldActivities, newActivities);
      assertActivityInstancesEquals(oldActivitiesSubSimple, newActivitiesSubSimple);
      assertActivityInstancesEquals(oldActivitiesSubSimpleManual,
            newActivitiesSubSimpleManual);
      assertDataExists(subSimpleManual.getOID(), writeActivitySub.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, dataInput1);

      assertDataExists(pi.getOID(), writeActivityOuter.getOID(),
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL,
            ArchiveModelConstants.DATA_ID_TEXTDATA1, dataInput2);

   }

   @Test
   public void testExportImportSubProcessesInModelExplicitRequestForSubProcesses()
         throws Exception
   {
      WorkflowService workflowService = sf.getWorkflowService();
      QueryService queryService = sf.getQueryService();
      String dataInput1 = "aaaa";
      String dataInput2 = "bbb";
      final ProcessInstance pi = workflowService.startProcess(
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL, null, true);

      final ActivityInstance writeActivityOuter = completeNextActivity(pi,
            ArchiveModelConstants.DATA_ID_TEXTDATA1, dataInput2);

      ProcessInstanceQuery querySubSimple = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLE);
      querySubSimple.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstances subProcessInstancesSimple = queryService
            .getAllProcessInstances(querySubSimple);
      assertNotNull(subProcessInstancesSimple);
      assertEquals(1, subProcessInstancesSimple.size());
      ProcessInstance subSimple = subProcessInstancesSimple.iterator().next();
      completeNextActivity(subSimple, null, null);

      ProcessInstanceQuery querySubManual = ProcessInstanceQuery
            .findForProcess(ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL);
      querySubManual.where(ProcessInstanceHierarchyFilter.SUB_PROCESS);
      ProcessInstances subProcessInstancesManual = queryService
            .getAllProcessInstances(querySubManual);
      assertNotNull(subProcessInstancesManual);
      assertEquals(1, subProcessInstancesManual.size());
      ProcessInstance subSimpleManual = subProcessInstancesManual.iterator().next();
      ActivityInstance writeActivitySub = completeNextActivity(subSimpleManual,
            ArchiveModelConstants.DATA_ID_TEXTDATA, dataInput1);
      completeNextActivity(subSimpleManual, null, null);

      completeNextActivity(pi, null, null);

      ProcessInstanceStateBarrier.instance().await(pi.getOID(),
            ProcessInstanceState.Completed);

      assertDataExists(subSimpleManual.getOID(), writeActivitySub.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, dataInput1);

      assertDataExists(pi.getOID(), writeActivityOuter.getOID(),
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL,
            ArchiveModelConstants.DATA_ID_TEXTDATA1, dataInput2);

      ProcessInstanceQuery pQueryRoot = new ProcessInstanceQuery();
      pQueryRoot.where(ProcessInstanceQuery.OID.isEqual(pi.getOID()));

      ActivityInstanceQuery aQuery = new ActivityInstanceQuery();
      aQuery.where(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pi.getOID()));
      ActivityInstanceQuery aQuerySubSimple = new ActivityInstanceQuery();
      aQuerySubSimple.where(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(subSimple
            .getOID()));
      ActivityInstanceQuery aQuerySubSimpleManual = new ActivityInstanceQuery();
      aQuerySubSimpleManual.where(ActivityInstanceQuery.PROCESS_INSTANCE_OID
            .isEqual(subSimpleManual.getOID()));

      ProcessInstances oldInstances = queryService.getAllProcessInstances(pQueryRoot);
      ProcessInstances oldInstancesSubSimple = queryService
            .getAllProcessInstances(querySubSimple);
      ProcessInstances oldInstancesSubManual = queryService
            .getAllProcessInstances(querySubManual);
      ActivityInstances oldActivities = queryService.getAllActivityInstances(aQuery);
      ActivityInstances oldActivitiesSubSimple = queryService
            .getAllActivityInstances(aQuerySubSimple);
      ActivityInstances oldActivitiesSubSimpleManual = queryService
            .getAllActivityInstances(aQuerySubSimpleManual);
      assertNotNull(oldInstances);
      assertNotNull(oldInstancesSubSimple);
      assertNotNull(oldInstancesSubManual);
      assertNotNull(oldActivities);
      assertNotNull(oldActivitiesSubSimple);
      assertNotNull(oldActivitiesSubSimpleManual);
      assertEquals(1, oldInstances.size());
      assertEquals(5, oldActivities.size());
      assertEquals(2, oldActivitiesSubSimple.size());
      assertEquals(3, oldActivitiesSubSimpleManual.size());
      assertEquals(pi.getOID(), oldInstances.get(0).getOID());
      assertNotNull(pi.getScopeProcessInstanceOID());
      assertNotNull(pi.getRootProcessInstanceOID());

      List<Long> oids = Arrays.asList(pi.getOID(), subSimple.getOID(),
            subSimpleManual.getOID());
      byte[] rawData = (byte[]) workflowService.execute(new ExportProcessesCommand(oids));
      assertNotNull(rawData);

      ProcessInstances clearedInstances = queryService.getAllProcessInstances(pQueryRoot);
      ProcessInstances clearedInstancesSubSimple = queryService
            .getAllProcessInstances(querySubSimple);
      ProcessInstances clearedInstancesSubManual = queryService
            .getAllProcessInstances(querySubManual);
      ActivityInstances clearedActivities = queryService.getAllActivityInstances(aQuery);
      ActivityInstances clearedActivitiesSubSimple = queryService
            .getAllActivityInstances(aQuerySubSimple);
      ActivityInstances clearedActivitiesSubSimpleManual = queryService
            .getAllActivityInstances(aQuerySubSimpleManual);
      assertNotNull(clearedInstances);
      assertNotNull(clearedInstancesSubSimple);
      assertNotNull(clearedInstancesSubManual);
      assertNotNull(clearedActivities);
      assertNotNull(clearedActivitiesSubSimple);
      assertNotNull(clearedActivitiesSubSimpleManual);
      assertEquals(0, clearedInstances.size());
      assertEquals(0, clearedInstancesSubSimple.size());
      assertEquals(0, clearedInstancesSubManual.size());
      assertEquals(0, clearedActivities.size());
      assertEquals(0, clearedActivitiesSubSimple.size());
      assertEquals(0, clearedActivitiesSubSimpleManual.size());
      assertDataNotExists(subSimpleManual.getOID(), writeActivitySub.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, dataInput1);

      assertDataNotExists(pi.getOID(), writeActivityOuter.getOID(),
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL,
            ArchiveModelConstants.DATA_ID_TEXTDATA1, dataInput2);

      int count = (Integer) workflowService.execute(new ImportProcessesCommand(rawData));
      assertEquals(3, count);
      ProcessInstances newInstances = queryService.getAllProcessInstances(pQueryRoot);
      ProcessInstances newInstancesSubSimple = queryService
            .getAllProcessInstances(querySubSimple);
      ProcessInstances newInstancesSubManual = queryService
            .getAllProcessInstances(querySubManual);
      ActivityInstances newActivities = queryService.getAllActivityInstances(aQuery);
      ActivityInstances newActivitiesSubSimple = queryService
            .getAllActivityInstances(aQuerySubSimple);
      ActivityInstances newActivitiesSubSimpleManual = queryService
            .getAllActivityInstances(aQuerySubSimpleManual);

      assertProcessInstancesEquals(oldInstances, newInstances);
      assertProcessInstancesEquals(oldInstancesSubSimple, newInstancesSubSimple);
      assertProcessInstancesEquals(oldInstancesSubManual, newInstancesSubManual);
      assertActivityInstancesEquals(oldActivities, newActivities);
      assertActivityInstancesEquals(oldActivitiesSubSimple, newActivitiesSubSimple);
      assertActivityInstancesEquals(oldActivitiesSubSimpleManual,
            newActivitiesSubSimpleManual);
      assertDataExists(subSimpleManual.getOID(), writeActivitySub.getOID(),
            ArchiveModelConstants.PROCESS_DEF_SIMPLEMANUAL,
            ArchiveModelConstants.DATA_ID_TEXTDATA, dataInput1);

      assertDataExists(pi.getOID(), writeActivityOuter.getOID(),
            ArchiveModelConstants.PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL,
            ArchiveModelConstants.DATA_ID_TEXTDATA1, dataInput2);

   }


   
   private boolean hasStructuredDateField(long processInstanceOid, String dataId, String fieldName, Object value) throws Exception
   {
      String sql = "select cd.stringvalue from data d inner join data_value dv on d.oid = dv.data"
               + " inner join clob_data cd on dv.number_value = cd.oid"
               + " where d.id = ?"
               + " and dv.processinstance = ?";
      
      final DataSource ds = testClassSetup.dataSource();
      final boolean result;

      Connection connection = null;
      PreparedStatement stmt = null;
      try
      {
         connection = ds.getConnection();
         stmt = connection.prepareStatement(sql);
         stmt.setString(1, dataId);
         stmt.setLong(2, processInstanceOid);
         final ResultSet rs = stmt.executeQuery();
         if (rs.next()) {
            result = rs.getString(1).contains(">" + value + "</" + fieldName + ">");
         } else {
            result = false;
         }
      }
      finally
      {
         if (stmt != null)
         {
            stmt.close();
         }
         if (connection != null)
         {
            connection.close();
         }
      }

      return result;
   }
   
   private boolean hasEntryInDbForObject(final String tableName, String fieldName,
         final long id) throws SQLException
   {
      final DataSource ds = testClassSetup.dataSource();
      final boolean result;

      Connection connection = null;
      Statement stmt = null;
      try
      {
         connection = ds.getConnection();
         stmt = connection.createStatement();
         final ResultSet rs = stmt.executeQuery("SELECT * FROM PUBLIC." + tableName
               + " WHERE " + fieldName + " = " + id);
         result = rs.first();
      }
      finally
      {
         if (stmt != null)
         {
            stmt.close();
         }
         if (connection != null)
         {
            connection.close();
         }
      }

      return result;
   }

   private void assertDataExists(long processInstanceOid, long activityOid,
         String processName, String dataId, Serializable expectedValue)
   {
      checkDataValue(processInstanceOid, activityOid, processName, dataId, expectedValue,
            true);
   }

   private void assertDataNotExists(long processInstanceOid, long activityOid,
         String processName, String dataId, Serializable expectedValue)
   {
      checkDataValue(processInstanceOid, activityOid, processName, dataId, expectedValue,
            false);
   }

   private void checkDataValue(long processInstanceOid, long activityOid,
         String processName, String dataId, Serializable expectedValue, boolean shouldExists)
   {
      QueryService queryService = sf.getQueryService();

      ActivityInstances ais = queryService.getAllActivityInstances(ActivityInstanceQuery
            .findInStateHavingData(processName, dataId, expectedValue,
                  ActivityInstanceState.Completed));

      boolean found = false;
      for (int i = 0; i < ais.getSize(); i++)
      {
         ActivityInstance ai = ais.get(i);
         if (ai.getOID() == activityOid)
         {
            found = true;
         }
      }

      if (shouldExists)
      {
         StringBuffer errorMsg = new StringBuffer();
         errorMsg.append("No matching data found for process isntance oid '");
         errorMsg.append(processInstanceOid);
         errorMsg.append("' and value '");
         errorMsg.append(expectedValue);
         errorMsg.append("'");

         assertTrue(errorMsg.toString(), found);
      }
      else
      {
         StringBuffer errorMsg = new StringBuffer();
         errorMsg.append("Data found for process isntance oid '");
         errorMsg.append(processInstanceOid);
         errorMsg.append("' and value '");
         errorMsg.append(expectedValue);
         errorMsg.append("' but should not exist");

         assertFalse(errorMsg.toString(), found);
      }
   }

   private void assertProcessInstancesEquals(ProcessInstances oldInstances,
         ProcessInstances newInstances) throws Exception
   {
      int countCompared = 0;
      assertNotNull(newInstances);
      assertEquals(oldInstances.size(), newInstances.size());
      for (ProcessInstance process : oldInstances)
      {
         assertThat(
               NL + testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB,
               hasEntryInDbForObject("PROCINST_SCOPE", "SCOPEPROCESSINSTANCE",
                     process.getScopeProcessInstanceOID()), is(true));
         assertThat(
               NL + testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB,
               hasEntryInDbForObject("PROCINST_SCOPE", "ROOTPROCESSINSTANCE",
                     process.getRootProcessInstanceOID()), is(true));
         for (ProcessInstance newProcess : newInstances)
         {
            if (process.getOID() == newProcess.getOID())
            {
               assertObjectEquals(process, newProcess, process);
               countCompared++;
               break;
            }
         }
      }
      assertEquals(oldInstances.size(), countCompared);
   }

   private void assertActivityInstancesEquals(ActivityInstances oldActivities,
         ActivityInstances newActivities) throws Exception
   {
      int countCompared = 0;
      assertNotNull(newActivities);
      assertEquals(oldActivities.size(), newActivities.size());
      for (ActivityInstance activity : oldActivities)
      {
         assertThat(
               NL + testMethodSetup.testMethodName() + ASSERTION_MSG_HAS_ENTRY_IN_DB,
               hasEntryInDbForObject("ACTIVITY_INSTANCE", "OID", activity.getOID()),
               is(true));
         for (ActivityInstance newActivity : newActivities)
         {
            if (activity.getOID() == newActivity.getOID())
            {
               assertObjectEquals(activity, newActivity, activity);
               countCompared++;
               break;
            }
         }
      }
      assertEquals(oldActivities.size(), countCompared);
   }

   private void assertObjectEquals(Object a, Object b, Object from) throws Exception
   {
      if (a == null && b == null)
      {
         return;
      }
      if (a == null && b != null)
      {
         fail("Original object was null but imported object is not: " + b);
         return;
      }
      if (a != null && b == null)
      {
         fail("Original object was not null but imported object is null: " + a);
         return;
      }
      BeanInfo beanInfo = Introspector.getBeanInfo(a.getClass());

      for (PropertyDescriptor property : beanInfo.getPropertyDescriptors())
      {
         Object valueA = property.getReadMethod().invoke(a);
         Object valueB = property.getReadMethod().invoke(b);
         if (property.getPropertyType().getPackage() != null
               && property.getPropertyType().getPackage().getName()
                     .contains("org.eclipse.stardust.engine"))
         {
            if (from.equals(valueA))
            {
               return;
            }
            assertObjectEquals(valueA, valueB, valueA);
         }
         else
         {
            if (property.getPropertyType().isArray())
            {
               assertArrayEquals(a.getClass().getSimpleName() + " " + property.getName()
                     + " not equals. Expected " + valueA + " but got " + valueB,
                     (Object[]) valueA, (Object[]) valueB);
            }
            else
            {
               assertEquals(a.getClass().getSimpleName() + " " + property.getName()
                     + " not equals. Expected " + valueA + " but got " + valueB, valueA,
                     valueB);
            }
         }
      }
   }
   
   @SuppressWarnings("unused")
   private void assertObjectEqualsAlt(Object a, Object b, Object from) throws Exception
   {
      if (a == null && b == null)
      {
         return;
      }
      if (a == null && b != null)
      {
         fail("Original object was null but imported object is not: " + b);
         return;
      }
      if (a != null && b == null)
      {
         fail("Original object was not null but imported object is null: " + a);
         return;
      }

      Method[] methods = a.getClass().getMethods();
      for (Method method : methods)
      {
         if (method.getParameterTypes().length == 0 && method.getName().startsWith("get"))
         {
            if (method.getReturnType() != null)
            {
               Object valueA = method.invoke(a);
               Object valueB = method.invoke(b);

               if (method.getReturnType().getPackage() != null
                     && method.getReturnType().getPackage().getName()
                           .contains("org.eclipse.stardust.engine"))
               {
                  if (from.equals(valueA))
                  {
                     return;
                  }
                  assertObjectEquals(valueA, valueB, valueA);
               }
               else
               {
                  if (method.getReturnType().isArray())
                  {
                     assertArrayEquals(method.getName() + " not equals. Expected "
                           + valueA + " but got " + valueB, (Object[]) valueA,
                           (Object[]) valueB);
                  }
                  else
                  {
                     assertEquals(method.getName() + " not equals. Expected " + valueA
                           + " but got " + valueB, valueA, valueB);
                  }
               }
            }
         }

      }

   }
}
