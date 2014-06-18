/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Florin.Herinean (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.engine.core.spi.runtime;

import org.eclipse.stardust.engine.api.runtime.Service;

public interface IServiceProvider<T extends Service>
{
   public static interface Factory
   {
      <T extends Service> IServiceProvider get(Class<T> clazz);
   }

   T getInstance();

   String getName();

   String getLocalName();

   String getServiceName();

   String getSpringBeanName();

   String getJndiPropertyName();

   Class<?> getEJBRemoteClass();

   Class<?> getEJBHomeClass();

   Class<?> getLocalHomeClass();

   String getEJB3ModuleName();

   String getLocalEJB3ClassName();

   String getRemoteEJB3ClassName();
}
