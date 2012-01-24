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
public class ServiceInfo
{
   private QName name;
   private List ports;

   public ServiceInfo(QName name, List ports)
   {
      this.name = name;
      this.ports = ports;
   }

   public QName getName()
   {
      return name;
   }

   public List getPorts()
   {
      return ports;
   }

   public String toString()
   {
      return name.getLocalPart();
   }

   public PortInfo findPort(String name)
   {
      for (int i = 0; i < ports.size(); i++)
      {
         PortInfo port = (PortInfo) ports.get(i);
         if (port.getName().equals(name))
         {
            return port;
         }
      }
      return null;
   }
}
