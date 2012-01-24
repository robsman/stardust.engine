/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.extensions.jaxws.app;

import java.util.List;

import javax.xml.namespace.QName;

/**
 * @author fherinean
 * @version $Revision$
 */
public class WSDLInfo
{
   private List services;

   public WSDLInfo(List services)
   {
      this.services = services;
   }

   public List getServices()
   {
      return services;
   }

   public ServiceInfo findService(QName name)
   {
      for (int i = 0; i < services.size(); i++)
      {
         ServiceInfo service = (ServiceInfo) services.get(i);
         if (service.getName().equals(name))
         {
            return service;
         }
      }
      return null;
   }
}
