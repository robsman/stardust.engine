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

import java.util.Iterator;
import java.util.Vector;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
class Application extends IdentifiableElement
{
   private boolean interactive;
   private String applicationTypeId;
   private Vector accessPoints = new Vector();
   private Vector contexts = new Vector();

   public Application(String id, String name, String description)
   {
      super(id, name, description);
   }

   public void setInteractive(boolean interactive)
   {
      this.interactive = interactive;
   }

   public ApplicationContext createContext(String name)
   {
      ApplicationContext context = new ApplicationContext(name);
      contexts.add(context);
      return context;
   }

   public void setApplicationTypeId(String type)
   {
      applicationTypeId = type;
   }

   public void addAccessPoint(AccessPoint ap)
   {
      accessPoints.add(ap);
   }

   public String getApplicationTypeId()
   {
      return applicationTypeId;
   }

   public boolean isInteractive()
   {
      return interactive;
   }

   public Iterator getAllContexts()
   {
      return contexts.iterator();
   }

   public Iterator getAllAccessPoints()
   {
      return accessPoints.iterator();
   }
}
