package org.eclipse.stardust.engine.extensions.camel;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.*;
import static org.eclipse.stardust.engine.extensions.camel.RouteHelper.*;
import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

public class EndpointHelperTest
{

   @Test
   public void testUriHavingMultipleAnd()
   {
      String uri = "imaps://imap.gmail.com?username=test@gmail.com&password=test&delete=false&unseen=true&consumer.delay=60000";
      String response = EndpointHelper.sanitizeUri(uri);
      String expectedResponse = "imaps://imap.gmail.com?username=test@gmail.com&amp;password=test&amp;delete=false&amp;unseen=true&amp;consumer.delay=60000";
      assertTrue(expectedResponse.equalsIgnoreCase(response));

   }

   @Test
   public void testUriHavingOneAnd()
   {
      String uri = "imaps://imap.gmail.com?username=test@gmail.com&password=test";
      String response = EndpointHelper.sanitizeUri(uri);
      String expectedResponse = "imaps://imap.gmail.com?username=test@gmail.com&amp;password=test";
      assertTrue(expectedResponse.equalsIgnoreCase(response));
   }

//   @Test
//   public void testKeyClassMapping()
//   {
//      Map<String, String> endpoints = getManagedEndpoints();
//      String className = GENERIC_ENDPOINT;
//      String reponse = getKeyByValue(endpoints, className);
//   }

   @Test
   public void testReplaceSymbolicEndpoint()
   {
      String expected = "<route id=\"FileTrigger\" autoStartup=\"true\" ><from uri=\"file://C:/tmp/camel/sdt?delay=5000\" /><convertBodyTo type=\"java.lang.String\"/><to uri=\"ipp:authenticate:setCurrent?user=motu&amp;password=motu\" /><to uri=\"ipp:process:start?processId=FileTriggerWithBodyConversion&amp;data=BodyContent::$simple{bodyAs(java.lang.String)}\"/></route>";

      String providedRouteDefinition = "<route id=\"FileTrigger\" autoStartup=\"true\" ><from uri=\"file://C:/tmp/camel/sdt?delay=5000\" />"
            + "<convertBodyTo type=\"java.lang.String\"/>" + "<to uri=\"ipp:direct\" />" + "</route>";
      String replacement = "ipp:authenticate:setCurrent?user=motu&amp;password=motu\" />"
            + "<to uri=\"ipp:process:start?processId=FileTriggerWithBodyConversion&amp;data=BodyContent::$simple{bodyAs(java.lang.String)}\"";
      assertTrue(replaceSymbolicEndpoint(providedRouteDefinition, replacement).equalsIgnoreCase(expected));
   }

   @Test
   public void testReplaceSymbolicEndpointAnotherExample()
   {
      String providedRouteDefinition = "<from uri=\"file://C:/tmp/camel/sdt?delay=5000\" />"
+"<convertBodyTo type=\"java.lang.String\"/>"
+"<to uri=\"ipp:direct\" />";
      String replacement = "ipp:authenticate:setCurrent?user=motu&amp;password=motu\" />"
            + "<to uri=\"ipp:process:start?processId=FileTriggerWithBodyConversion&amp;data=BodyContent::$simple{bodyAs(java.lang.String)}\"";
      replaceSymbolicEndpoint(providedRouteDefinition, replacement);
   }
}
