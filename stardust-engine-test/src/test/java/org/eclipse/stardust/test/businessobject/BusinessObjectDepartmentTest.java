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

   public static final String BO_ADMIN_ROLE = new QName(MODEL_NAME, "BO_Admin").toString();

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
    * Tests visibility of department scope restricted and propagated related business
    * objects for Administrator Role.
    */
   @Test
   public void test01AdministratorAccess()
   {
      // create
      UserHome.create(sf, U1, ModelParticipantInfo.ADMINISTRATOR);

      createClientU1("c1", "cg1");
      createClientGroupU1("cg1", "c1");
      createClientU1("c2", null);
      createClientGroupU1("cg2", null);

      // read
      assertValuesU1(CLIENT_BO, 2);
      assertValuesU1(CLIENT_GROUP_BO, 2);

      // update
      updateClientU1("c1", "c2");
      updateClientU1("c2", "c1");

      // delete
      deleteClientU1("c1");
      deleteClientU1("c2");

      // read
      assertValuesU1(CLIENT_BO, 0);
      assertValuesU1(CLIENT_GROUP_BO, 2);

   }

   /**
    * Tests visibility of department scope restricted and propagated related business
    * objects for Auditor Role.
    */
   @Test
   public void test02AuditorAccess()
   {
      createClient("c1", "cg1");
      createClientGroup("cg1", "c1");
      createClient("c2", null);
      createClientGroup("cg2", null);

      UserHome.create(sf, U1, getAuditorRole());

      // read as Auditor
      assertValuesU1(CLIENT_BO, 2);
      assertValuesU1(CLIENT_GROUP_BO, 2);
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

      assertValuesU1(CLIENT_BO, 1);
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

      assertValuesU1(CLIENT_BO, 1);

      if (!ex)
      {
         Assert.fail();
      }

   }

   /**
    * Tests department restriction on update. In case the access to the department of the
    * to be updated business object instance is not granted.
    */
   @Test
   public void test05DepartmentRestrictionUpdateOldValueDenied()
   {
      UserHome.create(sf, U1, getScopedOrg(CLIENT_ORG, "c1"));

      createClient("c1", null);
      createClient("c2", null);

      updateClientU1("c1", "c1");
      boolean ex = false;
      try
      {
         // should throw AccessForbiddenException because c2 as old value is not granted.
         updateClientU1("c2", "c1");
      }
      catch (AccessForbiddenException e)
      {
         ex = true;
      }

      assertValuesU1(CLIENT_BO, 1);

      if (!ex)
      {
         Assert.fail();
      }
   }

   /**
    * Tests department restriction on update. In case the access to the department of the
    * to be used new value is not granted.
    */
   @Test
   public void test06DepartmentRestrictionUpdateNewValueDenied()
   {
      UserHome.create(sf, U1, getScopedOrg(CLIENT_ORG, "c1"));

      createClient("c1", null);
      boolean ex = false;
      try
      {
         // should throw AccessForbiddenException because c2 as new value is not granted
         updateClientU1("c1", "c2");
      }
      catch (AccessForbiddenException e)
      {
         ex = true;
      }

      assertValuesU1(CLIENT_BO, 1);

      if (!ex)
      {
         Assert.fail();
      }
   }

   /**
    * Tests department restriction on delete.
    */
   @Test
   public void test07DepartmentRestrictionDelete()
   {
      UserHome.create(sf, U1, getScopedOrg(CLIENT_ORG, "c1"));
      createClient("c1", null);
      createClient("c2", null);

      assertValuesU1(CLIENT_BO, 1);

      deleteClientU1("c1");

      boolean ex = false;
      try
      {
         // should throw AccessForbiddenException
         deleteClientU1("c2");
      }
      catch (AccessForbiddenException e)
      {
         ex = true;
      }

      if (!ex)
      {
         Assert.fail();
      }
   }

   /**
    * Tests visibility of department scope restricted and propagated related business
    * objects for BO_Admin (non scoped role which has modify and read data permissions on Client and ClientGroup).
    */
   @Test
   public void test08NonScopedRoleAccess()
   {
      // create
      UserHome.create(sf, U1, getUnscopedParticipant(BO_ADMIN_ROLE));

      createClientU1("c1", "cg1");
      createClientU1("c2", null);

      createClientGroupU1("cg1", "c1");
      createClientGroupU1("cg2", null);

      // read
      assertValuesU1(CLIENT_BO, 2);
      assertValuesU1(CLIENT_GROUP_BO, 2);

      // update
      updateClientU1("c1", "c2");
      updateClientU1("c2", "c1");

      // delete
      deleteClientU1("c1");
      deleteClientU1("c2");

      // read
      assertValuesU1(CLIENT_BO, 0);
      assertValuesU1(CLIENT_GROUP_BO, 2);

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

      assertValuesU1(CLIENT_GROUP_BO, 1);
   }


   //************************** UTILITY ***************************************

   private ModelParticipantInfo getAuditorRole()
   {
      QueryService queryService = sf.getQueryService();
      Models models = queryService.getModels(findForId(PREDEFINED_MODEL_ID));
      Participant auditorParticipant = queryService.getParticipant(models.get(0)
            .getModelOID(), PredefinedConstants.AUDITOR_ROLE);
      Assert.assertNotNull(auditorParticipant);
      return (ModelParticipantInfo) auditorParticipant;
   }

   private ModelParticipantInfo getUnscopedParticipant(String qualifiedModelParticipantId)
   {
      QueryService queryService = sf.getQueryService();
      Participant auditorParticipant = queryService.getParticipant(qualifiedModelParticipantId);
      Assert.assertNotNull(auditorParticipant);
      return (ModelParticipantInfo) auditorParticipant;
   }

   private void assertValuesU1(String businessObjectId, int expectedCount)
   {
      ServiceFactory u1Sf = ServiceFactoryLocator.get(U1, U1);

      BusinessObjectQuery query = BusinessObjectQuery.findForBusinessObject(businessObjectId);
      query.setPolicy(new BusinessObjectQuery.Policy(
            BusinessObjectQuery.Option.WITH_VALUES));
      BusinessObjects bos = u1Sf.getQueryService().getAllBusinessObjects(query);

      if (expectedCount > 0)
      {
         Assert.assertEquals("Query Result BOs", 1, bos.getSize());
         BusinessObject bo = bos.get(0);
         List<BusinessObject.Value> values = bo.getValues();
         Assert.assertEquals("Values", expectedCount, values.size());
      }
      else
      {
         Assert.assertEquals("Query Result BOs", 0, bos.getSize());
      }

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
      client.put("Department", id);

      return sf.getWorkflowService().createBusinessObjectInstance(CLIENT_BO,
            (Serializable) client);
   }

   private BusinessObject updateClientU1(String id, String newDepartment)
   {
      ServiceFactory u1Sf = ServiceFactoryLocator.get(U1, U1);
      try
      {
         return updateClient(u1Sf, id, newDepartment);
      }
      finally
      {
         u1Sf.close();
      }
   }

   private BusinessObject updateClient(ServiceFactory sf, String id, String newDepartment)
   {
      final Map<String, Object> client = CollectionUtils.newMap();
      client.put("ClientId", id);
      client.put("ClientName", id);
      client.put("Department", newDepartment);

      return sf.getWorkflowService().updateBusinessObjectInstance(CLIENT_BO, client);
   }

   private void deleteClientU1(String id)
   {
      ServiceFactory u1Sf = ServiceFactoryLocator.get(U1, U1);
      try
      {
         deleteClient(u1Sf, id);
      }
      finally
      {
         u1Sf.close();
      }
   }

   private void deleteClient(ServiceFactory sf, String id)
   {
      sf.getWorkflowService().deleteBusinessObjectInstance(CLIENT_BO, id);
   }

   private BusinessObject createClientGroup(String id, String ref)
   {
      return createClientGroup(sf, id, ref);
   }

   private BusinessObject createClientGroupU1(String id, String ref)
   {
      ServiceFactory u1Sf = ServiceFactoryLocator.get(U1, U1);
      try
      {
         return createClientGroup(u1Sf, id, ref);
      }
      finally
      {
         u1Sf.close();
      }
   }

   private BusinessObject createClientGroup(ServiceFactory sf, String id, String ref)
   {
      final Map<String, Object> client = CollectionUtils.newMap();
      client.put("ClientGroupId", id);
      client.put("ClientRef", ref);

      return sf.getWorkflowService().createBusinessObjectInstance(CLIENT_GROUP_BO,
            (Serializable) client);
   }
}