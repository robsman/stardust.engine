/*
 * $Id$
 * (C) 2000 - 2013 CARNOT AG
 */
package org.eclipse.stardust.engine.extensions.camel.converter;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.camel.Exchange;
import org.apache.camel.TypeConverter;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.dto.ModelDetails;
import org.eclipse.stardust.engine.api.model.AccessPoint;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.TypeDeclaration;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.DetailsFactory;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManager;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.struct.ClientXPathMap;
import org.eclipse.stardust.engine.core.struct.IXPathMap;
import org.eclipse.stardust.engine.core.struct.StructuredDataConverter;
import org.eclipse.stardust.engine.core.struct.StructuredTypeRtUtils;
import org.eclipse.stardust.engine.core.struct.TypedXPath;
import org.eclipse.stardust.engine.core.struct.emfxsd.XPathFinder;
import org.eclipse.stardust.engine.core.struct.sxml.Node;
import org.eclipse.stardust.engine.extensions.camel.CamelConstants;
import org.eclipse.stardust.engine.extensions.camel.util.client.ClientEnvironment;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import org.eclipse.xsd.XSDNamedComponent;
import org.eclipse.xsd.XSDSchema;

public abstract class AbstractBpmTypeConverter implements IBpmTypeConverter
{
   protected Exchange exchange;

   protected AbstractBpmTypeConverter(Exchange exchange)
   {
      this.exchange = exchange;
   }

   public Object findDataValue(DataMapping mapping, Map<String, Object> extendedAttributes)
   {

      Object dataValue = null;

      if (inBody(mapping, extendedAttributes))
      {
         dataValue = this.exchange.getIn().getBody();
      }
      else
      {
         dataValue = this.exchange.getIn().getHeader(mapping.getApplicationAccessPoint().getId());
      }

      if (dataValue instanceof InputStream)
      {
         TypeConverter converter = this.exchange.getContext().getTypeConverter();
         dataValue = converter.convertTo(String.class, dataValue);
      }

      return dataValue;

   }

   public void replaceDataValue(DataMapping mapping, Object dataValue, Map<String, Object> extendedAttributes)
   {
      if (inBody(mapping, extendedAttributes))
      {
         exchange.getOut().setBody(dataValue);
      }
      else
      {
         exchange.getOut().setHeader(mapping.getApplicationAccessPoint().getId(), dataValue);
      }
   }

   protected void processJsonArray(JsonArray jsonArray, List<Object> complexTypes, String path, IXPathMap xPathMap)
   {

      for (Iterator<JsonElement> _iterator = jsonArray.iterator(); _iterator.hasNext();)
      {

         JsonElement element = _iterator.next();

         if (element.isJsonObject())
         {

            Map<String, Object> nestedType = new HashMap<String, Object>();
            processJsonObject(element.getAsJsonObject(), nestedType, path, xPathMap);
            complexTypes.add(nestedType);

         }
      }
   }

   protected void processJsonObject(JsonObject jsonObject, Map<String, Object> complexType, String path,
         IXPathMap xPathMap)
   {

      for (Iterator<Entry<String, JsonElement>> _iterator = jsonObject.entrySet().iterator(); _iterator.hasNext();)
      {

         Entry<String, JsonElement> entry = _iterator.next();

         if (entry.getValue().isJsonObject())
         {

            String xPath = "".equals(path) ? entry.getKey() : path + "/" + entry.getKey();

            Map<String, Object> nestedType = new HashMap<String, Object>();

            processJsonObject(entry.getValue().getAsJsonObject(), nestedType, xPath, xPathMap);

            complexType.put(entry.getKey(), nestedType);

         }
         else if (entry.getValue().isJsonArray())
         {

            String xPath = "".equals(path) ? entry.getKey() : path + "/" + entry.getKey();

            List<Object> complexTypes = new ArrayList<Object>();

            processJsonArray(entry.getValue().getAsJsonArray(), complexTypes, xPath, xPathMap);

            complexType.put(entry.getKey(), complexTypes);

         }
         else if (entry.getValue().isJsonPrimitive())
         {

            JsonPrimitive primitive = entry.getValue().getAsJsonPrimitive();

            String xPath = "".equals(path) ? entry.getKey() : path + "/" + entry.getKey();

            TypedXPath typedXPath = xPathMap.getXPath(xPath);

            if (typedXPath != null)
            {

               switch (typedXPath.getType())
               {

               case 0:
                  complexType.put(entry.getKey(), primitive.getAsBoolean());
                  break;
               case 2:
                  complexType.put(entry.getKey(), primitive.getAsByte());
                  break;
               case 3:
                  complexType.put(entry.getKey(), primitive.getAsShort());
                  break;
               case 4:
                  complexType.put(entry.getKey(), primitive.getAsInt());
                  break;
               case 5:
                  complexType.put(entry.getKey(), primitive.getAsLong());
                  break;
               case 6:
                  complexType.put(entry.getKey(), primitive.getAsFloat());
                  break;
               case 7:
                  complexType.put(entry.getKey(), primitive.getAsDouble());
                  break;
               case 8:
                  complexType.put(entry.getKey(), primitive.getAsString());
                  break;
               case 9:
                  complexType.put(entry.getKey(), primitive.getAsString());
                  break;
               default:
                  complexType.put(entry.getKey(), primitive.getAsString());
                  break;
               }
            }
         }
      }
   }

   private boolean inBody(DataMapping mapping, Map<String, Object> extendedAttributes)
   {
      String bodyAccessPoint = null;

      boolean multipleAccessPoints = false;

      if (extendedAttributes.get(CamelConstants.SUPPORT_MULTIPLE_ACCESS_POINTS) != null)
      {
         multipleAccessPoints = (Boolean) extendedAttributes.get(CamelConstants.SUPPORT_MULTIPLE_ACCESS_POINTS);
      }

      if (multipleAccessPoints)
      {

         if (Direction.IN.equals(mapping.getDirection()))
         {
            if (extendedAttributes.get(CamelConstants.CAT_BODY_IN_ACCESS_POINT) != null)
               bodyAccessPoint = (String) extendedAttributes.get(CamelConstants.CAT_BODY_IN_ACCESS_POINT);
         }
         else
         {
            if (extendedAttributes.get(CamelConstants.CAT_BODY_OUT_ACCESS_POINT) != null)
               bodyAccessPoint = (String) extendedAttributes.get(CamelConstants.CAT_BODY_OUT_ACCESS_POINT);
         }

         if (bodyAccessPoint != null && !"".equals(bodyAccessPoint))
         {
            return bodyAccessPoint.equals(mapping.getApplicationAccessPoint().getId());
         }
         else
         {
            return false;
         }

      }

      return true;
   }

   protected boolean isStuctured(DataMapping mapping)
   {
      String typeDeclarationId = this.getTypeDeclarationId(mapping);

      if (typeDeclarationId != null)
      {
         Model model = lookupModel(mapping.getModelOID());
         TypeDeclaration typeDeclaration = model.getTypeDeclaration(typeDeclarationId);

         if (typeDeclaration != null)
         {
            return true;
         }

      }

      return false;
   }

   protected boolean isPrimitive(DataMapping mapping)
   {
      AccessPoint ap = mapping.getApplicationAccessPoint();
      if (ap != null && ap.getAttribute("carnot:engine:type") != null)
      {
         return true;
      }

      return false;
   }

   protected String getTypeDeclarationId(DataMapping mapping)
   {
      AccessPoint ap = mapping.getApplicationAccessPoint();

      Object dataType = ap.getAttribute("carnot:engine:dataType");
      String namespace = mapping.getNamespace();

      if (dataType != null && namespace != null)
      {
         return "{" + namespace + "}" + dataType;
      }

      return null;
   }

   protected static Model lookupModel(long modelOid)
   {

      BpmRuntimeEnvironment bpmRt = PropertyLayerProviderInterceptor.getCurrent();

      if (bpmRt != null)
      {
         ModelManager modelManager = ModelManagerFactory.getCurrent();
         IModel model = modelManager.findModel(modelOid);
         return DetailsFactory.create(model, IModel.class, ModelDetails.class);
      }
      else
      {
         ServiceFactory sf = ClientEnvironment.getCurrentServiceFactory();
         return sf.getQueryService().getModel(modelOid);
      }
   }

   protected class SDTConverter
   {
      private StructuredDataConverter converter;
      private XSDSchema xsdSchema;
      private IXPathMap xPathMap;

      protected SDTConverter(String typeDeclarationId, long modelOid)
      {

         Model model = lookupModel(modelOid);

         TypeDeclaration typeDeclaration = model.getTypeDeclaration(typeDeclarationId);

         if (model != null && typeDeclaration != null)
         {
            this.xsdSchema = StructuredTypeRtUtils.getXSDSchema(model, typeDeclaration);
         }
         else
         {
            throw new RuntimeException("Model: " + modelOid + " Type: " + typeDeclarationId);
         }

         XSDNamedComponent component = StructuredTypeRtUtils.findElementOrTypeDeclaration(xsdSchema,
               typeDeclaration.getId(), false);

         Set xPathSet = XPathFinder.findAllXPaths(xsdSchema,
               component);
         this.xPathMap = new ClientXPathMap(xPathSet);

         this.converter = new StructuredDataConverter(xPathMap);
      }

      protected StructuredDataConverter getConverter()
      {
         return converter;
      }

      protected void setConverter(StructuredDataConverter converter)
      {
         this.converter = converter;
      }

      protected XSDSchema getXsdSchema()
      {
         return xsdSchema;
      }

      protected void setXsdSchema(XSDSchema xsdSchema)
      {
         this.xsdSchema = xsdSchema;
      }

      public IXPathMap getxPathMap()
      {
         return xPathMap;
      }

      public void setxPathMap(IXPathMap xPathMap)
      {
         this.xPathMap = xPathMap;
      }

      public Object toCollection(String xml, boolean namespaceAware) throws PublicException
      {
         return converter.toCollection(xml, "", namespaceAware);
      }

      public Node[] toDom(Object object, boolean namespaceAware)
      {
         return converter.toDom(object, "", namespaceAware, true);
      }
   }

}