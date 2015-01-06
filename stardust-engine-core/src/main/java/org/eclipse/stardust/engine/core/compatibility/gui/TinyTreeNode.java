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
package org.eclipse.stardust.engine.core.compatibility.gui;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public abstract class TinyTreeNode
{
   private static final Logger trace = LogManager.getLogger(TinyTreeNode.class);

   public static final Vector LEAF = new Vector();

   private static Map icons = new HashMap();

   private Vector children;

   public abstract Vector bootstrap();

   public abstract String getIconLocation();

   public abstract String getText();

   public Icon getIcon()
   {
      String iconLocation = getClass().getName() + "#" + getIconLocation();
      Icon result;
      if ((result = (Icon) icons.get(iconLocation)) == null)
      {
         URL resource = getClass().getResource(getIconLocation());
         if (resource == null)
         {
            trace.warn("Resource '" + iconLocation + "' not found.");
            return null;
         }
         try
         {
            result = new ImageIcon(resource);
         }
         catch (Exception e)
         {
            trace.warn("Couldn't load icon for resource '" + iconLocation + "'.");
            return null;
         }
         icons.put(iconLocation, result);
      }
      return result;
   }

   public  Object getChild(int index)
   {
      if (children == null)
      {
         children = bootstrap();
      }
      return children.elementAt(index);
   }

   public int getChildCount()
   {
      if (children == null)
      {
         children = bootstrap();
      }
      return children.size();
   }

   public int getIndexOfChild(Object child)
   {
      if (children == null)
      {
         children = bootstrap();
      }
      return children.indexOf(child);
   }

   public int getFontStyle()
   {
      return 0;
   }

}
