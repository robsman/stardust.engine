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

import static org.eclipse.stardust.engine.api.query.DeployedModelQuery.findForId;
import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.eclipse.stardust.test.businessobject.BusinessObjectModelConstants.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.ObjectExistsException;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.query.BusinessObjectQuery;
import org.eclipse.stardust.engine.api.query.BusinessObjects;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.DepartmentHome;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

/**
 * <p>
 * This class tests BO functionality.
 * <p>
 *
 * @author Barry.Grotjahn
 * @version $Revision$
 */
public class CreateBusinessObjectTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_NAME1);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(sf);

   @Test
   public void testCleanupAuditTrailKeepBO()
   {
      Map<String, Object> initialValue = CollectionUtils.newMap();
      initialValue.put("name", "Company A"); // PK
      initialValue.put("street", "Dark way");
      initialValue.put("price", 300);

      BusinessObject businessObjectInstance = sf.getWorkflowService().createBusinessObjectInstance(qualifiedBusinessObjectId1, initialValue);
      assertNotNull(businessObjectInstance);
      assertThat(businessObjectInstance.getId(), is(getBOId(qualifiedBusinessObjectId1)));

      sf.getAdministrationService().cleanupRuntime(true, true);

      BusinessObjectQuery query = BusinessObjectQuery.findForBusinessObject(qualifiedBusinessObjectId1);
      query.setPolicy(new BusinessObjectQuery.Policy(BusinessObjectQuery.Option.WITH_VALUES));
      BusinessObjects bos = sf.getQueryService().getAllBusinessObjects(query);
      assertThat(bos.size(), is(not(0)));
      businessObjectInstance = bos.get(0);
      assertNotNull(businessObjectInstance);
      assertThat(businessObjectInstance.getId(), is(getBOId(qualifiedBusinessObjectId1)));

      sf.getAdministrationService().cleanupRuntime(true, false);

      query = BusinessObjectQuery.findForBusinessObject(qualifiedBusinessObjectId1);
      query.setPolicy(new BusinessObjectQuery.Policy(BusinessObjectQuery.Option.WITH_VALUES));
      bos = sf.getQueryService().getAllBusinessObjects(query);
      assertThat(bos.size(), is(0));
   }

   /**
    * Test if the business object is created via the WorkflowService. Furthermore a
    * department must be created which is assigned to the managed organization.
    */
   @Test
   public void testCreateBO()
   {
      Map<String, Object> initialValue = CollectionUtils.newMap();
      initialValue.put("name", "Company A"); // PK
      initialValue.put("street", "Dark way"); // Name Value
      initialValue.put("price", 300);

      Data data = findDataForUpdate(qualifiedBusinessObjectId1);

      BusinessObject businessObjectInstance = sf.getWorkflowService().createBusinessObjectInstance(qualifiedBusinessObjectId1, initialValue);
      assertNotNull(businessObjectInstance);
      assertThat(businessObjectInstance.getId(), is(getBOId(qualifiedBusinessObjectId1)));

      final List<Department> deps = sf.getQueryService().findAllDepartments(null, null);
      assertNotNull(deps);
      assertThat(deps.size(), is(1));
      Department targetDepartment = deps.iterator().next();
      assertNotNull(targetDepartment);
      assertNotNull(targetDepartment.getOrganization());
      assertThat(targetDepartment.getId(), is(getPK(data, initialValue)));
      assertThat(targetDepartment.getName(), is(getNameValue(data, initialValue)));
      assertThat(targetDepartment.getOrganization().getId(), is(getOrganizationIds(data).get(0)));
   }

   /**
    * Validate that it is not possible to create more than one business object instances
    * with the same primary key
    */
   @Test(expected = ObjectExistsException.class)
   public void testCreateBOExists()
   {
      Map<String, Object> initialValue = CollectionUtils.newMap();
      initialValue.put("name", "Company A"); // PK
      initialValue.put("street", "Dark way");
      initialValue.put("price", 300);

      BusinessObject businessObjectInstance = sf.getWorkflowService().createBusinessObjectInstance(qualifiedBusinessObjectId1, initialValue);
      assertNotNull(businessObjectInstance);

      initialValue.put("name", "Company A"); // PK
      initialValue.put("street", "Light way");
      initialValue.put("price", 400);

      businessObjectInstance = sf.getWorkflowService().createBusinessObjectInstance(qualifiedBusinessObjectId1, initialValue);
   }

   /**
    * Test if the automatic department generation for business objects is reusing
    * the already existing department if a new BO is created where the primary key of
    * the BO instance has the same value as the corresponding departmentId.
    */
   @Test
   public void testCreateBODepartmentExists()
   {
      Data data = findDataForUpdate(qualifiedBusinessObjectId1);

      Map<String, Object> initialValue = CollectionUtils.newMap();
      initialValue.put("name", "Company A");
      initialValue.put("street", "Dark way");
      initialValue.put("price", 300);

      DepartmentHome.create(sf, (String) getPK(data, initialValue),
            (String) getNameValue(data, initialValue), getOrganizationIds(data).get(0), null);

      BusinessObject businessObjectInstance = sf.getWorkflowService().createBusinessObjectInstance(qualifiedBusinessObjectId1, initialValue);
      assertNotNull(businessObjectInstance);
      assertThat(businessObjectInstance.getId(), is(getBOId(qualifiedBusinessObjectId1)));

      final List<Department> deps = sf.getQueryService().findAllDepartments(null, null);
      assertNotNull(deps);
      assertThat(deps.size(), is(1));
      Department targetDepartment = deps.iterator().next();

      assertNotNull(targetDepartment);
      assertNotNull(targetDepartment.getOrganization());
      assertThat(targetDepartment.getId(), is(getPK(data, initialValue)));
      assertThat(targetDepartment.getName(), is(getNameValue(data, initialValue)));
      assertThat(targetDepartment.getOrganization().getId(), is(getOrganizationIds(data).get(0)));
   }

   /**
    * Test that the business object instance can be updated and that the engine doesn't
    * try to create a new department but is reusing the already existing one.
    */
   @Test
   public void testUpdateBODepartmentExists()
   {
      Map<String, Object> initialValue1 = CollectionUtils.newMap();
      initialValue1.put("name", "Company A");
      initialValue1.put("street", "Dark way");
      initialValue1.put("price", 300);
      Map<String, Object> initialValue2 = CollectionUtils.newMap();
      initialValue2.put("name", "Company A");
      initialValue2.put("street", "Dark way2");
      initialValue2.put("price", 3002);

      Data data = findDataForUpdate(qualifiedBusinessObjectId1);

      DepartmentHome.create(sf, (String) getPK(data, initialValue1),
            (String) getNameValue(data, initialValue1), getOrganizationIds(data).get(0), null);

      BusinessObject businessObjectInstance1 = sf.getWorkflowService().createBusinessObjectInstance(qualifiedBusinessObjectId1, initialValue1);
      assertNotNull(businessObjectInstance1);
      assertThat(businessObjectInstance1.getId(), is(getBOId(qualifiedBusinessObjectId1)));

      BusinessObject businessObjectInstance2 = sf.getWorkflowService().updateBusinessObjectInstance(qualifiedBusinessObjectId1, initialValue2);
      assertNotNull(businessObjectInstance2);
      assertThat(businessObjectInstance2.getId(), is(getBOId(qualifiedBusinessObjectId1)));

      final List<Department> deps = sf.getQueryService().findAllDepartments(null, null);
      assertNotNull(deps);
      assertThat(deps.size(), is(1));
      Department targetDepartment = deps.iterator().next();

      assertNotNull(targetDepartment);
      assertNotNull(targetDepartment.getOrganization());
      assertThat(targetDepartment.getId(), is(getPK(data, initialValue2)));
      assertThat(targetDepartment.getName(), is(getNameValue(data, initialValue2)));
      assertThat(targetDepartment.getOrganization().getId(), is(getOrganizationIds(data).get(0)));
   }

   /* helper methods */

   private String getBOId(String qualifiedBusinessObjectId)
   {
      QName qname = QName.valueOf(qualifiedBusinessObjectId);
      return qname.getLocalPart();
   }

   private Data findDataForUpdate(String qualifiedBusinessObjectId)
   {
      QName qname = QName.valueOf(qualifiedBusinessObjectId);
      String modelId = qname.getNamespaceURI();
      String businessObjectId = qname.getLocalPart();

      Models models = sf.getQueryService().getModels(findForId(modelId));
      if (models == null)
      {
         return null;
      }
      Model model = sf.getQueryService().getModel(models.get(0).getModelOID(), false);
      if(model != null)
      {
         @SuppressWarnings("unchecked")
         List<Data> allData = model.getAllData();
         for (Data data : allData)
         {
            String dataId = data.getId();
            if (businessObjectId.equals(dataId))
            {
               return data;
            }
         }
      }
      return null;
   }

   private List<String> getOrganizationIds(Data data)
   {
      List<String> organizationIds = new ArrayList<String>();

      String managedOrganizations = (String) data.getAttribute(PredefinedConstants.BUSINESS_OBJECT_MANAGEDORGANIZATIONS);
      if(!StringUtils.isEmpty(managedOrganizations))
      {
         String[] managedOrganizationsArray = managedOrganizations.split(",");
         for (String organizationFullId : managedOrganizationsArray)
         {
            organizationFullId = organizationFullId.substring(1, organizationFullId.length() - 1);
            //organizationFullId = organizationFullId.replaceAll("\\[", "");
            //organizationFullId = organizationFullId.replaceAll("\\]", "");
            organizationFullId = organizationFullId.replaceAll("\\\"", "");

            String organizationId = organizationFullId;
            if (organizationFullId.split(":").length > 1)
            {
               organizationId = organizationFullId.split(":")[1];
               organizationIds.add(organizationId);
            }
         }
      }

      return organizationIds;
   }

   private Object getNameValue(Data data, Object value)
   {
      String nameExpression = (String) data.getAttribute(PredefinedConstants.BUSINESS_OBJECT_NAMEEXPRESSION);
      if (value instanceof Map)
      {
         Object nameValue = ((Map<?, ?>) value).get(nameExpression);
         if (nameValue != null)
         {
            return nameValue;
         }
      }

      return null;
   }

   private Object getPK(Data data, Object value)
   {
      String pkId = (String) data.getAttribute(PredefinedConstants.PRIMARY_KEY_ATT);
      if (value instanceof Map)
      {
         Object pk = ((Map<?, ?>) value).get(pkId);
         if (pk != null)
         {
            return pk;
         }
      }

      return null;
   }
}