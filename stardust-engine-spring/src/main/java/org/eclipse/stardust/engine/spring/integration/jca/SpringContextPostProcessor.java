package org.eclipse.stardust.engine.spring.integration.jca;

import org.eclipse.stardust.engine.api.spring.SpringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

public class SpringContextPostProcessor implements BeanFactoryPostProcessor, ApplicationContextAware
{
   private ApplicationContext context;

   @Override
   public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException
   {
      SpringUtils.setApplicationContext((ConfigurableApplicationContext) this.context);
   }

   @Override
   public void setApplicationContext(ApplicationContext context) throws BeansException
   {
      this.context = context;
   }
}
