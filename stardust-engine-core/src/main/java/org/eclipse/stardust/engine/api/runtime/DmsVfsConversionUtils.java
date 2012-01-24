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
package org.eclipse.stardust.engine.api.runtime;

import static org.eclipse.stardust.common.StringUtils.isEmpty;

import java.util.*;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.runtime.beans.DocumentToIFileAdapter;
import org.eclipse.stardust.engine.core.runtime.beans.FolderToIFolderAdapter;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.extensions.dms.data.*;

import com.sungard.infinity.bpm.vfs.*;



public class DmsVfsConversionUtils
{
   private static final Logger trace = LogManager.getLogger(DmsVfsConversionUtils.class);

   public static boolean isSecurityEnabled(IDocumentRepositoryService vfs,
         String resourceId)
   {
      boolean securityEnabled = false;

      try
      {
         securityEnabled = ( !vfs.getApplicablePolicies(VfsUtils.REPOSITORY_ROOT)
               .isEmpty() || !vfs.getPolicies(VfsUtils.REPOSITORY_ROOT).isEmpty());
      }
      catch (RepositoryOperationFailedException rofe)
      {
         // due to no READ_ACL privileges
         securityEnabled = true;
      }
      return securityEnabled;
   }

   public static boolean hasValidPartitionPrefix(String path, String partitionPrefix,
         AccessMode accessMode)
   {
      if (isEmpty(partitionPrefix) || path.startsWith(partitionPrefix))
      {
         return true;
      }

      if (hasValidPathPattern(path, accessMode))
      {
         return true;
      }

      if (trace.isDebugEnabled())
      {
         trace.debug("Path partition prefix not valid '" + path
               + "'. Valid path must start with '" + partitionPrefix + "'");
      }
      return false;
   }

   public static boolean hasValidPathPattern(String path, AccessMode accessMode)
   {
      switch (accessMode)
      {
      case Read:
         String pathPattern = Parameters.instance().getString(
               "DocumentManagement.ReadAllowed.PathPattern", null);
         if ( !isEmpty(pathPattern))
         {

            if (path.matches(replacePlaceholder(pathPattern)))
            {
               return true;
            }
         }
         break;
      case Write:
         String writePathPattern = Parameters.instance().getString(
               "DocumentManagement.WriteAllowed.PathPattern", null);
         if ( !isEmpty(writePathPattern))
         {
            if (path.matches(replacePlaceholder(writePathPattern)))
            {
               return true;
            }
         }
         break;
         }
      return false;
   }

   private static String replacePlaceholder(String pathPattern)
   {
      return pathPattern.trim().replace("{PARTITION_ID}",
            SecurityProperties.getPartition().getId());
   }

   public static List<Document> fromVfsDocumentList(List/* <IFile> */vfsItems,
         String partitionPrefix)
   {
      // convert list of files
      List<Document> documents = new ArrayList<Document>();

      for (int i = 0; i < vfsItems.size(); ++i)
      {
         IFile file = (IFile) vfsItems.get(i);
         if (hasValidPartitionPrefix(file.getPath(), partitionPrefix, AccessMode.Read))
         {
            documents.add(fromVfs(file, partitionPrefix));
         }
      }
      return documents;
   }

   public static List<Folder> fromVfsFolderList(List/* <IFolder> */vfsItems,
         String partitionPrefix)
   {
      // convert list of folders
      List<Folder> folders = new ArrayList<Folder>();

      for (int i = 0; i < vfsItems.size(); ++i)
      {
         IFolder folder = (IFolder) vfsItems.get(i);
         if (hasValidPartitionPrefix(folder.getPath(), partitionPrefix, AccessMode.Read))
         {
            folders.add(fromVfs(folder, partitionPrefix));
         }
      }
      return folders;
   }

   public static Document fromVfs(IFile file, String partitionPrefix)
   {
      if (null != file)
      {
         Map legoObject = CollectionUtils.newHashMap();
         AuditTrailUtils.updateFileFromVfs(legoObject, file, partitionPrefix);

         return new DmsDocumentBean(legoObject);
      }
      else
      {
         return null;
      }
   }

   public static Folder fromVfs(IFolder folder, String partitionPrefix)
   {
      if (null != folder)
      {
         Map legoObject = CollectionUtils.newHashMap();
         AuditTrailUtils.updateFolderFromVfs(legoObject, folder, partitionPrefix);

         return new DmsFolderBean(legoObject);
      }
      else
      {
         return null;
      }
   }

   public static Set<Privilege> fromVfsPrivileges(Set<IPrivilege> privileges)
   {
      if (null != privileges)
      {
         Set<Privilege> result = CollectionUtils.newSet();
         for (IPrivilege vfsPrivilege : privileges)
         {
            result.add(fromVfs(vfsPrivilege));
         }
         return result;
      }
      else
      {
         return null;
      }
   }

   public static Privilege fromVfs(final IPrivilege vfsPrivilege)
   {
      if (vfsPrivilege == null)
      {
         return null;
      }
      else if (IPrivilege.ALL_PRIVILEGE.equals(vfsPrivilege.getName()))
      {
         return DmsPrivilege.ALL_PRIVILEGES;
      }
      else if (IPrivilege.CREATE_PRIVILEGE.equals(vfsPrivilege.getName()))
      {
         return DmsPrivilege.CREATE_PRIVILEGE;
      }
      else if (IPrivilege.DELETE_CHILDREN_PRIVILEGE.equals(vfsPrivilege.getName()))
      {
         return DmsPrivilege.DELETE_CHILDREN_PRIVILEGE;
      }
      else if (IPrivilege.DELETE_PRIVILEGE.equals(vfsPrivilege.getName()))
      {
         return DmsPrivilege.DELETE_PRIVILEGE;
      }
      else if (IPrivilege.MODIFY_ACL_PRIVILEGE.equals(vfsPrivilege.getName()))
      {
         return DmsPrivilege.MODIFY_ACL_PRIVILEGE;
      }
      else if (IPrivilege.MODIFY_PRIVILEGE.equals(vfsPrivilege.getName()))
      {
         return DmsPrivilege.MODIFY_PRIVILEGE;
      }
      else if (IPrivilege.READ_ACL_PRIVILEGE.equals(vfsPrivilege.getName()))
      {
         return DmsPrivilege.READ_ACL_PRIVILEGE;
      }
      else if (IPrivilege.READ_PRIVILEGE.equals(vfsPrivilege.getName()))
      {
         return DmsPrivilege.READ_PRIVILEGE;
      }
      else
      {
         throw new IllegalArgumentException("Unknown VFS Privilege:" + vfsPrivilege);
      }
   }

   public static Set<AccessControlPolicy> fromVfsPolicies(
         Set<IAccessControlPolicy> vfsPolicies)
   {
      if (null != vfsPolicies)
      {
         Set<AccessControlPolicy> result = CollectionUtils.newSet();
         for (IAccessControlPolicy vfsPolicy : vfsPolicies)
         {
            result.add(fromVfs(vfsPolicy));
         }
         return result;
      }
      else
      {
         return null;
      }
   }

   public static AccessControlPolicy fromVfs(IAccessControlPolicy vfsPolicy)
   {
      if (null != vfsPolicy)
      {
         Set<AccessControlEntry> aces = CollectionUtils.newSet();
         for (IAccessControlEntry vfsAce : vfsPolicy.getAccessControlEntries())
         {
            aces.add(new DmsAccessControlEntry(vfsAce.getPrincipal(),
                  fromVfsPrivileges(vfsAce.getPrivileges())));
         }
         return new DmsAccessControlPolicy(aces, vfsPolicy.isNew(),
               vfsPolicy.isReadonly());
      }
      else
      {
         return null;
      }
   }

   public static IFileInfo toVfs(DocumentInfo docInfo)
   {
      return AuditTrailUtils.toFileInfo(((DmsResourceBean) docInfo).vfsResource());
   }

   public static IFile toVfs(Document doc, String partitionPrefix)
   {
      return new DocumentToIFileAdapter(doc, partitionPrefix);
   }

   public static IFolderInfo toVfs(FolderInfo folderInfo)
   {
      return AuditTrailUtils.toFolderInfo(((DmsResourceBean) folderInfo).vfsResource());
   }

   public static IFolder toVfs(Folder folder, String partitionPrefix)
   {
      return new FolderToIFolderAdapter(folder, partitionPrefix);
   }

   public static IAccessControlPolicy toVfs(AccessControlPolicy policy)
   {
      return new IAccessControlPolicyAdapter((DmsAccessControlPolicy) policy);
   }

   public static enum AccessMode {
      Read, Write
   }

}
