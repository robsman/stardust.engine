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
public class View extends ModelElement
{
   private String name;
   private Vector views = new Vector();
   private Vector viewables = new Vector();

   public View(String name, String description, int elementOID, Model model)
   {
      this.name = name;
      setDescription(description);
      model.register(this, elementOID);
   }

   public void addViewable(ModelElement viewable)
   {
      viewables.add(viewable);
   }

   public void addView(View view)
   {
      views.add(view);
   }

   public String getName()
   {
      return name;
   }

   public Iterator getAllSubViews()
   {
      return views.iterator();
   }

   public Iterator getAllViewables()
   {
      return viewables.iterator();
   }
}
