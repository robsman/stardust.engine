package org.eclipse.stardust.engine.core.persistence.archive;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;

public class ZipArchiveManager implements IArchiveManager
{
   private static final String ZIP = ".zip";

   private static final Logger LOGGER = LogManager.getLogger(ZipArchiveManager.class);

   private static final String EXT = ".dat";

   private static final String MODEL_FILENAME_PREFIX = "model_";

   private static final String FILENAME_PREFIX = "exportdata_";

   private static final String ZIP_FILENAME_PREFIX = "export_";

   private static volatile ZipArchiveManager manager;

   private final String rootFolder;

   private final ExportFilenameFilter filter = new ExportFilenameFilter();

   public static final int BUFFER_SIZE = 1024;

   private ZipArchiveManager()
   {
      // this will be bound to auditTrail
      this.rootFolder = "c:/temp/";
   }

   public static ZipArchiveManager getInstance()
   {
      if (manager == null)
      {
         synchronized (ZipArchiveManager.class)
         {
            if (manager == null)
            {
               manager = new ZipArchiveManager();
            }
         }
      }
      return manager;
   }

   @Override
   public Serializable open(String partition, Date indexDate)
   {
      File dataFolder = getFolder(partition, indexDate);
      File file;
      // allow one thread at a time to lock the folder for this server
      synchronized (dataFolder.getPath())
      {
         // make sure the folder exists
         if (!dataFolder.exists())
         {
            dataFolder = getFolder(partition, indexDate);
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
            FileUtils.writeByteArrayToFile((File) key, results, true);
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
            String name = MODEL_FILENAME_PREFIX + getIndex(key) + EXT;
            File modelFile = new File(dataFile.getParentFile(), name);
            FileUtils.writeByteArrayToFile(modelFile, results, false);
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
   public boolean close(Serializable key)
   {
      File dataFile = (File) key;
      File dataFolder = dataFile.getParentFile();
      int exportIndex = getIndex(key);

      ExportFilenameFilter filter = new ExportFilenameFilter(exportIndex);
      File[] filesToZip = dataFolder.listFiles(filter);
      boolean success = zip(filesToZip, dataFolder, ZIP_FILENAME_PREFIX + exportIndex);
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

   private File getFolder(String name, Date date)
   {
      final DateFormat dateFormat = new SimpleDateFormat("\\yyyy\\mm\\dd\\HH");
      File file;
      String dateString = dateFormat.format(date);
      file = new File(rootFolder + name + dateString);
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
      return FILENAME_PREFIX + (++maxIndex) + EXT;
   }

   private boolean zip(File filesToZip[], File parentFoder,
         String zipFileNameWithoutExtension)
   {
      boolean success = true;
      if (filesToZip != null && filesToZip.length > 0)
      {
         String zippedFileName = parentFoder.getPath() + "/"
               + zipFileNameWithoutExtension + ZIP;

         if (LOGGER.isDebugEnabled())
         {
            LOGGER.debug("Zip file being created: " + zippedFileName);
         }
         ZipOutputStream out = null;
         FileInputStream in = null;
         byte[] buffer = new byte[BUFFER_SIZE];
         try
         {
            out = new ZipOutputStream(new FileOutputStream(zippedFileName));
            out.setLevel(Deflater.DEFAULT_COMPRESSION);

            for (File fileToZip : filesToZip)
            {
               try
               {
                  in = new FileInputStream(fileToZip);
                  int len = in.read(buffer);
                  if (len < 1)
                  {
                     throw new Exception("Empty Input");
                  }
                  out.putNextEntry(new ZipEntry(fileToZip.getName()));
                  while (len > 0)
                  {
                     out.write(buffer, 0, len);
                     len = in.read(buffer);
                  }
                  out.closeEntry();
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
                           fileToZip.delete();
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
            String modelPattern = MODEL_FILENAME_PREFIX;
            String zipPattern = ZIP_FILENAME_PREFIX;
            if (index > -1)
            {
               pattern += index;
               modelPattern += index;
            }

            // match path name extension
            if (ext.equals(EXT)
                  && (fileName.startsWith(pattern) || fileName.startsWith(modelPattern)))
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
   };
}
