/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Florin.Herinean (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.engine.core.upgrade.framework;

import java.io.Reader;
import java.util.HashSet;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.sax.SAXSource;

import org.eclipse.stardust.engine.core.model.parser.info.ModelInfoRetriever;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

public abstract class ModelUpgradeInfo
{
   public static enum ModelType {
      cwm, xpdl
   }

   String id;
   String name;
   String version;
   String vendor;

   long maxOid;
   HashSet<String> participantIds = new HashSet<String>();
   HashSet<String> dataIds = new HashSet<String>();

   @Override
   public String toString()
   {
      return id;
   }

   public String getId()
   {
      return id;
   }

   public String getName()
   {
      return name;
   }

   public String getVersion()
   {
      return version;
   }

   public String getVendor()
   {
      return vendor;
   }

   public long getNextOid()
   {
      return ++maxOid;
   }

   public boolean hasParticipant(String id)
   {
      return participantIds != null && participantIds.contains(id);
   }

   public boolean hasData(String id)
   {
      return dataIds != null && dataIds.contains(id);
   }

   public abstract ModelType getType();

   static ModelUpgradeInfo get(Reader reader) throws JAXBException, SAXException
   {
      return get(new InputSource(reader));
   }

   private static JAXBContext context;

   private static JAXBContext getContext() throws JAXBException
   {
      if (context == null)
      {
         context = JAXBContext.newInstance(XpdlUpgradeInfo.class, CwmUpgradeInfo.class);
      }
      return context;
   }

   private static ModelUpgradeInfo get(InputSource inputSource) throws SAXException, JAXBException
   {
      OidFilter filter = new OidFilter(ModelInfoRetriever.getNameSpaceFilter());
      SAXSource source = ModelInfoRetriever.getModelSource(inputSource, filter);
      JAXBContext context = getContext();
      Unmarshaller um = context.createUnmarshaller();
      ModelUpgradeInfo info = (ModelUpgradeInfo) um.unmarshal(source);
      info.maxOid = filter.maxOid;
      return info;
   }

   private final static class OidFilter extends XMLFilterImpl
   {
      long maxOid = 0;
      boolean known = false;
      boolean isXpdl = false;

      public OidFilter(XMLReader parent)
      {
         super(parent);
      }

      @Override
      public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
      {
         if (known)
         {
            String oid = atts.getValue(isXpdl ? "Oid" : "oid");
            if (oid != null)
            {
               try
               {
                  long parsedOid = Long.parseLong(oid);
                  maxOid = Math.max(maxOid, parsedOid);
               }
               catch (NumberFormatException ex)
               {
                  // ignore
               }
            }
         }
         else if ("Package".equals(localName))
         {
            known = true;
            isXpdl = true;
         }
         else if ("model".equals(localName))
         {
            known = true;
            isXpdl = false;
         }
         super.startElement(uri, localName, qName, atts);
      }
   }

   @XmlRootElement(name="Package", namespace="http://www.wfmc.org/2008/XPDL2.1")
   final static class XpdlUpgradeInfo extends ModelUpgradeInfo
   {
      @XmlElement(name="Participants", namespace="http://www.wfmc.org/2008/XPDL2.1")
      void setDataFields(XpdlParticipants participants)
      {
         participantIds = participants.participantIds;
      }

      @XmlElement(name="DataFields", namespace="http://www.wfmc.org/2008/XPDL2.1")
      void setDataFields(XpdlDataFields dataFieldsInfo)
      {
         dataIds = dataFieldsInfo.dataIds;
      }

      @XmlElement(name="PackageHeader", namespace="http://www.wfmc.org/2008/XPDL2.1")
      void setPackageHeader(XpdlPackageHeader packageHeader)
      {
         vendor = packageHeader.vendor;
      }

      @XmlElement(name="ExtendedAttributes", namespace="http://www.wfmc.org/2008/XPDL2.1")
      void setExtendedAttributes(XpdlExtendedAttributes extendedAttributes)
      {
         version = extendedAttributes.version;
      }

      @XmlAttribute(name="Id")
      void setId(String id)
      {
         this.id = id;
      }

      @XmlAttribute(name="Name")
      void setName(String name)
      {
         this.name = name;
      }

      final static class XpdlParticipants
      {
         HashSet<String> participantIds = new HashSet<String>();

         @XmlElement(name="Participant", namespace="http://www.wfmc.org/2008/XPDL2.1")
         void setParticipant(XpdlObject participant)
         {
            participantIds.add(participant.id);
         }
      }

      final static class XpdlDataFields
      {
         HashSet<String> dataIds = new HashSet<String>();

         @XmlElement(name="DataField", namespace="http://www.wfmc.org/2008/XPDL2.1")
         void setData(XpdlObject data)
         {
            dataIds.add(data.id);
         }
      }

      final static class XpdlPackageHeader
      {
         @XmlElement(name="Vendor", namespace="http://www.wfmc.org/2008/XPDL2.1")
         String vendor;
      }

      final static class XpdlObject
      {
         @XmlAttribute(name="Id")
         String id;
      }

      final static class XpdlExtendedAttributes
      {
         String version;

         @XmlElement(name="ExtendedAttribute", namespace="http://www.wfmc.org/2008/XPDL2.1")
         void setExtendedAttribute(XpdlExtendedAttribute extendedAttribute)
         {
            if ("CarnotExt".equals(extendedAttribute.name) && version == null)
            {
               version = extendedAttribute.version;
            }
         }
      }

      final static class XpdlExtendedAttribute
      {
         String version;

         @XmlAttribute(name="Name")
         String name;

         @XmlElement(name="Package", namespace="http://www.carnot.ag/xpdl/3.1")
         void setCarnotPackage(CarnotPackage carnotPackage)
         {
            version = carnotPackage.version;
         }
      }

      final static class CarnotPackage
      {
         @XmlAttribute(name="CarnotVersion")
         String version;
      }

      @Override
      public ModelType getType()
      {
         return ModelType.xpdl;
      }
   }

   @XmlRootElement(name="model", namespace="http://www.carnot.ag/workflowmodel/3.1")
   final static class CwmUpgradeInfo extends ModelUpgradeInfo
   {
      @XmlElement(name="role", namespace="http://www.carnot.ag/workflowmodel/3.1")
      void setRole(CwmObject role)
      {
         participantIds.add(role.id);
      }

      @XmlElement(name="data", namespace="http://www.carnot.ag/workflowmodel/3.1")
      void setData(CwmObject data)
      {
         dataIds.add(data.id);
      }

      @XmlAttribute(name="id")
      void setId(String id)
      {
         this.id = id;
      }

      @XmlAttribute(name="name")
      void setName(String name)
      {
         this.name = name;
      }

      @XmlAttribute(name="carnotVersion")
      void setVersion(String version)
      {
         this.version = version;
      }

      @XmlAttribute(name="vendor")
      void setVendor(String vendor)
      {
         this.vendor = vendor;
      }

      final static class CwmObject
      {
         @XmlAttribute
         String id;
      }

      @Override
      public ModelType getType()
      {
         return ModelType.cwm;
      }
   }
}