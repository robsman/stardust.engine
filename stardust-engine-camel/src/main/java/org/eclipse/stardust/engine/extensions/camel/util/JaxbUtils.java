package org.eclipse.stardust.engine.extensions.camel.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Utility class for marshalling and unmarshalling between XML data and JAXB ojbects. To
 * be used in particular for working with Structured Data.
 * 
 * @author JanHendrik.Scheufen
 */
public final class JaxbUtils
{

   private static final Logger LOG = LoggerFactory.getLogger(JaxbUtils.class);

   // JAXBContext is thread-safe, so they can be cached for re-use
   private static Map<String, JAXBContext> _jaxbContextCache = new HashMap<String, JAXBContext>();

   private JaxbUtils()
   {}

   /**
    * Unmarshals the specified Element into a bean of the specified returnType.
    * 
    * @param returnType
    *           class of the expected bean
    * @param element
    *           XML to umarshal
    * 
    * @return <T> instance of the bean
    * @throws JAXBException
    *            if there's a problem during unmarshalling
    */
   public static <T> T unmarshal(Class<T> returnType, Element element) throws JAXBException
   {
      if (returnType == null)
      {
         throw new IllegalArgumentException("Expected Returntype null");
      }
      if (element == null)
      {
         throw new IllegalArgumentException("Passed Element null");
      }
      JAXBContext context = getJaxbContext(returnType);
      Unmarshaller unmarshaller = context.createUnmarshaller();
      JAXBElement<T> userElement = unmarshaller.unmarshal(element, returnType);
      Object value = userElement.getValue();

      if (value != null)
      {
         if (!returnType.isAssignableFrom(value.getClass()))
            throw new IllegalArgumentException("Returntype does not match the passed Element");
      }

      return returnType.cast(value);
   }

   /**
    * Unmarshals the specified XML byte array into a bean of the specified returnType.
    * 
    * @param returnType
    *           class of the expected bean
    * @param xml
    *           XML to unmarshal
    * 
    * @return <T> instance of the bean
    * @throws JAXBException
    *            if there's a problem during unmarshalling
    */
   public static <T> T unmarshal(Class<T> returnType, byte[] xml) throws JAXBException
   {
      return unmarshal(returnType, new ByteArrayInputStream(xml));
   }

   /**
    * 
    * @param returnType
    *           class of the expected bean
    * @param xml
    *           XML to unmarshal
    * @return instance of the bean
    * @throws JAXBException
    *            if there's a problem during unmarshalling
    */
   public static <T> T unmarshal(Class<T> returnType, InputStream xml) throws JAXBException
   {
      if (returnType == null)
      {
         throw new IllegalArgumentException("Expected Returntype null");
      }
      if (xml == null)
      {
         throw new IllegalArgumentException("Passed Byte array null");
      }
      JAXBContext context = getJaxbContext(returnType);
      Unmarshaller unmarshaller = context.createUnmarshaller();
      JAXBElement<T> jaxbElement;
      try
      {
         jaxbElement = unmarshaller.unmarshal(XMLInputFactory.newInstance().createXMLStreamReader(xml), returnType);
      }
      catch (XMLStreamException e)
      {
         throw new RuntimeException("Unable to create XMLStreamReader", e);
      }
      catch (FactoryConfigurationError e)
      {
         throw new RuntimeException("Unable to create XMLInputFactory", e);
      }
      Object value = jaxbElement.getValue();

      if (value != null)
      {
         if (!returnType.isAssignableFrom(value.getClass()))
            throw new IllegalArgumentException("Returntype does not match the passed Element");
      }

      return returnType.cast(value);
   }

   /**
    * Marshals the specified Object into its XML representation. NOTE: This method uses
    * the simple class name of the specified object to create a QName. If the QName of the
    * object is known, you should use {@link #marshalElement(Class, Object, QName)}
    * 
    * @param declareType
    * @param value
    *           the object to marshal
    * @return Element the XML as an Element
    * @throws JAXBException
    *            if there's a problem during marshalling
    */
   public static <T> Element marshalElement(Class<T> declaredType, Object value) throws JAXBException
   {
      return marshalElement(declaredType, value, new QName(value.getClass().getSimpleName()));
   }

   /**
    * 
    * @param declaredType
    * @param value
    * @param qname
    * @return
    * @throws JAXBException
    */
   public static <T> Element marshalElement(Class<T> declaredType, Object value, QName qname) throws JAXBException
   {
      try
      {
         if (value == null)
         {
            throw new IllegalArgumentException("Passed object is null");
         }

         JAXBContext context = getJaxbContext(value.getClass());
         Marshaller mashaller = context.createMarshaller();

         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
         dbf.setNamespaceAware(true);

         DocumentBuilder db = dbf.newDocumentBuilder();
         Document doc = db.newDocument();
         JAXBElement<T> jaxbElement = new JAXBElement<T>(qname, declaredType, declaredType.cast(value));
         mashaller.marshal(jaxbElement, doc);

         return (Element) doc.getFirstChild();
      }
      catch (ParserConfigurationException e)
      {
         LOG.error(e.getMessage(), e);
         throw new JAXBException(e.getMessage(), e);
      }
   }

   /**
    * Marshals the specified Object into its XML representation. NOTE: This method uses
    * the simple class name of the specified object to create a QName. If the QName of the
    * object is known, you should use {@link #marshalByteArray(Class, Object, QName)}
    * 
    * @param declareType
    * @param value
    *           the object to marshal
    * @return the XML as a byte array
    * @throws JAXBException
    *            if there's a problem during marshalling
    */
   public static <T> byte[] marshalByteArray(Class<T> declaredType, Object value) throws JAXBException
   {
      return marshalOutputStream(declaredType, value).toByteArray();
   }

   /**
    * Marshals the specified Object into its XML representation.
    * 
    * @param declaredType
    * @param value
    * @param qname
    * @return the XML as a byte array
    * @throws JAXBException
    */
   public static <T> byte[] marshalByteArray(Class<T> declaredType, Object value, QName qname) throws JAXBException
   {
      return marshalOutputStream(declaredType, value, qname).toByteArray();
   }

   /**
    * Marshals the specified Object into its XML representation NOTE: This method uses the
    * simple class name of the specified object to create a QName. If the QName of the
    * object is known, you should use {@link #marshalByteArray(Class, Object, QName)}
    * 
    * @param declaredType
    * @param value
    *           the object to marshal
    * @return the XML as an output stream
    * @throws JAXBException
    *            if there's a problem during marshalling
    */
   public static <T> ByteArrayOutputStream marshalOutputStream(Class<T> declaredType, Object value)
         throws JAXBException
   {
      return marshalOutputStream(declaredType, value, new QName(value.getClass().getSimpleName()));
   }

   /**
    * Marshals the specified object into its XML representation.
    * 
    * @param declaredType
    * @param dataBean
    * @param qname
    * @return the XML as an output stream
    * @throws JAXBException
    */
   public static <T> ByteArrayOutputStream marshalOutputStream(Class<T> declaredType, Object dataBean, QName qname)
         throws JAXBException
   {
      if (dataBean == null)
      {
         throw new IllegalArgumentException("Passed object is null");
      }

      JAXBContext context = getJaxbContext(dataBean.getClass());
      Marshaller marshaller = context.createMarshaller();

      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      JAXBElement<T> jaxbElement = new JAXBElement<T>(qname, declaredType, declaredType.cast(dataBean));
      marshaller.marshal(jaxbElement, outputStream);

      return outputStream;
   }

   private static <T> JAXBContext getJaxbContext(Class<T> type) throws JAXBException
   {
      JAXBContext context = _jaxbContextCache.get(type.getName());
      if (null == context)
      {
         context = JAXBContext.newInstance(type);
         _jaxbContextCache.put(type.getName(), context);
      }
      return context;
   }
}