/*
 * $Id: Addressing.java 47832 2011-08-01 10:29:24Z nicolas.werlein $
 * (C) 2000 - 2008 CARNOT AG
 */
package org.eclipse.stardust.engine.extensions.jaxws.addressing;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.util.JAXBSource;
import javax.xml.namespace.QName;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.spi.Provider;

import org.eclipse.stardust.engine.extensions.jaxws.addressing.infinity.*;
import org.eclipse.stardust.engine.extensions.jaxws.addressing.wsdl.ServiceNameType;

public final class WSAddressing
{
   private WSAddressing () {};
   
   private static org.eclipse.stardust.engine.extensions.jaxws.addressing.infinity.ObjectFactory carnotFactory =
      new org.eclipse.stardust.engine.extensions.jaxws.addressing.infinity.ObjectFactory();
   
   private static org.eclipse.stardust.engine.extensions.jaxws.addressing.ObjectFactory addressingFactory =
      new org.eclipse.stardust.engine.extensions.jaxws.addressing.ObjectFactory();

   private static org.eclipse.stardust.engine.extensions.jaxws.addressing.wsdl.ObjectFactory wsdlFactory =
      new org.eclipse.stardust.engine.extensions.jaxws.addressing.wsdl.ObjectFactory();

   private static JAXBContext addressingContext;

   static
   {
      try
      {
         addressingContext = JAXBContext.newInstance(
               "org.eclipse.stardust.engine.extensions.jaxws.addressing:" +
               "org.eclipse.stardust.engine.extensions.jaxws.addressing.wsdl:" +
               "org.eclipse.stardust.engine.extensions.jaxws.addressing.infinity");
      }
      catch (JAXBException e)
      {
         // TODO: trace! (see CRNT-21309)
         e.printStackTrace();
      }
   }
   
   public static EndpointReferenceType newEndpointReference(QName serviceName, String endpointName)
   {
      EndpointReferenceType epr =  new IPPEndpointReference();
      setEndpointInfo(epr, serviceName, endpointName);
      return epr;
   }
   
   public static void setEndpointAddress(EndpointReferenceType epr, String address)
   {
      AttributedURIType attributedUri = addressingFactory.createAttributedURIType();
      attributedUri.setValue(address);
      epr.setAddress(attributedUri);
   }

   public static EndpointReference toJaxwsEndpointReference(EndpointReferenceType ref)
      throws JAXBException
   {
      JAXBElement<EndpointReferenceType> epr = addressingFactory.createEndpointReference(ref);
      JAXBSource infoset = new JAXBSource(addressingContext, epr);
      return Provider.provider().readEndpointReference(infoset);
   }

   private static void setId(EndpointReferenceType epr, IdEndpointType endpointType, String id)
   {
      InfinityIdExtensionType idExtension = carnotFactory.createInfinityIdExtensionType();
      idExtension.setElementType(endpointType);
      idExtension.setValue(id);
      epr.getMetadata().getAny().add(carnotFactory.createID(idExtension));
   }

   private static void setOid(EndpointReferenceType epr, OidEndpointType endpointType, long oid)
   {
      InfinityOidExtensionType oidExtension = carnotFactory.createInfinityOidExtensionType();
      oidExtension.setElementType(endpointType);
      oidExtension.setValue(oid);
      epr.getMetadata().getAny().add(carnotFactory.createOID(oidExtension));
   }

   private static void setValue(EndpointReferenceType epr, ValueEndpointType endpointType, String value)
   {
      InfinityValueExtensionType valueExtension = carnotFactory.createInfinityValueExtensionType();
      valueExtension.setElementType(endpointType);
      valueExtension.setValue(value);
      epr.getMetadata().getAny().add(carnotFactory.createVALUE(valueExtension));
   }

   private static void setEndpointInfo(EndpointReferenceType epr, QName serviceName,
         String endpointName)
   {
      ServiceNameType service = wsdlFactory.createServiceNameType();
      service.setValue(serviceName);
      service.setEndpointName(endpointName);
      MetadataType metadataType = addressingFactory.createMetadataType();
      metadataType.getAny().add(wsdlFactory.createServiceName(service));
      epr.setMetadata(metadataType);
   }
   
   public static class IPPEndpointReference extends EndpointReferenceType
   {
      public void setServiceRegistryID(String id)
      {
         setId(this, IdEndpointType.REGISTRY, id);
      }

      public void setFactoryID(String id)
      {
         setId(this, IdEndpointType.FACTORY, id);
      }

      public void setInstanceID(String id)
      {
         setId(this, IdEndpointType.INSTANCE, id);
      }

      public void setActivityID(String id)
      {
         setId(this, IdEndpointType.ACTIVITY, id);
      }

      public void setDataID(String id)
      {
         setId(this, IdEndpointType.DATA, id);
      }

      public void setInstanceOID(long oid)
      {
         setOid(this, OidEndpointType.INSTANCE, oid);
      }

      public void setActivityOID(long oid)
      {
         setOid(this, OidEndpointType.ACTIVITY, oid);
      }

      public void setDataValue(String value)
      {
         setValue(this, ValueEndpointType.DATA, value);
      }
      
      public void setEndpointAddress(String address)
      {
         WSAddressing.setEndpointAddress(this, address);
      }
   }
}
