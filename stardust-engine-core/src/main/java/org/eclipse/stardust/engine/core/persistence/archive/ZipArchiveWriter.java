package org.eclipse.stardust.engine.core.persistence.archive;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.util.StringUtils;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;

public class ZipArchiveWriter implements IArchiveWriter
{
   public static final int BUFFER_SIZE = 1024 * 16;
   
   private static final String FOLDER_MODELS = "models";
   
   private static final String ZIP = ".zip";

   private static final String ZIP_PART = ".part";

   private static final Logger LOGGER = LogManager.getLogger(ZipArchiveWriter.class);
   
   public static final String EXT_DAT = ".dat";

   private static final String EXT_XPDL = ".xpdl";
   
   private static final String FILENAME_XPDL_PREFIX = "xpdl_";
   
   private static final String FILENAME_MODEL_PREFIX = "model_";

   private static final String FILENAME_PREFIX = "exportdata_";

   private static final String FILENAME_INDEX_PREFIX = "index_";

   private static final String FILENAME_ZIP_PREFIX = "export_";
   
   private static final String FILENAME_DOCUMENT_PREFIX = "doc_";

   private static final String FILENAME_DOC = "doc.dat";

   private String archiveManagerId;

   private String rootFolder;

   private long zipFileSize;

   private String folderFormat;

   private String dateFormat;
   
   private boolean autoArchive;
   
   private boolean isKeyDescriptorsOnly;
   
   private DocumentOption documentOption;

   private final ExportFilenameFilter filter = new ExportFilenameFilter();

   public ZipArchiveWriter()
   {}
   
   public void init(Map<String, String> preferences)
   {
      String rootFolder = preferences.get(ArchiveManagerFactory.CARNOT_ARCHIVE_WRITER_ROOTFOLDER);
      if (StringUtils.isEmpty(rootFolder.trim()))
      {
         throw new IllegalArgumentException(
               ArchiveManagerFactory.CARNOT_ARCHIVE_WRITER_ROOTFOLDER
                     + " must be provided for ZIP archive type");
      }
      String id = preferences.get(
            ArchiveManagerFactory.CARNOT_ARCHIVE_WRITER_MANAGER_ID);

      if (StringUtils.isEmpty(id))
      {     
         throw new IllegalArgumentException(
                  ArchiveManagerFactory.CARNOT_ARCHIVE_WRITER_MANAGER_ID
                        + " must be provided for ZIP archive type");
      }
      this.archiveManagerId = id;
      if (!rootFolder.endsWith(File.separator))
      {
         rootFolder += File.separator;
      }
      String folderFormat = preferences.get(ArchiveManagerFactory.CARNOT_ARCHIVE_WRITER_FOLDER_FORMAT);
      int zipFileSize = Integer.valueOf(preferences.get(ArchiveManagerFactory.CARNOT_ARCHIVE_WRITER_ZIP_FILE_SIZE_MB));
      String dateFormat = preferences.get(ArchiveManagerFactory.CARNOT_ARCHIVE_WRITER_DATE_FORMAT);
      boolean auto = "true".equals(preferences.get(ArchiveManagerFactory.CARNOT_ARCHIVE_WRITER_AUTO_ARCHIVE));
      boolean isKeyDescriptorsOnly = "true".equals(preferences.get(ArchiveManagerFactory.CARNOT_ARCHIVE_WRITER_KEY_DESCRIPTOR_ONLY));
      this.documentOption = DocumentOption.valueOf(preferences.get(ArchiveManagerFactory.CARNOT_ARCHIVE_WRITER_AUTO_ARCHIVE_DOCUMENTS));
      
      if (zipFileSize <= 0)
      {
         zipFileSize = new Integer(ArchiveManagerFactory.DEFAULT_ARCHIVE_ZIP_FILE_SIZE_MB);
      }
      zipFileSize *= 1024 * 1024;
      this.rootFolder = rootFolder;
      this.folderFormat = folderFormat;
      this.dateFormat = dateFormat;
      this.zipFileSize = zipFileSize;
      this.autoArchive = auto;
      this.isKeyDescriptorsOnly = isKeyDescriptorsOnly;
   }
   
   @Override
   public String getArchiveManagerId()
   {
      return archiveManagerId;
   }
   
   @Override
   public String getDateFormat()
   {
      return dateFormat;
   }

   @Override
   public boolean isAutoArchive()
   {
      return autoArchive;
   }
   
   @Override
   public DocumentOption getDocumentOption()
   {
      return documentOption;
   }

   @Override
   public boolean isKeyDescriptorsOnly()
   {
      return isKeyDescriptorsOnly;
   }

   @Override
   public Serializable open(Date indexDate, ExportIndex exportIndex)
   {
      File dataFolder = getFolder(indexDate, exportIndex.getDumpLocation());
      File file;
      // allow one thread at a time to lock the folder for this server
      synchronized (dataFolder.getPath())
      {
         // make sure the folder exists
         if (!dataFolder.exists())
         {
            dataFolder = getFolder(indexDate, exportIndex.getDumpLocation());
            if (!dataFolder.exists())
            {
               dataFolder.mkdirs();
            }
         }
         File lock = null;
         try
         {
            // get a lock file so only one server can calculate unique name at a time
            lock = getLockFile(dataFolder);
            if (lock != null)
            {
               String name = getUniqueFileName(dataFolder);
               file = new File(dataFolder, name);
               try
               {
                  file.createNewFile();
               }
               catch (IOException e)
               {
                  LOGGER.error("Failed creating archive file.", e);
                  file = null;
               }
            }
            else
            {
               LOGGER.error("Failed creating lock file.");
               file = null;
            }
         }
         finally
         {
            if (lock != null)
            {
               lock.delete();
            }
         }
      }
      return file;
   }

   /**
    * get a lock file in this datafolder, so only one server can calculate unique name at
    * a time
    * 
    * @param dataFolder
    * @return
    */
   private File getLockFile(File dataFolder)
   {
      File lock = new File(dataFolder, ".lock");
      boolean createdLock;
      try
      {
         // Atomically creates a new, empty file if and only if a file with this name does
         // not yet exist.
         createdLock = lock.createNewFile();
         if (LOGGER.isDebugEnabled())
         {
            LOGGER.debug("Lock file created");
         }
      }
      catch (IOException e)
      {
         createdLock = false;
         // this is normal during high concurrency
         LOGGER.info("Problem creating lock file. Retry will be attempted. Error: "
               + e.getMessage());
      }

      if (!createdLock)
      {
         try
         {
            Thread.sleep(1000L);
         }
         catch (InterruptedException e)
         {
            LOGGER.warn("Sleep interrupted upon retry of creating lock file. Retry will be attempted. Error: "
                  + e.getMessage());
         }
         getLockFile(dataFolder);
      }
      return lock;
   }

   @Override
   public boolean add(Serializable key, byte[] results)
   {
      boolean success;
      try
      {
         if (key != null && results != null)
         {
            File file = (File) key;
            writeByteArrayToFile(file, results);
            success = true;
         }
         else
         {
            success = false;
            LOGGER.error("Add Data: Key or Results is Null. Key: " + key + ", results:" + results);
         }
      }
      catch (IOException e)
      {
         success = false;
         LOGGER.error("Failed adding to archive.", e);
      }
      return success;
   }
   
   @Override
   public boolean addModelXpdl(Serializable dumpLocation, String uuid, String xpdl)
   {
      boolean success;
      File lock = null;
      synchronized (uuid)
      {
         if (!isModelExported(dumpLocation, uuid))
         {
            try
            {
               if (!StringUtils.isEmpty(xpdl) && !StringUtils.isEmpty(uuid))
               {
                  File dataFolder = new File(getPartitionFolderName(dumpLocation) + File.separatorChar + FOLDER_MODELS);
                  if(!dataFolder.exists())
                  {
                     dataFolder.mkdirs();
                  }
                  lock = getLockFile(dataFolder);
                  if (lock != null)
                  {
                     String name = uuid + EXT_XPDL;
                     File xpdlFile = new File(dataFolder, name);
                     writeByteArrayToFile(xpdlFile, xpdl.getBytes());
                     success = true;
                  }
                  else
                  {
                     success = false;
                     LOGGER.error("Failed creating lock file for models folder.");
                  }
               }
               else
               {
                  success = false;
                  LOGGER.error("UUID or XPDL is Null. uuid:" + uuid + ", xpdl:" + xpdl);
               }
            }
            catch (IOException e)
            {
               success = false;
               LOGGER.error("Failed adding model xpdl to archive.", e);
            }
            finally
            {
               if (lock != null)
               {
                  lock.delete();
               }
            }
         }
         else
         {
            success = true;
         }
      }
      return success;
   }
   
   public boolean isModelExported(Serializable dumpLocation, String uuid)
   {
      String name = uuid + EXT_XPDL;
      File dataFolder = new File(getPartitionFolderName(dumpLocation) + File.separatorChar + FOLDER_MODELS);
      File xpdlFile = new File(dataFolder, name);
      return xpdlFile.exists();
   }

   @Override
   public boolean addModel(Serializable key, String model)
   {
      boolean success;
      try
      {
         if (key != null && model != null)
         {
            File dataFile = (File) key;
            String name = FILENAME_MODEL_PREFIX + getIndex(key) + EXT_JSON;
            File modelFile = new File(dataFile.getParentFile(), name);
            writeByteArrayToFile(modelFile, model.getBytes());
            success = true;
         }
         else
         {
            success = false;
            LOGGER.error("Key or Model is Null. Key: " + key + ", Model:" + model);
         }
      }
      catch (IOException e)
      {
         success = false;
         LOGGER.error("Failed adding model to archive.", e);
      }
      return success;
   }

   @Override
   public boolean addIndex(Serializable key, String result)
   {
      boolean success;
      try
      {
         if (key != null && result != null)
         {
            File dataFile = (File) key;
            String name = FILENAME_INDEX_PREFIX + getIndex(key) + EXT_JSON;
            File indexFile = new File(dataFile.getParentFile(), name);
            writeByteArrayToFile(indexFile, result.getBytes());
            success = true;
         }
         else
         {
            success = false;
            LOGGER.error("Key or Model is Null. Key: " + key + ", Model:" + result);
         }
      }
      catch (IOException e)
      {
         success = false;
         LOGGER.error("Failed adding model to archive.", e);
      }
      return success;
   }


   @Override
   public boolean addDocuments(Serializable key, byte[] results)
   {
      boolean success;
      try
      {
         if (key != null && results != null)
         {
            File dataFile = (File) key;
            String name = FILENAME_DOCUMENT_PREFIX + getIndex(key) + EXT_DAT;
            File documentFile = new File(dataFile.getParentFile(), name);
            writeByteArrayToFile(documentFile, results);
            success = true;
         }
         else
         {
            success = false;
            LOGGER.error("Add Documents: Key or Results is Null. Key: " + key + ", results:" + results);
         }
      }
      catch (IOException e)
      {
         success = false;
         LOGGER.error("Failed adding document to archive.", e);
      }
      return success;
   }
   
   
   @Override
   public boolean close(Serializable key, Date indexDate, ExportResult exportResult)
   {
      File dataFile = (File) key;
      File dataFolder = dataFile.getParentFile();
      int index = getIndex(key);
      boolean success;
      ExportFilenameFilter filter = new ExportFilenameFilter(index);

      String[] filesToZip = dataFolder.list(filter);
      String zipFileNameWithoutExtension = FILENAME_ZIP_PREFIX + index;
      success = zip(filesToZip, dataFolder.getAbsolutePath(),
            zipFileNameWithoutExtension, exportResult.getProcessInstanceOids(indexDate),
            exportResult.getProcessLengths(indexDate), 
            exportResult.getExportIndex(indexDate).getDumpLocation(),
            exportResult.getDocumentLengths(indexDate), 
            exportResult.getDocumentNames(indexDate));
      if (!success)
      {
         LOGGER.error("Error creating Zipped archive for export: " + dataFolder.getPath()
               + " export index: " + index);
      }
      else
      {
         if (LOGGER.isDebugEnabled())
         {
            LOGGER.debug("Zip file created for export: " + dataFolder.getPath()
                  + " export index: " + index);
         }
      }
      return success;
   }

   private int getIndex(Serializable key)
   {
      String name = ((File) key).getName();
      int lastIndex = name.lastIndexOf('.');
      String fileName = name.substring(0, lastIndex);
      String[] parts = fileName.split("_");
      int exportIndex = Integer.valueOf(parts[1]);
      return exportIndex;
   }

   private File getFolder(Date date, String dumpLocation)
   {
      final DateFormat dateFormat = new SimpleDateFormat(folderFormat);
      String dateString = dateFormat.format(date);
      File file = new File(getPartitionFolderName(dumpLocation) + dateString);
      return file;
   }

   private String getPartitionFolderName(Serializable dumpLocation)
   {
      String partition = SecurityProperties.getPartition().getId();
      if (dumpLocation == null)
      {
         return rootFolder + partition;
      }
      else
      {
         return dumpLocation + partition;
      }
   }
   
   private String getUniqueFileName(File dataFolder)
   {
      File[] allfiles = dataFolder.listFiles(filter);
      int maxIndex = 0;
      for (int i = 0; i < allfiles.length; i++)
      {
         String name = allfiles[i].getName();
         int lastIndex = name.lastIndexOf('.');
         String fileName = name.substring(0, lastIndex);
         String[] parts = fileName.split("_");
         lastIndex = parts[1].indexOf('.');
         if (lastIndex > -1)
         {
            parts[1] = parts[1].substring(0, lastIndex);
         }
         int exportIndex = Integer.valueOf(parts[1]);
         if (maxIndex < exportIndex)
         {
            maxIndex = exportIndex;
         }
      }
      return FILENAME_PREFIX + (++maxIndex) + EXT_DAT;
   }

   private String getBaseFileName(String fileName)
   {
      int lastIndex = fileName.lastIndexOf('.');
      String[] parts = fileName.split("_");
      String ext = fileName.substring(lastIndex);
      if (EXT_XPDL.equals(ext))
      {
         return parts[2];   
      }
      else
      {
         return parts[0] + ext;   
      }
      
   }

   private boolean zip(String filesToZip[], String parentFoder,
         String zipFileNameWithoutExtension, List<Long> processIds, List<Integer> lengths,
         String dumpLocation, List<Integer> documentLenghts, List<String> documentNames)
   {
      boolean success = true;
      String part0Name;
      if (filesToZip != null && filesToZip.length > 0)
      {
         int part = 0;
         String zippedFileName = getZipFileName(parentFoder, 0,
               zipFileNameWithoutExtension);
         part0Name = zippedFileName;
         if (LOGGER.isDebugEnabled())
         {
            LOGGER.debug("Zip file being created: " + zippedFileName);
         }
         ZipOutputStream out = null;
         BufferedInputStream inData = null;
         BufferedInputStream inDoc = null;
         String dataFile = null;
         String docFile = null;
         byte[] buffer = new byte[BUFFER_SIZE];
         try
         {
            out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(
                  zippedFileName)));
            out.setLevel(Deflater.DEFAULT_COMPRESSION);
            long size = 0;
            // first add index and model to zip, they must always be in first zip created
            for (String fileToZip : filesToZip)
            {
               String baseFileName = getBaseFileName(fileToZip);
               String fileAbsolutePath = parentFoder + File.separatorChar + fileToZip;
               if (ZipArchive.FILENAME_MODEL.equals(baseFileName)
                     || ZipArchive.FILENAME_INDEX.equals(baseFileName)
                     || baseFileName.endsWith(EXT_XPDL))
               {
                  long entrySize = writeComplete(out, fileAbsolutePath, buffer,
                        baseFileName);
                  if (entrySize > -1)
                  {
                     size += entrySize;
                  }
                  else
                  {
                     success = false;
                     break;
                  }
               }
               else if (baseFileName.equals(FILENAME_DOC))
               {
                  docFile = fileAbsolutePath;
               }
               else
               {
                  dataFile = fileAbsolutePath;
               }
            }
            long entrySize = writeKey(out, part0Name, dumpLocation);
            if (entrySize > -1)
            {
               size += entrySize;
            }
            else
            {
               success = false;
            }
            // add process entries to zip, create more zip files if size grows beyond
            // limit
            if (dataFile != null && success)
            {
               inData = new BufferedInputStream(new FileInputStream(dataFile));
               for (Long processId : processIds)
               {
                  Integer length = lengths.get(processIds.indexOf(processId));
                  if ((size + length) >= zipFileSize)
                  {
                     out.close();
                     ++part;
                     zippedFileName = getZipFileName(parentFoder, part,
                           zipFileNameWithoutExtension);
                     out = new ZipOutputStream(new BufferedOutputStream(
                           new FileOutputStream(zippedFileName)));
                     entrySize = writeKey(out, part0Name, dumpLocation);
                     if (entrySize > -1)
                     {
                        size = entrySize;
                     }
                     else
                     {
                        success = false;
                        break;
                     }
                     if (LOGGER.isDebugEnabled())
                     {
                        LOGGER.debug("Zip file being created: " + zippedFileName);
                     }
                  }
                  entrySize = writeChunck(length, out, inData, processId);
                  if (entrySize > -1)
                  {
                     size += entrySize;
                  }
                  else
                  {
                     success = false;
                     break;
                  }
               }
               // add document entries to zip, create more zip files if size grows beyond
               // limit
               if (docFile != null && success)
               {
                  inDoc = new BufferedInputStream(new FileInputStream(docFile));
                  for (String docName : documentNames)
                  {
                     Integer length = documentLenghts.get(documentNames.indexOf(docName));
                     if ((size + length) >= zipFileSize)
                     {
                        out.close();
                        ++part;
                        zippedFileName = getZipFileName(parentFoder, part,
                              zipFileNameWithoutExtension);
                        out = new ZipOutputStream(new BufferedOutputStream(
                              new FileOutputStream(zippedFileName)));
                        entrySize = writeKey(out, part0Name, dumpLocation);
                        if (entrySize > -1)
                        {
                           size = entrySize;
                        }
                        else
                        {
                           success = false;
                           break;
                        }
                        if (LOGGER.isDebugEnabled())
                        {
                           LOGGER.debug("Zip file being created: " + zippedFileName);
                        }
                     }
                     entrySize = writeDocFile(length, out, inDoc, docName);
                     if (entrySize > -1)
                     {
                        size += entrySize;
                     }
                     else
                     {
                        success = false;
                        break;
                     }
                  }
               }
            }
         }
         catch (Exception e)
         {
            success = false;
            LOGGER.error("Error creating zip file", e);
         }
         finally
         {
            try
            {
               if (inData != null)
               {
                  inData.close();
                  if (success)
                  {
                     new File(dataFile).delete();
                  }
               }
               if (inDoc != null)
               {
                  inDoc.close();
                  if (success)
                  {
                     new File(docFile).delete();
                  }
               }
            }
            catch (IOException ioe)
            {
               LOGGER.error("Unable to close and delete file " + dataFile);
            }
            try
            {
               if (out != null)
               {
                  out.close();
               }
            }
            catch (Exception e)
            {
               success = false;
               LOGGER.error("Error closing Zipped outputstream", e);
            }
         }
      }
      else
      {
         success = false;
      }
      return success;
   }

   private long writeChunck(int chunkSize, ZipOutputStream out, BufferedInputStream in,
         Long processId)
   {
      long size = -1L;
      try
      {
         ZipEntry entry = new ZipEntry(processId + EXT_DAT);
         out.putNextEntry(entry);
         byte[] process = new byte[chunkSize];
         in.read(process);
         out.write(process);
         out.closeEntry();
         size = entry.getCompressedSize();
      }
      catch (Exception e)
      {
         size = -1;
         LOGGER.error("Error adding process to Zipped content. Process: " + processId, e);
      }
      return size;
   }
   
   private long writeDocFile(int chunkSize, ZipOutputStream out, BufferedInputStream in,
         String name)
   {
      long size = -1L;
      try
      {
         ZipEntry entry = new ZipEntry(name);
         out.putNextEntry(entry);
         byte[] process = new byte[chunkSize];
         in.read(process);
         out.write(process);
         out.closeEntry();
         size = entry.getCompressedSize();
      }
      catch (Exception e)
      {
         size = -1;
         LOGGER.error("Error adding process to Zipped content. Document: " + name, e);
      }
      return size;
   }

   private long writeKey(ZipOutputStream out, String part0Name, String dumpLocation)
   {
      long size = -1L;
      try
      {
         ZipEntry entry = new ZipEntry(ZipArchive.FILENAME_KEY);
         out.putNextEntry(entry);
         out.write(archiveManagerId.getBytes());
         out.write(",".getBytes());
         
         String partitionFolderName = getPartitionFolderName(dumpLocation);
         String name = part0Name.substring(partitionFolderName.length(),
               part0Name.length());
         name = name.substring(1, name.length());
               
         out.write(name.getBytes());
         out.closeEntry();
         size = entry.getCompressedSize();
      }
      catch (Exception e)
      {
         size = -1;
         LOGGER.error("Error adding key to Zipped content. Key: " + part0Name, e);
      }
      return size;
   }
   private long writeComplete(ZipOutputStream out, String fileAbsolutePath,
         byte[] buffer, String entryName)
   {
      BufferedInputStream in = null;
      long size = -1L;
      try
      {
         in = new BufferedInputStream(new FileInputStream(fileAbsolutePath));
         int len = in.read(buffer);
         if (len < 1)
         {
            throw new Exception("Empty Input");
         }
         ZipEntry entry = new ZipEntry(entryName);
         out.putNextEntry(entry);
         while (len > 0)
         {
            out.write(buffer, 0, len);
            len = in.read(buffer);
         }
         out.closeEntry();
         size = entry.getCompressedSize();
      }
      catch (Exception e)
      {
         size = -1;
         LOGGER.error("Error adding file to Zipped content. File: " + fileAbsolutePath, e);
      }
      finally
      {
         if (in != null)
         {
            try
            {
               in.close();
               if (size > -1L)
               {
                  new File(fileAbsolutePath).delete();
               }
            }
            catch (IOException ioe)
            {
               LOGGER.error("Unable to close and delete file " + fileAbsolutePath);
            }
         }
      }
      return size;
   }

   private String getZipFileName(String parentFoder, int part,
         String zipFileNameWithoutExtension)
   {
      String zippedFileName = parentFoder + File.separatorChar
            + zipFileNameWithoutExtension + ZIP_PART + part + ZIP;
      return zippedFileName;
   }

   private void writeByteArrayToFile(File file, byte[] data) throws IOException
   {

      byte[] buf = new byte[BUFFER_SIZE];
      int bytesRead = 0;
      BufferedOutputStream bos = null;
      BufferedInputStream bis = null;
      try
      {

         bis = new BufferedInputStream(new ByteArrayInputStream(data), BUFFER_SIZE);
         bos = new BufferedOutputStream(new FileOutputStream(file), BUFFER_SIZE);

         while ((bytesRead = bis.read(buf, 0, BUFFER_SIZE)) != -1)
         {
            bos.write(buf, 0, bytesRead);
         }

      }
      catch (IOException e)
      {
         throw e;
      }
      finally
      {
         if (bos != null)
         {
            bos.close();
         }
         if (bis != null)
         {
            bis.close();
         }
      }
   }

   private class ExportFilenameFilter implements FilenameFilter
   {
      private final int index;

      public ExportFilenameFilter()
      {
         this.index = -1;
      }

      public ExportFilenameFilter(int index)
      {
         this.index = index;
      }

      @Override
      public boolean accept(File dir, String name)
      {
         if (name.lastIndexOf('.') > 0)
         {
            // get last index for '.' char
            int lastIndex = name.lastIndexOf('.');

            // get extension
            String ext = name.substring(lastIndex);
            String fileName = name.substring(0, lastIndex);
            String pattern = FILENAME_PREFIX;
            String modelPattern = FILENAME_MODEL_PREFIX;
            String xpdlPattern = FILENAME_XPDL_PREFIX;
            String zipPattern = FILENAME_ZIP_PREFIX;
            String indexPattern = FILENAME_INDEX_PREFIX;
            String docPattern = FILENAME_DOCUMENT_PREFIX;
            if (index > -1)
            {
               pattern += index;
               modelPattern += index;
               indexPattern += index;
               xpdlPattern += index;
               docPattern += index;
            }

            // match path name extension
            if (ext.equals(EXT_DAT)
                  && fileName.startsWith(pattern))
            {
               return true;
            }
            else if (ext.equals(EXT_JSON) && (fileName.startsWith(indexPattern)
                  || fileName.startsWith(modelPattern)))
            {
               return true;
            }
            else if (ext.equals(EXT_XPDL) && fileName.startsWith(xpdlPattern))
            {
               return true;
            }
            else if (ext.equals(EXT_DAT) && fileName.startsWith(docPattern))
            {
               return true;
            }
            else if (index == -1 && ext.startsWith(ZIP)
                  && fileName.startsWith(zipPattern))
            {
               return true;
            }
         }
         return false;
      }
   }

}
