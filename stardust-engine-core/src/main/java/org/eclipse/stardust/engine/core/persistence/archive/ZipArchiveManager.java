package org.eclipse.stardust.engine.core.persistence.archive;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.activemq.util.ByteArrayInputStream;
import org.apache.commons.io.FileUtils;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;

public class ZipArchiveManager implements IArchiveManager
{
   private static final String ZIP = ".zip";

   private static final Logger LOGGER = LogManager.getLogger(ZipArchiveManager.class);

   public static final String EXT_DAT = ".dat";

   private static final String EXT_JSON = ".json";

   private static final String FILENAME_MODEL_PREFIX = "model_";

   private static final String FILENAME_PREFIX = "exportdata_";

   private static final String FILENAME_INDEX_PREFIX = "index_";

   private static final String FILENAME_ZIP_PREFIX = "export_";

   private static volatile ZipArchiveManager manager;

   private final String rootFolder;

   private final String folderFormat;

   private final ExportFilenameFilter filter = new ExportFilenameFilter();

   public static final int BUFFER_SIZE = 1024 * 16;

   private ZipArchiveManager(String rootFolder, String folderFormat)
   {
      this.rootFolder = rootFolder;
      this.folderFormat = folderFormat;
   }

   public static ZipArchiveManager getInstance(String rootFolder, String folderFormat)
   {
      if (manager == null)
      {
         synchronized (ZipArchiveManager.class)
         {
            if (manager == null)
            {
               manager = new ZipArchiveManager(rootFolder, folderFormat);
            }
         }
      }
      return manager;
   }

   @Override
   public ArrayList<IArchive> findArchives(List<Long> processInstanceOids)
   {
      // TODO Auto-generated method stub
      return findArchives();
   }

   @Override
   public ArrayList<IArchive> findArchives(Date fromDate, Date toDate)
   {
      // TODO Auto-generated method stub
      return findArchives();
   }

   @Override
   public ArrayList<IArchive> findArchives()
   {
      String partition = SecurityProperties.getPartition().getId();
      ArrayList<IArchive> archives = new ArrayList<IArchive>();
      File directory = new File(rootFolder + partition);
      Collection<File> allZipFiles = FileUtils.listFiles(directory, new String[] {"zip"},
            true);
      for (File f : allZipFiles)
      {
         archives.add(new ZipArchive(f.getAbsolutePath()));
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
   public boolean addModel(Serializable key, byte[] results)
   {
      boolean success;
      try
      {
         if (key != null && results != null)
         {
            File dataFile = (File) key;
            String name = FILENAME_MODEL_PREFIX + getIndex(key) + EXT_DAT;
            File modelFile = new File(dataFile.getParentFile(), name);
            writeByteArrayToFile(modelFile, results);
            success = true;
         }
         else
         {
            success = false;
            LOGGER.error("Key or Model is Null. Key: " + key + ", Model:" + results);
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
   public boolean close(Serializable key, List<Long> processIds,
         List<Integer> processLengths)
   {
      File dataFile = (File) key;
      File dataFolder = dataFile.getParentFile();
      int exportIndex = getIndex(key);
      boolean success;
      ExportFilenameFilter filter = new ExportFilenameFilter(exportIndex);

      String[] filesToZip = dataFolder.list(filter);
      success = zip(filesToZip, dataFolder.getAbsolutePath(), FILENAME_ZIP_PREFIX
            + exportIndex, processIds, processLengths);
      if (!success)
      {
         LOGGER.error("Error creating Zipped archive for export: " + dataFolder.getPath()
               + " export index: " + exportIndex);
      }
      else
      {
         if (LOGGER.isDebugEnabled())
         {
            LOGGER.debug("Zip file created for export: " + dataFolder.getPath()
                  + " export index: " + exportIndex);
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
      String partition = SecurityProperties.getPartition().getId();
      final DateFormat dateFormat = new SimpleDateFormat(folderFormat);
      String dateString = dateFormat.format(date);
      File file = new File(rootFolder + partition + dateString);
      return file;
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
      if (filesToZip != null && filesToZip.length > 0)
      {
         String zippedFileName = parentFoder + File.separatorChar
               + zipFileNameWithoutExtension + ZIP;

         if (LOGGER.isDebugEnabled())
         {
            LOGGER.debug("Zip file being created: " + zippedFileName);
         }
         ZipOutputStream out = null;
         BufferedInputStream in = null;
         byte[] buffer = new byte[BUFFER_SIZE];
         try
         {
            out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(
                  zippedFileName)));
            out.setLevel(Deflater.DEFAULT_COMPRESSION);

            for (String fileToZip : filesToZip)
            {
               String fileAbsolutePath = parentFoder + File.separatorChar + fileToZip;
               try
               {
                  in = new BufferedInputStream(new FileInputStream(fileAbsolutePath));
                  String baseFileName = getBaseFileName(fileToZip);
                  if (ZipArchive.FILENAME_MODEL.equals(baseFileName)
                        || ZipArchive.FILENAME_INDEX.equals(baseFileName))
                  {
                     int len = in.read(buffer);
                     if (len < 1)
                     {
                        throw new Exception("Empty Input");
                     }
                     out.putNextEntry(new ZipEntry(baseFileName));
                     while (len > 0)
                     {
                        out.write(buffer, 0, len);
                        len = in.read(buffer);
                     }
                     out.closeEntry();
                  }
                  else
                  {
                     for (Long processId : processIds)
                     {
                        out.putNextEntry(new ZipEntry(processId + EXT_DAT));
                        byte[] process = new byte[lengths.get(processIds
                              .indexOf(processId))];
                        in.read(process);
                        out.write(process);
                        out.closeEntry();
                     }
                  }
                  in.close();

                  if (LOGGER.isDebugEnabled())
                  {
                     LOGGER.debug("Successfully added file to zip: " + fileToZip);
                  }
               }
               catch (Exception e)
               {
                  success = false;
                  LOGGER.error("Error adding file to Zipped content. File: " + fileToZip,
                        e);
                  break;
               }
               finally
               {
                  if (in != null)
                  {
                     try
                     {
                        in.close();
                        if (success)
                        {
                           new File(fileAbsolutePath).delete();
                        }
                     }
                     catch (IOException ioe)
                     {
                        LOGGER.error("Unable to close and delete file " + fileToZip);
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
                  && (fileName.startsWith(pattern) || fileName.startsWith(modelPattern)))
            {
               return true;
            }
            else if (ext.equals(EXT_JSON) && fileName.startsWith(indexPattern))
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
}
