/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.rest;

import java.util.HashMap;
import java.util.Map;

import org.apache.cxf.jaxrs.spring.JAXRSServerFactoryBeanDefinitionParser.SpringJAXRSServerFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

public class CxfJaxRsEndpointPostProcessor
      implements BeanFactoryPostProcessor
{

   private Map<String, String> jaxRsModuleByAddress = new HashMap<String, String>();

   @Override
   public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
         throws BeansException
   {
      String[] beanNamesForType = beanFactory.getBeanNamesForType(SpringJAXRSServerFactoryBean.class, true,  false);

      for (String beanName : beanNamesForType)
      {
         BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
         MutablePropertyValues propertyValues = beanDefinition.getPropertyValues();
         PropertyValue addressPropertyValue = propertyValues.getPropertyValue("address");
         String address = (String) addressPropertyValue.getValue();

         String existingModule = jaxRsModuleByAddress.get(address);
         if (existingModule == null)
         {
            jaxRsModuleByAddress.put(address, beanName);
         }
         else
         {
            throw new UnsupportedOperationException("Jaxrs server address '" + address
                  + "' already used for module '" + existingModule + "'.");
         }
      }

   }

}
