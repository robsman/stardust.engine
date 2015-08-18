package org.eclipse.stardust.engine.extensions.camel.core.route.builder;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.model.ITrigger;
import org.eclipse.stardust.engine.core.model.beans.ModelBean;
import org.eclipse.stardust.engine.extensions.camel.CamelConstants;
import org.eclipse.stardust.engine.extensions.camel.core.CamelTriggerRouteContext;
import org.eclipse.stardust.engine.extensions.camel.core.RouteDefinitionBuilder;

public class CamelTriggerRouteBuilderTest
{
   @Test
   public void testRouteGenerationTransactedEANotPresent()
   {
      IModel model = new ModelBean("TEST_MODEL", "TEST_MODEL", "");
      model.createTriggerType("camel", "camel", false, false, 0);
      // IProcessDefinition processDefinition=new ProcessDefinitionBean("TEST_PROCESS",
      // "TEST_PROCESS", "");
      IProcessDefinition processDefinition = model.createProcessDefinition(
            "TEST_PROCESS", "TEST_PROCESS", "", false, 0);
      ITrigger trigger = processDefinition.createTrigger("test_trigger", "test_trigger",
            model.findTriggerType("camel"), 0);
      trigger.setAttribute("carnot:engine:camel::username", "${camelTriggerUsername}");
      trigger.setAttribute("carnot:engine:camel::password", "${camelTriggerPassword}");
      trigger.setAttribute("carnot:engine:camel::camelContextId", "defaultCamelContext");
      trigger
            .setAttribute("carnot:engine:camel::camelRouteExt",
                  "<from uri=\"direct:testStartProcessWithoutData\" /><to uri=\"ipp:direct\"; />");
      StringBuilder expected = new StringBuilder(
            "<route id=\"Consumer870758176\" autoStartup=\"true\"><description>This route is related to test_trigger defined in TEST_MODEL. The partition is :default</description><from uri=\"direct:testStartProcessWithoutData\"/><transacted ref=\"required\" /><to uri=\"ipp:process:start\"/>\n</route>");
      assertEquals(
            expected.toString(),
            RouteDefinitionBuilder
                  .createRouteDefintionForCamelTrigger(
                        new CamelTriggerRouteContext(trigger, "default",
                              "defaultCamelContext")).toString());
   }

   @Test
   public void testRouteGenerationTransactedEASetToTrue()
   {
      IModel model = new ModelBean("TEST_MODEL", "TEST_MODEL", "");
      model.createTriggerType("camel", "camel", false, false, 0);
      // IProcessDefinition processDefinition=new ProcessDefinitionBean("TEST_PROCESS",
      // "TEST_PROCESS", "");
      IProcessDefinition processDefinition = model.createProcessDefinition(
            "TEST_PROCESS", "TEST_PROCESS", "", false, 0);
      ITrigger trigger = processDefinition.createTrigger("test_trigger", "test_trigger",
            model.findTriggerType("camel"), 0);
      trigger.setAttribute("carnot:engine:camel::username", "${camelTriggerUsername}");
      trigger.setAttribute("carnot:engine:camel::password", "${camelTriggerPassword}");
      trigger.setAttribute("carnot:engine:camel::camelContextId", "defaultCamelContext");
      trigger.setAttribute(CamelConstants.TRANSACTED_ROUTE_EXT_ATT, true);
      trigger
            .setAttribute("carnot:engine:camel::camelRouteExt",
                  "<from uri=\"direct:testStartProcessWithoutData\" /><to uri=\"ipp:direct\"; />");
      StringBuilder expected = new StringBuilder(
            "<route id=\"Consumer870758176\" autoStartup=\"true\"><description>This route is related to test_trigger defined in TEST_MODEL. The partition is :default</description><from uri=\"direct:testStartProcessWithoutData\"/><transacted ref=\"required\" /><to uri=\"ipp:process:start\"/>\n</route>");
      assertEquals(
            expected.toString(),
            RouteDefinitionBuilder
                  .createRouteDefintionForCamelTrigger(
                        new CamelTriggerRouteContext(trigger, "default",
                              "defaultCamelContext")).toString());
   }

   @Test
   public void testRouteGenerationTransactedEASetToFalse()
   {
      IModel model = new ModelBean("TEST_MODEL", "TEST_MODEL", "");
      model.createTriggerType("camel", "camel", false, false, 0);
      // IProcessDefinition processDefinition=new ProcessDefinitionBean("TEST_PROCESS",
      // "TEST_PROCESS", "");
      IProcessDefinition processDefinition = model.createProcessDefinition(
            "TEST_PROCESS", "TEST_PROCESS", "", false, 0);
      ITrigger trigger = processDefinition.createTrigger("test_trigger", "test_trigger",
            model.findTriggerType("camel"), 0);
      trigger.setAttribute("carnot:engine:camel::username", "${camelTriggerUsername}");
      trigger.setAttribute("carnot:engine:camel::password", "${camelTriggerPassword}");
      trigger.setAttribute("carnot:engine:camel::camelContextId", "defaultCamelContext");
      trigger.setAttribute(CamelConstants.TRANSACTED_ROUTE_EXT_ATT, false);
      trigger
            .setAttribute("carnot:engine:camel::camelRouteExt",
                  "<from uri=\"direct:testStartProcessWithoutData\" /><to uri=\"ipp:direct\"; />");
      StringBuilder expected = new StringBuilder(
            "<route id=\"Consumer870758176\" autoStartup=\"true\"><description>This route is related to test_trigger defined in TEST_MODEL. The partition is :default</description><from uri=\"direct:testStartProcessWithoutData\"/><to uri=\"ipp:process:start\"/>\n</route>");
      assertEquals(
            expected.toString(),
            RouteDefinitionBuilder
                  .createRouteDefintionForCamelTrigger(
                        new CamelTriggerRouteContext(trigger, "default",
                              "defaultCamelContext")).toString());
   }

}
