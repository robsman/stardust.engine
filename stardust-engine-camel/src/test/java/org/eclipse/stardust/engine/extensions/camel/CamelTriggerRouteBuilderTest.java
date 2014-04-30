package org.eclipse.stardust.engine.extensions.camel;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IApplication;
import org.eclipse.stardust.engine.core.model.beans.ApplicationBean;
import org.eclipse.stardust.engine.core.model.beans.ApplicationTypeBean;
import org.eclipse.stardust.engine.core.model.beans.ModelBean;
import org.junit.Test;

public class CamelTriggerRouteBuilderTest
{
   private static final Logger logger = LogManager.getLogger(CamelTriggerRouteBuilderTest.class.getCanonicalName());

   @Test
   public void testRouteGenerationExampleWithSplit()
   {
      StringBuilder actual = new StringBuilder(
            "<route id=\"Consumer290263333\" autoStartup=\"true\"><from uri=\"file:c:/temp\"/><transacted ref=\"required\" /><split><token/><setHeader headerName=\"ippOrigin\"><constant>triggerConsumer</constant></setHeader><setHeader headerName=\"ippPassword\"><constant>motu</constant></setHeader><setHeader headerName=\"ippUser\"><constant>motu</constant></setHeader><setHeader headerName=\"ippPartition\"><constant>default</constant></setHeader><to uri=\"ipp:authenticate:setCurrent\" /><to uri=\"ipp:process:start?modelId=dummyModel&amp;processId=dummyProcess\"/></split>");
      actual.append("\n</route>");

      StringBuilder providedRouteDefinition = new StringBuilder("<from   uri=\"file:c:/temp\" />");
      providedRouteDefinition.append("<split>");
      providedRouteDefinition.append("<token/>");
      providedRouteDefinition.append("<to  uri=\"ipp:direct\" />");
      providedRouteDefinition.append("</split>");
      assertEquals(
            actual.toString(),
            RouteDefinitionBuilder.createRouteDefintionForCamelTrigger(providedRouteDefinition.toString(), "default",
                  "dummyModel", "dummyProcess", "dummyTrigger", "motu", "motu", new MappingExpression()).toString());
   }

   @Test
   public void testRouteGenerationBasicExample()
   {
      StringBuilder actual = new StringBuilder(
            "<route id=\"Consumer290263333\" autoStartup=\"true\"><from uri=\"file:c:/temp\"/><transacted ref=\"required\" /><setHeader headerName=\"ippOrigin\"><constant>triggerConsumer</constant></setHeader><setHeader headerName=\"ippPassword\"><constant>motu</constant></setHeader><setHeader headerName=\"ippUser\"><constant>motu</constant></setHeader><setHeader headerName=\"ippPartition\"><constant>default</constant></setHeader><to uri=\"ipp:authenticate:setCurrent\" /><to uri=\"ipp:process:start?modelId=dummyModel&amp;processId=dummyProcess\"/>");
      actual.append("\n</route>");

      StringBuilder providedRouteDefinition = new StringBuilder("<from   uri=\"file:c:/temp\" />");
      providedRouteDefinition.append("<to  uri=\"ipp:direct\" />");
      assertEquals(
            actual.toString(),
            RouteDefinitionBuilder.createRouteDefintionForCamelTrigger(providedRouteDefinition.toString(), "default",
                  "dummyModel", "dummyProcess", "dummyTrigger", "motu", "motu", new MappingExpression()).toString());
   }

}
