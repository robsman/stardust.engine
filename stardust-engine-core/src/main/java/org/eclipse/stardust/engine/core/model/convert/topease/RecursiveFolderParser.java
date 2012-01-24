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

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 *
 * @author kberberich
 * @version $Revision$
 */
public abstract class RecursiveFolderParser
{
   public Node searchRootFolder(Document document)
   {
      NodeList elements;
      NodeList attributes;
      Node classFolder = null;

      elements = document.getChildNodes();
      if ("Extent".equals(elements.item(0).getNodeName()))
      {
         elements = elements.item(0).getChildNodes();

         for (int elnr=0; elnr < elements.getLength(); elnr++)
         {
            if ("AccountModel".equals(elements.item(elnr).getNodeName()))
            {
               elements = SimpleTaskUtil.getContains(elements.item(elnr));
               break;
            }
         }

         if (elements != null)
         {
            for (int i=0; i<elements.getLength(); i++)
            {
               attributes = elements.item(i).getChildNodes();

               for (int attCount=0; attCount < attributes.getLength(); attCount++)
               {
                  if ("contentType".equals(attributes.item(attCount).getNodeName()))
                  {
                     if (getContentType().equals(attributes.item(attCount).getChildNodes()
                           .item(0).getNodeValue()))
                     {
                        classFolder = elements.item(i);
                        break;
                     }
                  }
               }

               if (classFolder != null)
                  break;
            }
         }
      }
      else
      {
         throw new RuntimeException("Unexpected XML Structure!");
      }

      return classFolder;
   }

   public Map loadFolder(Node folder)
   {
      return loadFolder(folder, null);
   }

   private Map loadFolder(Node folder, Package parentPakage)
   {

      NodeList folderEntries;
      Package pakage = null;
      String name = null;
      Map returnValue;
      Map loadedFolder;
      Map loadedItem;

      returnValue = new HashMap();
      name = SimpleTaskUtil.getName(folder);

      if (!getRootFolderName().equals(name))
      {
         pakage = new Package(name, parentPakage);
      }

      folderEntries = SimpleTaskUtil.getContains(folder);

      if (folderEntries != null)
      {
         for (int i=0; i<folderEntries.getLength(); i++)
         {
            if ("Folder".equals(folderEntries.item(i).getNodeName()))
            {
               loadedFolder = loadFolder(folderEntries.item(i), pakage);
               returnValue.putAll(loadedFolder);
            }
            else if (getItemName().equals(folderEntries.item(i).getNodeName()))
            {
               loadedItem = loadItem(folderEntries.item(i), pakage);
               returnValue.putAll(loadedItem);
            }
         }
      }

      return returnValue;
   }

   abstract Map loadItem(Node item, Package parentPackage);
   abstract String getRootFolderName();
   abstract String getItemName();
   abstract String getContentType();
}
