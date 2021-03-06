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
package org.eclipse.stardust.engine.spring.integration.jms;

import javax.jms.Destination;

/**
 * @author sauer
 * @version $Revision: $
 */
public class JmsResourceBinding
{

   private String name;

   private Destination resource;

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public Destination getResource()
   {
      return resource;
   }

   public void setResource(Destination resource)
   {
      this.resource = resource;
   }

}
