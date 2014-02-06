/*******************************************************************************
 * Copyright (c) 2011, 2013 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.common.config;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;


/**
 * Represents the current version of Infinity including major, minor, micro,
 * and build number. It is set automatically by the build process based
 * on the <code>version.properties</code>.
 */
public class CurrentVersion
{
   private static final String BUILD_VERSION_NAME = "-buildVersionName";
   private static final String VERSION_NAME = "-versionName";

   public static final String COPYRIGHT_YEARS = "2000-2014";
   public static final String COPYRIGHT_MESSAGE;

   public static final String VERSION;
   public static final String BUILD;

   public static final String VENDOR_NAME;
   public static final String PRODUCT_NAME;

   static
   {
      // load version.properties from 
      // org.eclipse.stardust.ui.web.viewscommon.common.spi.env.impl package
      ResourceBundle versionBundle = ResourceBundle.getBundle(
            CurrentVersion.class.getPackage().getName() + ".version",
            Locale.getDefault(), CurrentVersion.class.getClassLoader());
      VERSION = versionBundle.getString("version");
      BUILD = versionBundle.getString("build");
      COPYRIGHT_MESSAGE = versionBundle.getString("copyright.message");
      VENDOR_NAME = versionBundle.getString("vendor.name");
      PRODUCT_NAME = versionBundle.getString("product.name");
   }

   public static String getProductName()
   {
      return PRODUCT_NAME;
   }

   public static String getVendorName()
   {
      return VENDOR_NAME;
   }

   /**
    * String representation in the form
    */
   public static String getVersionName()
   {
      return VERSION;
   }

   public static Version getVersion()
   {
      return new Version(getVersionName());
   }

   public static Version getBuildVersion()
   {
      return new Version(getBuildVersionName());
   }

   public static String getBuildVersionName()
   {
      StringBuffer name = new StringBuffer(VERSION);

      name.append(".");
      name.append(BUILD);

      return name.toString();
   }

   /*public static void main(String[] args)
   {
      String message = null;
      if (args.length == 0)
      {
         message = getVerboseVersion();
      }
      else if (args.length == 1)
      {
         if (VERSION_NAME.equals(args[0]))
         {
            message = getVersionName();
         }
         else if (BUILD_VERSION_NAME.equals(args[0]))
         {
            message = getBuildVersionName();
         }
      }

      if (null == message)
      {
         message = MessageFormat.format(
               "Invalid arguments.\nUsage: CurrentVersion [{0}]|[{1}]", new Object[] {
                     VERSION_NAME, BUILD_VERSION_NAME });
      }

      System.out.println(message);
   }*/

   private static String getVerboseVersion()
   {
      return MessageFormat.format(COPYRIGHT_MESSAGE + "\n", new Object[] {
            getVersionName(), CurrentVersion.COPYRIGHT_YEARS });
   }
}
