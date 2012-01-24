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
package org.eclipse.stardust.common.utils.xml.stream;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.annotations.ConfigurationProperty;
import org.eclipse.stardust.common.annotations.PropertyValueType;
import org.eclipse.stardust.common.annotations.Status;
import org.eclipse.stardust.common.annotations.UseRestriction;
import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.ValueProvider;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;


public class StaxUtils
{
   private static final Logger trace = LogManager.getLogger(StaxUtils.class);

   private static final String PRP_PREFIX = "Carnot.Xml.StAX.";

   @ConfigurationProperty(status = Status.Experimental, useRestriction = UseRestriction.Public)
   @PropertyValueType(XMLInputFactory.class)
   public static final String PRP_XML_INPUT_FACTORY = PRP_PREFIX + "XmlInputFactory";

   @ConfigurationProperty(status = Status.Experimental, useRestriction = UseRestriction.Public)
   @PropertyValueType(Boolean.class)
   public static final String PRP_CACHE_XML_INPUT_FACTORY = PRP_PREFIX
         + "XmlInputFactory.Cached";

   @PropertyValueType(XMLOutputFactory.class)
   @ConfigurationProperty(status = Status.Experimental, useRestriction = UseRestriction.Public)
   public static final String PRP_XML_OUTPUT_FACTORY = PRP_PREFIX + "XmlOutputFactory";

   @ConfigurationProperty(status = Status.Experimental, useRestriction = UseRestriction.Public)
   @PropertyValueType(Boolean.class)
   public static final String PRP_CACHE_XML_OUTPUT_FACTORY = PRP_PREFIX
         + "XmlOutputFactory.Cached";

   @PropertyValueType(XMLEventFactory.class)
   @ConfigurationProperty(status = Status.Experimental, useRestriction = UseRestriction.Public)
   public static final String PRP_XML_EVENT_FACTORY = PRP_PREFIX + "XmlEventFactory";

   @ConfigurationProperty(status = Status.Experimental, useRestriction = UseRestriction.Public)
   @PropertyValueType(Boolean.class)
   public static final String PRP_CACHE_XML_EVENT_FACTORY = PRP_PREFIX
         + "XmlEventFactory.Cached";

   private static final String CACHED_XML_INPUT_FACTORY = StaxUtils.class.getName()
         + ".XmlInputFactory.Cached";

   private static final String CACHED_REPAIRING_XML_OUTPUT_FACTORY = StaxUtils.class.getName()
         + ".XmlOutputFactory.NamespaceRepairing.Cached";

   private static final String CACHED_NONREPAIRING_XML_OUTPUT_FACTORY = StaxUtils.class.getName()
         + ".XmlOutputFactory.NonNamespaceRepairing.Cached";

   private static final String CACHED_XML_EVENT_FACTORY = StaxUtils.class.getName()
         + ".XmlEventFactory.Cached";

   public static XMLInputFactory getXmlInputFactory()
   {
      if (Parameters.instance().getBoolean(PRP_CACHE_XML_INPUT_FACTORY, true))
      {
         return (XMLInputFactory) GlobalParameters.globals().getOrInitialize(
               CACHED_XML_INPUT_FACTORY, SHARED_INPUT_FACTORY_INIT_CALLBACK);
      }
      else
      {
         return createNewXmlInputFactory();
      }
   }

   public static XMLOutputFactory getXmlOutputFactory()
   {
      return getXmlOutputFactory(true);
   }

   public static XMLOutputFactory getXmlOutputFactory(boolean repairingNamespaces)
   {
      if (Parameters.instance().getBoolean(PRP_CACHE_XML_OUTPUT_FACTORY, true))
      {
         if (repairingNamespaces)
         {
            return (XMLOutputFactory) GlobalParameters.globals().getOrInitialize(
                  CACHED_REPAIRING_XML_OUTPUT_FACTORY,
                  SHARED_REPAIRING_OUTPUT_FACTORY_INIT_CALLBACK);
         }
         else
         {
            return (XMLOutputFactory) GlobalParameters.globals().getOrInitialize(
                  CACHED_NONREPAIRING_XML_OUTPUT_FACTORY,
                  SHARED_NONREPAIRING_OUTPUT_FACTORY_INIT_CALLBACK);
         }
      }
      else
      {
         return createNewXmlOutputFactory();
      }
   }

   public static XMLEventFactory getXmlEventFactory()
   {
      if (Parameters.instance().getBoolean(PRP_CACHE_XML_EVENT_FACTORY, true))
      {
         return (XMLEventFactory) GlobalParameters.globals().getOrInitialize(
               CACHED_XML_EVENT_FACTORY, SHARED_EVENT_FACTORY_INIT_CALLBACK);
      }
      else
      {
         return createNewXmlEventFactory();
      }
   }

   public static XMLInputFactory createNewXmlInputFactory()
   {
      String factoryClazz = Parameters.instance().getString(PRP_XML_INPUT_FACTORY);
      XMLInputFactory factory;
      if (StringUtils.isEmpty(factoryClazz))
      {
         factory = XMLInputFactory.newInstance();
      }
      else
      {
         factory = (XMLInputFactory) Reflect.createInstance(factoryClazz);
      }

      return factory;
   }

   public static XMLOutputFactory createNewXmlOutputFactory()
   {
      String factoryClazz = Parameters.instance().getString(PRP_XML_OUTPUT_FACTORY);
      XMLOutputFactory factory;
      if (StringUtils.isEmpty(factoryClazz))
      {
         factory = XMLOutputFactory.newInstance();
      }
      else
      {
         factory = (XMLOutputFactory) Reflect.createInstance(factoryClazz);
      }

      return factory;
   }

   public static XMLEventFactory createNewXmlEventFactory()
   {
      String factoryClazz = Parameters.instance().getString(PRP_XML_EVENT_FACTORY);
      XMLEventFactory factory;
      if (StringUtils.isEmpty(factoryClazz))
      {
         factory = XMLEventFactory.newInstance();
      }
      else
      {
         factory = (XMLEventFactory) Reflect.createInstance(factoryClazz);
      }

      return factory;
   }

   private static void makeThreadSafe(Object sharedFactory)
   {
      // best effort to fix thread safety issue with StAX input factory Sun 6 JRE factory
      // (for an explanation refer to https://issues.apache.org/jira/browse/AXIOM-74)
      try
      {
         if (sharedFactory instanceof XMLInputFactory)
         {
            ((XMLInputFactory) sharedFactory).setProperty("reuse-instance", Boolean.FALSE);
         }
         else if (sharedFactory instanceof XMLOutputFactory)
         {
            ((XMLOutputFactory) sharedFactory).setProperty("reuse-instance",
                  Boolean.FALSE);
         }
         else
         {
            throw new InternalException("Unsupported factory type: " + sharedFactory);
         }
      }
      catch (IllegalArgumentException iae)
      {
         // "reuse-instance" is specific to the factory included with Sun 6 JRE
         if (sharedFactory.getClass()
               .getName()
               .startsWith("com.sun.xml.internal.stream."))
         {
            trace.warn("Failed setting 'reuse-instance' property on StAX factory "
                  + sharedFactory);
         }
         else
         {
            trace.debug("Failed setting 'reuse-instance' property on StAX factory "
                  + sharedFactory);
         }
      }
   }

   private static final ValueProvider<XMLInputFactory> SHARED_INPUT_FACTORY_INIT_CALLBACK = new ValueProvider<XMLInputFactory>()
   {
      public XMLInputFactory getValue()
      {
         XMLInputFactory sharedFactory = createNewXmlInputFactory();

         makeThreadSafe(sharedFactory);

         return sharedFactory;
      }
   };

   private static final ValueProvider<XMLOutputFactory> SHARED_REPAIRING_OUTPUT_FACTORY_INIT_CALLBACK = new ValueProvider<XMLOutputFactory>()
   {
      public XMLOutputFactory getValue()
      {
         XMLOutputFactory sharedFactory = createNewXmlOutputFactory();

         sharedFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);

         makeThreadSafe(sharedFactory);

         return sharedFactory;
      }
   };

   private static final ValueProvider<XMLOutputFactory> SHARED_NONREPAIRING_OUTPUT_FACTORY_INIT_CALLBACK = new ValueProvider<XMLOutputFactory>()
   {
      public XMLOutputFactory getValue()
      {
         XMLOutputFactory sharedFactory = createNewXmlOutputFactory();

         sharedFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, false);

         makeThreadSafe(sharedFactory);

         return sharedFactory;
      }
   };

   private static final ValueProvider<XMLEventFactory> SHARED_EVENT_FACTORY_INIT_CALLBACK = new ValueProvider<XMLEventFactory>()
   {
      public XMLEventFactory getValue()
      {
         XMLEventFactory sharedFactory = createNewXmlEventFactory();

         // makeThreadSafe(sharedFactory);

         return sharedFactory;
      }
   };
}
