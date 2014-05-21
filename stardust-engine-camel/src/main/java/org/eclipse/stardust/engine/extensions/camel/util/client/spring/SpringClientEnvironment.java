package org.eclipse.stardust.engine.extensions.camel.util.client.spring;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.spring.SpringUtils;
import org.eclipse.stardust.engine.extensions.camel.util.client.ClientEnvironment;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;



/**
 * Singleton class representing a Spring-based client environment.
 * 
 * 
 * 
 * @author JanHendrik.Scheufen
 */
public class SpringClientEnvironment extends ClientEnvironment implements ApplicationContextAware, InitializingBean
{
   private static Logger LOG = LogManager.getLogger(SpringClientEnvironment.class);

   /**
    * Specifies whether to try to use local Spring service calls or not. This is a
    * workaround for ticket CRNT-16906
    */
   private boolean useLocalServices = true;
   private ApplicationContext applicationContext;

   public SpringClientEnvironment()
   {
      super();
      if (null != _instance)
      {
         LOG.info("Replacing ClientEnvironment singleton instance of type " + _instance.getClass().getName()
               + " with one of type " + this.getClass().getName());
         _instance = this;
      }
   }

   public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
   {
      this.applicationContext = applicationContext;
   }

   public void setUseLocalServices(boolean useLocalServices)
   {
      this.useLocalServices = useLocalServices;
   }

   /**
    * @throws ClassCastException
    *            if the {@link #applicationContext} is not of type
    *            {@link ConfigurableApplicationContext}
    */
   public void afterPropertiesSet() throws Exception
   {
      // TODO document that useLocalServices has the prerequisite that the Spring
      // application
      // context in which this SpringClientEnvironment has been defined also contains the
      // Spring service stubs
      // Replace whole mechansim after fix is available for CRNT-CRNT-16906
      if (useLocalServices)
      {
         if (null != applicationContext)
         {
            // at least give warning if the new app context does not seem to contain any
            // IPP beans
            if (null == applicationContext.getBean("carnotUserService"))
               LOG.warn("The Spring application context that is about to be injected into IPP's SpringUtils does "
                     + "not seem to contain the IPP service beans! Set property 'useLocalServices' to false if "
                     + "you're running as a remote client!");
            SpringUtils.setApplicationContext((ConfigurableApplicationContext) applicationContext);
         }
         else
            LOG.warn("No Spring application context set! Client Environment is not using local Spring service beans!");
      }
   }
}
