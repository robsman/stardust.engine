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
package org.eclipse.stardust.engine.core.model.convert.topease;

import java.util.Map;

import org.w3c.dom.Node;

/**
 *
 *
 * @author kberberich
 * @version $Revision$
 */
public class BVCFolderParser extends RecursiveFolderParser
{
   private static final String ROOT_FOLDER_NAME = "BVC";
   private static final String ITEM_NAME = "BVC";
   private static final String CONTENT_TYPE = "BVC";

   Map loadItem(Node item, Package parentPackage)
   {
      return null;
   }

   String getRootFolderName()
   {
      return ROOT_FOLDER_NAME;
   }

   String getItemName()
   {
      return ITEM_NAME;
   }

   String getContentType()
   {
      return CONTENT_TYPE;
   }
}
