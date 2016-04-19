package org.eclipse.stardust.engine.extensions.json;

import static org.junit.Assert.assertEquals;

import java.util.*;

import org.junit.Test;
import org.eclipse.stardust.engine.extensions.json.GsonHandler;
import org.eclipse.stardust.engine.extensions.templating.core.Request;

public class TestMarshaller
{
   @Test
   public void testMarshallSimpleStructure()
   {
      Request request = new Request();
      request.setTemplateUri("classpath://custom/templates/simpleTemplate.vm");
      request.setFormat("text");
      request.setConvertToPdf(false);
      Map<String, Object> parameters = new HashMap<String, Object>();
      Map<String, Object> customer = new HashMap<String, Object>();
      customer.put("firstName", "abc");
      customer.put("lastName", "cba");
      parameters.put("customer", customer);
      Map<String, Object> contract = new HashMap<String, Object>();
      contract.put("number", "1002");
      contract.put("Name", "cba");
      parameters.put("contract", contract);
      parameters.put("aName", "some name");
      request.setParameters(parameters);
      Map<String, Object> output = new HashMap<String, Object>();
      output.put("accessPoint", "key");
      output.put("activityInstance", 123);
      request.setOutput(output);
      GsonHandler marshaller = new GsonHandler();
      String json = marshaller.toJson(request);
      String expected = "{\"templateUri\":\"classpath://custom/templates/simpleTemplate.vm\",\"format\":\"text\",\"pdf\":false,\"parameters\":{\"aName\":\"some name\",\"contract\":{\"Name\":\"cba\",\"number\":\"1002\"},\"customer\":{\"lastName\":\"cba\",\"firstName\":\"abc\"}},\"output\":{\"activityInstance\":123,\"accessPoint\":\"key\"}}";
      assertEquals(expected.length(), json.length());
   }

   @Test
   public void testMarshallMapListStructure()
   {
      Request request = new Request();
      request.setTemplateUri("classpath://custom/templates/simpleTemplate.vm");
      request.setFormat("text");
      request.setConvertToPdf(false);
      Map<String, Object> parameters = new HashMap<String, Object>();
      Map<String, Object> customer = new HashMap<String, Object>();
      customer.put("firstName", "abc");
      customer.put("lastName", "cba");
      parameters.put("customer", customer);
      Map<String, Object> contract = new HashMap<String, Object>();
      contract.put("number", "1002");
      contract.put("Name", "cba");
      parameters.put("contract", contract);
      parameters.put("aName", "some name");
      List<Map<String, Object>> persons = new ArrayList<Map<String, Object>>();
      Map<String, Object> person1 = new HashMap<String, Object>();
      person1.put("firstName", "abc");
      person1.put("lastName", "cba");
      Map<String, Object> address1 = new HashMap<String, Object>();
      address1.put("street", "abc");
      address1.put("city", "cba");
      person1.put("address", address1);
      persons.add(person1);
      Map<String, Object> person2 = new HashMap<String, Object>();
      person2.put("firstName", "abc");
      person2.put("lastName", "cba");
      Map<String, Object> address2 = new HashMap<String, Object>();
      address2.put("street", "abc");
      address2.put("city", "cba");
      person2.put("address", address2);
      persons.add(person2);
      parameters.put("persons", persons);
      request.setParameters(parameters);
      Map<String, Object> output = new HashMap<String, Object>();
      output.put("accessPoint", "key");
      output.put("activityInstance", 123);
      request.setOutput(output);
      GsonHandler marshaller = new GsonHandler();
      String json = marshaller.toJson(request);
      String expected = "{\"templateUri\":\"classpath://custom/templates/simpleTemplate.vm\",\"format\":\"text\",\"pdf\":false,\"parameters\":{\"aName\":\"some name\",\"contract\":{\"Name\":\"cba\",\"number\":\"1002\"},\"customer\":{\"lastName\":\"cba\",\"firstName\":\"abc\"},\"persons\":[{\"lastName\":\"cba\",\"address\":{\"street\":\"abc\",\"city\":\"cba\"},\"firstName\":\"abc\"},{\"lastName\":\"cba\",\"address\":{\"street\":\"abc\",\"city\":\"cba\"},\"firstName\":\"abc\"}]},\"output\":{\"activityInstance\":123,\"accessPoint\":\"key\"}}";
      assertEquals(expected.length(), json.length());
   }
}
