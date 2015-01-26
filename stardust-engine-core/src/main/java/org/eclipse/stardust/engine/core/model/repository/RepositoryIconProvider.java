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
package org.eclipse.stardust.engine.core.model.repository;

import javax.swing.ImageIcon;

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.compatibility.gui.IconProvider;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class RepositoryIconProvider implements IconProvider
{
   private static ImageIcon icon;
   private static ImageIcon releasedIcon;
   private static ImageIcon privateVersionIcon;
   private static ImageIcon openVersionIcon;
   private static ImageIcon unloadedIcon;
   private static RepositoryIconProvider instance;

   public ImageIcon getIcon(Object object)
   {
      if (object instanceof ModelNode)
      {
         ModelNode model = (ModelNode) object;
         if (icon == null)
         {
            loadIcons();
         }
         if (model.isPrivateVersion())
         {
            return privateVersionIcon;
         }
         else if (model.isReleased())
         {
            return releasedIcon;
         }

         return openVersionIcon;
      }
      else
      {
         return null;
      }
   }

   /**
    * Loads all icons required to display instances of this class.
    */
   private static void loadIcons()
   {
      try
      {
         icon = new ImageIcon(ModelNodeBean.class.getResource("images/model.gif"));
      }
      catch (Exception x)
      {
         throw new PublicException(
               BpmRuntimeError.DIAG_CANNOT_LOAD_RESOURCE.raise("images/model.gif"));
      }

      try
      {
         releasedIcon = new ImageIcon(ModelNodeBean.class.getResource("images/released_version.gif"));
      }
      catch (Exception x)
      {
         throw new PublicException(
               BpmRuntimeError.DIAG_CANNOT_LOAD_RESOURCE
                     .raise("images/released_version.gif"));
      }

      try
      {
         privateVersionIcon = new ImageIcon(ModelNodeBean.class.getResource("images/private_version.gif"));
      }
      catch (Exception x)
      {
         throw new PublicException(
               BpmRuntimeError.DIAG_CANNOT_LOAD_RESOURCE
                     .raise("images/private_version.gif"));
      }

      try
      {
         openVersionIcon = new ImageIcon(ModelNodeBean.class.getResource("images/open_version.gif"));
      }
      catch (Exception x)
      {
         throw new PublicException(
               BpmRuntimeError.DIAG_CANNOT_LOAD_RESOURCE.raise("images/open_version.gif"));
      }
      try
      {
         unloadedIcon = new ImageIcon((ModelNodeBean.class.getResource("images/broken_version.gif")));
      }
      catch (Exception e)
      {
         throw new PublicException(
               BpmRuntimeError.DIAG_CANNOT_LOAD_RESOURCE
                     .raise("images/broken_version.gif"));
      }
   }

   public static IconProvider instance()
   {
      if (instance == null)
      {
         instance = new RepositoryIconProvider();
      }
      return instance;
   }

}
