/*
 * $Id$
 * (C) 2000 - 2012 SunGard CSA LLC
 */
package org.eclipse.stardust.engine.api.runtime;

import java.io.*;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.eclipse.stardust.common.CollectionUtils;

public class QuickModelInfo
{
   private static Set<String> stopTags = asSet(new String[] {
         "TypeDeclarations", "Participants", "Applications", "DataFields", "WorkflowProcesses", "ExtendedAttributes"
   });
   
   private String modelId;
   private List<String> referencedModelIds = null;
   
   private static Set<String> asSet(String[] strings)
   {
      Set<String> set = CollectionUtils.newSet();
      for (String string : strings)
      {
         set.add(string);
      }
      return set;
   }

   public String getModelId()
   {
      return modelId;
   }

   public List<String> getReferencedModelIds()
   {
      return referencedModelIds;
   }

   public QuickModelInfo(DeploymentElement de)
   {
      this(de.getContent());
   }
   
   public QuickModelInfo(byte[] content)
   {
      this(new InputStreamReader(new ByteArrayInputStream(content)));
   }
   
   public QuickModelInfo(File file) throws FileNotFoundException
   {
      this(new FileReader(file));
   }
   
   private QuickModelInfo(Reader reader)
   {
      Walker scrapper = null;
      try
      {
         scrapper = new Walker(reader, true);
         QName tag = scrapper.nextStartTag();
         if (tag != null)
         {
            if ("model".equals(tag.getLocalPart()))
            {
               modelId = scrapper.attribute("id");
            }
            else if ("Package".equals(tag.getLocalPart()))
            {
               modelId = scrapper.attribute("Id");
            }
            if (modelId != null)
            {
               while ((tag = scrapper.nextStartTag()) != null)
               {
                  if ("ExternalPackages".equals(tag.getLocalPart()))
                  {
                     while ((tag = scrapper.nextStartTag()) != null)
                     {
                        if ("ExternalPackage".equals(tag.getLocalPart()))
                        {
                           if (referencedModelIds == null)
                           {
                              referencedModelIds = CollectionUtils.newList();
                           }
                           referencedModelIds.add(scrapper.attribute("href"));
                        }
                        scrapper.skipContent();
                     }
                     break;
                  }
                  else if (stopTags.contains(tag.getLocalPart()))
                  {
                     break;
                  }
                  else
                  {
                     scrapper.skipContent();
                  }
               }
            }
         }
      }
      catch (XMLStreamException ex)
      {
         // TODO: (fh)
      }
      finally
      {
         if (scrapper != null)
         {
            scrapper.close();
         }
      }
   }
   
   @Override
   public String toString()
   {
      return "QuickModelInfo [modelId=" + modelId + ", referencedModelIds=" + referencedModelIds + "]";
   }

   private static class Walker
   {
      private XMLStreamReader reader;
      private int pos;

      private Walker(Reader input, boolean namespaceAware) throws XMLStreamException
      {
         XMLInputFactory factory = XMLInputFactory.newInstance();
         factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.valueOf(namespaceAware));
         factory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.valueOf(false));
         reader = factory.createXMLStreamReader(input);
         pos = 0;
      }
      
      private void close()
      {
         try
         {
            reader.close();
         }
         catch (XMLStreamException e)
         {
            // TODO: (fh) ignore ?
         }
         reader = null;
      }

      private QName nextStartTag() throws XMLStreamException
      {
         while (reader.hasNext())
         {
            switch (reader.next())
            {
            case XMLStreamReader.END_DOCUMENT:
               return null;
            case XMLStreamReader.END_ELEMENT:
               pos--;
               return null;
            case XMLStreamReader.START_ELEMENT:
               pos++;
               return reader.getName();
            }
         }
         return null;
      }

      private String attribute(String id)
      {
         for (int i = 0, l = reader.getAttributeCount(); i < l; i++)
         {
            if (id.equalsIgnoreCase(reader.getAttributeLocalName(i)))
            {
               return reader.getAttributeValue(i);
            }
         }
         return null;
      }

      private boolean skipContent() throws XMLStreamException
      {
         int c = pos;
         while (true)
         {
            switch (reader.next())
            {
            case XMLStreamReader.END_DOCUMENT:
               return false;
            case XMLStreamReader.END_ELEMENT:
               pos--;
               if (pos < c)
               {
                  return true;
               }
               break;
            case XMLStreamReader.START_ELEMENT:
               pos++;
            }
         }
      }
   }
}
