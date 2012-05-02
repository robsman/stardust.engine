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

import static junit.framework.Assert.assertEquals;
import static org.eclipse.stardust.test.department.DepartmentModelConstants.*;
import static org.eclipse.stardust.test.util.TestConstants.MOTU;
import static org.junit.Assert.fail;

import org.eclipse.stardust.common.error.InvalidArgumentException;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.dto.DepartmentInfoDetails;
import org.eclipse.stardust.engine.api.dto.OrganizationInfoDetails;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.model.OrganizationInfo;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.DepartmentExistsException;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.util.DepartmentHome;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

/**
 * <p>
 * This class tests CRUD functionality of the Administration Service
 * regarding <i>Departments</i>.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class AdminServiceCrudTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);
   
   private static final String DEPT_ID = "Dept";
   
   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);
   
   @ClassRule
   public static final LocalJcrH2TestSetup testClassSetup = new LocalJcrH2TestSetup(ADMIN_USER_PWD_PAIR, MODEL_NAME);
   
   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(sf);
   
   private AdministrationService adminService;
   private QueryService queryService;
   
   private Organization org;
   
   @Before
   public void setUp()
   {
      adminService = sf.getAdministrationService();
      queryService = sf.getQueryService();
      
      org = (Organization) queryService.getParticipant(ORG_ID_1);
   }
   
   /**
    * <p>
    * The id must not be null.
    * </p>
    */
   @Test(expected = InvalidArgumentException.class)
   public void testCreateDepartmentIdNull()
   {
      adminService.createDepartment(null, DEPT_ID, DEPT_ID, null, org);
      fail("The ID must not be null.");
   }

   /**
    * <p>
    * The id must not be empty.
    * </p>
    */
   @Test(expected = InvalidArgumentException.class)
   public void testCreateDepartmentIdEmpty()
   {
      adminService.createDepartment("", DEPT_ID, DEPT_ID, null, org);
      fail("The ID must not be empty.");
   }
   
   /**
    * <p>
    * The parent may be null for a top level department.
    * </p>
    */
   @Test
   public void testCreateDepartmentParentNullTld()
   {
      DepartmentHome.create(sf, DEPT_ID_DE, ORG_ID_1, null);
   }
   
   /**
    * <p>
    * The parent must not be null for a department that is not
    * a top level one.
    * </p>
    */
   @Test(expected = InvalidArgumentException.class)
   public void testCreateDepartmentParentNullNotTld()
   {
      DepartmentHome.create(sf, SUB_DEPT_ID_NORTH, SUB_ORG_ID_2, null);
      fail("The parent must not be null for a department that is not a top level one.");
   }
   
   /**
    * <p>
    * The parent must resolve to an existing department.
    * </p>
    */
   @Test(expected = ObjectNotFoundException.class)
   public void testCreateDepartmentParentDoesNotExist()
   {
      final DepartmentInfo dept = new DepartmentInfoDetails(-1, "N/A", "N/A", -1);
      DepartmentHome.create(sf, SUB_DEPT_ID_NORTH, SUB_ORG_ID_2, dept);
      fail("The parent must resolve to an exisiting department.");
   }
   
   /**
    * <p>
    * It is possible that the department's parent is a direct parent organization
    * of the organization the department is created for.
    * </p>
    */
   @Test
   public void testCreateDepartmentParentIsDirectParent()
   {
      final Department parent = DepartmentHome.create(sf, DEPT_ID_DE, ORG_ID_2, null);
      DepartmentHome.create(sf, SUB_DEPT_ID_NORTH, SUB_ORG_ID_2, parent);
   }

   /**
    * <p>
    * It is not possible that the department's parent is an indirect parent organization
    * of the organization the department is created for.
    * </p>
    */
   @Test(expected = InvalidArgumentException.class)
   public void testCreateDepartmentParentIsIndirectParent()
   {
      final Department parent = DepartmentHome.create(sf, DEPT_ID_DE, ORG_ID_2, null);
      DepartmentHome.create(sf, SUB_SUB_DEP_ID_HH, SUB_SUB_ORG_ID_2, parent);
   }
   
   /**
    * <p>
    * It shouldn't be possible that the department's parent isn't a direct parent organization
    * of the organization the department is created for.
    * </p>
    */
   @Test(expected = InvalidArgumentException.class)
   public void testCreateDepartmentParentIsNotParent()
   {
      final Department parent = DepartmentHome.create(sf, DEPT_ID_DE, ORG_ID_1, null);
      DepartmentHome.create(sf, SUB_DEPT_ID_NORTH, SUB_ORG_ID_2, parent);
      fail("The department's parent must be a direct parent organization " +
      		"of the organization the department is created for.");
   }
   
   /**
    * <p>
    * The organization must not be null.
    * </p>
    */
   @Test(expected = InvalidArgumentException.class)
   public void testCreateDepartmentOrganizationNull()
   {
      adminService.createDepartment(DEPT_ID_DE, DEPT_ID_DE, null, null, null);
      fail("The organization must not be null.");
   }
   
   /**
    * <p>
    * The organization must resolve to an existing model participant
    * organization.
    * </p>
    */
   @Test(expected = ObjectNotFoundException.class)
   public void testCreateDepartmentOrganizationDoesNotExist()
   {
      final OrganizationInfo org = new OrganizationInfoDetails("N/A");
      adminService.createDepartment(DEPT_ID_DE, DEPT_ID_DE, null, null, org);
      fail("The organization must resolve to an existing model participant organization");
   }

   /**
    * <p>
    * It should be possible to get a formerly created department.
    * </p>
    */
   @Test
   public void testGetDepartment()
   {
      final Department createdDep = DepartmentHome.create(sf, DEPT_ID_DE, ORG_ID_1, null);
      final Department retrievedDep = adminService.getDepartment(createdDep.getOID());
      assertEquals(createdDep, retrievedDep);
   }
   
   /**
    * <p>
    * An exception should be thrown when a department cannot be found.
    * </p>
    */
   @Test(expected = ObjectNotFoundException.class)
   public void testGetDepartmentDoesNotExist()
   {
      adminService.getDepartment(-1);
      fail("Found a non-existing department.");
   }
   
   /**
    * <p>
    * It should be possible to modify the description of a formerly created department.
    * </p>
    */
   @Test
   public void testModifyDepartmentDescription()
   {
      final Department createdDep = DepartmentHome.create(sf, DEPT_ID_DE, ORG_ID_1, null);
      final String newDesc = "the new description";
      adminService.modifyDepartment(createdDep.getOID(), createdDep.getName(), newDesc);
      
      final Department retrievedDep = adminService.getDepartment(createdDep.getOID());
      assertEquals(newDesc, retrievedDep.getDescription());
   }
   
   /**
    * <p>
    * An exception should be thrown when a department cannot be found.
    * </p>
    */
   @Test(expected = ObjectNotFoundException.class)
   public void testModifyDepartmentDoesNotExist()
   {
      adminService.modifyDepartment(-1, "Hello", null);
      fail("Found a department which does not exist.");
   }
   
   /**
    * <p>
    * It should be possible to remove a formerly created department.
    * </p> 
    */
   @Test(expected = ObjectNotFoundException.class)
   public void testRemoveDepartment()
   {
      final Department dept = DepartmentHome.create(sf, DEPT_ID_DE, ORG_ID_1, null);
      adminService.removeDepartment(dept.getOID());
      
      adminService.getDepartment(dept.getOID());
      fail("Found a department which has been removed.");
   }
   
   /**
    * <p>
    * An exception should be thrown when a department cannot be found.
    * </p>
    */
   @Test(expected = ObjectNotFoundException.class)
   public void testRemoveDepartmentDoesNotExist()
   {
      adminService.removeDepartment(-1);
      fail("Tried to remove a non-existing department.");
   }
   
   /**
    * <p>
    * Duplicate department entries for the same organization
    * should be rejected.
    * </p>
    */
   @Test(expected = DepartmentExistsException.class)
   public void testCreateDuplicateDepartmentIdForOneOrg()
   {
      DepartmentHome.create(sf, DEPT_ID_DE, ORG_ID_1, null);
      
      DepartmentHome.create(sf, DEPT_ID_DE, ORG_ID_1, null);
      fail("Duplicate department entries for the same organization should be rejected.");
   }
   
   /**
    * <p>
    * Duplicate department entries for different organizations
    * should be OK.
    * </p>
    */
   @Test
   public void testCreateDuplicateDepartmentIdForTwoOrgs()
   {
      DepartmentHome.create(sf, DEPT_ID_DE, ORG_ID_1, null);
      
      try 
      {
         DepartmentHome.create(sf, DEPT_ID_DE, ORG_ID_2, null);
      }
      catch (final DepartmentExistsException e)
      {
         fail("Duplicate department entries for different organizations should be OK.");
      }
   }
   
   /**
    * <p>
    * Creating a department for an unscoped organization should be refused.
    * </p>
    */
   @Test(expected = InvalidArgumentException.class)
   public void testCreatingDepartmentForUnscopedOrg()
   {
      DepartmentHome.create(sf, DEPT_ID_DE, ORG_ID_3, null);
      fail("Creating a department for an unscoped organization should be refused.");
   }
}
