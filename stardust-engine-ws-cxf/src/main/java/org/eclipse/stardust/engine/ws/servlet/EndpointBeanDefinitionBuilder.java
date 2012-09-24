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
package org.eclipse.stardust.engine.ws.servlet;

import java.util.List;

import javax.xml.ws.handler.Handler;

import org.apache.cxf.jaxws.spring.EndpointDefinitionParser.SpringEndpointImpl;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

/**
 * <p>
 * Wraps Spring's bean definition builder in order to facilitate
 * the creation of Spring endpoint beans.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision: 56688 $
 */
public class EndpointBeanDefinitionBuilder
{
   private static final String ADDRESS_PROPERTY_KEY = "address";
   private static final String CHECK_BLOCK_CONSTRUCT_PROPERTY_KEY = "checkBlockConstruct";
   private static final String HANDLERS_PROPERTY_KEY = "handlers";
   private static final String WSDL_LOCATION_KEY = "wsdlLocation";
   
   private static final String INIT_METHOD = "publish";
   private static final String DESTROY_METHOD = "stop";
   
   private final BeanDefinitionBuilder builder;

   public EndpointBeanDefinitionBuilder(final String busBeanRef, final Class<?> implementor)
   {
      if (busBeanRef == null)
      {
         throw new NullPointerException("CXF bus bean ID must not be null.");
      }
      if (busBeanRef.isEmpty())
      {
         throw new IllegalArgumentException("CXF bus bean ID must not be empty.");
      }
      if (implementor == null)
      {
         throw new NullPointerException("WS endpoint implementation class must not be null.");
      }
      
      this.builder = initBuilder(busBeanRef, implementor);
   }
      
   public EndpointBeanDefinitionBuilder address(final String address)
   {
      if (address == null)
      {
         throw new NullPointerException("Address must not be null.");
      }
      if (address.isEmpty())
      {
         throw new IllegalArgumentException("Address must not be empty.");
      }
      
      builder.addPropertyValue(ADDRESS_PROPERTY_KEY, address);
      return this;
   }

   public EndpointBeanDefinitionBuilder handlers(final List<? extends Handler<?>> handlers)
   {
      if (handlers == null)
      {
         throw new NullPointerException("Handlers must not be null.");
      }
      if (handlers.isEmpty())
      {
         throw new IllegalArgumentException("Handlers must not be empty.");
      }
      
      builder.addPropertyValue(HANDLERS_PROPERTY_KEY, handlers);
      return this;
   }
   
   public EndpointBeanDefinitionBuilder wsdlLocation(final String wsdlLocation)
   {
      if (wsdlLocation == null)
      {
         throw new NullPointerException("WSDL location must not be null.");
      }

      builder.addPropertyValue(WSDL_LOCATION_KEY, wsdlLocation);
      return this;
   }
   
   public BeanDefinition build()
   {
      return builder.getBeanDefinition();
   }
      
   private BeanDefinitionBuilder initBuilder(final String busBeanId, final Class<?> implementor)
   {
      final BeanDefinition implClass = BeanDefinitionBuilder.genericBeanDefinition(implementor).getBeanDefinition();

      final BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(SpringEndpointImpl.class);
      builder.addConstructorArgReference(busBeanId)
             .addConstructorArgValue(implClass)
             /* don't know why - taken from the CXF implementation 'org.apache.cxf.jaxws.spring.EndpointDefinitionParser' */
             .addPropertyValue(CHECK_BLOCK_CONSTRUCT_PROPERTY_KEY, Boolean.TRUE)
             .setInitMethodName(INIT_METHOD)
             .setDestroyMethodName(DESTROY_METHOD)
             .setLazyInit(false);
      
      return builder;
   }
}
