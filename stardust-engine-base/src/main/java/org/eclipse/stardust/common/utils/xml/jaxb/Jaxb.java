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
package org.eclipse.stardust.common.utils.xml.jaxb;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.*;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.soap.SAAJResult;
import javax.xml.soap.SOAPElement;
import javax.xml.transform.dom.DOMResult;

import org.eclipse.stardust.common.reflect.Reflect;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public final class Jaxb
{
   private Jaxb() {};
   
   private static Map<Object, JAXBContext> contexts = new HashMap<Object, JAXBContext>();

   private static DatatypeFactory datatypeFactory;
   
   public static DatatypeFactory getDatatypeFactory() throws DatatypeConfigurationException
   {
      if (datatypeFactory == null)
      {
         synchronized (DatatypeFactory.class)
         {
            if (datatypeFactory == null)
            {
               datatypeFactory = DatatypeFactory.newInstance();
            }
         }
      }
      return datatypeFactory;
   }

   public static <T> void marshall(SOAPElement soapElement, JAXBElement<T> jaxbObject)
         throws JAXBException
   {
      JAXBContext context = getContext(jaxbObject.getDeclaredType());
      Marshaller marshaller = context.createMarshaller();
      SAAJResult result = new SAAJResult(soapElement);
      marshaller.marshal(jaxbObject, result);
   }
   
   public static Element marshall(Object jaxbObject, JAXBContext context)
         throws JAXBException
   {
      Element element = null;

      Marshaller marshaller = context.createMarshaller();
      DOMResult result = new DOMResult();
      marshaller.marshal(jaxbObject, result);
      Node node = result.getNode();
      if (node instanceof Element)
      {
         element = (Element) node;
      }
      else if (node instanceof Document)
      {
         element = ((Document) node).getDocumentElement();
      }

      return element;
   }

   public static Element marshall(String mapping, Object input)
         throws ClassNotFoundException, JAXBException
   {
      Class<?> clazz = Reflect.getClassFromClassName(mapping);
      String packageName = clazz.getPackage().getName();
      JAXBContext context = getContext(packageName);
      return marshall(input, context);
   }

   public static Object unmarshall(String className, Element input)
         throws ClassNotFoundException, JAXBException
   {
      Class<?> clazz = Reflect.getClassFromClassName(className);
      return unmarshall(clazz, input);
   }

   public static Object unmarshall(Class<?> clazz, Element input)
         throws ClassNotFoundException, JAXBException
   {
      String packageName = clazz.getPackage().getName();
      JAXBContext context = getContext(packageName);
      return unmarshall(clazz, input, context);
   }

   private static synchronized JAXBContext getContext(String packageName) throws JAXBException
   {
      JAXBContext context = contexts.get(packageName);
      if (context == null)
      {
         context = JAXBContext.newInstance(packageName);
         contexts.put(packageName, context);
      }
      return context;
   }

   private static synchronized JAXBContext getContext(Class<?> clazz) throws JAXBException
   {
      JAXBContext context = contexts.get(clazz);
      if (context == null)
      {
         context = JAXBContext.newInstance(clazz);
         contexts.put(clazz, context);
      }
      return context;
   }

   private static <T> T unmarshall(Class<T> target, Element element, JAXBContext context)
         throws JAXBException
   {
      Unmarshaller marshaller = context.createUnmarshaller();
      JAXBElement<T> result = marshaller.unmarshal(element, target);
      return result.getValue();
   }
}
