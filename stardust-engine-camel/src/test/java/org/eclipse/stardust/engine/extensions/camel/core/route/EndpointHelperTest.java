package org.eclipse.stardust.engine.extensions.camel.core.route;

import static org.junit.Assert.*;

import java.net.URISyntaxException;
import java.util.Map;

import org.apache.camel.util.URISupport;
import org.junit.Test;

import org.eclipse.stardust.engine.extensions.camel.EndpointHelper;

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

   @Test
   public void testRawFirstExample() throws URISyntaxException
   {
      Map<String, Object> response = URISupport.parseQuery("authMethod=Basic&authUsername=motu&authPassword=RAW(motu)");
      URISupport.resolveRawParameterValues(response);
      assertEquals(3, response.size());
      assertEquals(response.get("authPassword"), "motu");
      assertEquals(response.get("authMethod"), "Basic");
      assertEquals(response.get("authUsername"), "motu");
   }

   @Test
   public void testRawSecondExample() throws URISyntaxException
   {
      Map<String, Object> response = URISupport.parseQuery("password=RAW(se+re?t&23)&binary=true");
      URISupport.resolveRawParameterValues(response);
      assertEquals(2, response.size());
      assertEquals(response.get("password"), "se+re?t&23");
      assertEquals(response.get("binary"), "true");
   }

}
