package org.eclipse.stardust.engine.extensions.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.model.language.SimpleExpression;
import org.junit.Test;

public class HeaderEvaluationTest
{
   @Test
 public void testAuthenticateWithPwd() throws Exception
 {
      CamelContext context=new DefaultCamelContext();
      Exchange exchange=new DefaultExchange(context);
      exchange.getIn().setHeader("user", "abc");
      
      exchange.getIn().setBody("test");
      
      Expression expression=new SimpleExpression("${header.user}");
      System.out.println(expression.evaluate(exchange, String.class));
      
      exchange.getIn().setHeader("personne", new Person("testName","testLAstName"));
      
      Expression expression2=new SimpleExpression("${header.personne.firstName}");
      System.out.println(expression2.evaluate(exchange, String.class));
 }
}
