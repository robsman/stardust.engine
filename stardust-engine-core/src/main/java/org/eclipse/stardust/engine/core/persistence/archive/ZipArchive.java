package org.eclipse.stardust.engine.core.persistence.archive;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;

public class ZipArchive implements IArchive, Serializable
{
   private static final long serialVersionUID = 1L;

   private final String absolutePath;

   private static final Logger LOGGER = LogManager.getLogger(ZipArchive.class);

   public static final int SIXTEEN_K = 16 * 1024;

   private static final String FILENAME_MODEL = "model.dat";

   private static final String FILENAME_DATA = "exportdata.dat";

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
   public byte[] getData()
   {
      return uncompressZipEntry(FILENAME_DATA);
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
         zipEntry = zipFile.getEntry(zipEntryName);
         inputStream = zipFile.getInputStream(zipEntry);

         bufferedInputStream = new BufferedInputStream(inputStream, SIXTEEN_K);

         bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream, SIXTEEN_K);

         byte[] buffer = new byte[SIXTEEN_K];
         IOUtils.copyLarge(bufferedInputStream, bufferedOutputStream, buffer);
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
