package org.eclipse.stardust.engine.extensions.camel.component;

import static org.eclipse.stardust.engine.extensions.camel.component.CamelHelper.*;
import org.apache.camel.Consumer;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.language.simple.SimpleLanguage;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.extensions.camel.CamelConstants;

public class AuthenticationEndpoint extends AbstractIppEndpoint
{

   final static Logger LOG = LogManager.getLogger(AuthenticationEndpoint.class);

   protected Expression user;
   protected Expression password;
   protected Expression partition;
   protected Expression realm;
   protected Expression domain;

   public AuthenticationEndpoint(String uri, IppComponent component)
   {
      super(uri, component);
   }
   
   /**
    * Creates a new producer which is used send messages into the endpoint
    *
    * @return a newly created producer
    * @throws Exception can be thrown
    */
public Producer createProducer() throws Exception
   {
      return new AuthenticationProducer(this);
   }

/**
 * Creates a new Event Driven Consumer which consumes messages 
 * from the endpoint using the given processor
 *
 * @param processor  the given processor
 * @return a newly created consumer
 * @throws Exception can be thrown
 */
public Consumer createConsumer(Processor processor) throws Exception
   {
      throw new UnsupportedOperationException("This endpoint cannot be used as a consumer:" + getEndpointUri());
   }

/**
 * 
 * @param exchange
 * @param strict flag
 * @return user account
 */
public String evaluateUser(Exchange exchange, boolean strict)
   {
      if (null != this.user)
      {
         return this.user.evaluate(exchange, String.class);
      }
      else
      {
         String account = exchange.getIn().getHeader(CamelConstants.MessageProperty.USER, String.class);
         if (StringUtils.isEmpty(account) && strict)
         {
            throw new IllegalStateException("Missing required user account.");
         }
         return account;
      }
   }

   /**
	 * @param exchange
	 * @return password
	 */
public String evaluatePassword(Exchange exchange)
   {
      if (null != this.password)
         return this.password.evaluate(exchange, String.class);
      else
         return exchange.getIn().getHeader(CamelConstants.MessageProperty.PASSWORD, String.class);
   }

	/**
	 * @param exchange
	 * @return partition
	 */
public String evaluatePartition(Exchange exchange)
   {
      if (null != this.partition)
         return this.partition.evaluate(exchange, String.class);
      else
         return exchange.getIn().getHeader(CamelConstants.MessageProperty.PARTITION, String.class);
   }

	/**
	 * @param exchange
	 * @return realm
	 */
public String evaluateRealm(Exchange exchange)
   {
      if (null != this.realm)
         return this.realm.evaluate(exchange, String.class);
      else
         return exchange.getIn().getHeader(CamelConstants.MessageProperty.REALM, String.class);
   }

   /**
 * @param exchange
 * @return domain
 */
public String evaluateDomain(Exchange exchange)
   {
      if (null != this.domain)
         return this.domain.evaluate(exchange, String.class);
      else
         return exchange.getIn().getHeader(CamelConstants.MessageProperty.DOMAIN, String.class);
   }

/**
 * @param user
 */
public void setUser(String user)
   {
      if (user.startsWith("${") && user.endsWith("}"))
         this.user = SimpleLanguage.simple(extractTokenFromExpression(user));
      else
         this.user = SimpleLanguage.simple(user);

   }

/**
 * @param password
 */
public void setPassword(String password)
   {
      if (password.startsWith("${") && password.endsWith("}"))
         this.password = SimpleLanguage.simple(extractTokenFromExpression(password));
      else
         this.password = SimpleLanguage.simple(password);
   }

/**
 * @param partition
 */
public void setPartition(String partition)
   {
      if (partition.startsWith("${") && partition.endsWith("}"))
         this.partition = SimpleLanguage.simple(extractTokenFromExpression(partition));
      else
         this.partition = SimpleLanguage.simple(partition);
   }

/**
 * @param realm
 */
public void setRealm(String realm)
   {
      if (realm.startsWith("${") && realm.endsWith("}"))
         this.realm = SimpleLanguage.simple(extractTokenFromExpression(realm));
      else
         this.realm = SimpleLanguage.simple(realm);
   }

/**
 * @param domain
 */
public void setDomain(String domain)
   {
      if (domain.startsWith("${") && domain.endsWith("}"))
         this.domain = SimpleLanguage.simple(extractTokenFromExpression(domain));
      else
         this.domain = SimpleLanguage.simple(domain);
   }

}
