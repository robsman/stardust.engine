package org.eclipse.stardust.engine.extensions.camel.core.endpoint.authentication;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.PASSWORD;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.USER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.engine.extensions.camel.CamelConstants.SubCommand;
import org.eclipse.stardust.engine.extensions.camel.common.CamelTestUtils;
import org.eclipse.stardust.engine.extensions.camel.util.client.ClientEnvironment;
import org.eclipse.stardust.engine.extensions.camel.util.client.ServiceFactoryAccess;
import org.eclipse.stardust.engine.extensions.camel.util.test.SpringTestUtils;


@ContextConfiguration(locations = {
      "AuthenticationEndpointTest-context.xml", 
      "classpath:carnot-spring-context.xml", "classpath:jackrabbit-jcr-context.xml"})
public class AuthenticationEndpointTest extends AbstractJUnit4SpringContextTests
{

   private static final transient Logger LOG = LoggerFactory.getLogger(AuthenticationEndpointTest.class);

   @Resource
   private SpringTestUtils testUtils;

   @Resource
   CamelContext camelContext;

   /**
    * Use this service factory access for testing assumptions!
    */
   @Resource
   private ServiceFactoryAccess serviceFactoryAccess;

   @Produce(uri = "direct:ippTestStart")
   protected ProducerTemplate template;

   @EndpointInject(uri = "mock:ippTestEnd")
   protected MockEndpoint resultEndpoint;

   private boolean initiated = false;

   private String testUser1 = "ikkebot.krane";

   private String testPwd1 = "XX8392aip";

   private String testUser2 = "zippifit.mbawe";

   private String testPwd2 = "ZZ233xxx";

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
      // Inject this application context into the IPP engine
      // SpringUtils.setApplicationContext( (ConfigurableApplicationContext)
      // applicationContext );

      // initiate environment
      testUtils.setUpGlobal();

      ServiceFactory sf = serviceFactoryAccess.getDefaultServiceFactory();
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
