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

import java.io.Serializable;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;


/**
 * Represents a CARNOT version as a tuple of major, minor and micro number.
 *
 * @see CurrentVersion
 * @author kberberich, ubirkemeyer
 * @version $Revision$
 */
public class Version implements Comparable<Version>, Serializable
{
   private static final long serialVersionUID = 2L;

   private int major;
   private int minor;
   private int micro;
   private String build;


   // This product name flags special comparison treatment on Version
   private static final String PRODUCT_NAME_STARDUST = "Eclipse Process Manager";

   private static final Map<Version, Version> mapStardust2Ipp = CollectionUtils.newHashMap();

   static
   {
      mapStardust2Ipp.put(Version.createFixedVersion(1, 0, 0), Version.createFixedVersion(7, 1, 0));
      mapStardust2Ipp.put(Version.createFixedVersion(1, 0, 1), Version.createFixedVersion(7, 1, 0));
      mapStardust2Ipp.put(Version.createFixedVersion(1, 1, 0), Version.createFixedVersion(7, 2, 0));
      mapStardust2Ipp.put(Version.createFixedVersion(1, 1, 1), Version.createFixedVersion(7, 2, 0));
      mapStardust2Ipp.put(Version.createFixedVersion(1, 1, 2), Version.createFixedVersion(7, 2, 0));
      mapStardust2Ipp.put(Version.createFixedVersion(2, 0, 0), Version.createFixedVersion(8, 0, 0));
      // map DEV builds to latest IPP release
      mapStardust2Ipp.put(Version.createFixedVersion(9, 9, 9), Version.createFixedVersion(8, 0, 0));
   }

   // some Versions coded in product are fixed and are not allowed to be altered during compare
   private boolean fixed = false;

   /**
    * This creates a Version instance and marks it to be fixed.
    *
    * @param major
    * @param minor
    * @param micro
    *
    * @return
    */
   public static Version createFixedVersion(int major, int minor, int micro)
   {
      return createFixedVersion(major, minor, micro, "");
   }

   /**
    * This creates a Version instance and marks it to be fixed.
    *
    * @param major
    * @param minor
    * @param micro
    * @param build
    *
    * @return
    */
   public static Version createFixedVersion(int major, int minor, int micro, String build)
   {
      Version fixedVersion = new Version(major, minor, micro, build);
      fixedVersion.setFixed(true);
      return fixedVersion;
   }

   /**
    * Returns version based on models version string. This depends on the content of vendorString as
    * special comparison needs to be enabled for comparison of version from different vendors/products.
    *
    * @param versionString
    * @param vendorString
    *
    * @return
    */
   public static Version createModelVersion(String versionString, String vendorString)
   {
      Version version = new Version(versionString);
      if ( !vendorString.contains(PRODUCT_NAME_STARDUST))
      {
         // product name not EPM -> assumed to be created with IPP
         version = Version.createFixedVersion(version.getMajor(), version.getMinor(),
               version.getMicro());
      }

      return version;
   }

   public Version(int major, int minor, int micro)
   {
      this(major, minor, micro, "");
   }

   public Version(int major, int minor, int micro, String build)
   {
      this.major = major;
      this.minor = minor;
      this.micro = micro;
      this.build = build;
   }

   public Version(String versionString)
   {
      StringTokenizer _tokenizer = new StringTokenizer(versionString, ".");
      major = Integer.parseInt(_tokenizer.nextToken());
      minor = Integer.parseInt(_tokenizer.nextToken());
      micro = Integer.parseInt(_tokenizer.nextToken().replaceFirst("-.*SNAPSHOT", ""));
      build = "";
      if (_tokenizer.hasMoreTokens())
      {
         build = _tokenizer.nextToken();
      }
   }

   public int getMajor()
   {
      return major;
   }

   public int getMinor()
   {
      return minor;
   }

   public int getMicro()
   {
      return micro;
   }

   public String getBuild()
   {
      return build;
   }

   public int compareTo(Version otherVersion) throws ClassCastException
   {
      return compareTo(otherVersion, true);
   }

   public int compareTo(Version otherVersion, boolean includeMicro) throws ClassCastException
   {
      if (PRODUCT_NAME_STARDUST.equals(CurrentVersion.PRODUCT_NAME)
            && this.fixed != otherVersion.fixed)
      {
         if (this.fixed)
         {
            return compareTo(mapStardust2Ipp.get(otherVersion), includeMicro);
         }
         else // otherVersion.fixed
         {
            return mapStardust2Ipp.get(this).compareTo(otherVersion, includeMicro);
         }
      }

      if (major < otherVersion.major)
      {
         return -1;
      }
      else if (major > otherVersion.major)
      {
         return 1;
      }

      if (minor < otherVersion.minor)
      {
         return -1;
      }
      else if (minor > otherVersion.minor)
      {
         return 1;
      }

      if (includeMicro)
      {
         if (micro < otherVersion.micro)
         {
            return -1;
         }
         else if (micro > otherVersion.micro)
         {
            return 1;
         }
      }

      return 0;
   }

   public boolean equals(Object obj)
   {
      boolean result  = obj instanceof Version;
      if(result) {
         Version that = (Version) obj;
         result = (this.major == that.major && this.minor == that.minor && this.micro == that.micro);
      }
      return result;
   }

   public int hashCode() {
      int result = 17;
      result += 37 * result + major;
      result += 37 * result + minor;
      result += 37 * result + micro;
      return result;
   }

   public String toShortString()
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append(major).append('.').append(minor);
      return buffer.toString();
   }

   public String toCompleteString()
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append(major).append('.').append(minor).append('.').append(micro);

      if (StringUtils.isNotEmpty(build))
      {
         buffer.append('.').append(build);
      }
      return buffer.toString();
   }

   public String toString()
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append(major).append('.').append(minor).append('.').append(micro);
      return buffer.toString();
   }

   private void setFixed(boolean fixed)
   {
      this.fixed = fixed;
   }
}
