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

import java.util.Vector;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class ApplicationContext extends ModelElement
{
   private String id;
   private Vector accessPoints = new Vector();

   public ApplicationContext(String name)
   {
      this.id = name;
   }

   public void addAccessPoint(AccessPoint ap)
   {
      accessPoints.add(ap);
   }

   public String getId()
   {
      return id;
   }
}
