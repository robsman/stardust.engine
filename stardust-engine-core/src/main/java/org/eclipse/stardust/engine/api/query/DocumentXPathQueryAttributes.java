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
package org.eclipse.stardust.engine.api.query;

import static org.eclipse.stardust.vfs.VfsUtils.NS_PREFIX_VFS;
import static org.eclipse.stardust.vfs.VfsUtils.VFS_ATTRIBUTES;
import static org.eclipse.stardust.vfs.VfsUtils.VFS_NAME;
import static org.eclipse.stardust.vfs.VfsUtils.VFS_OWNER;
import static org.eclipse.stardust.vfs.VfsUtils.VFS_ATTRIBUTES_TYPE_ID;
import static org.eclipse.stardust.vfs.VfsUtils.VFS_ATTRIBUTES_TYPE_SCHEMA_LOCATION;

public class DocumentXPathQueryAttributes
{
   public static final String DATE_CREATED;

   public static final String DATE_LAST_MODIFIED;

   public static final String CONTENT;

   public static final String CONTENT_DATA;

   public static final String CONTENT_TYPE;

   public static final String ID;

   public static final String NAME;

   public static final String OWNER;

   public static final String META_DATA_ANY;

   public static final String META_DATA_NAMED;

   public static final String VFS_META_DATA;

   public static final String ATTRIBUTES_TYPE_ID;

   public static final String ATTRIBUTES_TYPE_SCHEMA_LOCATION;


   static
   {
      DATE_CREATED = "@jcr:created";
      DATE_LAST_MODIFIED = "jcr:content/@jcr:lastModified";
      CONTENT = "jcr:content";
      CONTENT_DATA = "jcr:content/@jcr:data";
      CONTENT_TYPE = "jcr:content/@jcr:mimeType";
      ID = "@jcr:uuid";
      NAME = "@" + addVfsPrefix(VFS_NAME);
      OWNER = "@" + addVfsPrefix(VFS_OWNER);
      ATTRIBUTES_TYPE_ID = "@" + addVfsPrefix(VFS_ATTRIBUTES_TYPE_ID);
      ATTRIBUTES_TYPE_SCHEMA_LOCATION = "@"
            + addVfsPrefix(VFS_ATTRIBUTES_TYPE_SCHEMA_LOCATION);
      META_DATA_ANY = addVfsPrefix(VFS_ATTRIBUTES) + "//.";
      META_DATA_NAMED = addVfsPrefix(VFS_ATTRIBUTES) + "/@" + addVfsPrefix("");
      VFS_META_DATA = addVfsPrefix("metaData/");
   }

   private static String addVfsPrefix(String string)
   {
      return NS_PREFIX_VFS + ":" + string;
   }

}
