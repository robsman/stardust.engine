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
package org.eclipse.stardust.test.camel.core.endpoint.authentication;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.PASSWORD;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.USER;
import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.engine.extensions.camel.CamelConstants.SubCommand;
import org.eclipse.stardust.engine.extensions.camel.util.client.ClientEnvironment;
import org.eclipse.stardust.test.api.setup.AbstractCamelIntegrationTest;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestRtEnvException;
import org.eclipse.stardust.test.api.setup.TestRtEnvException.TestRtEnvAction;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.eclipse.stardust.test.camel.common.CamelTestUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * TODO JavaDoc
 * </p>
 *
 * @author Sabri.Bousselmi
 */
public class AuthenticationEndpointTest extends AbstractCamelIntegrationTest
{
   private static final transient Logger LOG = LoggerFactory.getLogger(AuthenticationEndpointTest.class);

   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(sf);

   private static CamelContext camelContext;

   private static ProducerTemplate template;

   private static MockEndpoint resultEndpoint;

   private boolean initiated = false;

   private String testUser1 = "ikkebot.krane";

   private String testPwd1 = "XX8392aip";

   private String testUser2 = "zippifit.mbawe";

   private String testPwd2 = "ZZ233xxx";

   @BeforeClass
   public static void beforeClass() throws Exception
   {
      camelContext = testClassSetup.getBean("defaultCamelContext", CamelContext.class);
      camelContext.addRoutes(createSetCurrentTestRouteBuilder());

      template = camelContext.createProducerTemplate();
      template.setDefaultEndpointUri("direct:ippTestStart");

      resultEndpoint = camelContext.getEndpoint("mock:ippTestEnd", MockEndpoint.class);
   }

   @AfterClass
   public static void tearDownOnce()
   {
      try
      {
         resultEndpoint.stop();
         template.stop();
      }
      catch (Exception e)
      {
         throw new TestRtEnvException("Unable to stop producer.", e, TestRtEnvAction.APP_CTX_TEARDOWN);
      }
   }

   @Test
   public void testAuthenticateWithPwd() throws Exception
   {
      String uri;
      Map<String, Object> headerMap;
      Exchange exchange = new DefaultExchange(camelContext);
      ServiceFactory sf = null;

      // test with credentials explicitely in the endpoint URI
      // Note that the password contains illegal URL characters
      assertNull(ClientEnvironment.getCurrentServiceFactory());
      uri = "ipp:authenticate:" + SubCommand.Authenticate.COMMAND_SET_CURRENT + "?user=" + testUser1 + "&password="
            + URLEncoder.encode(testPwd1, "UTF-8");
      exchange = CamelTestUtils.invokeEndpoint(uri, exchange, null, null);
      sf = ClientEnvironment.getCurrentServiceFactory();
      assertNotNull(sf);
      assertEquals(testUser1, sf.getUserService().getUser().getAccount());

      // switch context using an object in the header
      uri = "ipp:authenticate:" + SubCommand.Authenticate.COMMAND_SET_CURRENT
            + "?user=${header.user.id}&password=${in.header.user.password}";
      headerMap = new HashMap<String, Object>();
      headerMap.put("user", new TestUser(testUser2, testPwd2));
      exchange = CamelTestUtils.invokeEndpoint(uri, exchange, headerMap, null);
      sf = ClientEnvironment.getCurrentServiceFactory();
      assertNotNull(sf);
      assertEquals(testUser2, sf.getUserService().getUser().getAccount());

      // switch back to user1 using special header constants
      uri = "ipp:authenticate:" + SubCommand.Authenticate.COMMAND_SET_CURRENT;
      headerMap = new HashMap<String, Object>();
      headerMap.put(USER, testUser1);
      headerMap.put(PASSWORD, testPwd1);
      exchange = CamelTestUtils.invokeEndpoint(uri, exchange, headerMap, null);
      sf = ClientEnvironment.getCurrentServiceFactory();
      assertNotNull(sf);
      assertEquals(testUser1, sf.getUserService().getUser().getAccount());

      // check if cleanly removed
      uri = "ipp:authenticate:" + SubCommand.Authenticate.COMMAND_REMOVE_CURRENT;
      exchange = CamelTestUtils.invokeEndpoint(uri, exchange, null, null);
      assertNull(ClientEnvironment.getCurrentServiceFactory());
   }

   @Test
   public void testAuthenticationEndpoint() throws Exception
   {
      template.sendBody("test");

      resultEndpoint.setExpectedMessageCount(1);
      resultEndpoint.assertIsSatisfied();
   }

   @Before
   public void setUp() throws Exception
   {
      if (!initiated)
         setUpGlobal();
   }

   public void setUpGlobal() throws Exception
   {
      try
      {
         QueryService qService = sf.getQueryService();
         UserService uService = sf.getUserService();

         UserQuery query1 = UserQuery.findActive();
         query1.where(UserQuery.ACCOUNT.isEqual(testUser1));
         UserQuery query2 = UserQuery.findActive();
         query2.where(UserQuery.ACCOUNT.isEqual(testUser2));

         Users result = qService.getAllUsers(query1);
         if (result.size() == 0)
         {
            LOG.info("Creating test user: " + testUser1);
            uService.createUser(testUser1, testUser1, testUser1, "", testPwd1, "", null, null);
         }
         result = qService.getAllUsers(query2);
         if (result.size() == 0)
         {
            LOG.info("Creating test user: " + testUser2);
            uService.createUser(testUser2, testUser1, testUser1, "", testPwd2, "", null, null);
         }
      }
      finally
      {
         if (null != sf)
            sf.close();
      }

      initiated = true;
   }

   public static RouteBuilder createSetCurrentTestRouteBuilder()
   {
      return new RouteBuilder()
      {
         @Override
         public void configure() throws Exception
         {
            from("direct:ippTestStart").to("ipp:authenticate:setCurrent?user=motu&password=motu")
                  .log("Current session for user motu established.").to("ipp:authenticate:removeCurrent")
                  .log("Session for user motu removed.").to("mock:ippTestEnd");
         }
      };
   }
}
