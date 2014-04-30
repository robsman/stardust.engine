package org.eclipse.stardust.engine.extensions.camel.util.converter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.ITypeDeclaration;
import org.eclipse.stardust.engine.api.runtime.LoginUtils;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingService;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.AbstractLoginInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.struct.ClientXPathMap;
import org.eclipse.stardust.engine.core.struct.IXPathMap;
import org.eclipse.stardust.engine.core.struct.StructuredDataConverter;
import org.eclipse.stardust.engine.core.struct.StructuredTypeRtUtils;
import org.eclipse.stardust.engine.core.struct.emfxsd.XPathFinder;
import org.eclipse.stardust.engine.core.struct.sxml.Document;
import org.eclipse.stardust.engine.core.struct.sxml.DocumentBuilder;
import org.eclipse.stardust.engine.core.struct.sxml.Element;
import org.eclipse.xsd.XSDNamedComponent;
import org.eclipse.xsd.XSDSchema;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

/**
 * 
 * @author JanHendrik.Scheufen
 */
public class StructuredDataTranslator
{
   private static Logger log = LogManager.getLogger(StructuredDataTranslator.class);

   private XSDSchema xsdSchema;
   private ForkingService forkingService;

   private String extractModelId(String expression)
   {
      int begin = expression.indexOf("reference:");
      int end = expression.indexOf("::");

      return expression.substring(begin + 10, end);
   }

   private String extractDataId(String expression)
   {
      int begin = expression.indexOf("::") + 2;
      return expression.substring(begin);

   }

   /**
    * sets the Xsd schema classpath location
    * 
    * @param schemaPath
    * @param partition
    * @throws IOException
    */
   @SuppressWarnings("unchecked")
   public void setXsdSchemaClasspathLocation(String schemaPath, final String partition) throws IOException
   {
      if (schemaPath.startsWith("internal:") || schemaPath.startsWith("reference:"))
      {
         final String modelId = extractModelId(schemaPath);
         final String dataId = extractDataId(schemaPath);
         IModel model = (IModel) this.forkingService.isolate(new Action<IModel>()
         {

            public IModel execute()
            {
               BpmRuntimeEnvironment bpmRt = PropertyLayerProviderInterceptor.getCurrent();
               Map<String, String> properties = new HashMap<String, String>();
               properties.put(SecurityProperties.PARTITION, partition);
               LoginUtils.mergeDefaultCredentials(Parameters.instance(), properties);
               AbstractLoginInterceptor.setCurrentPartitionAndDomain(Parameters.instance(), bpmRt, properties);
               return ModelManagerFactory.getCurrent().findActiveModel(modelId);
            }
         });

         for (Iterator<ITypeDeclaration> i = model.getTypeDeclarations().iterator(); i.hasNext();)
         {
            ITypeDeclaration type = i.next();
            if (dataId.equalsIgnoreCase(type.getId()))
            {
               this.xsdSchema = StructuredTypeRtUtils.getXSDSchema(model, type);
               break;
            }
         }

      } else if (schemaPath.startsWith("http:") ){//get url resource
         setXsdSchema(new UrlResource("url:"+schemaPath));
      }
      else
      {//classpath resource
         String xsdPath = schemaPath.substring(schemaPath.indexOf(":") + 1, schemaPath.length());
         setXsdSchema(new ClassPathResource(xsdPath));
      }
   }

   /**
    * sets the xsd schema
    * 
    * @param xsdSchema
    * @throws IOException
    */
   public void setXsdSchema(Resource xsdSchema) throws IOException
   {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024);

      byte[] bytes = new byte[512];

      // Read bytes from the input stream in bytes.length-sized chunks and
      // write
      // them into the output stream
      int readBytes;
      // Convert the contents of the output stream into a byte array
      byte[] byteData;
      try
      {
         InputStream input = xsdSchema.getInputStream();
         while ((readBytes = input.read(bytes)) > 0)
         {
            outputStream.write(bytes, 0, readBytes);
         }
         byteData = outputStream.toByteArray();
         // Close the streams
         input.close();
         outputStream.close();
      }
      catch (IOException e)
      {
         log.error("Unable to convert resource " + xsdSchema.getFilename() + " into byte[].");
         throw e;
      }

      this.xsdSchema = StructuredTypeRtUtils.deserializeSchema(byteData);
   }

   /**
    * @param schemaElementName
    * @param rootElement
    * @return output Map object
    */
   @SuppressWarnings("unchecked")
   public Map convert(String schemaElementName, Element rootElement)
   {
      Map outputMap = null;

      XSDNamedComponent component = StructuredTypeRtUtils.findElementOrTypeDeclaration(xsdSchema, schemaElementName,
            false);
      Set xPathSet = XPathFinder.findAllXPaths(xsdSchema, component);
      IXPathMap xPathMap = new ClientXPathMap(xPathSet);

      StructuredDataConverter structuredDataConverter = new StructuredDataConverter(xPathMap);
      outputMap = (Map) structuredDataConverter.toCollection(rootElement.toXML(), "", true);
      return outputMap;
   }

   /**
    * @param schemaElementName
    * @param xmlString
    * @return
    */
   @SuppressWarnings("unchecked")
   public Map convert(String schemaElementName, String xmlString)
   {
      return convert(schemaElementName, extractElement(xmlString, null));
   }

   @SuppressWarnings("unchecked")
   public Map convert(String schemaElementName, String xmlString, String childElementName)
   {
      return convert(schemaElementName, extractElement(xmlString, childElementName));
   }

   private Element extractElement(String xmlString, String childElementName)
   {
      Document document;
      try
      {
         document = DocumentBuilder.buildDocument(new ByteArrayInputStream(xmlString.getBytes()));
      }
      catch (PublicException e)
      {
         throw new RuntimeException("Unhandled exception creating document.", e);
      }
      catch (IOException e)
      {
         throw new RuntimeException("Unable to read XML String.", e);
      }

      if (StringUtils.isNotEmpty(childElementName))
      {
         return searchChildRecursively(document.getRootElement(), childElementName);
      }
      else
      {
         return document.getRootElement();
      }
   }

   private Element searchChildRecursively(Element parentElement, String childElementName)
   {
      Element child = parentElement.getFirstChildElement(childElementName);
      if (null == child)
      {
         List<Element> list = parentElement.getChildElements();
         for (Element element : list)
         {
            child = searchChildRecursively(element, childElementName);
            if (null != child)
               break;
         }
      }
      if (null != child)
         return child;
      else
         throw new IllegalStateException("Unable to find a child element with name '" + childElementName + "'.");
   }

   public ForkingService getForkingService()
   {
      return forkingService;
   }

   public void setForkingService(ForkingService forkingService)
   {
      this.forkingService = forkingService;
   }

}