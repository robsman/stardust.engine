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
package org.eclipse.stardust.engine.core.pojo.data;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.model.gui.SymbolIconProvider;
import org.eclipse.stardust.engine.core.pojo.utils.JavaAccessPointType;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;


// @todo (france, ub): remove?!

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class IconProvider
{
   public static final Logger trace = LogManager.getLogger(IconProvider.class);

   private static Icon returnValueIcon;
   private static Icon parameterIcon;
   private static Icon dataIcon;
   private static Icon defaultAccessPointIcon;
   private static boolean iconsLoaded;

   private static IconProvider instance;

   /**
    * Loads all icons required to display instances of this class.
    */
   private static void loadIcons()
   {
      try
      {
         dataIcon = new ImageIcon(SymbolIconProvider.class.getResource("images/data.gif"));
         // todo: (france, fh) we need an icon for the access points.
         defaultAccessPointIcon = new ImageIcon(SymbolIconProvider.class.getResource("images/activity_application.gif"));
         // @todo/hiob (ub) choose different icon
         parameterIcon = new ImageIcon(SymbolIconProvider.class.getResource("images/param.gif"));
         returnValueIcon = new ImageIcon(SymbolIconProvider.class.getResource("images/ret_val.gif"));
      }
      catch (Exception x)
      {
//         throw new PublicException("Can't load image icon. Message was: " + x.getMessage());
         trace.warn("Can't load image icon", x);
      }
   }

   public Icon getAccessPointIcon(AccessPoint point)
   {
      if (!iconsLoaded)
      {
         loadIcons();
         iconsLoaded = true;
      }
      if (point instanceof IData)
      {
         return dataIcon;
      }
      if (JavaAccessPointType.PARAMETER.equals(point.getAttribute(PredefinedConstants.FLAVOR_ATT)))
      {
         // @todo/hiob (ub) provide icon
         return parameterIcon;
      }
      if (JavaAccessPointType.RETURN_VALUE.equals(point.getAttribute(PredefinedConstants.FLAVOR_ATT)))
      {
         // @todo/hiob (ub) provide icon
         return returnValueIcon;
      }
      return defaultAccessPointIcon;
   }

   public static IconProvider instance()
   {
      if (instance == null)
      {
         instance = new IconProvider();
      }
      return instance;
   }
}
