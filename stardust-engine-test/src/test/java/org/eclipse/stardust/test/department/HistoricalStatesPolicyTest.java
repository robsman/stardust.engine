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
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.query.HistoricalStatesPolicy;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.test.api.ClientServiceFactory;
import org.eclipse.stardust.test.api.LocalJcrH2Test;
import org.eclipse.stardust.test.api.RuntimeConfigurer;
import org.junit.Assert;
import org.junit.Before;
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
 * @version $Revision: $
 */
public class HistoricalStatesPolicyTest extends LocalJcrH2Test
{
   private static final String USER_NAME = "u1";
   private static final String USER_PWD = "u1";
   
   private Organization org;

   private Department depDe;

   private Department depEn;

   private ModelParticipantInfo orgDe;

   private ModelParticipantInfo orgEn;

   private ActivityInstance ai;

   private final ClientServiceFactory adminSf = new ClientServiceFactory(MOTU, MOTU);
   private final RuntimeConfigurer rtConfigurer = new RuntimeConfigurer(MODEL_NAME, adminSf);
   private final ClientServiceFactory userSf = new ClientServiceFactory(USER_NAME, USER_PWD);

   @Rule
   public TestRule chain = RuleChain.outerRule(adminSf)
                                    .around(rtConfigurer)
                                    .around(userSf);
   
   @Before
   public void setUp()
   {
      createUser();
      startProcess();
      waitForFirstActivityInstanceToGetAlive();
      ai = adminSf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findAlive());
   }

   /**
    * A query that specifies that historical state should be retrieved
    * should contain all historical state informations (including ones
    * for departments).
    */
   @Test
   public void testWithHistoricalStatesPolicy()
   {
      delegateToEnAndBack();

      final ActivityInstanceQuery query = createQueryWithPolicy(HistoricalStatesPolicy.WITH_HIST_STATES);
      ai = adminSf.getQueryService().findFirstActivityInstance(query);

      final List<HistoricalState> states = ai.getHistoricalStates();

      Assert.assertEquals(3, states.size());
      Assert.assertEquals(depDe.getOID(), getDepartmentOidFor(states.get(0)));
      Assert.assertEquals(depEn.getOID(), getDepartmentOidFor(states.get(1)));
      Assert.assertEquals(depDe.getOID(), getDepartmentOidFor(states.get(2)));
   }

   /**
    * Tests whether the 'onBehalfOf' information is correct in the
    * activity instances' history after first delegating the activity
    * instance to other departments and then delegating it to a user,
    * i.e. 'onBehalfOf' should still point to the last department after
    * delegating to a user.
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
      Assert.assertEquals(depDe.getOID(), getOnBehalfOfDepartmentOidFor(states.get(0)));
      Assert.assertEquals(depDe.getOID(), getOnBehalfOfDepartmentOidFor(states.get(1)));
      Assert.assertEquals(depEn.getOID(), getOnBehalfOfDepartmentOidFor(states.get(2)));
      Assert.assertEquals(depDe.getOID(), getOnBehalfOfDepartmentOidFor(states.get(3)));
   }

   /**
    * A query that specifies that no historical state should be retrieved should
    * not contain historical state at all.
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
      depDe = adminSf.getAdministrationService().createDepartment(DEP_ID_DE, DEP_ID_DE, null, null, org);
      depEn = adminSf.getAdministrationService().createDepartment(DEP_ID_EN, DEP_ID_EN, null, null, org);

      orgDe = depDe.getScopedParticipant(org);
      orgEn = depEn.getScopedParticipant(org);

      final User user = adminSf.getUserService().createUser(USER_NAME, USER_NAME, USER_NAME, USER_NAME, USER_PWD, null, null, null);
      user.addGrant(orgDe);
      user.addGrant(orgEn);
      adminSf.getUserService().modifyUser(user);
   }

   private void startProcess()
   {
      /* start process in scope (DE) */
      final Map<String, String> ccData = Collections.singletonMap(COUNTRY_CODE_DATA_NAME, DEP_ID_DE);
      userSf.getWorkflowService().startProcess(PROCESS_ID_1, ccData, true);
   }

   private void waitForFirstActivityInstanceToGetAlive()
   {
      boolean isAlive;
      do
      {
         final ActivityInstances ais = adminSf.getQueryService().getAllActivityInstances(ActivityInstanceQuery.findAlive());
         isAlive = ais.isEmpty() ? false : true;
      }
      while (!isAlive);
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
         Assert.fail("Participant must be of type 'ModelParticipantInfo'.");
      }
      final DepartmentInfo depInfo = ((ModelParticipantInfo) participant).getDepartment();
      Assert.assertNotNull("department must not be null", depInfo);
      return depInfo.getOID();
   }

   private long getOnBehalfOfDepartmentOidFor(final HistoricalState state)
   {
      final ParticipantInfo participant = state.getOnBehalfOfParticipant();
      if (!(participant instanceof ModelParticipantInfo))
      {
         Assert.fail("Participant must be of type 'ModelParticipantInfo'.");
      }
      final DepartmentInfo depInfo = ((ModelParticipantInfo) participant).getDepartment();
      Assert.assertNotNull("department must not be null", depInfo);
      return depInfo.getOID();
   }
}
