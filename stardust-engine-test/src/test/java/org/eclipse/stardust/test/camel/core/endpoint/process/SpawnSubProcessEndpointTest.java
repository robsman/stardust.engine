/**********************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.camel.core.endpoint.process;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.PROCESS_INSTANCE_OID;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.SubCommand.Authenticate.COMMAND_REMOVE_CURRENT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.SubCommand.Authenticate.COMMAND_SET_CURRENT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.SubCommand.Process.COMMAND_START;
import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultExchange;
import org.eclipse.stardust.test.camel.common.CamelTestUtils;
import org.eclipse.stardust.test.api.setup.AbstractCamelIntegrationTest;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

/**
 * <p>
 * TODO JavaDoc
 * </p>
 *
 * @author Sabri.Bousselmi
 */
public class SpawnSubProcessEndpointTest extends AbstractCamelIntegrationTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   public static final String[] MODEL_IDS = { "GetPropertiesInvokerModel", "PropertiesProducerModel", "ExternalXSDModel", "ProcessContinueModel", "CamelTestModel", "SpawnSubProcessModel" };

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_IDS);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(sf);

   private static CamelContext defaultCamelContext;

   private static final String FULL_ROUTE_BEGIN = "direct:startSpawnSubProcessEndpointTestRoute";

   @BeforeClass
   public static void beforeClass() throws Exception
   {
      defaultCamelContext = testClassSetup.getBean("defaultCamelContext", CamelContext.class);

      defaultCamelContext.addRoutes(createFullRoute());
   }

   @Test
   public void testStartProcess() throws Exception
   {
      Map<String, Object> headerMap = new HashMap<String, Object>();
      Exchange exchange = new DefaultExchange(defaultCamelContext);
      String messageBody = "Message from Unit Test";

      exchange = CamelTestUtils.invokeEndpoint(FULL_ROUTE_BEGIN, exchange, headerMap,
            messageBody);
   }

   public static RouteBuilder createFullRoute()
   {
      return new RouteBuilder()
      {
         @Override
         public void configure() throws Exception
         {
            from(FULL_ROUTE_BEGIN)
                  .to("ipp:authenticate:" + COMMAND_SET_CURRENT
                        + "?user=motu&password=motu")
                  .to("ipp:process:"
                        + COMMAND_START
                        + "?processId=MainProcess&modelId=SpawnSubProcessModel&synchronousMode=true&data=MessageBody::${body}")
                  .log("Created process instance OID: ${header." + PROCESS_INSTANCE_OID
                        + "}")
                  .to("ipp:process:spawnSubprocess?parentProcessInstanceOid=${header."
                        + PROCESS_INSTANCE_OID
                        + "}&processId=CustomSubProcess&copyData=false&data=MessageBody::${body}")
                  .to("ipp:authenticate:" + COMMAND_REMOVE_CURRENT);
         }
      };
   }
}
