/*******************************************************************************
 * Copyright (c) 2011, 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.common.config;

import java.util.Locale;
import java.util.ResourceBundle;

import org.eclipse.stardust.common.StringUtils;


/**
 * Represents the current version of Infinity including major, minor, micro,
 * and build number. It is set automatically by the build process based
 * on the <code>version.properties</code>.
 */
public class CurrentVersion
{
   public static final String COPYRIGHT_YEARS = "2000-2016";
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
      String version = versionBundle.getString("version");
      VERSION = version.replaceFirst("-.*SNAPSHOT", "");
      StringBuilder build = new StringBuilder(versionBundle.getString("build"));
      // if the version contains a snapshot identifier like -RC1-SNAPSHOT...
      if(!VERSION.equals(version))
      { 
         // ..put it to the build identifier so that the info isn't lost
         String snapshotAlias = version.replace(VERSION, "").replace("-SNAPSHOT", "");
         if(StringUtils.isNotEmpty(snapshotAlias))
         {
            if(snapshotAlias.charAt(0) != '-')
            {
               build.append("-");
            }
            build.append(snapshotAlias);
         }
      }
      BUILD = build.toString();
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

}
