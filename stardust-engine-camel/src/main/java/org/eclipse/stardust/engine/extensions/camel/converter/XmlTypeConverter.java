/*
 * $Id$
 * (C) 2000 - 2013 CARNOT AG
 */
package org.eclipse.stardust.engine.extensions.camel.converter;

import java.io.ByteArrayOutputStream;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.camel.Exchange;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.core.struct.sxml.Document;
import org.eclipse.stardust.engine.core.struct.sxml.Element;
import org.eclipse.stardust.engine.core.struct.sxml.Node;
import org.eclipse.stardust.engine.core.struct.sxml.converters.DOMConverter;
import org.eclipse.stardust.engine.extensions.transformation.format.IMessageFormat;
import org.eclipse.stardust.engine.extensions.transformation.format.RuntimeFormatManager;

public class XmlTypeConverter extends AbstractBpmTypeConverter
{
   public XmlTypeConverter(Exchange exchange)
   {
      super(exchange);
   }

   @Override
   public void unmarshal(DataMapping dataMapping, Map<String, Object> extendedAttributes)
   {
      Object dataMap = findDataValue(dataMapping, extendedAttributes);

      if (dataMap != null)
      {
         ByteArrayOutputStream stream = new ByteArrayOutputStream();

         String typeDeclarationId = this.getTypeDeclarationId(dataMapping);

         // TODO: is that the correct OID?
         long modelOid = new Long(dataMapping.getModelOID());
         SDTConverter converter = new SDTConverter(typeDeclarationId, modelOid);

         Node[] nodes = converter.toDom(dataMap, true);
         Document document = new Document((Element) nodes[0]);

         IMessageFormat messageFormat = RuntimeFormatManager.getMessageFormat("XML");

         try
         {
            org.w3c.dom.Document w3cDocument = DOMConverter.convert(document, getDOMImplementation());
            messageFormat.serialize(w3cDocument, stream, converter.getXsdSchema());
         }
         catch (Exception e)
         {
            new RuntimeException(e);
         }

         replaceDataValue(dataMapping, new String(stream.toByteArray()), extendedAttributes);
      }
   }

   @Override
   public void marshal(DataMapping dataMapping, Map<String, Object> extendedAttributes)
   {
      String xml = (String) findDataValue(dataMapping, extendedAttributes);

      if (xml != null)
      {
         String typeDeclarationId = this.getTypeDeclarationId(dataMapping);

         long modelOid = new Long(dataMapping.getModelOID());
         SDTConverter converter = new SDTConverter(typeDeclarationId, modelOid);

         Object dataMap = converter.toCollection(xml, true);
         replaceDataValue(dataMapping, dataMap, extendedAttributes);
      }
   }

   private org.w3c.dom.DOMImplementation getDOMImplementation() throws ParserConfigurationException
   {
      // TODO: Caching !!
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      return factory.newDocumentBuilder().getDOMImplementation();
   }

}
