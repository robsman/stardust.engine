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


/**
 * @author roland.stamm, rsauer
 * @version $Revision: $
 */
public class JcaResourceBinding
{

   private String name;

   private Object resource;

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public Object getResource()
   {
      return resource;
   }

   public void setResource(Object resource)
   {
      this.resource = resource;
   }

}
