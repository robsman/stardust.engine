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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.Documents;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.Resource;
import org.eclipse.stardust.engine.extensions.dms.data.DmsDocumentBean;
import org.eclipse.stardust.engine.extensions.dms.data.DmsFolderBean;
import org.eclipse.stardust.engine.extensions.dms.data.DmsResourceBean;

/**
 * Utility to handle the repositoryId prefix of {@link Document#getId()} and {@link Folder#getId()}.
 *
 * @author Roland.Stamm
 */
public class RepositoryIdUtils
{
   private RepositoryIdUtils()
   {
      // utility class
   }

   /**
    * The prefix that is used to identify a repositoryId.
    */
   public static final String REPOSITORY_ID_PREFIX = "{urn:repositoryId:";

   public static String extractRepositoryId(String prefixedId)
   {
      String repositoryId = null;

      if (prefixedId != null)
      {
         boolean startsWith = prefixedId.startsWith(REPOSITORY_ID_PREFIX);
         if (startsWith && prefixedId.length() > REPOSITORY_ID_PREFIX.length())
         {
            repositoryId = prefixedId.substring(REPOSITORY_ID_PREFIX.length(), prefixedId.indexOf('}'));
         }
      }

      return repositoryId;
   }

   public static List<String> extractRepositoryId(List<String> prefixedIds)
   {
      ArrayList<String> extractedIds = null;
      if (prefixedIds != null)
      {
         extractedIds = CollectionUtils.newArrayList();

         for (String prefixedId : prefixedIds)
         {
            extractedIds.add(extractRepositoryId(prefixedId));
         }
      }
      return extractedIds;
   }

   public static String extractRepositoryId(Resource resource)
   {
      if (resource != null)
      {
         String extractedId = extractRepositoryId(resource.getId());
         return extractedId;
      }
      return null;
   }

   public static String stripRepositoryId(String prefixedId)
   {
      String strippedId = null;

      if (prefixedId != null)
      {
         boolean startsWith = prefixedId.startsWith(REPOSITORY_ID_PREFIX);
         if (startsWith && prefixedId.length() > REPOSITORY_ID_PREFIX.length())
         {
            strippedId = prefixedId.substring(prefixedId.indexOf('}')+1);
         }
         else
         {
            strippedId = prefixedId;
         }
      }
      return strippedId;
   }

   public static List<String> stripRepositoryId(List<String> prefixedIds)
   {
      ArrayList<String> strippedIds = null;
      if (prefixedIds != null)
      {
         strippedIds = CollectionUtils.newArrayList();

         for (String prefixedId : prefixedIds)
         {
            strippedIds.add(stripRepositoryId(prefixedId));
         }
      }
      return strippedIds;
   }

   public static <T extends Resource> T stripRepositoryId(T resource)
   {
      if (resource != null)
      {
         String strippedId = stripRepositoryId(resource.getId());
         if (resource instanceof DmsResourceBean)
         {
            ((DmsResourceBean) resource).setId(strippedId);
         }
      }
      return resource;
   }

   public static String addRepositoryId(String id, String repositoryId)
   {
      if (repositoryId != null)
      {
         StringBuilder sb = new StringBuilder();
         sb.append(REPOSITORY_ID_PREFIX).append(repositoryId).append("}").append(id);
         return sb.toString();
      }
      else
      {
         return id;
      }
   }

   public static <T extends Resource> T addRepositoryId(T resource, String repositoryId)
   {
      if (resource != null)
      {
         String prefixedId = addRepositoryId(resource.getId(), repositoryId);

         if (resource instanceof DmsResourceBean)
         {
            ((DmsResourceBean) resource).setId(prefixedId);
         }
         if (resource instanceof DmsDocumentBean)
         {
            String revisionId = ((Document) resource).getRevisionId();
            if (!StringUtils.isEmpty(revisionId) && !RepositoryConstants.VERSION_UNVERSIONED.equals(revisionId))
            {
               String prefixedRevisionId = addRepositoryId(revisionId, repositoryId);
               ((DmsDocumentBean) resource).setRevisionId(prefixedRevisionId);
            }
         }
         else if (resource instanceof DmsFolderBean)
         {
            Folder folder = (Folder) resource;
            addRepositoryId(folder.getFolders(), repositoryId);
            addRepositoryId(folder.getDocuments(), repositoryId);
         }
      }
      return resource;
   }

   public static <T extends Resource> List<T> addRepositoryId(List<T> resources,
         String repositoryId)
   {
      List<T> prefixedResources = null;
      if (resources != null)
      {
         prefixedResources = CollectionUtils.newArrayList();
         for (T resource : resources)
         {
            prefixedResources.add(addRepositoryId(resource, repositoryId));
         }
      }
      return prefixedResources;
   }

   public static Documents addRepositoryId(Documents documents, String repositoryId)
   {
      for (Document resource : documents)
      {
         addRepositoryId(resource, repositoryId);
      }
      return documents;
   }

   public static String replaceRepositoryId(String prefixedId, String newRepositoryId)
   {
      String strippedRepositoryId = stripRepositoryId(prefixedId);
      if (newRepositoryId != null)
      {
         return addRepositoryId(strippedRepositoryId, newRepositoryId);
      }
      else
      {
         return strippedRepositoryId;
      }

   }

}
