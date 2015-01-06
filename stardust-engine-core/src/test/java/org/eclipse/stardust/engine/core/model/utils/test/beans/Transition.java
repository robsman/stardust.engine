/*******************************************************************************
 * Copyright (c) 2011, 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.model.utils.test.beans;

import javax.swing.ImageIcon;

import org.eclipse.stardust.engine.core.model.utils.ConnectionBean;
import org.eclipse.stardust.engine.core.model.utils.Identifiable;


/**
 * @author ubirkemeyer
 * @version $Revision: 42549 $
 */
public class Transition extends ConnectionBean implements Identifiable
{
   private static final long serialVersionUID = -5970719524801847268L;

   private String id;

   Transition() {}

   public Transition(String id, Activity start, Activity end)
   {
      super(start, end);
      this.id = id;
   }

   public String getId()
   {
      return id;
   }

   public String getName()
   {
      return null;  //To change body of implemented methods use Options | File Templates.
   }

   public ImageIcon getIcon()
   {
      return null;  //To change body of implemented methods use Options | File Templates.
   }

   public Activity getFromActivity()
   {
      return (Activity) getFirst();
   }

   public Activity getToActivity()
   {
      return (Activity) getSecond();
   }
}
