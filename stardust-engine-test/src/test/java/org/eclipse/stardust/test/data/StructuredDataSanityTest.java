/**********************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.data;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.eclipse.stardust.test.data.DataModelConstants.*;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.InvalidArgumentException;
import org.eclipse.stardust.common.error.InvalidValueException;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.query.DataFilter;
import org.eclipse.stardust.engine.api.query.ProcessInstanceFilter;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

/**
 * /**
 * <p>
 * Tests if structured data works as designed.
 * Creation, Modification and Deletion should affect both CLOB_DATA and STRUCT_DATA_VALUE tables.
 * </p>
 *
 * @author Roland.Stamm
 *
 */
public class StructuredDataSanityTest
{

   private static final String XPATH_ID = "IntValue";

   private static final String DEFAULT_CONTEXT = "default";

   private static final String OUT_DATA_ID = "TestData";

   private static final String XPATH_VALUE = "Test";

   private static final String XPATH_NOT_DEFINED_ID = "NotDefinedId";

   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);
   
   @Rule
   public final ExpectedException exception = ExpectedException.none();

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_NAME);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(sf);

   private long piOid;

   @Before
   public void setUp()
   {
      piOid = startProcess();
   }

   @Test
   public void testDeleteLastListElementOnlyOneList()
   {
      WorkflowService wfs = sf.getWorkflowService();

      List<String> stringsList = CollectionUtils.newList();
      stringsList.add("string1");
      stringsList.add("string2");

      Map<String, Object> legoMap = CollectionUtils.newMap();
      legoMap.put("strings", stringsList);

      // Initialize data with "string1" and "string2".
      wfs.setOutDataPath(piOid, MY_STRUC_OUT_DATA_PATH, legoMap);
      testXPathQuery(MY_STRUC_DATA_ID, "strings", "string1", true);
      testXPathQuery(MY_STRUC_DATA_ID, "strings", "string2", true);

      // Remove "string2".
      stringsList.remove(1);
      // Set a structured data with only one list element "string1".
      wfs.setOutDataPath(piOid, MY_STRUC_OUT_DATA_PATH, legoMap);
      testXPathQuery(MY_STRUC_DATA_ID, "strings", "string1", true);
      testXPathQuery(MY_STRUC_DATA_ID, "strings", "string2", false);

      // Remove "string1".
      stringsList.remove(0);
      // Set a structured data with empty list.
      wfs.setOutDataPath(piOid, MY_STRUC_OUT_DATA_PATH, legoMap);
      testXPathQuery(MY_STRUC_DATA_ID, "strings", "string1", false);
      testXPathQuery(MY_STRUC_DATA_ID, "strings", "string2", false);

      // Set strings list to null.
      legoMap.remove("strings");
      // Set a structured data with nulled string list.
      wfs.setOutDataPath(piOid, MY_STRUC_OUT_DATA_PATH, legoMap);
      testXPathQuery(MY_STRUC_DATA_ID, "strings", "string1", false);
      testXPathQuery(MY_STRUC_DATA_ID, "strings", "string2", false);

   }

   @Test
   public void testDeleteLastListElementOtherDataPresent()
   {
      WorkflowService wfs = sf.getWorkflowService();

      List<String> stringsList = CollectionUtils.newList();
      stringsList.add("string1");
      stringsList.add("string2");

      List<String> strings2List = CollectionUtils.newList(stringsList);

      Map<String, Object> legoMap = CollectionUtils.newMap();
      legoMap.put("string", "singleString");
      legoMap.put("strings", stringsList);
      legoMap.put("strings2", strings2List);

      // Initialize data with "string1" and "string2".
      wfs.setOutDataPath(piOid, MY_STRUC_OUT_DATA_PATH, legoMap);
      testXPathQuery(MY_STRUC_DATA_ID, "string", "singleString", true);
      testXPathQuery(MY_STRUC_DATA_ID, "strings", "string1", true);
      testXPathQuery(MY_STRUC_DATA_ID, "strings", "string2", true);
      testXPathQuery(MY_STRUC_DATA_ID, "strings2", "string1", true);
      testXPathQuery(MY_STRUC_DATA_ID, "strings2", "string2", true);

      // Remove "string2".
      stringsList.remove(1);
      // Set a structured data with only one list element "string1".
      wfs.setOutDataPath(piOid, MY_STRUC_OUT_DATA_PATH, legoMap);
      testXPathQuery(MY_STRUC_DATA_ID, "string", "singleString", true);
      testXPathQuery(MY_STRUC_DATA_ID, "strings", "string1", true);
      testXPathQuery(MY_STRUC_DATA_ID, "strings", "string2", false);
      testXPathQuery(MY_STRUC_DATA_ID, "strings2", "string1", true);
      testXPathQuery(MY_STRUC_DATA_ID, "strings2", "string2", true);

      // Remove "string1".
      stringsList.remove(0);
      // Set a structured data with empty list.
      wfs.setOutDataPath(piOid, MY_STRUC_OUT_DATA_PATH, legoMap);
      testXPathQuery(MY_STRUC_DATA_ID, "string", "singleString", true);
      testXPathQuery(MY_STRUC_DATA_ID, "strings", "string1", false);
      testXPathQuery(MY_STRUC_DATA_ID, "strings", "string2", false);
      testXPathQuery(MY_STRUC_DATA_ID, "strings2", "string1", true);
      testXPathQuery(MY_STRUC_DATA_ID, "strings2", "string2", true);

      // Set strings list to null.
      legoMap.remove("strings");
      // Set a structured data with nulled string list.
      wfs.setOutDataPath(piOid, MY_STRUC_OUT_DATA_PATH, legoMap);
      testXPathQuery(MY_STRUC_DATA_ID, "string", "singleString", true);
      testXPathQuery(MY_STRUC_DATA_ID, "strings", "string1", false);
      testXPathQuery(MY_STRUC_DATA_ID, "strings", "string2", false);
      testXPathQuery(MY_STRUC_DATA_ID, "strings2", "string1", true);
      testXPathQuery(MY_STRUC_DATA_ID, "strings2", "string2", true);

      // Set string to null.
      legoMap.remove("string");
      // Set a structured data with nulled strings list and nulled string.
      wfs.setOutDataPath(piOid, MY_STRUC_OUT_DATA_PATH, legoMap);
      testXPathQuery(MY_STRUC_DATA_ID, "string", "singleString", false);
      testXPathQuery(MY_STRUC_DATA_ID, "strings", "string1", false);
      testXPathQuery(MY_STRUC_DATA_ID, "strings", "string2", false);
      testXPathQuery(MY_STRUC_DATA_ID, "strings2", "string1", true);
      testXPathQuery(MY_STRUC_DATA_ID, "strings2", "string2", true);

      // Set strings2 to null.
      legoMap.remove("strings2");
      // Set a structured data with nulled strings2 list.
      wfs.setOutDataPath(piOid, MY_STRUC_OUT_DATA_PATH, legoMap);
      testXPathQuery(MY_STRUC_DATA_ID, "string", "singleString", false);
      testXPathQuery(MY_STRUC_DATA_ID, "strings", "string1", false);
      testXPathQuery(MY_STRUC_DATA_ID, "strings", "string2", false);
      testXPathQuery(MY_STRUC_DATA_ID, "strings2", "string1", false);
      testXPathQuery(MY_STRUC_DATA_ID, "strings2", "string2", false);
   }

   private void testXPathQuery(String dataId, String xPath, Serializable value, boolean shouldExist)
   {
      QueryService qs = sf.getQueryService();

      ProcessInstanceQuery query = ProcessInstanceQuery.findAlive(PROCESS_ID_3);
      query.where(DataFilter.isEqual(dataId, xPath , value));
      query.where(ProcessInstanceFilter.in(Collections.singleton(piOid)));

      try
      {
         ProcessInstance pi = qs.findFirstProcessInstance(query);
         Assert.assertEquals(shouldExist, pi != null);
      }
      catch (ObjectNotFoundException onfe)
      {
         Assert.assertFalse(shouldExist);
      }
   }
   
   /**
    * Tests if a more specific exception subclass of
    * org.eclipse.stardust.common.error.PublicException is thrown in case
    * WorkflowService.complete() is called using a structured data that has an undefined
    * element id.
    * 
    * <p>
    * See also <a
    * href="https://www.csa.sungard.com/jira/browse/CRNT-27698">CRNT-27698</a>.
    * </p>
    */
   @Test
   public void testCompleteWithNotDefinedElementId()
   {
      WorkflowService wfService = sf.getWorkflowService();
      ProcessInstance procInst = wfService.startProcess(PROCESS_ID_4, null, true);
      ActivityInstance actInst = wfService
            .activateNextActivityInstanceForProcessInstance(procInst.getOID());
      Map<String, String> testData = new HashMap<String, String>();
      testData.put(XPATH_NOT_DEFINED_ID, XPATH_VALUE);
      Map<String, Object> outData = new HashMap<String, Object>();
      outData.put(OUT_DATA_ID, testData);
      exception.expect(InvalidArgumentException.class);
      wfService.complete(actInst.getOID(), DEFAULT_CONTEXT, outData);
   }
   
   /**
    * Tests if a more specific exception subclass of
    * org.eclipse.stardust.common.error.PublicException is thrown in case
    * WorkflowService.complete() is called using a structured data that has an invalid
    * number value.
    * 
    * <p>
    * See also <a
    * href="https://www.csa.sungard.com/jira/browse/CRNT-27698">CRNT-27698</a>.
    * </p>
    */
   @Test
   public void testCompleteWithInvalidDataType()
   {
      WorkflowService wfService = sf.getWorkflowService();
      ProcessInstance procInst = wfService.startProcess(PROCESS_ID_4, null, true);
      ActivityInstance actInst = wfService
            .activateNextActivityInstanceForProcessInstance(procInst.getOID());
      Map<String, String> testData = new HashMap<String, String>();
      testData.put(XPATH_ID, XPATH_VALUE);

      Map<String, Object> outData = new HashMap<String, Object>();
      outData.put(OUT_DATA_ID, testData);
      exception.expect(InvalidValueException.class);
      wfService.complete(actInst.getOID(), DEFAULT_CONTEXT, outData);
   }

   private long startProcess()
   {
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_ID_3, null, true);
      return pi.getOID();
   }

}
