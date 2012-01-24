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
package org.eclipse.stardust.engine.core.repository;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.List;

import org.eclipse.stardust.common.LRUCache;
import org.eclipse.stardust.common.config.ExtensionProviderUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;


/**
 * @author sauer
 * @version $Revision: $
 */
public class DocumentServiceContentProvider implements IRepositoryContentProvider
{
   private static final Logger trace = LogManager.getLogger(DocumentServiceContentProvider.class);
   
   private LRUCache documentCache;
   private LRUCache documentContentCache;
   
   private final AbstractDocumentServiceRepositoryManager manager;
   
   private final Folder contentFolder;
   
   public DocumentServiceContentProvider(
         AbstractDocumentServiceRepositoryManager manager, Folder contentFolder)
   {
      this.manager = manager;
      this.contentFolder = contentFolder;
      
      final long cacheTTL = Parameters.instance().getInteger(
            KernelTweakingProperties.CONFIGURATION_CACHE_TTL, 60000);
      final int maxCacheSize = Parameters.instance().getInteger(
            KernelTweakingProperties.CONFIGURATION_MAXIMUM_CACHE_ITEMS, 100);
      this.documentCache = new LRUCache(cacheTTL, maxCacheSize, true);
      this.documentContentCache = new LRUCache(cacheTTL, maxCacheSize, true);
   }

   public InputStream getContentStream(String fileId)
   {
      // TODO Auto-generated method stub
      
      URL contentUrl = getContentUrl(fileId);

      if (null != contentUrl)
      {
         try
         {
            contentUrl.openConnection();
            
            return contentUrl.openStream();
         }
         catch (IOException ioe)
         {
            trace.warn("unexpected IO exception", ioe);
         }
      }

      return null;
   }

   public URL getContentUrl(String fileId)
   {
      StringBuffer filePath = new StringBuffer(1024);
      filePath.append(contentFolder.getPath());
      if ( !contentFolder.getPath().endsWith("/"))
      {
         filePath.append("/");
      }
      filePath.append(fileId);
      
      
      String filePathString = filePath.toString();
      Document doc = (Document) this.documentCache.get(filePathString);
      if (doc == null)
      {
         doc = manager.getDocumentService().getDocument(filePathString);
         this.documentCache.put(filePathString, doc);
      }

      if (null != doc)
      {
         final long contentStreamingThreshold = Parameters.instance().getLong(
               KernelTweakingProperties.CONTENT_STREAMING_THRESHOLD, 1024 * 500);

         if (doc.getSize() <= contentStreamingThreshold)
         {
            return getContentUrl(filePathString, doc);
         }
         else
         {
            try
            {
               // try to serve large files directly via DMS content servlet
               String contentServletUrlBase = getContentServletUrlBase();

               String contentDownloadUri = manager.getDocumentService()
                     .requestDocumentContentDownload(doc.getId());

               return new URL(contentServletUrlBase + contentDownloadUri);
            } 
            catch (Exception e)
            {
               // fallback to direct content retrieval in case of errors
               trace.warn("unexpected exception on trying to stream large document content, fallback to direct retrieval", e);
               return getContentUrl(filePathString, doc); 
            }
         }
      }
      
      return null;
   }
   
   private URL getContentUrl (String filePathString, Document doc)
   {
      byte [] content = (byte[]) this.documentContentCache.get(filePathString);
      if (content == null)
      {
         content = (null != doc)
            ? manager.getDocumentService().retrieveDocumentContent(doc.getId())
            : null;
         this.documentContentCache.put(filePathString, content);
      }
      try
      {
         return new URL(null,
               "http://localhost/not-used/",
               new CachedDocumentContentUrlStreamHandler(doc, content));
      }
      catch (MalformedURLException mue)
      {
         // ignore
         mue.printStackTrace();
         return null;
      }
   }
   
   private String getContentServletUrlBase() 
   {
      List extensionProviders = ExtensionProviderUtils.getExtensionProviders(
            IDocumentServiceStreamingSupport.class);
      if (extensionProviders.size() == 0)
      {
         throw new InternalException(
               "Could not find implementations of IDocumentServiceStreamingSupport to determine the ContentServletUrlBase.");
      }
      else
      {
         IDocumentServiceStreamingSupport streamingSupport = (IDocumentServiceStreamingSupport)extensionProviders.get(0);
         return streamingSupport.getContentServletUrlBase();
      }
   }
   
   private class CachedDocumentContentUrlStreamHandler extends URLStreamHandler
   {

      private final Document document;
      
      private final byte[] content;
      
      public CachedDocumentContentUrlStreamHandler(Document document, byte[] content)
      {
         this.document = document;
         this.content = content;
      }

      protected URLConnection openConnection(URL url) throws IOException
      {
         return new CachedDocumentContentUrlConnection(url, document, content);
      }
      
   }
   
   private class CachedDocumentContentUrlConnection extends URLConnection
   {
      
      private final Document document;
      
      private final byte[] content;
      
      private boolean connected;
      
      public CachedDocumentContentUrlConnection(URL url, Document document, byte[] content)
      {
         super(url);
         
         this.document = document;
         this.content = content;
         
         this.connected = false;
      }

      public void connect() throws IOException
      {
         this.connected = true;
      }

      public int getContentLength()
      {
         return (int) document.getSize();
      }

      public String getContentType()
      {
         return document.getContentType();
      }

      public String getContentEncoding()
      {
         return document.getEncoding();
      }

      public String getHeaderField(String name)
      {
        if("content-encoding".equals(name))
        {
          return getContentEncoding();
        }
        else if ("content-length".equals(name))
        {
          return String.valueOf(getContentLength());
        }
        else if ("content-type".equals(name))
        {
          return getContentType();
        }
        
        return null;
      }
    
      public InputStream getInputStream() throws IOException
      {
         return new ByteArrayInputStream(content);
      }

   }
}
