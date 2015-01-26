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
package org.eclipse.stardust.engine.core.model.utils.test.beans;

import javax.swing.ImageIcon;

import org.eclipse.stardust.engine.core.model.utils.ConnectionBean;


/**
 * @author ubirkemeyer
 * @version $Revision: 4834 $
 */
public class DataMapping extends ConnectionBean
{
   private static final long serialVersionUID = -2802669689996211960L;

   private String id;

   DataMapping() {}

   public DataMapping(String id, Activity activity, Data data)
   {
      super(activity, data);
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
}
