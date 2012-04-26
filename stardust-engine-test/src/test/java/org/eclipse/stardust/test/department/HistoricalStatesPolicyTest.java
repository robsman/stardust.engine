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
package org.eclipse.stardust.test.department;

import static org.eclipse.stardust.test.department.DepartmentModelConstants.*;
import static org.eclipse.stardust.test.util.TestConstants.MOTU;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.engine.api.dto.HistoricalState;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.query.ActivityInstanceFilter;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.HistoricalStatesPolicy;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.test.api.setup.ClientServiceFactory;
import org.eclipse.stardust.test.api.setup.LocalJcrH2Test;
import org.eclipse.stardust.test.api.setup.RuntimeConfigurer;
import org.eclipse.stardust.test.api.util.ActivityInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.DepartmentHome;
import org.eclipse.stardust.test.api.util.UserHome;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

/**
 * <p>
 * Tests whether information about former departments may be retrieved from
 * historical state.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class HistoricalStatesPolicyTest
{
   private static final String USER_NAME = "u1";
   private static final String USER_PWD = "u1";
   
   private Organization org;

   private Department deptDe;

   private Department deptEn;

   private ModelParticipantInfo orgDe;

   private ModelParticipantInfo orgEn;

   private ActivityInstance ai;

   private final ClientServiceFactory adminSf = new ClientServiceFactory(MOTU, MOTU);
   private final RuntimeConfigurer rtConfigurer = new RuntimeConfigurer(adminSf, MODEL_NAME);
   private final ClientServiceFactory userSf = new ClientServiceFactory(USER_NAME, USER_PWD);

   @ClassRule
   public static LocalJcrH2Test testSetup = new LocalJcrH2Test();
   
   @Rule
   public TestRule chain = RuleChain.outerRule(adminSf)
                                    .around(rtConfigurer)
                                    .around(userSf);
   
   @Before
   public void setUp() throws Exception
   {
      createUser();
      final long piOid = startProcess();
      ActivityInstanceStateBarrier.instance().awaitAliveActivityInstance(piOid);
      ai = adminSf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findAlive());
   }

   /**
    * <p>
    * A query that specifies that historical state should be retrieved
    * should contain all historical state informations (including ones
    * for departments).
    * </p>
    */
   @Test
   public void testWithHistoricalStatesPolicy()
   {
      delegateToEnAndBack();

      final ActivityInstanceQuery query = createQueryWithPolicy(HistoricalStatesPolicy.WITH_HIST_STATES);
      ai = adminSf.getQueryService().findFirstActivityInstance(query);

      final List<HistoricalState> states = ai.getHistoricalStates();

      Assert.assertEquals(3, states.size());
      Assert.assertEquals(deptDe.getOID(), getDepartmentOidFor(states.get(0)));
      Assert.assertEquals(deptEn.getOID(), getDepartmentOidFor(states.get(1)));
      Assert.assertEquals(deptDe.getOID(), getDepartmentOidFor(states.get(2)));
   }

   /**
    * <p>
    * Tests whether the 'onBehalfOf' information is correct in the
    * activity instances' history after first delegating the activity
    * instance to other departments and then delegating it to a user,
    * i.e. 'onBehalfOf' should still point to the last department after
    * delegating to a user.
    * </p>
    */
   @Test
   public void testWithHistoricalStatesPolicyOnBehalfOf()
   {
      delegateToEnAndBack();
      delegateToUser();

      final ActivityInstanceQuery query = createQueryWithPolicy(HistoricalStatesPolicy.WITH_HIST_STATES);
      ai = adminSf.getQueryService().findFirstActivityInstance(query);

      final List<HistoricalState> states = ai.getHistoricalStates();

      Assert.assertEquals(4, states.size());
      Assert.assertEquals(deptDe.getOID(), getOnBehalfOfDepartmentOidFor(states.get(0)));
      Assert.assertEquals(deptDe.getOID(), getOnBehalfOfDepartmentOidFor(states.get(1)));
      Assert.assertEquals(deptEn.getOID(), getOnBehalfOfDepartmentOidFor(states.get(2)));
      Assert.assertEquals(deptDe.getOID(), getOnBehalfOfDepartmentOidFor(states.get(3)));
   }

   /**
    * <p>
    * A query that specifies that no historical state should be retrieved should
    * not contain historical state at all.
    * </p>
    */
   @Test
   public void testWithoutHistoricalStatesPolicy()
   {
      delegateToEnAndBack();

      final ActivityInstanceQuery query = createQueryWithPolicy(HistoricalStatesPolicy.NO_HIST_STATES);
      ai = adminSf.getQueryService().findFirstActivityInstance(query);

      Assert.assertTrue(ai.getHistoricalStates().isEmpty());
   }
   
   private void createUser()
   {
      final Model model = adminSf.getQueryService().getActiveModel();

      org = model.getOrganization(ORG_ID_1);
      deptDe = DepartmentHome.create(adminSf, DEPT_ID_DE, ORG_ID_1, null);
      deptEn = DepartmentHome.create(adminSf, DEPT_ID_EN, ORG_ID_1, null);

      orgDe = deptDe.getScopedParticipant(org);
      orgEn = deptEn.getScopedParticipant(org);

      UserHome.create(adminSf, USER_NAME, orgDe, orgEn);
   }

   private long startProcess()
   {
      /* start process in scope (DE) */
      final Map<String, String> ccData = Collections.singletonMap(COUNTRY_CODE_DATA_NAME, DEPT_ID_DE);
      final ProcessInstance pi = userSf.getWorkflowService().startProcess(PROCESS_ID_1, ccData, true);
      return pi.getOID();
   }

   /**
    * CountryCode&lt;DE&gt; -> CountryCode&lt;EN&gt; -> CountryCode&lt;DE&gt;
    */
   private void delegateToEnAndBack()
   {
      adminSf.getWorkflowService().delegateToParticipant(ai.getOID(), orgEn);
      adminSf.getWorkflowService().delegateToParticipant(ai.getOID(), orgDe);
   }

   private void delegateToUser()
   {
      final User user = userSf.getWorkflowService().getUser();
      userSf.getWorkflowService().delegateToUser(ai.getOID(), user.getOID());
   }

   private ActivityInstanceQuery createQueryWithPolicy(final HistoricalStatesPolicy policy)
   {
      final ActivityInstanceQuery query = new ActivityInstanceQuery();
      query.getFilter().add(new ActivityInstanceFilter(ai.getOID()));
      query.setPolicy(policy);
      return query;
   }

   private long getDepartmentOidFor(final HistoricalState state)
   {
      final ParticipantInfo participant = state.getParticipant();
      if (!(participant instanceof ModelParticipantInfo))
      {
         fail("Participant must be of type 'ModelParticipantInfo'.");
      }
      final DepartmentInfo deptInfo = ((ModelParticipantInfo) participant).getDepartment();
      assertNotNull("Department must not be null.", deptInfo);
      return deptInfo.getOID();
   }

   private long getOnBehalfOfDepartmentOidFor(final HistoricalState state)
   {
      final ParticipantInfo participant = state.getOnBehalfOfParticipant();
      if (!(participant instanceof ModelParticipantInfo))
      {
         fail("Participant must be of type 'ModelParticipantInfo'.");
      }
      final DepartmentInfo deptInfo = ((ModelParticipantInfo) participant).getDepartment();
      assertNotNull("Department must not be null.", deptInfo);
      return deptInfo.getOID();
   }
}
