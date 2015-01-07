package org.eclipse.stardust.test.dms.repository.mock;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.query.DocumentQuery;
import org.eclipse.stardust.engine.api.runtime.AccessControlPolicy;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentInfo;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException;
import org.eclipse.stardust.engine.api.runtime.Documents;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.FolderInfo;
import org.eclipse.stardust.engine.api.runtime.Privilege;
import org.eclipse.stardust.engine.api.runtime.RepositoryMigrationReport;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryService;
import org.eclipse.stardust.engine.core.spi.dms.RepositoryResourceUtils;
import org.eclipse.stardust.engine.extensions.dms.data.DmsDocumentBean;

public class MockService implements IRepositoryService
{

   public static final String MOCK_DOCUMENT_ID = "abc";
   public static final String MOCK_DOCUMENT_REV_ID = "abc_ver";
   public static final String MOCK_FOLDER_ID = "fol";

   @Override
   public Document getDocument(String documentId)
         throws DocumentManagementServiceException
   {
      return RepositoryResourceUtils.createDocument(MOCK_DOCUMENT_ID);
   }

   @Override
   public List< ? extends Document> getDocumentVersions(String documentId)
         throws DocumentManagementServiceException
   {
      return Collections.singletonList(
            RepositoryResourceUtils.createDocument(MOCK_DOCUMENT_ID, "/", 0, null, null,
                  null, MOCK_DOCUMENT_REV_ID, "v0", null, null));
   }

   @Override
   public List< ? extends Document> getDocuments(List<String> documentIds)
         throws DocumentManagementServiceException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public byte[] retrieveDocumentContent(String documentId)
         throws DocumentManagementServiceException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public void retrieveDocumentContentStream(String documentId, OutputStream target)
         throws DocumentManagementServiceException
   {
      // TODO Auto-generated method stub

   }

   @Override
   public Folder getFolder(String folderId) throws DocumentManagementServiceException
   {
      // return root folder with mock document.
      Document document = getDocument(MOCK_DOCUMENT_ID);
      return RepositoryResourceUtils.createFolder(MOCK_FOLDER_ID, "/", null, null, Collections.singletonList(document), null, Folder.LOD_LIST_MEMBERS);
   }

   @Override
   public Folder getFolder(String folderId, int levelOfDetail)
         throws DocumentManagementServiceException
   {
      // return root folder with mock document.
      Document document = getDocument(MOCK_DOCUMENT_ID);
      return RepositoryResourceUtils.createFolder(MOCK_FOLDER_ID, "/", null, null, Collections.singletonList(document), null, levelOfDetail);
   }

   @Override
   public List< ? extends Folder> getFolders(List<String> folderIds, int levelOfDetail)
         throws DocumentManagementServiceException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Document createDocument(String folderId, DocumentInfo document)
         throws DocumentManagementServiceException
   {
      return (Document) document;
   }

   @Override
   public Document createDocument(String folderId, DocumentInfo document, byte[] content,
         String encoding) throws DocumentManagementServiceException
   {
      return (Document) document;
   }

   @Override
   public Document versionDocument(String documentId, String versionComment,
         String versionLabel) throws DocumentManagementServiceException
   {
      return RepositoryResourceUtils.createDocument(MOCK_DOCUMENT_ID, "/", 0, null, null,
            null, MOCK_DOCUMENT_REV_ID, "v0", versionComment, null);
   }

   @Override
   public void removeDocumentVersion(String documentId, String documentRevisionId)
         throws DocumentManagementServiceException
   {
      // TODO Auto-generated method stub

   }

   @Override
   public Document moveDocument(String documentId, String targetPath)
         throws DocumentManagementServiceException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Document updateDocument(Document document, boolean createNewRevision,
         String versionComment, String versionLabel, boolean keepLocked)
         throws DocumentManagementServiceException
   {
      DmsDocumentBean doc = (DmsDocumentBean) document;
      doc.setRevisionId(MOCK_DOCUMENT_REV_ID);

      return doc;
   }

   @Override
   public Document updateDocument(Document document, byte[] content, String encoding,
         boolean createNewRevision, String versionComment, String versionLabel,
         boolean keepLocked) throws DocumentManagementServiceException
   {

      DmsDocumentBean doc = (DmsDocumentBean) document;
      doc.setRevisionId(MOCK_DOCUMENT_REV_ID);

      return doc;
   }

   @Override
   public void uploadDocumentContentStream(String documentId, InputStream source,
         String contentType, String contentEncoding)
         throws DocumentManagementServiceException
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void removeDocument(String documentId)
         throws DocumentManagementServiceException
   {
      // TODO Auto-generated method stub

   }

   @Override
   public Folder createFolder(String parentFolderId, FolderInfo folder)
         throws DocumentManagementServiceException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Folder updateFolder(Folder folder) throws DocumentManagementServiceException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public void removeFolder(String folderId, boolean recursive)
         throws DocumentManagementServiceException
   {
      // TODO Auto-generated method stub

   }

   @Override
   public Set<Privilege> getPrivileges(String resourceId)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Set<AccessControlPolicy> getEffectivePolicies(String resourceId)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Set<AccessControlPolicy> getPolicies(String resourceId)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Set<AccessControlPolicy> getApplicablePolicies(String resourceId)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public void setPolicy(String resourceId, AccessControlPolicy policy)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public RepositoryMigrationReport migrateRepository(int batchSize,
         boolean evaluateTotalCount) throws DocumentManagementServiceException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public byte[] getSchemaDefinition(String schemaLocation)
         throws ObjectNotFoundException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Documents findDocuments(DocumentQuery query)
   {
      // TODO Auto-generated method stub
      return null;
   }

}
