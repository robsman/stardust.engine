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
package org.eclipse.stardust.test.data;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.Serializable;
import java.util.*;

import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

/**
 * <p>
 * Tests whether setting and retrieving enumeration process data
 * via in and out data mappings works correctly.
 * </p>
 * 
 * @author Barry.Grotjahn
 * @version $Revision$
 */
public class EnumDataInOutDataMappingsTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);
   
   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);
   
   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, "EnumDataModel");
   
   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(sf);
   
   private long aiOid;
   
   @Before
   public void setUp()
   {
      startProcess();
      aiOid = findFirstAliveActivityInstanceFor();
   }
   
   /**
    * <p>
    * Tests whether setting and retrieving a <i>enum</i> process data
    * via an in and out data mapping works correctly.
    * </p>
    */
   @Test
   public void testInOutDataPathForEnum()
   {
      final Object value = "B";
      String dataMapping = "EnumData";
   
      sf.getWorkflowService().activate(aiOid);
      
      Map<String, String> dataMap = new HashMap<String, String>();
      dataMap.put(dataMapping, (String) value);
      
      sf.getWorkflowService().suspendToUser(aiOid, null, dataMap);
      final Map<String, Serializable> mapValue = sf.getWorkflowService().getInDataValues(aiOid, null, null);
      final Object retrievedValue = mapValue.get(dataMapping);
      
      
      assertThat(retrievedValue, notNullValue());
      assertThat(retrievedValue, equalTo(value));
   }
      
   private long startProcess()
   {
      final ProcessInstance pi = sf.getWorkflowService().startProcess("{EnumDataModel}ProcessDefinition1", null, true);
      return pi.getOID();
   }
   
   private long findFirstAliveActivityInstanceFor()
   {
      final ActivityInstanceQuery aiQuery = ActivityInstanceQuery.findAlive("{EnumDataModel}ProcessDefinition1");
      final ActivityInstance ai = sf.getQueryService().findFirstActivityInstance(aiQuery);
      return ai.getOID();
   }
}