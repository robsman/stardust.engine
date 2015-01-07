/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.spi.dms;

import java.io.Serializable;

public class StringEntry implements Serializable
{
   private static final long serialVersionUID = 1L;

   private String key;

   private String type;

   private String value;

   private StringMap collection;

   public StringEntry()
   {
   }

   public StringEntry(String key, String type, String value, StringMap collection)
   {
      super();
      this.key = key;
      this.type = type;
      this.value = value;
      this.collection = collection;
   }

   public String getKey()
   {
      return key;
   }

   public String getType()
   {
      return type;
   }

   public String getValue()
   {
      return value;
   }

   public StringMap getCollection()
   {
      return collection;
   }

   @Override
   public String toString()
   {
      StringBuilder builder = new StringBuilder();
      builder.append("StringEntry [key=");
      builder.append(key);
      builder.append(", type=");
      builder.append(type);
      builder.append(", value=");
      builder.append(value);
      builder.append(", collection=");
      builder.append(collection);
      builder.append("]");
      return builder.toString();
   }



}