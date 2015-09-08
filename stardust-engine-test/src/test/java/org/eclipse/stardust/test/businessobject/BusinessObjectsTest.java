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

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.eclipse.stardust.test.businessobject.BusinessObjectModelConstants.*;

import java.io.Serializable;
import java.util.*;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.ObjectExistsException;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.api.runtime.BusinessObject.Value;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.RtEnvHome;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.ActivityInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.ProcessInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

import org.junit.*;
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
public class BusinessObjectsTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);
   
   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);
   
   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_NAME2, MODEL_NAME3);
   
   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(sf);

   @Before
   public void setup()
   {
      for(int customerId = 1; customerId <= 5; customerId++)
      {
         ProcessInstance pi = sf.getWorkflowService().startProcess(new QName(MODEL_NAME2, "OrderCreation").toString(), null, true);
         List<ActivityInstance> w = getWorklist();
         Assert.assertEquals("worklist", 1, w.size());
         ActivityInstance ai = w.get(0);
         Assert.assertEquals("process instance", pi.getOID(), ai.getProcessInstanceOID());
         Assert.assertEquals("activity instance", "EnterOrderData", ai.getActivity().getId());
         Map<String, Object> order = CollectionUtils.newMap();
         order.put("date", new Date());
         order.put("customerId", customerId);
         ai = complete(ai, PredefinedConstants.DEFAULT_CONTEXT, Collections.singletonMap("Order", order));
         
         try
         {
            ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);
         }
         catch (Exception e)
         {
         }
         w = getWorklist();
      }
   }
   
   /**
    * Validate if BusinessObjectQuery returns business objects which are defined
    * in the models MODEL_NAME2 and MODEL_NAME3.
    * Note: 
    *   If BusinessObjectsList is executed standalone than bos.size() == expected.size().
    *   But if it is executed after CheckFiltering2 then bos.size() > expected.size()
    *   because CheckFiltering2 deployes MODEL_NAME3 as a new version. This means that
    *   bos.size() == expected.size()+2 in this case.
    */
   @Test
   public void BusinessObjectsList()
   {
      BusinessObjectQuery query = BusinessObjectQuery.findAll();
      query.setPolicy(new BusinessObjectQuery.Policy(BusinessObjectQuery.Option.WITH_DESCRIPTION));

      BusinessObjects bos = sf.getQueryService().getAllBusinessObjects(query);
      
      List<String> expected = CollectionUtils.newArrayListFromElements(Arrays.asList(
            new QName(MODEL_NAME2, "Account").toString(),
            new QName(MODEL_NAME2, "Customer").toString(),
            new QName(MODEL_NAME2, "Order").toString(),
            new QName(MODEL_NAME2, "Fund").toString(),
            new QName(MODEL_NAME2, "FundGroup").toString(),
            new QName(MODEL_NAME3, "Employee").toString(),
            new QName(MODEL_NAME3, "Fund").toString()));
      
      List<String> removedEntries = CollectionUtils.newArrayList(expected.size());
      
      for (BusinessObject bo : bos)
      {
         String qualifiedBOId = new QName(bo.getModelId(), bo.getId()).toString();
         if(expected.remove(qualifiedBOId))
         {
            removedEntries.add(qualifiedBOId);
         }
         else
         {
            Assert.assertTrue("Not expected entry: " + qualifiedBOId, 
                  removedEntries.contains(qualifiedBOId));
         }
      }
      Assert.assertTrue("Missing business objects: " + expected, expected.isEmpty());
   }
   
   /**
    * Test if the business object query returns all business object instances for a 
    * given business object id. The BO instances was created by the OrderCreation process
    * resp. EnterOrderData activity in the setup() method.
    */
   @Test
   public void CheckOrders()
   {
      DeployedModelDescription model = sf.getQueryService().getModels(DeployedModelQuery.findActiveForId(MODEL_NAME2)).get(0);

      BusinessObjectQuery query = BusinessObjectQuery.findForBusinessObject(new QName(model.getId(), "Order").toString());
      query.setPolicy(new BusinessObjectQuery.Policy(BusinessObjectQuery.Option.WITH_VALUES));

      BusinessObjects bos =  sf.getQueryService().getAllBusinessObjects(query);
      Assert.assertEquals("Objects", 1, bos.getSize());
      BusinessObject bo = bos.get(0);
      List<BusinessObject.Value> values = bo.getValues();
      Assert.assertTrue("Expected at least 5 values: " + values.size(), 5 <= values.size());
      checkValue(values, false, "customerId", 1, 2, 3, 4, 5);
   }
   
   /**
    * Create business object instances via API (not via process instances) and validate
    * if they're created and if a business object query for a given primary key
    * returns the corresponding BO.
    */
   @Test
   public void CreateCustomersCheck()
   {
      DeployedModelDescription model = sf.getQueryService().getModels(DeployedModelQuery.findActiveForId(MODEL_NAME2)).get(0);
      for (int i = 1; i <= 3; i++)
      {
         createCustomer(model, i);
      }

      String businessObjectQualifiedId = new QName(model.getId(), "Customer").toString();
      BusinessObjectQuery query = BusinessObjectQuery.findForBusinessObject(businessObjectQualifiedId);
      query.setPolicy(new BusinessObjectQuery.Policy(BusinessObjectQuery.Option.WITH_VALUES));
      BusinessObjects bos = sf.getQueryService().getAllBusinessObjects(query);
      Assert.assertEquals("Objects", 1, bos.getSize());
      BusinessObject bo = bos.get(0);
      List<BusinessObject.Value> values = bo.getValues();
      Assert.assertEquals("Values", 3, values.size());

      query = BusinessObjectQuery.findWithPrimaryKey(businessObjectQualifiedId, 2);
      query.setPolicy(new BusinessObjectQuery.Policy(BusinessObjectQuery.Option.WITH_VALUES));
      bos = sf.getQueryService().getAllBusinessObjects(query);
      Assert.assertEquals("Objects", 1, bos.getSize());
      bo = bos.get(0);
      values = bo.getValues();
      Assert.assertEquals("Values", 1, values.size());
      checkValue(values, true, "firstName", "Danny2");
   }
   
   /**
    * Validate if BO instances can be created once only with the same primary key and that
    * they can be queried either via findWithPrimaryKey() or with help of data filters.
    */
   @Test
   public void CreateOrdersCheck()
   {
      DeployedModelDescription model = sf.getQueryService().getModels(DeployedModelQuery.findActiveForId(MODEL_NAME2)).get(0);
      createOrder(model, 666);
      try
      {
         createOrder(model, 666);
         Assert.fail("Extected BPMRT03825 error message");
      }
      catch (ObjectExistsException ex)
      {
         Assert.assertEquals("Error code", "BPMRT03825", ex.getError().getId());
      }

      String businessObjectQualifiedId = new QName(model.getId(), "Order").toString();
      BusinessObjectQuery query = BusinessObjectQuery.findWithPrimaryKey(businessObjectQualifiedId, 666);
      query.setPolicy(new BusinessObjectQuery.Policy(BusinessObjectQuery.Option.WITH_VALUES));
      BusinessObjects bos = sf.getQueryService().getAllBusinessObjects(query);
      Assert.assertEquals("Objects", 1, bos.getSize());
      BusinessObject bo = bos.get(0);
      List<Value> values = bo.getValues();
      Assert.assertEquals("Values", 1, values.size());
      checkValue(values, true, "customerId", 666);

      query = BusinessObjectQuery.findForBusinessObject(businessObjectQualifiedId);
      query.getFilter().addOrTerm()
            .or(DataFilter.isEqual("Order", "customerId", 2))
            .or(DataFilter.isEqual("Order", "customerId", 4));
      query.setPolicy(new BusinessObjectQuery.Policy(BusinessObjectQuery.Option.WITH_VALUES));
      bos = sf.getQueryService().getAllBusinessObjects(query);
      Assert.assertEquals("Objects", 1, bos.getSize());
      bo = bos.get(0);
      values = bo.getValues();
      Assert.assertEquals("Values", 2, values.size());
      checkValue(values, true, "customerId", 2, 4);
   }
   
   /**
    * Test if a field, other than the primary key, of a BO instance can be modified.
    */
   @Test
   public void ModifyOrdersCheck()
   {
      DeployedModelDescription model = sf.getQueryService().getModels(DeployedModelQuery.findActiveForId(MODEL_NAME2)).get(0);
      BusinessObject bo = createOrder(model, 777);
      @SuppressWarnings("unchecked") final Map<String, Object> order = (Map<String, Object>) bo.getValues().get(0).getValue();
      Date date = (Date) order.get("date");
      order.put("date", new Date(date.getTime() + TIME_LAPSE));
      sf.getWorkflowService().updateBusinessObjectInstance(new QName(model.getId(), "Order").toString(), (Serializable) order);
      BusinessObjectQuery query = BusinessObjectQuery.findWithPrimaryKey(new QName(model.getId(), "Order").toString(), 777);
      query.setPolicy(new BusinessObjectQuery.Policy(BusinessObjectQuery.Option.WITH_VALUES));
      BusinessObjects bos = sf.getQueryService().getAllBusinessObjects(query);
      bo = bos.get(0);
      @SuppressWarnings("unchecked") Map<String, Object> updatedOrder = (Map<String, Object>) bo.getValues().get(0).getValue();
      Date updatedDate = (Date) updatedOrder.get("date");
      Assert.assertEquals("Time difference", TIME_LAPSE, updatedDate.getTime() - date.getTime());
   }
   
   /**
    * Check if an already created BO instance can be deleted and created again later.
    */
   @Test
   public void DeleteOrdersCheck()
   {
      DeployedModelDescription model = sf.getQueryService().getModels(DeployedModelQuery.findActiveForId(MODEL_NAME2)).get(0);
      createOrder(model, 888);
      BusinessObjectQuery query = BusinessObjectQuery.findWithPrimaryKey(new QName(model.getId(), "Order").toString(), 888);
      query.setPolicy(new BusinessObjectQuery.Policy(BusinessObjectQuery.Option.WITH_VALUES));
      BusinessObjects bos = sf.getQueryService().getAllBusinessObjects(query);
      Assert.assertEquals("Values", 1, bos.getSize());
      
      sf.getWorkflowService().deleteBusinessObjectInstance(new QName(model.getId(), "Order").toString(), 888);
      
      bos = sf.getQueryService().getAllBusinessObjects(query);
      Assert.assertEquals("Values", 0, bos.getSize());
      createOrder(model, 888);
      bos = sf.getQueryService().getAllBusinessObjects(query);
      Assert.assertEquals("Values", 1, bos.getSize());
   }
   
   /**
    * Create some instances for a given BO and validate if they can be queried:
    * <ul>
    *   <li>where the qualified business object id is set</li>
    *   <li>the primary key is passed to findForBusinessObject()</li>
    *   <li>the qualified business object id is passed to findForBusinessObject
    *       and the primary key is set as a data filter</li>
    *   <li>the qualified business object id is passed to findForBusinessObject
    *       and an attribute of the BO is set as a data filter</li>
    * </ul> 
    */
   @Test
   public void CheckFiltering() throws Exception
   {
      DeployedModelDescription model = sf.getQueryService().getModels(DeployedModelQuery.findActiveForId(MODEL_NAME3)).get(0);

      String businessObjectQualifiedId = new QName(model.getId(), "Fund").toString();
      for (int i = 1; i <= 9; i++)
      {
         final Map<String, Object> fund = CollectionUtils.newMap();
         fund.put("AccountNumber", "100100" + i);
         fund.put("AccountName", "Fund" + i);

         sf.getWorkflowService().createBusinessObjectInstance(businessObjectQualifiedId, (Serializable) fund);
      }

      BusinessObjectQuery query = BusinessObjectQuery.findForBusinessObject(businessObjectQualifiedId);
      query.setPolicy(new BusinessObjectQuery.Policy(BusinessObjectQuery.Option.WITH_VALUES));
      BusinessObjects bos = sf.getQueryService().getAllBusinessObjects(query);
      Assert.assertEquals("Objects", 1, bos.getSize());
      BusinessObject bo = bos.get(0);
      List<BusinessObject.Value> values = bo.getValues();
      Assert.assertEquals("Values", 9, values.size());

      query = BusinessObjectQuery.findWithPrimaryKey(businessObjectQualifiedId, "1001003");
      query.setPolicy(new BusinessObjectQuery.Policy(BusinessObjectQuery.Option.WITH_VALUES));
      bos = sf.getQueryService().getAllBusinessObjects(query);
      Assert.assertEquals("Objects", 1, bos.getSize());
      bo = bos.get(0);
      values = bo.getValues();
      Assert.assertEquals("Values", 1, values.size());
      checkValue(values, true, "AccountNumber", "1001003");

      query = BusinessObjectQuery.findForBusinessObject(businessObjectQualifiedId);
      query.where(DataFilter.isEqual("Fund", "AccountNumber", "1001005"));
      query.setPolicy(new BusinessObjectQuery.Policy(BusinessObjectQuery.Option.WITH_VALUES));
      bos = sf.getQueryService().getAllBusinessObjects(query);
      Assert.assertEquals("Objects", 1, bos.getSize());
      bo = bos.get(0);
      values = bo.getValues();
      Assert.assertEquals("Values", 1, values.size());
      checkValue(values, true, "AccountNumber", "1001005");

      query = BusinessObjectQuery.findForBusinessObject(businessObjectQualifiedId);
      query.where(DataFilter.isEqual("Fund", "AccountName", "Fund7"));
      query.setPolicy(new BusinessObjectQuery.Policy(BusinessObjectQuery.Option.WITH_VALUES));
      bos = sf.getQueryService().getAllBusinessObjects(query);
      Assert.assertEquals("Objects", 1, bos.getSize());
      bo = bos.get(0);
      values = bo.getValues();
      Assert.assertEquals("Values", 1, values.size());
      checkValue(values, true, "AccountName", "Fund7");
   }
   
   /**
    * Create some instances for a given BO and validate if they can be queried where:
    * <ul>
    *   <li>the qualified business object id is set</li>
    *   <li>the primary key is passed to findForBusinessObject()</li>
    *   <li>the qualified business object id is passed to findForBusinessObject
    *       and an attribute of the BO is set as a data filter</li>
    *   <li>the qualified business object id is passed to findForBusinessObject and
    *       the query is restricted to the currently active model</li>
    *   <li>the qualified business object id is passed to findForBusinessObject and
    *       across all deployed model versions</li>
    *   <li>the qualified business object id is passed to findForBusinessObject and
    *       the query is restricted to a given modelOid</li>
    * </ul> 
    */
   @Test
   public void CheckFiltering2() throws Exception
   {
      DeployedModelDescription model = sf.getQueryService().getModels(DeployedModelQuery.findActiveForId(MODEL_NAME3)).get(0);

      String businessObjectQualifiedId = new QName(model.getId(), "Employee").toString();
      
      createEmployee(businessObjectQualifiedId, "1", "Florin");
      createEmployee(businessObjectQualifiedId, "Sid", "2");

      BusinessObjectQuery query = BusinessObjectQuery.findForBusinessObject(businessObjectQualifiedId);
      query.setPolicy(new BusinessObjectQuery.Policy(BusinessObjectQuery.Option.WITH_VALUES));
      BusinessObjects bos = sf.getQueryService().getAllBusinessObjects(query);
      Assert.assertEquals("Objects", 1, bos.getSize());
      BusinessObject bo = bos.get(0);
      List<BusinessObject.Value> values = bo.getValues();
      Assert.assertEquals("Values", 2, values.size());

      query = BusinessObjectQuery.findWithPrimaryKey(businessObjectQualifiedId, "1");
      query.setPolicy(new BusinessObjectQuery.Policy(BusinessObjectQuery.Option.WITH_VALUES));
      bos = sf.getQueryService().getAllBusinessObjects(query);
      Assert.assertEquals("Objects", 1, bos.getSize());
      bo = bos.get(0);
      values = bo.getValues();
      Assert.assertEquals("Values", 1, values.size());
      checkValue(values, true, "EmpID", "1");

      query = BusinessObjectQuery.findWithPrimaryKey(businessObjectQualifiedId, "Sid");
      query.setPolicy(new BusinessObjectQuery.Policy(BusinessObjectQuery.Option.WITH_VALUES));
      bos = sf.getQueryService().getAllBusinessObjects(query);
      Assert.assertEquals("Objects", 1, bos.getSize());
      bo = bos.get(0);
      values = bo.getValues();
      Assert.assertEquals("Values", 1, values.size());
      checkValue(values, true, "EmpID", "Sid");

      query = BusinessObjectQuery.findForBusinessObject(businessObjectQualifiedId);
      query.where(DataFilter.isEqual("Employee", "EmpName", "2"));
      query.setPolicy(new BusinessObjectQuery.Policy(BusinessObjectQuery.Option.WITH_VALUES));
      bos = sf.getQueryService().getAllBusinessObjects(query);
      Assert.assertEquals("Objects", 1, bos.getSize());
      bo = bos.get(0);
      values = bo.getValues();
      Assert.assertEquals("Values", 1, values.size());
      checkValue(values, true, "EmpName", "2");

      query = BusinessObjectQuery.findForBusinessObject(businessObjectQualifiedId);
      query.where(DataFilter.isEqual("Employee", "EmpName", "Florin"));
      query.setPolicy(new BusinessObjectQuery.Policy(BusinessObjectQuery.Option.WITH_VALUES));
      bos = sf.getQueryService().getAllBusinessObjects(query);
      Assert.assertEquals("Objects", 1, bos.getSize());
      bo = bos.get(0);
      values = bo.getValues();
      Assert.assertEquals("Values", 1, values.size());
      checkValue(values, true, "EmpName", "Florin");
      
      RtEnvHome.deployModel(sf.getAdministrationService(), null, MODEL_NAME3);
      createEmployee(businessObjectQualifiedId, "3", "Meyer");
      query = BusinessObjectQuery.findForBusinessObject(PredefinedConstants.ACTIVE_MODEL, businessObjectQualifiedId);
      query.setPolicy(new BusinessObjectQuery.Policy(BusinessObjectQuery.Option.WITH_VALUES));
      bos = sf.getQueryService().getAllBusinessObjects(query);
      Assert.assertEquals("Objects", 1, bos.getSize());
      bo = bos.get(0);
      values = bo.getValues();
      Assert.assertEquals("Values", 1, values.size());

      query = BusinessObjectQuery.findForBusinessObject(businessObjectQualifiedId);
      query.setPolicy(new BusinessObjectQuery.Policy(BusinessObjectQuery.Option.WITH_VALUES));
      bos = sf.getQueryService().getAllBusinessObjects(query);
      Assert.assertEquals("Objects", 2, bos.getSize());
      Assert.assertEquals("Values", 3, getTotalSize(bos));

      query = BusinessObjectQuery.findForBusinessObject(model.getModelOID(), businessObjectQualifiedId);
      query.setPolicy(new BusinessObjectQuery.Policy(BusinessObjectQuery.Option.WITH_VALUES));
      bos = sf.getQueryService().getAllBusinessObjects(query);
      Assert.assertEquals("Objects", 1, bos.getSize());
      bo = bos.get(0);
      values = bo.getValues();
      Assert.assertEquals("Values", 2, values.size());
   }
   
   /**
    * The following test case should ensure that
    * <ul>
    *    <li>Any modifications to an attribute of a BOI via API isn't reflected to 
    *        process data which are using the BO</li>
    *    <li>Any modifications to an attribute of a BOI via the process data is only reflected
    *        to the BOI which is attached to the synthetic process instance and that it
    *        doesn't affect other BOIs which are used in other processes
    * </ul>
    */
   @Test
   public void checkFilteringOnBusinessObjectAttrChange()
   {
      // setup
      final int customerIdOffset = 100;
      final int customerCount = 3;
      for(int customerId = 1; customerId <= customerCount; customerId++)
      {
         ProcessInstance pi = sf.getWorkflowService().startProcess(new QName(MODEL_NAME2, 
               "DistributedOrder").toString(), null, true);
         List<ActivityInstance> w = getWorklist(pi);
         Assert.assertEquals("worklist", 1, w.size());
         ActivityInstance ai = w.get(0);
         Assert.assertEquals("activity instance", "CreateOrder", ai.getActivity().getId());
         Map<String, Object> order = CollectionUtils.newMap();
         order.put("date", new Date());
         order.put("customerId", customerIdOffset + customerId);
         order.put("items", "item " + customerId);
         ai = complete(ai, PredefinedConstants.DEFAULT_CONTEXT, Collections.singletonMap("Order", order));
         
         try
         {
            ActivityInstanceStateBarrier.instance().await(ai.getOID(), ActivityInstanceState.Completed);
         }
         catch (Exception e)
         {
         }
      }
      
      // after DistributeCreation activity is completed we have the following state:
      // * 2 asynchronous subprocesses are started: one which copies the data and the 
      //   other one which doesn't
      // * 3 synchronous subprocesses are triggered: one with shared data, one with separate
      //   but copied data and the last one with separate data without copying
      // This results into the following state:
      // * Each process has created four business object instances
      //   * One which is attached to a synthetic process instance
      //   * 3 other BOIs which are attached to a real process instance
      String businessObjectQualifiedId = new QName(MODEL_NAME2, "Order").toString();
      BusinessObjectQuery businessObjectQuery = BusinessObjectQuery.findForBusinessObject(businessObjectQualifiedId);
      businessObjectQuery.getFilter().addAndTerm().add(DataFilter.greaterThan("Order", "customerId", customerIdOffset));
      businessObjectQuery.setPolicy(new BusinessObjectQuery.Policy(BusinessObjectQuery.Option.WITH_VALUES, BusinessObjectQuery.Option.WITH_DESCRIPTION));
      BusinessObjects bos = sf.getQueryService().getAllBusinessObjects(businessObjectQuery);
      Assert.assertEquals("Only one business object, namely Order, is expected", 1, bos.getSize());
      Assert.assertEquals("Business object instances count isn't the same as started process ergo the count of the synthetic process instances", 
            customerCount, getTotalSize(bos));
      
      // Wait that all ShowOrder processes are started (unfortunately we cannot use ProcessInstanceStateBarrier here
      // because of the async processes.
      ProcessInstanceQuery piQuery = ProcessInstanceQuery.findAlive("ShowOrder");
      boolean waitForPIs = true;
      while(waitForPIs)
      {
         long instanceCount = sf.getQueryService().getProcessInstancesCount(piQuery);
         waitForPIs = instanceCount != (customerCount * 5);
         
         if(waitForPIs)
         {
            try
            {
               Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
            }
         }
      }
      
      BusinessObject bo = bos.get(0);
      BusinessObject.Value customer101 = null;
      for(BusinessObject.Value boValue : bo.getValues())
      {
         Map<?, ?> boAttr = (Map< ? , ? >) boValue.getValue();
         Integer customerId = (Integer) boAttr.get("customerId");
         if(Integer.valueOf(customerIdOffset+1).equals(customerId))
         {
            customer101 = boValue;
         }
      }
      Assert.assertNotNull("Customer " + customerIdOffset+1 + " not found", customer101);
      
      // Update BOI via API...
      ((Map)customer101.getValue()).put("items", "newitems");
      sf.getWorkflowService().updateBusinessObjectInstance(businessObjectQualifiedId, customer101.getValue());
      
      
      // ...and validate if no process data is modified
      piQuery = ProcessInstanceQuery.findActive();
      FilterTerm filter = piQuery.getFilter().addAndTerm();
      filter.add(DataFilter.between("Order", "customerId", customerIdOffset, customerIdOffset+customerCount));
      filter.add(DataFilter.like("Order", "items", "item%"));
      filter.addAndTerm().add(ProcessInstanceHierarchyFilter.ROOT_PROCESS);
      piQuery.setPolicy(SubsetPolicy.UNRESTRICTED);
      ProcessInstances rootPIs = sf.getQueryService().getAllProcessInstances(piQuery);
      // Root process instances are the DistributedOrder processes and the ShowOrder processes which was started
      // as async processes and which had copied the data
      Assert.assertEquals("Changes in BOIs must not be reflected in process instance data",
            customerCount * 2, rootPIs.getTotalCount());
      
      // Update BOI for a given process via data path...
      long piOid = rootPIs.get(0).getOID();
      ((Map)customer101.getValue()).put("items", "newitems1");
      sf.getWorkflowService().setOutDataPath(piOid, "OrderDataPath", (Map)customer101.getValue());
      
      // ...and validate if the BOI is updated...
      businessObjectQuery = BusinessObjectQuery.findWithPrimaryKey(
            businessObjectQualifiedId, ((Map)customer101.getValue()).get("customerId"));
      businessObjectQuery.setPolicy(new BusinessObjectQuery.Policy(BusinessObjectQuery.Option.WITH_VALUES));
      bos = sf.getQueryService().getAllBusinessObjects(businessObjectQuery);
      Assert.assertEquals("Only one business object, namely Order, is expected", 1, bos.getSize());
      List<BusinessObject.Value> boValues = bos.get(0).getValues();
      Assert.assertEquals(1, boValues.size());
      Assert.assertEquals("newitems1", ((Map)boValues.get(0).getValue()).get("items"));
      
      // ...but the other process instance data should be untouched
      piQuery = ProcessInstanceQuery.findActive();
      filter = piQuery.getFilter().addAndTerm();
      filter.add(DataFilter.between("Order", "customerId", customerIdOffset, customerIdOffset+customerCount));
      filter.add(DataFilter.like("Order", "items", "item%"));
      filter.addAndTerm().add(ProcessInstanceHierarchyFilter.ROOT_PROCESS);
      piQuery.setPolicy(SubsetPolicy.UNRESTRICTED);
      rootPIs = sf.getQueryService().getAllProcessInstances(piQuery);
      Assert.assertEquals("Changes in BOIs must not be reflected in process instance data",
            (customerCount * 2) - 1, rootPIs.getTotalCount());
      
   }

   /* helper methods */
      
   private BusinessObject createOrder(DeployedModelDescription model, int customerId)
   {
      final Map<String, Object> order = CollectionUtils.newMap();
      order.put("date", new Date());
      order.put("customerId", customerId);

      return sf.getWorkflowService().createBusinessObjectInstance(new QName(model.getId(), "Order").toString(), (Serializable) order);
   }
   
   private void createEmployee(String bo, String id, String name)
   {
      final Map<String, Object> fund = CollectionUtils.newMap();
      fund.put("EmpID", id);
      fund.put("EmpName", name);
      sf.getWorkflowService().createBusinessObjectInstance(bo, (Serializable) fund);
   }
   
   private BusinessObject createCustomer(DeployedModelDescription model, int customerId)
   {
      final Map<String, Object> order = CollectionUtils.newMap();
      order.put("id", customerId);
      order.put("firstName", "Danny" + customerId);
      order.put("lastName", "North" + customerId);

      return sf.getWorkflowService().createBusinessObjectInstance(new QName(model.getId(), "Customer").toString(), (Serializable) order);
   }
      
   private void checkValue(List<BusinessObject.Value> boValues, boolean strict, String name, Object... values)
   {
      Set<Object> expected = CollectionUtils.newSetFromIterator(Arrays.asList(values).iterator());
      Set<Object> actual = CollectionUtils.newSet();
      for (BusinessObject.Value boValue : boValues)
      {
         Map<?, ?> data = (Map<? , ? >) boValue.getValue();
         actual.add(data.get(name));
      }
      if (strict)
      {
         Assert.assertEquals("Values: ", expected, actual);
      }
      else
      {
         expected.removeAll(actual);
         Assert.assertTrue("Missing values: " + expected, expected.isEmpty());
      }
   }
   
   private ActivityInstance complete(ActivityInstance ai, String context, Map<String, ?> data)
   {
      WorkflowService ws = sf.getWorkflowService();
      if (ai.getState() != ActivityInstanceState.Application)
      {
         ai = ws.activate(ai.getOID());
      }
      ai = ws.complete(ai.getOID(), context, data);
      return ai;
   }
   
   @SuppressWarnings("unchecked")
   private List<ActivityInstance> getWorklist(EvaluationPolicy... policies)
   {
      WorkflowService ws = sf.getWorkflowService();
      WorklistQuery query = WorklistQuery.findCompleteWorklist();
      if (policies != null)
      {
         for (EvaluationPolicy policy : policies)
         {
            query.setPolicy(policy);
         }
      }
      Worklist worklist = ws.getWorklist(query);
      return worklist.getCumulatedItems();
   }
   
   @SuppressWarnings("unchecked")
   private List<ActivityInstance> getWorklist(ProcessInstance pi, EvaluationPolicy... policies)
   {
      WorkflowService ws = sf.getWorkflowService();
      WorklistQuery query = WorklistQuery.findCompleteWorklist();
      if (policies != null)
      {
         for (EvaluationPolicy policy : policies)
         {
            query.setPolicy(policy);
         }
      }
      query.getFilter().add(new ProcessInstanceFilter(pi.getOID(), false));
      Worklist worklist = ws.getWorklist(query);
      return worklist.getCumulatedItems();
   }
   
   private int getTotalSize(BusinessObjects bos)
   {
      int size = 0;
      for (BusinessObject bo : bos)
      {
         size += bo.getValues().size();
      }
      return size;
   }
}