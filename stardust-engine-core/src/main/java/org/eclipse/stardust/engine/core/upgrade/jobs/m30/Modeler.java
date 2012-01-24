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
package org.eclipse.stardust.engine.core.upgrade.jobs.m30;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class Modeler extends IdentifiableElement
{
   private String password;

   public Modeler(String id, String name, String description, String password, int elementOID, Model model)
   {
      super(id, name, description);
      this.password = password;
      model.register(this, elementOID);
   }

   public String getPassword()
   {
      return password;
   }
}
