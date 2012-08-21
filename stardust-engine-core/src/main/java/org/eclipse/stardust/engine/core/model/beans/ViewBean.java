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
package org.eclipse.stardust.engine.core.model.beans;

import java.util.Iterator;

import org.eclipse.stardust.engine.api.model.IView;
import org.eclipse.stardust.engine.api.model.IViewable;
import org.eclipse.stardust.engine.core.model.utils.Link;
import org.eclipse.stardust.engine.core.model.utils.ModelElementBean;
import org.eclipse.stardust.engine.core.model.utils.MultiRef;


/**
 * @author mgille
 */
public class ViewBean extends ModelElementBean
      implements IView
{
   private static final String NAME_ATT = "Name";
   private String name;

   private MultiRef viewables = new MultiRef(this, "Viewables");
   private Link views = new Link(this, "Subviews");

   ViewBean()
   {
   }

   public ViewBean(String name, String description)
   {
      this.name = name;
      setDescription(description);
   }

   public String toString()
   {
      return "View: " + getName();
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      markModified();

      this.name = name;
   }

   public void addToViewables(IViewable viewable)
   {
      viewables.add(viewable);
   }

   public void removeFromViewables(IViewable viewable)
   {
      viewables.remove(viewable);
   }

   public java.util.Iterator getAllViewables()
   {
      return viewables.iterator();
   }

   public IView createView(String name, String description, int elementOID)
   {
      markModified();

      ViewBean view = new ViewBean(name, description);

      views.add(view);

      view.register(elementOID);

      return view;
   }

   public void addToViews(org.eclipse.stardust.engine.api.model.IView view)
   {
      markModified();

      views.add(view);
   }

   public void removeFromViews(org.eclipse.stardust.engine.api.model.IView view)
   {
      markModified();

      views.remove(view);
   }

   public Iterator getAllViews()
   {
      return views.iterator();
   }
}
