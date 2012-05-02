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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.dto.DepartmentInfoDetails;
import org.eclipse.stardust.engine.api.dto.OrganizationInfoDetails;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.model.OrganizationInfo;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.util.DepartmentHome;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

/**
 * <p>
 * This class tests query functionality
 * regarding <i>Departments</i>.
 * <p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class DepartmentQueryTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);
   
   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);
   
   @ClassRule
   public static final LocalJcrH2TestSetup testClassSetup = new LocalJcrH2TestSetup(ADMIN_USER_PWD_PAIR, MODEL_NAME);
   
   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(sf);
   
   /**
    * <p>
    * It should be possible to find all created departments via the Query Service.
    * </p>
    */
   @Test
   public void testFindAllDepartments()
   {
      boolean firstFound = false, secondFound = false;
      
      DepartmentHome.create(sf, DEPT_ID_DE, ORG_ID_1, null);
      DepartmentHome.create(sf, DEPT_ID_EN, ORG_ID_1, null);
      
      final Organization org = sf.getQueryService().getActiveModel().getOrganization(ORG_ID_1);
      
      final List<Department> depts = sf.getQueryService().findAllDepartments(null, org);
      for (final Department dept : depts)
      {
         if (dept.getId().equals(DEPT_ID_DE))
         {
            firstFound = true;
         }
         else if (dept.getId().equals(DEPT_ID_EN))
         {
            secondFound = true;
         }
      }
      
      assertTrue(firstFound && secondFound);
   }
   
   /**
    * <p>
    * It should be possible to find created departments by ID via the Query Service.
    * </p>
    */
   @Test
   public void testFindDepartmentById()
   {
      final Department createdDept = DepartmentHome.create(sf, DEPT_ID_DE, ORG_ID_1, null);
      final Department retrievedDept = sf.getQueryService().findDepartment(null, DEPT_ID_DE, new OrganizationInfoDetails(ORG_ID_1));
      assertEquals(createdDept.getOID(), retrievedDept.getOID());
   }
   
   /**
    * <p>
    * It should be possible to retrieve all top level departments by specifying
    * null for both department and organization.
    * </p>
    */
   @Test
   public void testFindAllDepartmentsParentNullOrgNull()
   {
      final Department deptEn = DepartmentHome.create(sf, DEPT_ID_EN, ORG_ID_1, null);
      final Department deptDe = DepartmentHome.create(sf, DEPT_ID_DE, ORG_ID_2, null);
      final Department deptNorth = DepartmentHome.create(sf, SUB_DEPT_ID_NORTH, SUB_ORG_ID_2, deptDe);
      
      final List<Department> tldDepts = sf.getQueryService().findAllDepartments(null, null);
      
      assertTrue(tldDepts.contains(deptEn));
      assertTrue(tldDepts.contains(deptDe));
      assertFalse(tldDepts.contains(deptNorth));
   }
   
   /**
    * <p>
    * If the parent is not null, but the organization is null, then the result contains
    * all direct children of the parent department, regardless of the organization
    * to which they are assigned.
    * </p>
    */
   @Test
   public void testFindAllDepartmentsParentNotNullOrgNull()
   {
      final Department deptDe = DepartmentHome.create(sf, DEPT_ID_DE, ORG_ID_2, null);
      final Department deptNorth = DepartmentHome.create(sf, SUB_DEPT_ID_NORTH, SUB_ORG_ID_2, deptDe);
      final Department deptHh = DepartmentHome.create(sf, SUB_SUB_DEP_ID_HH, SUB_SUB_ORG_ID_2, deptNorth);
      
      final List<Department> depts = sf.getQueryService().findAllDepartments(deptDe, null);
      
      assertTrue(depts.contains(deptNorth));
      assertFalse(depts.contains(deptHh));
      assertFalse(depts.contains(deptDe));
   }
   
   /**
    * <p>
    * If the parent is null, but the organization is not null, then the result contains
    * all departments assigned to the organization, regardless of their parent
    * department.
    * </p>
    */
   @Test
   public void testFindAllDepartmentsParentNullOrgNotNull()
   {
      final Department deptDe = DepartmentHome.create(sf, DEPT_ID_DE, ORG_ID_2, null);
      final Department deptEn = DepartmentHome.create(sf, DEPT_ID_EN, ORG_ID_2, null);
      
      final Department deptDeNorth = DepartmentHome.create(sf, SUB_DEPT_ID_NORTH, SUB_ORG_ID_2, deptDe);
      final Department deptEnSouth = DepartmentHome.create(sf, SUB_DEPT_ID_SOUTH, SUB_ORG_ID_2, deptEn);
      
      final Organization subOrg2 = sf.getQueryService().getActiveModel().getOrganization(SUB_ORG_ID_2);
      
      final List<Department> depts = sf.getQueryService().findAllDepartments(null, subOrg2);
      
      assertTrue(depts.contains(deptDeNorth));
      assertTrue(depts.contains(deptEnSouth));
      assertFalse(depts.contains(deptDe));
      assertFalse(depts.contains(deptEn));
   }
   
   /**
    * <p>
    * If both parent and organization are not null, then the result contains all 
    * departments assigned to the organization, that have as direct parent the 
    * specified department.
    * </p>
    */
   @Test
   public void testFindAllDepartmentsParentNotNullOrgNotNull()
   {
      final Department deptDe = DepartmentHome.create(sf, DEPT_ID_DE, ORG_ID_2, null);
      final Department deptEn = DepartmentHome.create(sf, DEPT_ID_EN, ORG_ID_2, null);
      
      final Department deptDeNorth = DepartmentHome.create(sf, SUB_DEPT_ID_NORTH, SUB_ORG_ID_2, deptDe);
      final Department deptDeSouth = DepartmentHome.create(sf, SUB_DEPT_ID_SOUTH, SUB_ORG_ID_2, deptDe);
      final Department deptDeHh = DepartmentHome.create(sf, SUB_SUB_DEP_ID_HH, SUB_SUB_ORG_ID_2, deptDeNorth);
      
      final Department deptEnNorth = DepartmentHome.create(sf, SUB_DEPT_ID_NORTH, SUB_ORG_ID_2, deptEn);
      final Department deptEnSouth = DepartmentHome.create(sf, SUB_DEPT_ID_SOUTH, SUB_ORG_ID_2, deptEn);
      
      final Organization subOrg2 = sf.getQueryService().getActiveModel().getOrganization(SUB_ORG_ID_2);
      
      final List<Department> depts = sf.getQueryService().findAllDepartments(deptDe, subOrg2);
      
      assertTrue(depts.contains(deptDeNorth));
      assertTrue(depts.contains(deptDeSouth));
      assertFalse(depts.contains(deptDeHh));
      assertFalse(depts.contains(deptDe));
      assertFalse(depts.contains(deptEnNorth));
      assertFalse(depts.contains(deptEnSouth));
   }
   
   /**
    * <p>
    * If there aren't any departments that satisfy the search criteria
    * an empty list should be returned, instead of null.
    * </p>
    */
   @Test
   public void testFindAllDepartmentsResultNotNullButEmpty()
   {
      final List<Department> tldDeps = sf.getQueryService().findAllDepartments(null, null);
      assertNotNull(tldDeps);
      assertEquals(0, tldDeps.size());
   }
   
   /**
    * <p>
    * If the specified parent department isn't resolvable, the query should be refused.
    * </p>
    */
   @Test(expected = ObjectNotFoundException.class)
   public void testFindAllDepartmentsParentNotResolvable()
   {
      final DepartmentInfo parent = new DepartmentInfoDetails(-1, "N/A", "N/A", -1);
      final Organization org = sf.getQueryService().getActiveModel().getOrganization(ORG_ID_1);
      
      sf.getQueryService().findAllDepartments(parent, org);
      fail("The parent department does not exist.");
   }
   
   /**
    * <p>
    * If the specified organization isn't resolvable, the query should be refused.
    * </p>
    */
   @Test(expected = ObjectNotFoundException.class)
   public void testFindAllDepartmentsOrgNotResolvable()
   {
      final Department parent = DepartmentHome.create(sf, DEPT_ID_DE, ORG_ID_1, null);
      final OrganizationInfo org = new OrganizationInfoDetails("N/A");
      
      sf.getQueryService().findAllDepartments(parent, org);
      fail("The Organization does not exist.");
   }
   
   /**
    * <p>
    * If specified, the parent must be resolvable.
    * </p>
    */
   @Test(expected = ObjectNotFoundException.class)
   public void testFindDepartmentParentCouldNotBeResolved()
   {
      final Department dept = DepartmentHome.create(sf, DEPT_ID_DE, ORG_ID_1, null);
      final DepartmentInfo parent = new DepartmentInfoDetails(-1, "N/A", "N/A", -1);
      
      sf.getQueryService().findDepartment(parent, dept.getId(), new OrganizationInfoDetails(ORG_ID_1));
      fail("The parent should not be resolvable.");
   }
   
   /**
    * <p>
    * The ID must not be null.
    * </p>
    */
   @Test(expected = ObjectNotFoundException.class)
   public void testFindDepartmentIdNull()
   {
      sf.getQueryService().findDepartment(null, null, null);
      fail("The ID must not be null.");
   }

   /**
    * <p>
    * The ID must not be empty.
    * </p>
    */
   @Test(expected = ObjectNotFoundException.class)
   public void testFindDepartmentIdEmpty()
   {
      sf.getQueryService().findDepartment(null, "", null);
      fail("The ID must not be empty.");
   }
   
   /**
    * <p>
    * The ID must resolve to an exisiting department.
    * </p>
    */
   @Test(expected = ObjectNotFoundException.class)
   public void testFindDepartmentDepartmentDoesNotExist()
   {
      sf.getQueryService().findDepartment(null, "N/A", null);
      fail("The ID must resolve to an exisiting department.");
   }
   
   /**
    * <p>
    * Tests finding a formerly created top level department by ID.
    * </p>
    */
   @Test
   public void testFindDepartmentTld()
   {
      final Department dept = DepartmentHome.create(sf, DEPT_ID_DE, ORG_ID_1, null);
      final Department retrievedDep = sf.getQueryService().findDepartment(null, DEPT_ID_DE, new OrganizationInfoDetails(ORG_ID_1));
      assertEquals(dept, retrievedDep);
   }
   
   /**
    * <p>
    * The department must not be found when there's no parent scope defined
    * and the department is not a top-level one.
    * </p>
    */
   @Test(expected = ObjectNotFoundException.class)
   public void testFindDepartmentNotTldWithoutParent()
   {
      final Department parent = DepartmentHome.create(sf, DEPT_ID_DE, ORG_ID_2, null);
      final Department dept = DepartmentHome.create(sf, SUB_DEPT_ID_NORTH, SUB_ORG_ID_2, parent);
      
      sf.getQueryService().findDepartment(null, dept.getId(), new OrganizationInfoDetails(SUB_ORG_ID_2));
      fail("The department must not be found.");
   }
   
   /**
    * <p>
    * It should be possible to find a department, which isn't a top level one, by specifying
    * the parent department.
    * </p>
    */
   @Test
   public void testFindDepartmentNotTldWithParent()
   {
      final Department parent = DepartmentHome.create(sf, DEPT_ID_DE, ORG_ID_2, null);
      final Department dept = DepartmentHome.create(sf, SUB_DEPT_ID_NORTH, SUB_ORG_ID_2, parent);
      final Department retrievedDep = sf.getQueryService().findDepartment(parent, dept.getId(), new OrganizationInfoDetails(SUB_ORG_ID_2));
      assertEquals(dept, retrievedDep);
   }
}
