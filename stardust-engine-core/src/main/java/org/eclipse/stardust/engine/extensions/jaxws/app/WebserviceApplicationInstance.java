/*
 * $Id$
 * (C) 2000 - 2005 CARNOT AG
 */
package org.eclipse.stardust.engine.extensions.jaxws.app;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.*;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.soap.SOAPFaultException;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.ExtensionProviderUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.common.utils.xml.jaxb.Jaxb;
import org.eclipse.stardust.engine.api.model.Application;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.core.runtime.utils.XmlUtils;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.ApplicationInvocationContext;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.SynchronousApplicationInstance;
import org.eclipse.stardust.engine.extensions.jaxws.addressing.EndpointReferenceType;
import org.eclipse.stardust.engine.extensions.jaxws.addressing.WSAddressing;
import org.eclipse.stardust.engine.extensions.jaxws.app.spi.JaxwsClientConfigurer;
import org.eclipse.stardust.engine.extensions.jaxws.wssecurity.WSSecurity;

import org.w3c.dom.*;
import org.w3c.dom.Node;


/**
 * @author fherinean
 * @version $Revision$
 */
public class WebserviceApplicationInstance implements SynchronousApplicationInstance
{
   public static final Logger trace = LogManager.getLogger(WebserviceApplicationInstance.class);

   public static final String DYNAMIC_SERVICE_NAME = "DynamicService";
   public static final String DYNAMIC_PORT_NAME = "DynamicPort";

   public static final QName DYNAMIC_SERVICE_QNAME = new QName(
         WSConstants.DYNAMIC_BOUND_SERVICE_QNAME.getNamespaceURI(), DYNAMIC_SERVICE_NAME);
   public static final QName DYNAMIC_PORT_QNAME = new QName(
         WSConstants.DYNAMIC_BOUND_SERVICE_QNAME.getNamespaceURI(), DYNAMIC_PORT_NAME);

   private static final Set<String> primitiveTypes = new HashSet<String>();

   static {
      primitiveTypes.add(boolean.class.getName());
      primitiveTypes.add(Boolean.class.getName());

      primitiveTypes.add(float.class.getName());
      primitiveTypes.add(Float.class.getName());

      primitiveTypes.add(double.class.getName());
      primitiveTypes.add(Double.class.getName());

      primitiveTypes.add(Integer.class.getName());
      primitiveTypes.add(String.class.getName());

      primitiveTypes.add(char.class.getName());
      primitiveTypes.add(Character.class.getName());

      primitiveTypes.add(Long.class.getName());
      primitiveTypes.add(long.class.getName());

      primitiveTypes.add(Short.class.getName());
      primitiveTypes.add(short.class.getName());

      primitiveTypes.add(byte.class.getName());
      primitiveTypes.add(Byte.class.getName());
   }

   public static class JaxwsInvocationContext extends ApplicationInvocationContext
   {
      public JaxwsInvocationContext(ActivityInstance ai)
      {
         super(ai);
      }

      // both needed to instantiate the service.
      public QName serviceName;
      public String endpointName;
   
      public String inputOrder;
      public String outputOrder;
   
      public HashMap<String,String> typeMappings;
      public HashMap<String,QName> namespaces;
   
      public AuthenticationParameters auth;
      public EndpointReferenceType ref;
      public URL wsdlLocation;
   
      public Map<String, Object> inValues = new HashMap<String, Object>();
   
      public String endpointAddress;
      public String soapAction;
      public String soapProtocol;
   }

   private JaxwsInvocationContext ctx;

   private JaxwsClientConfigurer configurer;

   @SuppressWarnings("unchecked")
   public void bootstrap(ActivityInstance activityInstance)
   {
      Application application = activityInstance.getActivity().getApplication();
      Map<String, String> properties = application.getAllAttributes();

      this.ctx = new JaxwsInvocationContext(activityInstance);

      // TODO move this method into JaxwsInvocationContext construction, possibly allowing
      // to set fields final
      processProperties(properties);
      
      this.configurer = null;
      for (JaxwsClientConfigurer.Factory configurerFactory : ExtensionProviderUtils
            .getExtensionProviders(JaxwsClientConfigurer.Factory.class))
      {
         this.configurer = configurerFactory.createConfigurer(ctx);
         if (null != configurer)
         {
            trace.info("Discovered custom client configurer " + configurer + " for " + activityInstance);
            break;
         }
      }
   }

   private void processProperties(Map<String, String> properties)
   {
      String wsdlUrl = properties.get(WSConstants.WS_WSDL_URL_ATT);
      ctx.serviceName = QName.valueOf(properties.get(WSConstants.WS_SERVICE_NAME_ATT));

      ctx.endpointName = properties.get(WSConstants.WS_PORT_NAME_ATT);
      String noRuntimeWsdl = properties.get(WSConstants.WS_NO_RUNTIME_WSDL_ATT);
      if ("true".equals(noRuntimeWsdl)
            || WSConstants.DYNAMIC_BOUND_SERVICE_QNAME.equals(ctx.serviceName))
      {
         ctx.endpointName = DYNAMIC_PORT_NAME;
         ctx.serviceName = DYNAMIC_SERVICE_QNAME;
      }
      else
      {
         try
         {
            ctx.wsdlLocation = new URL(XmlUtils.resolveResourceUri(wsdlUrl));
         }
         catch (MalformedURLException e)
         {
            trace.warn("Invalid WSDL location: " + wsdlUrl, e);
         }
      }
      if (ctx.wsdlLocation == null)
      {
         ctx.wsdlLocation = WebserviceApplicationInstance.class.getResource("dummyServices.wsdl");
      }

      String implementation = (String) properties.get(WSConstants.WS_IMPLEMENTATION_ATT);
      if (WSConstants.WS_CARNOT_EPR.equals(implementation)
            || WSConstants.WS_GENERIC_EPR.equals(implementation))
      {
         ctx.ref = WSAddressing.newEndpointReference(ctx.serviceName, ctx.endpointName);
      }

      String authenticationType = (String) properties.get(WSConstants.WS_AUTHENTICATION_ATT);
      if (authenticationType != null)
      {
         ctx.auth = new AuthenticationParameters(authenticationType);
         String variant = (String) properties.get(WSConstants.WS_VARIANT_ATT);
         if (!StringUtils.isEmpty(variant))
         {
            ctx.auth.setVariant(variant);
         }
      }

      ctx.typeMappings = new HashMap<String, String>();
      ctx.namespaces = new HashMap<String, QName>();
      for (Iterator<Map.Entry<String, String>> i = properties.entrySet().iterator(); i.hasNext();)
      {
         Map.Entry<String, String> entry = i.next();
         if (entry.getKey().startsWith(WSConstants.WS_MAPPING_ATTR_PREFIX))
         {
            // We store the qualified part name in the mappings table.
            // Prefix may be one of "input:", "output:" or "fault:" + <fault_name>
            String name = entry.getKey().substring(
                  WSConstants.WS_MAPPING_ATTR_PREFIX.length());
            ctx.typeMappings.put(name, entry.getValue());
         }
         else if (entry.getKey().startsWith(WSConstants.WS_NAMESPACE_ATTR_PREFIX))
         {
            // We store the qualified part name in the mappings table.
            // Prefix may be one of "input:", "output:" or "fault:" + <fault_name>
            String name = entry.getKey().substring(
                  WSConstants.WS_NAMESPACE_ATTR_PREFIX.length());
            ctx.namespaces.put(name, QName.valueOf(entry.getValue()));
         }
         else if (entry.getKey().startsWith(WSConstants.WS_TEMPLATE_ATTR_PREFIX))
         {
            // Must not forget to add "input:" to the prefix.
            setInAccessPointValue(entry.getKey().substring(
                  (WSConstants.WS_TEMPLATE_ATTR_PREFIX + "input:").length()), entry.getValue());
         }
      }

      ctx.soapAction = properties.get(WSConstants.WS_SOAP_ACTION_URI_ATT);
      ctx.soapProtocol = properties.get(WSConstants.WS_SOAP_PROTOCOL_ATT);
      if (ctx.soapProtocol == null)
      {
         ctx.soapProtocol = SOAPConstants.DEFAULT_SOAP_PROTOCOL;
      }
      ctx.inputOrder = properties.get(WSConstants.WS_INPUT_ORDER_ATT);
      ctx.outputOrder = properties.get(WSConstants.WS_OUTPUT_ORDER_ATT);

      if (DYNAMIC_SERVICE_QNAME == ctx.serviceName)
      {
         String endpointAddress = (String) properties.get(WSConstants.WS_UDDI_ACCESS_POINT_ATT);
         setEndpointAddress(endpointAddress);
      }
   }

   private void setEndpointAddress(String address)
   {
      trace.debug("Set endpoint address to: " + ctx.endpointAddress);
      if (ctx.ref == null)
      {
         ctx.endpointAddress = address;
      }
      else
      {
         WSAddressing.setEndpointAddress(ctx.ref, address);
      }
   }

   @SuppressWarnings({"unchecked", "rawtypes"})
   public Map invoke(Set outDataTypes) throws InvocationTargetException
   {
      while (true) // TODO why is this a loop?
      {
         try
         {
            if (null != configurer)
            {
               configurer.initializeClientEnvironment(ctx);
            }
            
            SOAPMessage request = createRequestMessage();
            Service service = Service.create(ctx.wsdlLocation, ctx.serviceName);
            Dispatch<SOAPMessage> dispatch = null;
            if (ctx.ref != null)
            {
               EndpointReference epr = WSAddressing.toJaxwsEndpointReference(ctx.ref);
               dispatch = service.createDispatch(epr, SOAPMessage.class,
                  Service.Mode.MESSAGE, new AddressingFeature(true, false));
            }
            else
            {
               dispatch = service.createDispatch(new QName(ctx.serviceName.getNamespaceURI(), ctx.endpointName),
                     SOAPMessage.class, Service.Mode.MESSAGE);
            }
            setRequestProperties(dispatch);
            SOAPMessage response = dispatch.invoke(request);
            return processResponseMessage(response, outDataTypes);
         }
         catch (Exception e)
         {
            processSoapException(e);
            if (e instanceof InvocationTargetException)
            {
               throw (InvocationTargetException) e;
            }
            else
            {
               throw new InvocationTargetException(e, "Web Service call failed.");
            }
         }
      }
   }

   private void setRequestProperties(Dispatch<SOAPMessage> dispatch)
         throws InvocationTargetException
   {
      Map<String, Object> requestContext = dispatch.getRequestContext();
      if (ctx.auth != null)
      {
         if (WSConstants.WS_BASIC_AUTHENTICATION.equals(ctx.auth.getMechanism()))
         {
            if (ctx.auth.getUsername() == null)
            {
               throw new InvocationTargetException(null, "Basic authentication requires a username to be specified.");
            }
            requestContext.put(
                  BindingProvider.USERNAME_PROPERTY, ctx.auth.getUsername());
            if (ctx.auth.getPassword() != null)
            {
               requestContext.put(
                  BindingProvider.PASSWORD_PROPERTY, ctx.auth.getPassword());
            }
         }
      }
      if (ctx.soapAction != null)
      {
         requestContext.put(BindingProvider.SOAPACTION_USE_PROPERTY, Boolean.TRUE);
         requestContext.put(BindingProvider.SOAPACTION_URI_PROPERTY, ctx.soapAction);
      }
      if (ctx.endpointAddress != null)
      {
         requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, ctx.endpointAddress);
      }
   }

   private void processSoapException(Exception e) throws InvocationTargetException
   {
      if (e instanceof SOAPFaultException)
      {
         Throwable t = null;
         SOAPFaultException soapEx = (SOAPFaultException) e;
         SOAPFault fault = soapEx.getFault();
         String message = fault.getFaultString();
         Detail detail = fault.getDetail();
         if (detail != null)
         {
            // Search through fault mappings to find a matching one.
            // The first one found will be used.
            // The mapped exception must be annotated with WebFault annotation.
            for (Iterator<Map.Entry<String, String>> j = ctx.typeMappings.entrySet().iterator(); j.hasNext();)
            {
               Map.Entry<String, String> entry = j.next();
               String key = entry.getKey();
               if (key.startsWith("fault:"))
               {
                  String exceptionMapping = entry.getValue();
                  Class<?> exceptionClass = null;
                  try
                  {
                     exceptionClass = Reflect.getClassFromClassName(exceptionMapping);
                     WebFault wfa = exceptionClass.getAnnotation(WebFault.class);
                     if (wfa != null)
                     {
                        Element info = getElement(detail, wfa.targetNamespace(), wfa.name());
                        if (info != null)
                        {
                           Constructor<?> constructor = getExceptionConstructor(exceptionClass);
                           if (constructor != null)
                           {
                              Class<?> detailsClass = constructor.getParameterTypes()[1];
                              Object details = Jaxb.unmarshall(detailsClass, info);
                              t = (Exception) constructor.newInstance(message, details);
                           }
                        }
                     }
                  }
                  catch (Exception ex)
                  {
                     // ignore...
                     // exceptions here may come from:
                     // - Class.forName when the class is not found or not accessible
                     // - Jaxb.unmarshall when the parameter do not correspond to the target class
                     // - newInstance when the instantiation of the exception fails.
                  }
               }
            }
/*
            for (Iterator<DetailEntry> i = detail.getDetailEntries(); i.hasNext();)
            {
               DetailEntry entry = i.next();
               String partName = entry.getElementQName().toString();
               String exceptionName = partName;
               String mapping = typeMappings.get("fault:" + exceptionName + ":" + partName);
               if (mapping != null)
               {
                  try
                  {
                     Class<?> exceptionClass = Reflect.getClassFromClassName(mapping);
                     Constructor<?> constructor = getExceptionConstructor(exceptionClass);
                     if (constructor != null)
                     {
                        Class<?> detailsClass = constructor.getParameterTypes()[1];
                        Object details = Jaxb.unmarshall(detailsClass, entry);
                        t = (Exception) constructor.newInstance(message, details);
                     }
                  }
                  catch (Exception ex)
                  {
                     // ignore
                  }
               }
            }*/
         }
         throw new InvocationTargetException(t == null ? e : t, fault.getFaultString());
      }
   }

   private static Element getElement(Element parent, String namespace, String name)
   {
      return (Element) parent.getElementsByTagNameNS(namespace, name).item(0);
   }

   private static Constructor<?> getExceptionConstructor(Class<?> exceptionClass)
         throws ClassNotFoundException
   {
      Constructor<?>[] constructors = exceptionClass.getConstructors();
      for (int i = 0; i < constructors.length; i++)
      {
         Class<?>[] parameters = constructors[i].getParameterTypes();
         if (parameters.length == 2 && parameters[0].equals(String.class))
         {
            return constructors[i];
         }
      }
      return null;
   }

   private Map<String, ?> processResponseMessage(SOAPMessage response, Set<String> outDataTypes) throws Exception
   {
      Map<String, Object> data = new HashMap<String, Object>();
      if (ctx.outputOrder != null) // it may not have an output !
      {
         String[] parts = ctx.outputOrder.split(",");
         Iterator<?> i = response.getSOAPBody().getChildElements();
         int j = 0;

         while (i.hasNext() && j < parts.length)
         {
            String name = parts[j];
            Object value = i.next();

            // skip over non Element children
            // see #CRNT-11857 : JBOSS may include non Element children.
            if (!(value instanceof Element))
            {
               continue;
            }

            if (outDataTypes.contains(name))
            {
               String mapping = ctx.typeMappings.get("output:" + name);
               if (mapping != null)
               {
                  if (isPrimitive(mapping))
                  {
                     value = Reflect.convertStringToObject(mapping, trim(getTextContent((Element) value)));
                  }
                  else
                  {
                     value = Jaxb.unmarshall(mapping, (Element) value);
                  }
               }
               data.put(name, value);
            }

            if (outDataTypes.contains(name + WSConstants.STRUCT_POSTFIX))
            {
               data.put(name + WSConstants.STRUCT_POSTFIX, value);
            }

            j++;
         }
      }
      return data;
   }

   private String getTextContent(Node node) throws DOMException
   {
      if (node instanceof CharacterData)
      {
         // (fh) direct return of the node text content
         return ((CharacterData) node).getNodeValue();
      }
      Node child = node.getFirstChild();
      if (child != null)
      {
         Node next = child.getNextSibling();
         if (next == null)
         {
            // (fh) simple optimization to prevent creation of too many StringBuffers
            return hasTextContent(child) ? getTextContent(child) : "";
         }
         StringBuffer buf = new StringBuffer();
         getTextContent(buf, node);
         return buf.toString();
      }
      return "";
   }

   private void getTextContent(StringBuffer buf, Node node) throws DOMException
   {
      if (node instanceof CharacterData)
      {
         buf.append(((CharacterData) node).getNodeValue());
         return;
      }
      Node child = node.getFirstChild();
      while (child != null)
      {
         if (hasTextContent(child))
         {
            getTextContent(buf, child);
         }
         child = child.getNextSibling();
      }
   }

   private boolean hasTextContent(Node child)
   {
      return child.getNodeType() != Node.COMMENT_NODE &&
         child.getNodeType() != Node.PROCESSING_INSTRUCTION_NODE;
   }

   @SuppressWarnings({"rawtypes", "unchecked"})
   private SOAPMessage createRequestMessage() throws Exception
   {
      MessageFactory mf = null;
      try
      {
         mf = MessageFactory.newInstance(ctx.soapProtocol);
      }
      catch (Error err)
      {
         mf = MessageFactory.newInstance();
      }

      SOAPFactory sf = SOAPFactory.newInstance(ctx.soapProtocol);

      SOAPMessage request = mf.createMessage();
      if (ctx.soapAction != null && ctx.soapAction.length() > 0)
      {
         // workaround for implementations that do not recognize BindingProvider.SOAPACTION_URI_PROPERTY
         request.getMimeHeaders().addHeader("SOAPAction", ctx.soapAction);
      }

      WSSecurity.INSTANCE.setWSSHeaders(request.getSOAPHeader(), ctx.auth);

      SOAPBody body = request.getSOAPBody();

      if (ctx.inputOrder != null) // it may not have an input !
      {
         String[] parts = ctx.inputOrder.split(",");
         for (String name : parts)
         {
            Element partValue = null;
            Object value = ctx.inValues.get(name);
            if (value == null)
            {
               // try to get struct value if the old-style string/document value is not set
               value = ctx.inValues.get(name + WSConstants.STRUCT_POSTFIX);
            }
            String mapping = ctx.typeMappings.get("input:" + name);
            if (mapping != null)
            {
               if (isPrimitive(mapping))
               {
                  QName qname = ctx.namespaces.get("input:" + name);
                  SOAPElement soapElement = sf.createElement(qname);
                  soapElement.addTextNode(String.valueOf(value));
                  body.addChildElement(soapElement);
               }
               else
               {
                  if (!(value instanceof XmlRootElement))
                  {
                     QName qname = ctx.namespaces.get("input:" + name);
                     value = new JAXBElement(qname, value.getClass(), value);
                  }
                  partValue = Jaxb.marshall(mapping, value);
               }
            }
            else
            {
               partValue = getAsElement(value);
            }
            if (partValue != null)
            {
               SOAPElement soapElement = createElement(sf, partValue);
               body.addChildElement(soapElement);
            }
         }
      }
      request.saveChanges();
      return request;
   }

   private SOAPElement createElement(SOAPFactory sf, Element source)
         throws SOAPException
   {
      if (source == null)
      {
         return null;
      }
      if (source instanceof SOAPElement)
      {
         return (SOAPElement) source;
      }

      SOAPElement copy = sf.createElement(source.getLocalName(), source.getPrefix(), source.getNamespaceURI());
      copyContent(source, copy);
      return copy;
   }

   private Node importNode(Element source, Document doc)
   {
      String namespace = source.getNamespaceURI();
      Element copy = namespace == null || namespace.isEmpty()
         ? doc.createElement(source.getTagName())
         : doc.createElementNS(namespace, source.getTagName());
      copyContent(source, copy);
      return copy;
   }

   private void copyContent(Element source, Element target)
   {
      String namespace = target.getNamespaceURI();
      if (namespace != null && !namespace.isEmpty())
      {
         String prefix = target.getPrefix();
         if (prefix == null || prefix.isEmpty())
         {
            prefix = source.lookupPrefix(namespace);
            if (prefix == null)
            {
               int c = 0;
               String base = target.getLocalName();
               if (base.length() > 3)
               {
                  base = base.substring(0, 3);
               }
               do
               {
                  prefix = c++ == 0 ? base : base + c;
               }
               while (source.lookupNamespaceURI(prefix) != null);
               target.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE + ':' + prefix, namespace);
            }
            target.setPrefix(prefix);
         }
      }

      Document doc = target.getOwnerDocument();

      NamedNodeMap attrMap = source.getAttributes();
      for (int i = 0; i < attrMap.getLength(); i++)
      {
         Attr nextAttr = (Attr) attrMap.item(i);
         Attr importedAttr = (Attr) doc.importNode(nextAttr, true);
         target.setAttributeNodeNS(importedAttr);
      }

      NodeList nl = source.getChildNodes();
      for (int i = 0; i < nl.getLength(); i++)
      {
         org.w3c.dom.Node next = nl.item(i);
         org.w3c.dom.Node imported = next instanceof Element
               ? importNode((Element) next, doc)
               : doc.importNode(next, true);
         target.appendChild(imported);
      }
   }

   private boolean isPrimitive(String mapping)
   {
      return primitiveTypes.contains(mapping);
   }

   private Element getAsElement(Object value)
   {
      Element element = null;

      if (value instanceof Element)
      {
         element = (Element) value;
      }
      else if (value instanceof Document)
      {
         element = ((Document) value).getDocumentElement();
      }
      else if (value instanceof String)
      {
         element = XmlUtils.parseString((String) value).getDocumentElement();
      }

      return element;
   }

   public void setInAccessPointValue(String name, Object value)
   {
      if (WSConstants.WS_ENDPOINT_REFERENCE_ID.equals(name))
      {
         ctx.ref = (EndpointReferenceType) value;
      }
      else if (WSConstants.WS_ENDPOINT_ADDRESS_ID.equals(name))
      {
         setEndpointAddress((String) value);
      }
      else if (WSConstants.WS_AUTHENTICATION_ID.equals(name))
      {
         ctx.auth = (AuthenticationParameters) value;
      }
      else
      {
         if (!ctx.typeMappings.containsKey("input:" + name))
         {
            // no type mapping, direct values
            if (value instanceof Element)
            {
               // value is already a DOM element
               ctx.inValues.put(name, value);
            }
            else
            {
               // old behavior, parse XML passed as string
               Document document = XmlUtils.parseString(String.valueOf(value));
               ctx.inValues.put(name, document);
            }
         }
         else
         {
            ctx.inValues.put(name, value);
         }
      }
   }

   public Object getOutAccessPointValue(String name)
   {
      if (WSConstants.WS_ENDPOINT_REFERENCE_ID.equals(name))
      {
         return ctx.ref;
      }
      else if (WSConstants.WS_ENDPOINT_ADDRESS_ID.equals(name))
      {
         return ctx.ref == null ? ctx.endpointAddress : ctx.ref.getAddress().getValue();
      }
      else if (WSConstants.WS_AUTHENTICATION_ID.equals(name))
      {
         return ctx.auth;
      }
      else
      {
         Object ret = ctx.inValues.get(name);
         if (ret == null)
         {
            try
            {
               String paramType = (String) ctx.typeMappings.get("input:" + name);
               Class<?> paramClass = Reflect.getClassFromClassName(paramType);
               if (null != paramClass)
               {
                  ret = paramClass.newInstance();
                  ctx.inValues.put(name, ret);
               }
            }
            catch (Exception e)
            {
               // silently ignore exceptions and return null
            }
         }
         return ret;
      }
   }

   public void cleanup()
   {
      if (null != configurer)
      {
         try
         {
            configurer.cleanupClientEnvironment(ctx);
         }
         catch (Exception e)
         {
            trace.warn("Failed cleaning up JAX-WS client environment.", e);
         }
         this.configurer = null;
      }
      this.ctx = null;
   }

   private static String trim(String text)
   {
      if (text.length() > 0)
      {
         int start = 0;
         int end = text.length();
         while (start < end && Character.isWhitespace(text.charAt(start)))
         {
            start++;
         }
         while (end > start && Character.isWhitespace(text.charAt(end - 1)))
         {
            end--;
         }
         if (start > 0 || end < text.length())
         {
            text = text.substring(start, end);
         }
      }
      return text;
   }
}
