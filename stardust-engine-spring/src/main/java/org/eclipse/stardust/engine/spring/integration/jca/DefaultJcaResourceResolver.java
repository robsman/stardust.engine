/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.spring.integration.jca;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.core.spi.jca.IJcaResourceProvider;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;



/**
 * @author roland.stamm, rsauer
 * @version $Revision: $
 */
public class DefaultJcaResourceResolver
      implements IJcaResourceProvider, ApplicationContextAware, InitializingBean
{
   
   public static final String COMP_ENV_PREFIX = "java:comp/env/";

   private ApplicationContext appContext;

   private Map<String, Object> jcaResources;

   @Override
   public Object resolveJcaResource(String name)
   {
      if ( !StringUtils.isEmpty(name) && name.startsWith(COMP_ENV_PREFIX))
      {
         name = name.substring(COMP_ENV_PREFIX.length());
      }
      
      return (null != jcaResources) ? jcaResources.get(name) : null;
   }

   public void setApplicationContext(ApplicationContext applicationContext)
         throws BeansException
   {
      this.appContext = applicationContext;
   }

   public void afterPropertiesSet() throws Exception
   {
      // automatically discover jca resource bindings.
      // allowEagerInit=false: do not pre-instantiate factory beans defined to be initialized lazily
      Map jcaResBindings = appContext.getBeansOfType(JcaResourceBinding.class, true, false);
      if ( !CollectionUtils.isEmpty(jcaResBindings))
      {
         if (null == jcaResources)
         {
            this.jcaResources = CollectionUtils.newMap();
         }
         
         for (Iterator i = jcaResBindings.values().iterator(); i.hasNext();)
         {
            JcaResourceBinding binding = (JcaResourceBinding) i.next();
            
            Object resource = binding.getResource();
            
            if (!jcaResources.containsKey(binding.getName()))
            {
               jcaResources.put(binding.getName(), resource);
            }
         }
      }
   }

}
