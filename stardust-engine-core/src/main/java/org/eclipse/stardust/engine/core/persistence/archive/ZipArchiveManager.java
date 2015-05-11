package org.eclipse.stardust.engine.core.persistence.archive;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;

public class ZipArchiveManager extends BaseArchiveManager
{
   private static final String ZIP = ".zip";

   private static final String ZIP_PART0 = ".part0" + ZIP;

   private static final String ZIP_PART = ".part";

   private static final Logger LOGGER = LogManager.getLogger(ZipArchiveManager.class);
   
   public static final String EXT_DAT = ".dat";

   private static final String EXT_JSON = ".json";

   private static final String FILENAME_MODEL_PREFIX = "model_";

   private static final String FILENAME_PREFIX = "exportdata_";

   private static final String FILENAME_INDEX_PREFIX = "index_";

   private static final String FILENAME_ZIP_PREFIX = "export_";

   private static volatile ZipArchiveManager manager;
   
   private final String archiveManagerId;

   private final String rootFolder;

   private final long zipFileSize;

   private final String folderFormat;

   private final ExportFilenameFilter filter = new ExportFilenameFilter();

   private final ZipFileFilter zipFileFilter = new ZipFileFilter();

   public static final int BUFFER_SIZE = 1024 * 16;

   private ZipArchiveManager(String archiveManagerId, String rootFolder, String folderFormat, long zipFileSize)
   {
      this.archiveManagerId = archiveManagerId;
      this.rootFolder = rootFolder;
      this.folderFormat = folderFormat;
      this.zipFileSize = zipFileSize;
   }

   public static ZipArchiveManager getInstance(String id, String rootFolder,
         String folderFormat, long zipFileSize)
   {
      if (manager == null)
      {
         synchronized (ZipArchiveManager.class)
         {
            if (manager == null)
            {

               if (zipFileSize <= 0)
               {
                  zipFileSize = ArchiveManagerFactory.DEFAULT_ARCHIVE_ZIP_FILE_SIZE_MB;
               }
               zipFileSize *= 1024 * 1024;
               manager = new ZipArchiveManager(id, rootFolder, folderFormat, zipFileSize);
            }
         }
      }
      return manager;
   }

   @Override
   public String getArchiveManagerId()
   {
      return archiveManagerId;
   }
   
   @Override
   public ArrayList<IArchive> findArchives(ArrayList<IArchive> unfilteredArchives,
         Date fromDate, Date toDate, Map<String, Object> descriptors)
   {
      Date fromIndex = ExportImportSupport.getIndexDateTime(fromDate);
      Date toIndex = ExportImportSupport.getIndexDateTime(toDate);
      final DateFormat dateFormat = new SimpleDateFormat(folderFormat);
      if (unfilteredArchives == null)
      {
         unfilteredArchives = findAllArchives();
      }

      String partitionFolderName = getPartitionFolderName();
      ArrayList<IArchive> archives = new ArrayList<IArchive>();
      try
      {
         for (IArchive archive : unfilteredArchives)
         {
            String filePath = (String)archive.getArchiveKey();
            String name = filePath.substring(partitionFolderName.length(),
                  filePath.length());
            name = name.substring(0, name.lastIndexOf(File.separatorChar));

            Date folderDate = dateFormat.parse(name);
            if (fromIndex.compareTo(folderDate) < 1 && toIndex.compareTo(folderDate) > -1)
            {
               archives.add(archive);
            }
            //we did not find a match based on processInstanceOid so search by descriptors
            if (!archives.contains(archive) && descriptors != null)
            {
               if (archive.getExportIndex().contains(descriptors))
               {
                  archives.add(archive);
               }
            }
         }
      }
      catch (ParseException e)
      {
         LOGGER.error("Failed finding archives.", e);
      }
      return archives;

   }
   
   @Override
   protected ArrayList<IArchive> findAllArchives()
   {
      ArrayList<IArchive> archives = new ArrayList<IArchive>();
      Map<String, List<String>> allZipFiles = findZipFiles();
      for (String filePath : allZipFiles.keySet())
      {
         archives.add(new ZipArchive(filePath, allZipFiles.get(filePath)));
      }
      return archives;
   }

   @Override
   public Serializable open(Date indexDate)
   {
      File dataFolder = getFolder(indexDate);
      File file;
      // allow one thread at a time to lock the folder for this server
      synchronized (dataFolder.getPath())
      {
         // make sure the folder exists
         if (!dataFolder.exists())
         {
            dataFolder = getFolder(indexDate);
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
            LOGGER.error("Key or Results is Null. Key: " + key + ", results:" + results);
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
            exportResult.getProcessLengths(indexDate));
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

   private File getFolder(Date date)
   {
      final DateFormat dateFormat = new SimpleDateFormat(folderFormat);
      String dateString = dateFormat.format(date);
      File file = new File(getPartitionFolderName() + dateString);
      return file;
   }

   private String getPartitionFolderName()
   {
      String partition = SecurityProperties.getPartition().getId();
      return rootFolder + partition;
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
      return parts[0] + ext;
   }

   private boolean zip(String filesToZip[], String parentFoder,
         String zipFileNameWithoutExtension, List<Long> processIds, List<Integer> lengths)
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
         BufferedInputStream in = null;
         String dataFile = null;
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
                     || ZipArchive.FILENAME_INDEX.equals(baseFileName))
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
               else
               {
                  dataFile = fileAbsolutePath;
               }
            }
            long entrySize = writeKey(out, part0Name);
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
               in = new BufferedInputStream(new FileInputStream(dataFile));
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
                     entrySize = writeKey(out, part0Name);
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
                  entrySize = writeChunck(length, out, in, processId);
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
         catch (Exception e)
         {
            success = false;
            LOGGER.error("Error creating zip file", e);
         }
         finally
         {
            try
            {
               if (in != null)
               {
                  in.close();
                  if (success)
                  {
                     new File(dataFile).delete();
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

   private long writeKey(ZipOutputStream out, String part0Name)
   {
      long size = -1L;
      try
      {
         ZipEntry entry = new ZipEntry(ZipArchive.FILENAME_KEY);
         out.putNextEntry(entry);
         out.write(archiveManagerId.getBytes());
         out.write(",".getBytes());
         
         String partitionFolderName = getPartitionFolderName();
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

   private Map<String, List<String>> findZipFiles()
   {
      File directory = new File(getPartitionFolderName());
      Map<String, List<String>> allZipFiles = new HashMap<String, List<String>>();
      findZipFiles(allZipFiles, directory);
      return allZipFiles;
   }

   private void findZipFiles(Map<String, List<String>> files, File directory)
   {
      File[] found = directory.listFiles(zipFileFilter);

      if (found != null)
      {
         for (File file : found)
         {
            if (file.isDirectory())
            {
               findZipFiles(files, file);
            }
            else
            {
               if (file.getName().endsWith(ZIP_PART0))
               {
                  List<String> allFiles = files.get(file.getAbsolutePath());
                  // another part could have added it.
                  if (allFiles == null)
                  {
                     allFiles = new ArrayList<String>();
                     files.put(file.getAbsolutePath(), allFiles);
                  }
               }
               else
               {
                  String path = file.getAbsolutePath();
                  int indexOfPart = path.indexOf(ZIP_PART);
                  String part0 = path.substring(0, indexOfPart) + ZIP_PART0;
                  List<String> allFiles = files.get(part0);
                  // another part could have added it.
                  if (allFiles == null)
                  {
                     allFiles = new ArrayList<String>();
                     files.put(part0, allFiles);
                  }
                  allFiles.add(file.getAbsolutePath());

               }
            }
         }
      }
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

   private class ZipFileFilter implements FileFilter
   {

      private String pattern;

      public ZipFileFilter()
      {
         this(null);
      }

      public ZipFileFilter(String startPattern)
      {
         pattern = startPattern;
      }

      @Override
      public boolean accept(File file)
      {
         boolean inFilter;
         if (file.isDirectory())
         {
            inFilter = true;
         }
         else if (pattern == null)
         {
            inFilter = file.getName().endsWith(ZIP);
         }
         else
         {
            inFilter = file.getName().startsWith(pattern) && file.getName().endsWith(ZIP);
         }
         return inFilter;
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
            String zipPattern = FILENAME_ZIP_PREFIX;
            String indexPattern = FILENAME_INDEX_PREFIX;
            if (index > -1)
            {
               pattern += index;
               modelPattern += index;
               indexPattern += index;
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
