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

import java.io.File;

/**
 * File filter that accepts files with a provided fileextension.
 */
public class SimpleFileFilter extends javax.swing.filechooser.FileFilter
{
   private String description;
   private String suffix;

   public SimpleFileFilter(String suffix, String description)
   {
      this.suffix = suffix;
      this.description = description;
   }

   public boolean accept(File file)
   {
      return ((!file.isDirectory()) && (suffix.equals(getSuffix(file))))
            || file.isDirectory();

   }

   public String getDescription()
   {
      return description;
   }

   protected String getSuffix(File file)
   {
      String _suffix = null;
      String _path = null;
      int _i = 0;

      if (file != null)
      {
         _path = file.getPath();
         if (_path.length() > 0)
         {
            _i = _path.lastIndexOf('.');
            if (_i > 0)
            {
               _suffix = _path.substring(_i + 1).toLowerCase();
            }
         }
      }
      return _suffix;
   }
}
