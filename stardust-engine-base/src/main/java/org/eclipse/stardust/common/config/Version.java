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
package org.eclipse.stardust.common.config;

import java.io.Serializable;
import java.util.StringTokenizer;

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
   private static final long serialVersionUID = 1L;
   
   private int major;
   private int minor;
   private int micro;
   private String build;

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
      micro = Integer.parseInt(_tokenizer.nextToken());
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
}
