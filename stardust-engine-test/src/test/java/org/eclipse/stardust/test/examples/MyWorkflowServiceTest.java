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
package org.eclipse.stardust.test.examples;

import static org.eclipse.stardust.test.examples.MyConstants.MODEL_NAME;
import static org.eclipse.stardust.test.util.TestConstants.MOTU;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.test.api.ClientServiceFactory;
import org.eclipse.stardust.test.api.LocalJcrH2Test;
import org.eclipse.stardust.test.api.RuntimeConfigurer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

/**
 * <p>
 * This is an example for a class that contains functional tests
 * for the <code>WorkflowService</code> running in a
 * local Spring environment, using a H2 DB and providing JCR support.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class MyWorkflowServiceTest extends LocalJcrH2Test
{
   private static final String PROCESS_DEF_ID = "ProcessDefinition_1";
   
   private final ClientServiceFactory serviceFactory = new ClientServiceFactory(MOTU, MOTU);
   private final RuntimeConfigurer rtConfigurer = new RuntimeConfigurer(serviceFactory, MODEL_NAME);
   
   @Rule
   public TestRule chain = RuleChain.outerRule(serviceFactory)
                                    .around(rtConfigurer);
   
   @Test
   public void testStartProcess()
   {
      final WorkflowService wfService = serviceFactory.getWorkflowService();
      final ProcessInstance pi = wfService.startProcess(PROCESS_DEF_ID, null, true);
      
      assertNotNull(pi);
      assertThat(pi.getState(), is(ProcessInstanceState.Completed));
   }
}
