package org.eclipse.stardust.engine.extensions.camel.component;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.SubCommand.Authenticate.COMMAND_REMOVE_CURRENT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.SubCommand.Authenticate.COMMAND_SET_CURRENT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.SubCommand.Authenticate.COMMAND_CURRENT_TX;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.PARTITION;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.runtime.CredentialProvider;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.ServiceFactoryLocator;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.extensions.camel.CamelConstants;
import org.eclipse.stardust.engine.extensions.camel.util.client.ClientEnvironment;

/**
 * Authentication Endpoint
 * 
 * @author Fradj.ZAYEN
 * 
 */
public class AuthenticationProducer extends AbstractIppProducer
{
   private AuthenticationEndpoint endpoint;

   public AuthenticationProducer(AuthenticationEndpoint endpoint)
   {
      super(endpoint);
      this.endpoint = endpoint;
   }

   /**
    * Processes the message exchange
    *
    * @param exchange
    *           the message exchange
    * @throws Exception
    *            if an internal processing error has occurred.
    */
   public void process(Exchange exchange) throws Exception
   {

      if (COMMAND_SET_CURRENT.equals(endpoint.getSubCommand()))
      {
         String user = endpoint.evaluateUser(exchange, true);
         String pwd = endpoint.evaluatePassword(exchange);
         String partition = endpoint.evaluatePartition(exchange);
         String realm = endpoint.evaluateRealm(exchange);
         String domain = endpoint.evaluateDomain(exchange);

         if (StringUtils.isEmpty(pwd))
         {
            ClientEnvironment.setCurrent(user, partition, realm, domain);
         }
         else
         {
            ClientEnvironment.setCurrent(user, pwd, partition, realm, domain);
         }

         if (exchange.getPattern().equals(ExchangePattern.OutOnly))
         {
            exchange.getOut().setHeader(PARTITION, partition);

            // TODO: remove after security token approach is in place
            exchange.getOut().removeHeader(CamelConstants.MessageProperty.USER);
            exchange.getOut().removeHeader(CamelConstants.MessageProperty.PASSWORD);
         }
         else
         {
            exchange.getIn().setHeader(PARTITION, partition);
            // TODO: remove after security token approach is in place
            exchange.getIn().removeHeader(CamelConstants.MessageProperty.USER);
            exchange.getIn().removeHeader(CamelConstants.MessageProperty.PASSWORD);
         }

      }
      else if (COMMAND_REMOVE_CURRENT.equals(endpoint.getSubCommand()))
      {
         ClientEnvironment.removeCurrent();
      }
      else if(COMMAND_CURRENT_TX.equals(endpoint.getSubCommand()))
      {
         ServiceFactory sf = ServiceFactoryLocator.get(CredentialProvider.CURRENT_TX);
         if(sf == null)
         {
            throw new IllegalStateException("currentTx command is invoked in the wrong context: Outside of an ongoing IPP transaction");
         } else if(SecurityProperties.getUser() == null)
         {
               throw new IllegalStateException("User not initialized. " +
                     "CurrentTx command is invoked in the wrong context: Outside of an ongoing IPP transaction");
         }
         ClientEnvironment.setCurrent(sf);
      }
   }

}
