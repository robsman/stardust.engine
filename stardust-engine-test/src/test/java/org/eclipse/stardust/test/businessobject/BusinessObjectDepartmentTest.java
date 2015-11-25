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
package org.eclipse.stardust.test.businessobject;

import static org.eclipse.stardust.engine.api.model.PredefinedConstants.PREDEFINED_MODEL_ID;
import static org.eclipse.stardust.engine.api.query.DeployedModelQuery.findForId;
import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.junit.*;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runners.MethodSorters;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.query.BusinessObjectQuery;
import org.eclipse.stardust.engine.api.query.BusinessObjects;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.test.api.setup.*;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.util.UserHome;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

/**
 * <p>
 * This class tests BO declarative security department restriction functionality.
 * <p>
 *
 * @author Roland.Stamm
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BusinessObjectDepartmentTest
{
   public static final String MODEL_NAME = "BOTestModel";

   public static final String CLIENT_BO = new QName(MODEL_NAME, "Client").toString();

   public static final String CLIENT_GROUP_BO = new QName(MODEL_NAME, "ClientGroup").toString();

   public static final String CLIENT_ORG = new QName(MODEL_NAME, "Client_Admin").toString();

   public static final String U1 = "u1";

   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(
         MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(
         ADMIN_USER_PWD_PAIR, testClassSetup);

   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(
         ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_NAME);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup).around(sf);


   /**
    * Tests visibility of department scope restricted and propagated related business objects.
    * For Administrator Role.
    */
   @Test
   public void test01AdministratorAccess()
   {
      createClient("c1", "cg1");
      createClientGroup("cg1", "c1");
      createClient("c2", null);
      createClientGroup("cg2", null);

      UserHome.create(sf, U1, ModelParticipantInfo.ADMINISTRATOR);

      assertValues(CLIENT_BO, 2);
      assertValues(CLIENT_GROUP_BO, 2);
   }

   /**
    * Tests visibility of department scope restricted and propagated related business objects.
    * For Auditor Role.
    */
   @Test
   public void test02AuditorAccess()
   {
      createClient("c1", "cg1");
      createClientGroup("cg1", "c1");
      createClient("c2", null);
      createClientGroup("cg2", null);

      UserHome.create(sf, U1, getAuditorRole());

      assertValues(CLIENT_BO, 2);
      assertValues(CLIENT_GROUP_BO, 2);
   }


   /**
    * Tests visibility of department scope restricted business objects.
    */
   @Test
   public void test03DepartmentRestrictionQuery()
   {
      createClient("c1", null);
      createClient("c2", null);

      UserHome.create(sf, U1, getScopedOrg(CLIENT_ORG, "c1"));

      assertValues(CLIENT_BO, 1);
   }


   /**
    * Tests department restriction on create.
    */
   @Test
   public void test04DepartmentRestrictionCreate()
   {
      UserHome.create(sf, U1, getScopedOrg(CLIENT_ORG, "c1"));

      createClientU1("c1", null);

      boolean ex = false;
      try
      {
         // should throw AccessForbiddenException
         createClientU1("c2", null);
      }
      catch (AccessForbiddenException e)
      {
         ex = true;
      }

      assertValues(CLIENT_BO, 1);

      if (!ex)
      {
         Assert.fail();
      }

   }

   /**
    * Tests department restriction on update.
    */
   @Test
   @Ignore
   public void test05DepartmentRestrictionUpdate()
   {
//      createClient("c1", null);
//      createClient("c2", null);
//
//      UserHome.create(sf, U1, getScopedOrg(CLIENT_ORG, "c1"));
//
//      assertValues(CLIENT_BO, 1);
   }

   /**
    * Tests department restriction on delete.
    */
   @Test
   @Ignore
   public void test06DepartmentRestrictionDelete()
   {
//      createClient("c1", null);
//      createClient("c2", null);
//
//      UserHome.create(sf, U1, getScopedOrg(CLIENT_ORG, "c1"));
//
//      assertValues(CLIENT_BO, 1);
   }

   /**
    * Tests visibility of business object with propagated access.
    */
   @Test
   public void test10DepartmentPropagation()
   {
      createClient("c1", "cg1");
      createClientGroup("cg1", "c1");
      createClient("c2", null);
      createClientGroup("cg2", null);

      UserHome.create(sf, U1, getScopedOrg(CLIENT_ORG, "c1"));

      assertValues(CLIENT_GROUP_BO, 1);
   }

   private ModelParticipantInfo getAuditorRole()
   {
      QueryService queryService = sf.getQueryService();
      Models models = queryService.getModels(findForId(PREDEFINED_MODEL_ID));
      Participant auditorParticipant = queryService.getParticipant(models.get(0)
            .getModelOID(), PredefinedConstants.AUDITOR_ROLE);
      Assert.assertNotNull(auditorParticipant);
      return (ModelParticipantInfo) auditorParticipant;
   }

   private void assertValues(String businessObjectId, int expectedCount)
   {
      ServiceFactory u1Sf = ServiceFactoryLocator.get(U1, U1);

      BusinessObjectQuery query = BusinessObjectQuery.findForBusinessObject(businessObjectId);
      query.setPolicy(new BusinessObjectQuery.Policy(
            BusinessObjectQuery.Option.WITH_VALUES));
      BusinessObjects bos = u1Sf.getQueryService().getAllBusinessObjects(query);

      Assert.assertEquals("Query Result BOs", 1, bos.getSize());
      BusinessObject bo = bos.get(0);
      List<BusinessObject.Value> values = bo.getValues();
      Assert.assertEquals("Values", expectedCount, values.size());

      u1Sf.close();
   }

   private ModelParticipantInfo getScopedOrg(String organizationId, String departmentId)
   {
      Organization org = getOrg(organizationId);

      Department dep;
      try
      {
         dep = sf.getQueryService().findDepartment(null, departmentId, org);
      }
      catch (ObjectNotFoundException e)
      {
         dep = sf.getAdministrationService().createDepartment(departmentId, departmentId,
               null, null, org);
      }

      return dep.getScopedParticipant(org);
   }

   private Organization getOrg(String organizationId)
   {
     return (Organization) sf.getQueryService().getParticipant(organizationId);
   }

   private BusinessObject createClient(String id, String ref)
   {
      return createClient(this.sf, id, ref);
   }

   private BusinessObject createClientU1(String id, String ref)
   {
      ServiceFactory u1Sf = ServiceFactoryLocator.get(U1, U1);
      try
      {
         return createClient(u1Sf, id, ref);
      }
      finally
      {
         u1Sf.close();
      }
   }

   private BusinessObject createClient(ServiceFactory sf, String id, String ref)
   {
      final Map<String, Object> client = CollectionUtils.newMap();
      client.put("ClientId", id);
      client.put("ClientName", id);
      client.put("ClientGroup", ref);

      return sf.getWorkflowService().createBusinessObjectInstance(CLIENT_BO,
            (Serializable) client);
   }

   private BusinessObject createClientGroup(String id, String ref)
   {
      final Map<String, Object> client = CollectionUtils.newMap();
      client.put("ClientGroupId", id);
      client.put("ClientRef", ref);

      return sf.getWorkflowService().createBusinessObjectInstance(CLIENT_GROUP_BO,
            (Serializable) client);
   }
}