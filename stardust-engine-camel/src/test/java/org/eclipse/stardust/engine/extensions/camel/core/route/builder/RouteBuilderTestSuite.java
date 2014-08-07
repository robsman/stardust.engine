package org.eclipse.stardust.engine.extensions.camel.core.route.builder;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
      CamelConsumerApplicationRouteBuilderTest.class,
      CamelProducerApplicationRouteBuilderTest.class, CamelTriggerRouteBuilderTest.class})
public class RouteBuilderTestSuite
{

}
