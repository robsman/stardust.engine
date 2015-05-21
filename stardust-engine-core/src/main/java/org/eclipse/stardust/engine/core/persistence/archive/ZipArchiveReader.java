package org.eclipse.stardust.engine.core.persistence.archive;

import java.io.File;
import java.io.FileFilter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.springframework.util.StringUtils;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;

public class ZipArchiveReader extends BaseArchiveReader
{
   private static final String ZIP = ".zip";

   private static final String ZIP_PART0 = ".part0" + ZIP;

   private static final String ZIP_PART = ".part";

   private static final Logger LOGGER = LogManager.getLogger(ZipArchiveReader.class);

   private final String rootFolder;

   private final String folderFormat;

   private final ZipFileFilter zipFileFilter = new ZipFileFilter();

   public static final int BUFFER_SIZE = 1024 * 16;

   public ZipArchiveReader(Map<String, String> preferences)
   {
      String rootFolder = preferences.get(ArchiveManagerFactory.CARNOT_ARCHIVE_READER_ROOTFOLDER);
      if (StringUtils.isEmpty(rootFolder.trim()))
      {
         throw new IllegalArgumentException(
               ArchiveManagerFactory.CARNOT_ARCHIVE_READER_ROOTFOLDER
                     + " must be provided for ZIP archive type");
      }
      
      if (!rootFolder.endsWith(File.separator))
      {
         rootFolder += File.separator;
      }
      String folderFormat = preferences.get(ArchiveManagerFactory.CARNOT_ARCHIVE_READER_FOLDER_FORMAT);
      
      this.rootFolder = rootFolder;
      this.folderFormat = folderFormat;
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

      String partitionFolderName = getPartitionFolderName(null);
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
      Map<String, List<String>> allZipFiles = findZipFiles(null);
      for (String filePath : allZipFiles.keySet())
      {
         archives.add(new ZipArchive(filePath, allZipFiles.get(filePath)));
      }
      return archives;
   }

   private String getPartitionFolderName(String dumpLocation)
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

   private Map<String, List<String>> findZipFiles(String dumpLocation)
   {
      File directory = new File(getPartitionFolderName(dumpLocation));
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

}
