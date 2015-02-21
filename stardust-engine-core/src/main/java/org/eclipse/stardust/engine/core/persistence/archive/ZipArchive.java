package org.eclipse.stardust.engine.core.persistence.archive;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.collections.EnumerationUtils;
import org.apache.commons.io.IOUtils;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;

public class ZipArchive implements IArchive, Serializable
{
   private static final long serialVersionUID = 1L;

   private final String absolutePath;

   private static final Logger LOGGER = LogManager.getLogger(ZipArchive.class);

   public static final int SIXTEEN_K = 16 * 1024;

   public static final String FILENAME_MODEL = "model.dat";
   
   public static final String FILENAME_INDEX = "index.json";
   
   private List<Long> processInstanceOids = null;

   public ZipArchive(String absolutePath)
   {
      this.absolutePath = absolutePath;
   }

   @Override
   public String getName()
   {
      return absolutePath;
   }

   @Override
   public List<Long> getProcessInstanceOids()
   {
      if (processInstanceOids == null)
      {
         processInstanceOids = new ArrayList<Long>();
      
      }
      return processInstanceOids;
   }
   
   @Override
   public byte[] getData(Long processInstanceOid)
   {
      if (processInstanceOids.contains(processInstanceOid))
      {
         return uncompressZipEntry(processInstanceOid + ZipArchiveManager.EXT_DAT);
      }
      return null;
   }

   @Override
   public byte[] getData()
   {
      return uncompressZipEntry(null);
   }
   
   @Override
   public byte[] getModelData()
   {
      return uncompressZipEntry(FILENAME_MODEL);
   }

   private byte[] uncompressZipEntry(String zipEntryName)
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
         zipFile = new ZipFile(absolutePath);
         
         List< ? extends ZipEntry> entries;
         if(StringUtils.isNotEmpty(zipEntryName))
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
            //if no zipEntryName is provided only unzip processes
            if (StringUtils.isEmpty(zipEntryName) && (FILENAME_INDEX.equals(entry.getName()) || FILENAME_MODEL.equals(entry.getName())))
            {
               continue;
            }
            inputStream = zipFile.getInputStream(entry);
   
            bufferedInputStream = new BufferedInputStream(inputStream, SIXTEEN_K);
   
            byte[] buffer = new byte[SIXTEEN_K];
            IOUtils.copyLarge(bufferedInputStream, bufferedOutputStream, buffer);
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

}
