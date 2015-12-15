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

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.query.BusinessObjectQuery;
import org.eclipse.stardust.engine.api.query.BusinessObjects;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.api.runtime.BusinessObject.Value;
import org.eclipse.stardust.test.api.setup.*;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.util.UserHome;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

/**
 * <p>
 * This class tests Business Object declarative security department restriction and access
 * propagation functionality.
 * <p>
 * The Model consists of following Business Objects:
 * <ul>
 * <li>Client - Represents a Client and is scoped by department.</li>
 * <li>ClientGroup - Groups Clients (no department).</li>
 * <li>MasterGroup - Groups ClientGroup (no department).</li>
 * </ul>
 *
 * @author Roland.Stamm
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BusinessObjectDepartmentTest
{
   public static final String MODEL_NAME = "BOTestModel";

   // ********* Business Objects ********

   public static final String CLIENT_BO = new QName(MODEL_NAME, "Client").toString();

   public static final String CLIENT_GROUP_BO = new QName(MODEL_NAME, "ClientGroup")
         .toString();

   public static final String MASTER_GROUP_BO = new QName(MODEL_NAME, "MasterGroup")
         .toString();

   // ********* Participants ********

   public static final String CLIENT_ADMIN_ROLE = new QName(MODEL_NAME, "Client_Admin")
         .toString();

   public static final String CLIENT_USER_ORG = new QName(MODEL_NAME, "Client_User")
         .toString();

   public static final String BO_ADMIN_ROLE = new QName(MODEL_NAME, "BO_Admin")
         .toString();

   // ********* Users ********

   public static final String U1 = "u1";

   // ******** Test setup ********

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

      createClientU1("c1", "g1");
      createClientGroupU1("g1", "c1");
      createClientU1("c2", null);
      createClientGroupU1("g2", null);

      // read
      assertValuesU1(CLIENT_BO, 2);
      assertValuesU1(CLIENT_GROUP_BO, 2);

      // update
      updateClientDepartmentU1("c1", "c2");
      updateClientDepartmentU1("c2", "c1");

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
   @Ignore
   public void test02AuditorAccess()
   {
      createClient("c1", "g1");
      createClientGroup("g1", "c1");
      createClient("c2", null);
      createClientGroup("g2", null);

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

      UserHome.create(sf, U1, getScopedOrg(CLIENT_USER_ORG, "c1"));

      assertValuesU1(CLIENT_BO, 1);
   }


   /**
    * Tests department restriction on create.
    */
   @Test
   public void test04DepartmentRestrictionCreate()
   {
      UserHome.create(sf, U1, getImplicitlyScopedRole(CLIENT_ADMIN_ROLE, CLIENT_USER_ORG, "c1"));

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
      UserHome.create(sf, U1, getImplicitlyScopedRole(CLIENT_ADMIN_ROLE, CLIENT_USER_ORG, "c1"));

      createClient("c1", null);
      createClient("c2", null);

      updateClientDepartmentU1("c1", "c1");
      boolean ex = false;
      try
      {
         // should throw AccessForbiddenException because c2 as old value is not granted.
         updateClientDepartmentU1("c2", "c1");
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
      UserHome.create(sf, U1, getImplicitlyScopedRole(CLIENT_ADMIN_ROLE, CLIENT_USER_ORG, "c1"));

      createClient("c1", null);
      boolean ex = false;
      try
      {
         // should throw AccessForbiddenException because c2 as new value is not granted
         updateClientDepartmentU1("c1", "c2");
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
      UserHome.create(sf, U1, getImplicitlyScopedRole(CLIENT_ADMIN_ROLE, CLIENT_USER_ORG, "c1"));
      createClient("c1", null);
      createClient("c2", null);

      assertValuesU1(CLIENT_BO, 1);

      deleteClientU1("c1");

      tryAndAssertFail(new Action<Object>()
      {
         public Object execute()
         {
            // should throw AccessForbiddenException
            deleteClientU1("c2");

            return null;
         }
      });
   }

   /**
    * Tests visibility of department scope restricted and propagated related business
    * objects for BO_Admin (non scoped role which has modify and read data permissions on
    * Client and ClientGroup).
    */
   @Test
   public void test08NonScopedRoleAccess()
   {
      // create
      UserHome.create(sf, U1, getUnscopedParticipant(BO_ADMIN_ROLE));

      createClientU1("c1", "g1");
      createClientU1("c2", null);

      createClientGroupU1("g1", "c1");
      createClientGroupU1("g2", null);

      // read
      assertValuesU1(CLIENT_BO, 2);
      assertValuesU1(CLIENT_GROUP_BO, 2);

      // update
      updateClientDepartmentU1("c1", "c2");
      updateClientDepartmentU1("c2", "c1");

      // delete
      deleteClientU1("c1");
      deleteClientU1("c2");

      // read
      assertValuesU1(CLIENT_BO, 0);
      assertValuesU1(CLIENT_GROUP_BO, 2);

   }


   /**
    * Tests visibility of department scope restricted and propagated related business
    * objects for BO_Admin (non scoped role which has modify and read data permissions on
    * Client and ClientGroup).
    */
   @Test
   public void test09NonScopedRoleAccessOnCyclicRelation()
   {
      // create
      UserHome.create(sf, U1, getUnscopedParticipant(BO_ADMIN_ROLE));

      createClient("c1", "g1");
      createClientGroup("g1", "c1", "m1");
      createMasterGroup("m1", "g1");

      createClient("c2", "g2");
      createClientGroup("g2", "c2", "m2");
      createMasterGroup("m2", "g2");

      createClient("c3", null);
      createClientGroup("g3", null);
      createMasterGroup("m3", null);

      // read
      assertValuesU1(CLIENT_BO, 3);
      assertValuesU1(CLIENT_GROUP_BO, 3);

      // update
      updateClientDepartmentU1("c1", "c2");
      updateClientDepartmentU1("c2", "c1");

      // delete
      deleteClientU1("c1");
      deleteClientU1("c2");
      deleteClientU1("c3");

      // read
      assertValuesU1(CLIENT_BO, 0);
      assertValuesU1(CLIENT_GROUP_BO, 3);
      assertValuesU1(CLIENT_GROUP_BO, 3);

   }

   /**
    * Tests visibility of business object with propagated access. With 1 level of
    * propagation.
    */
   @Test
   public void test10DepartmentPropagationRead1Level()
   {
      createClient("c1", "g1");
      createClientGroup("g1", "c1");

      createClient("c2", "g2");
      createClientGroup("g2", "c2");

      createClient("c3", null);
      createClientGroup("g3", null);

      UserHome.create(sf, U1,
            getScopedOrg(CLIENT_USER_ORG, "c1"));

      assertValuesU1(CLIENT_GROUP_BO, 1, "ClientGroupId", "g1");
   }

   /**
    * Tests visibility of business object with propagated access. With 2 levels and cyclic
    * propagation.
    */
   @Test
   public void test11DepartmentPropagationRead2LevelCyclicAccess1()
   {
      createClient("c1", "g1");
      createClientGroup("g1", "c1", "m1");
      createMasterGroup("m1", "g1");

      createClient("c2", "g2");
      createClientGroup("g2", "c2", "m2");
      createMasterGroup("m2", "g2");

      createClient("c3", null);
      createClientGroup("g3", null);
      createMasterGroup("m3", null);

      UserHome.create(sf, U1,
            getScopedOrg(CLIENT_USER_ORG, "c1"));

      assertValuesU1(CLIENT_GROUP_BO, 1, "ClientGroupId", "g1");
      assertValuesU1(MASTER_GROUP_BO, 1, "MasterGroupId", "m1");
   }

   /**
    * Tests visibility of business object with propagated access. With 2 levels and cyclic
    * propagation.
    */
   @Test
   public void test12DepartmentPropagationRead2LevelCyclicAccess2()
   {
      createClient("c1", "g1");
      createClientGroup("g1", "c1", "m1");
      createMasterGroup("m1", "g1");

      createClient("c2", "g2");
      createClientGroup("g2", "c2", "m2");
      createMasterGroup("m2", "g2");

      createClient("c3", null);
      createClientGroup("g3", null);
      createMasterGroup("m3", null);

      UserHome.create(sf, U1,
            getScopedOrg(CLIENT_USER_ORG, "c1"),
            getScopedOrg(CLIENT_USER_ORG, "c2"));

      assertValuesU1(CLIENT_GROUP_BO, 2);
      assertValuesU1(MASTER_GROUP_BO, 2);
   }

   /**
    * Tests visibility of business object with propagated access. With 2 levels and cyclic
    * propagation.
    */
   @Test
   public void test13DepartmentPropagationRead2LevelCyclicNoAccess()
   {
      createClient("c1", "g1");
      createClientGroup("g1", "c1", "m1");
      createMasterGroup("m1", "g1");

      createClient("c2", "g2");
      createClientGroup("g2", "c2", "m2");
      createMasterGroup("m2", "g2");

      createClient("c3", null);
      createClientGroup("g3", null);
      createMasterGroup("m3", null);

      UserHome.create(sf, U1);

      assertValuesU1(CLIENT_GROUP_BO, 0);
      assertValuesU1(MASTER_GROUP_BO, 0);
   }

   /**
    * Tests modification of business object with propagated access. With 1 level of
    * propagation.
    */
   @Test
   public void test14DepartmentPropagationModify1Level()
   {
      UserHome.create(sf, U1,
            getImplicitlyScopedRole(CLIENT_ADMIN_ROLE, CLIENT_USER_ORG, "c1"));// getScopedOrg(CLIENT_ADMIN_ORG,
                                                                               // "c1"));

      createClientU1("c1", "g1");
      createClientGroupU1("g1", "c1");

      tryAndAssertFail(new Action<Object>()
      {
         public Object execute()
         {
            // should throw AccessForbiddenException
            createClientU1("c2", "g2");

            return null;
         }
      });

      tryAndAssertFail(new Action<Object>()
      {
         public Object execute()
         {
            // should throw AccessForbiddenException
            createClientGroupU1("g2", "c2");

            return null;
         }
      });

      tryAndAssertFail(new Action<Object>()
      {
         public Object execute()
         {
            // should throw AccessForbiddenException
            createClientU1("c3", null);

            return null;
         }
      });

      tryAndAssertFail(new Action<Object>()
      {
         public Object execute()
         {
            // should throw AccessForbiddenException
            createClientGroupU1("g3", null);

            return null;
         }
      });

      assertValuesU1(CLIENT_BO, 1, "ClientId", "c1");
      assertValuesU1(CLIENT_GROUP_BO, 1, "ClientGroupId", "g1");
   }

   /**
    * Tests modification of business object with propagated access. With 2 levels and
    * cyclic propagation and access to one department.
    */
   @Test
   public void test15DepartmentPropagationModify2LevelCyclicAccess1()
   {
      UserHome.create(sf, U1, getImplicitlyScopedRole(CLIENT_ADMIN_ROLE, CLIENT_USER_ORG, "c1"));

      createClientU1("c1", "g1");
      createClientGroupU1("g1", "c1", "m1");
      createMasterGroupU1("m1", "g1");

      tryAndAssertFail(new Action<Object>()
      {
         public Object execute()
         {
            // should throw AccessForbiddenException
            createClientU1("c2", "g2");

            return null;
         }
      });

      tryAndAssertFail(new Action<Object>()
      {
         public Object execute()
         {
            // should throw AccessForbiddenException
            createClientGroupU1("g2", "c2", "m2");

            return null;
         }
      });

      tryAndAssertFail(new Action<Object>()
      {
         public Object execute()
         {
            // should throw AccessForbiddenException
            createMasterGroupU1("m2", "g2");

            return null;
         }
      });

      tryAndAssertFail(new Action<Object>()
      {
         public Object execute()
         {
            // should throw AccessForbiddenException
            createClientU1("c3", null);

            return null;
         }
      });

      tryAndAssertFail(new Action<Object>()
      {
         public Object execute()
         {
            // should throw AccessForbiddenException
            createClientGroupU1("g3", null, null);

            return null;
         }
      });

      tryAndAssertFail(new Action<Object>()
      {
         public Object execute()
         {
            // should throw AccessForbiddenException
            createMasterGroupU1("m3", null);

            return null;
         }
      });

      assertValuesU1(CLIENT_BO, 1, "ClientId", "c1");
      assertValuesU1(CLIENT_GROUP_BO, 1, "ClientGroupId", "g1");
   }

   /**
    * Tests modification of business object with propagated access. With 2 levels and
    * cyclic propagation and access to two departments.
    */
   @Test
   public void test16DepartmentPropagationModify2LevelCyclicAccess2()
   {
      UserHome.create(sf, U1, getImplicitlyScopedRole(CLIENT_ADMIN_ROLE, CLIENT_USER_ORG, "c1"),
            getImplicitlyScopedRole(CLIENT_ADMIN_ROLE, CLIENT_USER_ORG, "c2"));

      createClientU1("c1", "g1");
      createClientGroupU1("g1", "c1", "m1");
      createMasterGroupU1("m1", "g1");

      createClientU1("c2", "g2");
      createClientGroupU1("g2", "c2", "m2");
      createMasterGroupU1("m2", "g2");

      tryAndAssertFail(new Action<Object>()
      {
         public Object execute()
         {
            // should throw AccessForbiddenException
            createClientU1("c3", null);

            return null;
         }
      });

      tryAndAssertFail(new Action<Object>()
      {
         public Object execute()
         {
            // should throw AccessForbiddenException
            createClientGroupU1("g3", null, null);

            return null;
         }
      });

      tryAndAssertFail(new Action<Object>()
      {
         public Object execute()
         {
            // should throw AccessForbiddenException
            createMasterGroupU1("m3", null);

            return null;
         }
      });

      assertValuesU1(CLIENT_BO, 2);
      assertValuesU1(CLIENT_GROUP_BO, 2);
   }

   /**
    * Tests modification of business object with propagated access. With 2 levels and
    * cyclic propagation and no access to any department or role.
    */
   @Test
   public void test17DepartmentPropagationModify2LevelCyclicNoAccess()
   {
      UserHome.create(sf, U1);

      tryAndAssertFail(new Action<Object>()
      {
         public Object execute()
         {
            // should throw AccessForbiddenException
            createClientU1("c1", "g1");

            return null;
         }
      });

      tryAndAssertFail(new Action<Object>()
      {
         public Object execute()
         {
            // should throw AccessForbiddenException
            createClientGroupU1("g1", "c1", "m1");

            return null;
         }
      });

      tryAndAssertFail(new Action<Object>()
      {
         public Object execute()
         {
            // should throw AccessForbiddenException
            createMasterGroupU1("m1", "g1");
            return null;
         }
      });

      tryAndAssertFail(new Action<Object>()
      {
         public Object execute()
         {
            // should throw AccessForbiddenException
            createClientU1("c2", "g2");

            return null;
         }
      });

      tryAndAssertFail(new Action<Object>()
      {
         public Object execute()
         {
            // should throw AccessForbiddenException
            createClientGroupU1("g2", "c2", "m2");

            return null;
         }
      });

      tryAndAssertFail(new Action<Object>()
      {
         public Object execute()
         {
            // should throw AccessForbiddenException
            createMasterGroupU1("m2", "g2");

            return null;
         }
      });

      tryAndAssertFail(new Action<Object>()
      {
         public Object execute()
         {
            // should throw AccessForbiddenException
            createClientU1("c3", null);

            return null;
         }
      });

      tryAndAssertFail(new Action<Object>()
      {
         public Object execute()
         {
            // should throw AccessForbiddenException
            createClientGroupU1("g3", null, null);

            return null;
         }
      });

      tryAndAssertFail(new Action<Object>()
      {
         public Object execute()
         {
            // should throw AccessForbiddenException
            createMasterGroupU1("m3", null);

            return null;
         }
      });

      assertValuesU1(CLIENT_BO, 0);
      assertValuesU1(CLIENT_GROUP_BO, 0);
   }

   /**
    * Tests non allowed modification of business object with propagated access. Without propagation.
    */
   @Test(expected=AccessForbiddenException.class)
   public void test18DepartmentRestrictionModifyWithReadGrant()
   {
      UserHome.create(sf, U1,
            getScopedOrg(CLIENT_USER_ORG, "c1"));

      createClientU1("c1", "g1");
   }

   /**
    * Tests non allowed modification of business object with propagated access. With 1
    * level of propagation.
    */
   @Test
   public void test19DepartmentPropagationModifyWithReadGrant1Level()
   {
      UserHome.create(sf, U1,
            getScopedOrg(CLIENT_USER_ORG, "c1"));

      createClient("c1", "g1");

      tryAndAssertFail(new Action<Object>()
      {
         public Object execute()
         {
            // should throw AccessForbiddenException
            createClientGroupU1("g1", "c1");

            return null;
         }
      });
   }

   /**
    * Tests update order of business object with propagated access. With 1
    * level of propagation.
    */
   @Test
   public void test20DepartmentPropagationModifyReferencedPrimaryKey()
   {
      createClient("c1", "g1");
      createClientGroup("g1", "c1");

      createClient("c2", "g2");
      createClientGroup("g2", "c2");

      updateBOField(CLIENT_BO, "c2", "Department", "c1");

      UserHome.create(sf, U1, getImplicitlyScopedRole(CLIENT_ADMIN_ROLE, CLIENT_USER_ORG, "c1"));

      assertValuesU1(CLIENT_BO, 2);
      assertValuesU1(CLIENT_GROUP_BO, 2);

      // ref change works because permission is on CLIENT
      updateBOFieldU1(CLIENT_BO, "c2", "ClientGroupRef", "g3");

      // ref change does not work because it breaks permission chain to CLIENT
      tryAndAssertFail(new Action<Object>()
      {
         public Object execute()
         {
            // should throw AccessForbiddenException
            updateBOFieldU1(CLIENT_GROUP_BO, "g2", "ClientRef", "c3");

            return null;
         }
      });
   }


   //************************** UTILITY ***************************************

   private void tryAndAssertFail(Action<Object> action)
   {
      boolean ex = false;
      try
      {
         // should throw AccessForbiddenException
         action.execute();
      }
      catch (AccessForbiddenException e)
      {
         ex = true;
      }

      if (!ex)
      {
         Assert.fail("Expected AccessForbiddenException.");
      }
   }

   private ModelParticipantInfo getAuditorRole()
   {
      QueryService queryService = sf.getQueryService();
      Models models = queryService.getModels(findForId(PREDEFINED_MODEL_ID));
      Participant auditorParticipant = queryService.getParticipant(
            models.get(0).getModelOID(), PredefinedConstants.AUDITOR_ROLE);
      Assert.assertNotNull(auditorParticipant);
      return (ModelParticipantInfo) auditorParticipant;
   }

   private ModelParticipant getUnscopedParticipant(String qualifiedModelParticipantId)
   {
      QueryService queryService = sf.getQueryService();
      Participant auditorParticipant = queryService
            .getParticipant(qualifiedModelParticipantId);
      Assert.assertNotNull(auditorParticipant);
      return (ModelParticipant) auditorParticipant;
   }

   private void assertValuesU1(String businessObjectId, int expectedCount)
   {
      assertValuesU1(businessObjectId, expectedCount, null, null);
   }

   private void assertValuesU1(String businessObjectId, int expectedCount,
         String expectedId, String expectedValue)
   {
      ServiceFactory u1Sf = ServiceFactoryLocator.get(U1, U1);

      BusinessObjectQuery query = BusinessObjectQuery
            .findForBusinessObject(businessObjectId);
      query.setPolicy(
            new BusinessObjectQuery.Policy(BusinessObjectQuery.Option.WITH_VALUES));
      BusinessObjects bos = u1Sf.getQueryService().getAllBusinessObjects(query);

      if (expectedCount > 0)
      {
         Assert.assertEquals("Query Result BOs", 1, bos.getSize());
         BusinessObject bo = bos.get(0);
         List<BusinessObject.Value> values = bo.getValues();
         Assert.assertEquals("Values of " + businessObjectId, expectedCount,
               values.size());
         if (expectedId != null)
         {
            for (Value value : values)
            {
               Map< ? , ? > structMap = (Map< ? , ? >) value.getValue();
               Assert.assertEquals(expectedValue, structMap.get(expectedId));
            }
         }
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

   private ModelParticipantInfo getImplicitlyScopedRole(String roleId,
         String organizationId, String departmentId)
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

      ModelParticipant unscopedParticipant = getUnscopedParticipant(roleId);

      QualifiedModelParticipantInfo scopedParticipant = dep.getScopedParticipant(unscopedParticipant);

      Assert.assertTrue(scopedParticipant instanceof RoleInfo);
      Assert.assertEquals(roleId, scopedParticipant.getQualifiedId());
      Assert.assertNotNull(scopedParticipant.getDepartment());
      return scopedParticipant;
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
      client.put("ClientGroupRef", ref);
      client.put("Department", id);

      return sf.getWorkflowService().createBusinessObjectInstance(CLIENT_BO,
            (Serializable) client);
   }

   private BusinessObject updateClientDepartmentU1(String id, String newDepartment)
   {
      ServiceFactory u1Sf = ServiceFactoryLocator.get(U1, U1);
      try
      {
         return updateClientDepartment(u1Sf, id, newDepartment);
      }
      finally
      {
         u1Sf.close();
      }
   }

   private BusinessObject updateClientDepartment(ServiceFactory sf, String id, String newDepartment)
   {
      // TODO fix ref getting lost.
      final Map<String, Object> client = CollectionUtils.newMap();
      client.put("ClientId", id);
      client.put("ClientName", id);
      client.put("Department", newDepartment);

      return sf.getWorkflowService().updateBusinessObjectInstance(CLIENT_BO, client);
   }

   private BusinessObject updateBOFieldU1(String boId, String pk, String field, String newValue)
   {
      ServiceFactory u1Sf = ServiceFactoryLocator.get(U1, U1);
      try
      {
         return updateBOField(u1Sf, boId, pk, field, newValue);
      }
      finally
      {
         u1Sf.close();
      }
   }

   private BusinessObject updateBOField(String boId, String pk, String field, String newValue)
   {
      return updateBOField(sf, boId, pk, field, newValue);
   }

   private BusinessObject updateBOField(ServiceFactory sf, String boId, String pk, String field, String newValue)
   {
      BusinessObjectQuery query = BusinessObjectQuery.findWithPrimaryKey(boId, pk);
      query.setPolicy(
            new BusinessObjectQuery.Policy(BusinessObjectQuery.Option.WITH_VALUES));
      BusinessObjects bo = sf.getQueryService().getAllBusinessObjects(query);

      BusinessObject businessObject = bo.get(0);
      Value clientMap = businessObject.getValues().get(0);

      final Map<String, Object> client = (Map<String, Object>) clientMap.getValue();
      client.put(field, newValue);

      return sf.getWorkflowService().updateBusinessObjectInstance(boId, client);
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

   private BusinessObject createClientGroup(String id, String ref, String ref2)
   {
      return createClientGroup(sf, id, ref, ref2);
   }

   private BusinessObject createClientGroup(String id, String ref)
   {
      return createClientGroup(sf, id, ref, null);
   }

   private BusinessObject createClientGroupU1(String id, String ref)
   {
      ServiceFactory u1Sf = ServiceFactoryLocator.get(U1, U1);
      try
      {
         return createClientGroup(u1Sf, id, ref, null);
      }
      finally
      {
         u1Sf.close();
      }
   }

   private BusinessObject createClientGroupU1(String id, String ref, String ref2)
   {
      ServiceFactory u1Sf = ServiceFactoryLocator.get(U1, U1);
      try
      {
         return createClientGroup(u1Sf, id, ref, ref2);
      }
      finally
      {
         u1Sf.close();
      }
   }

   private BusinessObject createClientGroup(ServiceFactory sf, String id, String ref,
         String ref2)
   {
      final Map<String, Object> client = CollectionUtils.newMap();
      client.put("ClientGroupId", id);
      client.put("ClientRef", ref);
      client.put("MasterGroupRef", ref2);

      return sf.getWorkflowService().createBusinessObjectInstance(CLIENT_GROUP_BO,
            (Serializable) client);
   }

   private BusinessObject createMasterGroupU1(String id, String ref)
   {
      ServiceFactory u1Sf = ServiceFactoryLocator.get(U1, U1);
      try
      {
         return createMasterGroup(u1Sf, id, ref);
      }
      finally
      {
         u1Sf.close();
      }
   }

   private BusinessObject createMasterGroup(String id, String ref)
   {
      return createMasterGroup(sf, id, ref);
   }

   private BusinessObject createMasterGroup(ServiceFactory sf, String id, String ref)
   {
      final Map<String, Object> client = CollectionUtils.newMap();
      client.put("MasterGroupId", id);
      client.put("ClientGroupRef", ref);

      return sf.getWorkflowService().createBusinessObjectInstance(MASTER_GROUP_BO,
            (Serializable) client);
   }

}