package org.eclipse.stardust.engine.core.persistence.archive;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.collections.EnumerationUtils;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;

public class ZipArchive implements IArchive, Serializable
{
   private static final long serialVersionUID = 1L;

   private final String part0AbsolutePath;

   private final List<String> partsAbsolutePaths;

   private static final Logger LOGGER = LogManager.getLogger(ZipArchive.class);

   public static final int SIXTEEN_K = 16 * 1024;

   public static final String FILENAME_MODEL = "model.json";

   public static final String FILENAME_INDEX = "index.json";
   
   public static final String FILENAME_KEY = "key.txt";

   private ExportIndex exportIndex = null;
   private ExportModel exportModel = null;

   public ZipArchive(String part0AbsolutePath, List<String> partAbsolutePaths)
   {
      this.part0AbsolutePath = part0AbsolutePath;
      this.partsAbsolutePaths = partAbsolutePaths;
   }

   @Override
   public Serializable getArchiveKey()
   {
      return part0AbsolutePath;
   }

   @Override
   public ExportIndex getExportIndex()
   {
      if (exportIndex == null)
      {
         byte[] jsonEntry = uncompressZipEntry(part0AbsolutePath, FILENAME_INDEX);
         exportIndex = ExportImportSupport.getGson().fromJson(new String(jsonEntry), ExportIndex.class);
      }
      return exportIndex;
   }

   @Override
   public byte[] getData(List<Long> processInstanceOids)
   {
      byte[] result = new byte[] {};

      for (Long processInstanceOid : processInstanceOids)
      {
         if (getExportIndex().contains(processInstanceOid))
         {
            String path = getPartWithEntry(processInstanceOid + ZipArchiveManager.EXT_DAT);
            byte[] data = uncompressZipEntry(path, processInstanceOid
                  + ZipArchiveManager.EXT_DAT);
            result = ExportImportSupport.addAll(result, data);
         }
      }
      if (result.length == 0)
      {
         result = null;
      }
      return result;
   }

   @Override
   public ExportModel getExportModel()
   {
      if (exportModel == null)
      {
         byte[] jsonEntry = uncompressZipEntry(part0AbsolutePath, FILENAME_MODEL);
         exportModel = ExportImportSupport.getGson().fromJson(new String(jsonEntry), ExportModel.class);
      }
      return exportModel;
   }

   private String getPartWithEntry(String entry)
   {
      ZipFile zipFile = null;
      String result = null;
      try
      {
         zipFile = new ZipFile(part0AbsolutePath);
         ZipEntry zipEntry = zipFile.getEntry(entry);
         if (zipEntry == null)
         {
            for (String file : partsAbsolutePaths)
            {
               zipFile = new ZipFile(file);
               zipEntry = zipFile.getEntry(entry);
               if (zipEntry != null)
               {
                  result = file;
                  break;
               }
            }
         }
         else
         {
            result = part0AbsolutePath;
         }
      }
      catch (Exception exception)
      {
         LOGGER.error("Unable to determine which zipfile has entry " + entry, exception);
         return null;
      }
      finally
      {
         if (zipFile != null)
         {
            try
            {
               zipFile.close();
            }
            catch (IOException ioException)
            {
               LOGGER.error("Unable to close zipFile.", ioException);
               return null;
            }
         }
      }
      return result;
   }

   private byte[] uncompressZipEntry(String zipPath, String zipEntryName)
   {
      byte[] result;

      BufferedInputStream bufferedInputStream = null;
      BufferedOutputStream bufferedOutputStream = null;
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

      ZipFile zipFile = null;
      ZipEntry zipEntry;
      InputStream inputStream;

      try
      {
         zipFile = new ZipFile(zipPath);

         List< ? extends ZipEntry> entries;
         if (StringUtils.isNotEmpty(zipEntryName))
         {
            zipEntry = zipFile.getEntry(zipEntryName);
         }
         else
         {
            zipEntry = null;
         }
         if (zipEntry == null)
         {
            entries = EnumerationUtils.toList(zipFile.entries());
         }
         else
         {
            entries = Arrays.asList(zipEntry);
         }

         bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream, SIXTEEN_K);
         for (ZipEntry entry : entries)
         {
            // if no zipEntryName is provided only unzip processes
            if (StringUtils.isEmpty(zipEntryName)
                  && (FILENAME_INDEX.equals(entry.getName()) || FILENAME_MODEL
                        .equals(entry.getName())))
            {
               continue;
            }
            inputStream = zipFile.getInputStream(entry);

            bufferedInputStream = new BufferedInputStream(inputStream, SIXTEEN_K);

            byte[] buffer = new byte[SIXTEEN_K];
            copy(bufferedInputStream, bufferedOutputStream, buffer);
         }
         bufferedOutputStream.close();
         result = byteArrayOutputStream.toByteArray();
      }
      catch (Exception exception)
      {
         LOGGER.error("Unable to uncompress stream.", exception);
         return null;
      }
      finally
      {
         if (bufferedInputStream != null)
         {
            try
            {
               bufferedInputStream.close();
            }
            catch (IOException ioException)
            {
               LOGGER.error("Unable to close bufferedInputStream.", ioException);
               return null;
            }
         }

         if (bufferedOutputStream != null)
         {
            try
            {
               bufferedOutputStream.close();
            }
            catch (IOException ioException)
            {
               LOGGER.error("Unable to close bufferedOutputStream.", ioException);
               return null;
            }
         }

         if (zipFile != null)
         {
            try
            {
               zipFile.close();
            }
            catch (IOException ioException)
            {
               LOGGER.error("Unable to close zipFile.", ioException);
               return null;
            }
         }
      }

      return result;
   }

   private long copy(InputStream input, OutputStream output, byte[] buffer)
         throws IOException
   {
      long count = 0;
      int n = 0;
      while (-1 != (n = input.read(buffer)))
      {
         output.write(buffer, 0, n);
         count += n;
      }
      return count;
   }
   
   @Override
   public String getArchiveManagerId()
   {
      return getExportIndex().getArchiveManagerId();
   }

   @Override
   public boolean isDump()
   {
      return getExportIndex().isDump();
   }

}
