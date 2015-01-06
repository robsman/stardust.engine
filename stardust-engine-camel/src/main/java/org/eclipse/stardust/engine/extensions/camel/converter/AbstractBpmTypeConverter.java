/*
 * $Id$
 * (C) 2000 - 2013 CARNOT AG
 */
package org.eclipse.stardust.engine.extensions.camel.converter;

import java.util.*;
import java.util.Map.Entry;

import org.apache.camel.Exchange;

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManager;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.command.impl.RetrieveModelDetailsCommand;
import org.eclipse.stardust.engine.core.struct.*;
import org.eclipse.stardust.engine.core.struct.emfxsd.XPathFinder;
import org.eclipse.stardust.engine.core.struct.sxml.Node;
import org.eclipse.stardust.engine.extensions.camel.util.client.ClientEnvironment;

import org.eclipse.xsd.XSDNamedComponent;
import org.eclipse.xsd.XSDSchema;

import com.google.gson.*;

public abstract class AbstractBpmTypeConverter 
{
   protected Exchange exchange;

   protected AbstractBpmTypeConverter(Exchange exchange)
   {
      this.exchange = exchange;
   }

   public static XSDSchema loadXsdSchema(Model model, TypeDeclaration typeDeclaration)
   {
      if (model != null && typeDeclaration != null)
      {
         return StructuredTypeRtUtils.getXSDSchema(model, typeDeclaration);
      }
      else
      {
         throw new RuntimeException("Model: " + model.getModelOID() + " Type: "
               + typeDeclaration.getId());
      }
   }

   protected static XSDSchema loadXsdSchema(IModel model, ITypeDeclaration typeDeclaration)
   {
      if (model != null && typeDeclaration != null)
      {
         return StructuredTypeRtUtils.getXSDSchema(model, typeDeclaration);
      }
      else
      {
         throw new RuntimeException("Model: " + model.getOID() + " Type: "
               + typeDeclaration.getId());
      }
   }

   protected void processJsonArray(JsonArray jsonArray, List<Object> complexTypes,
         String path, IXPathMap xPathMap)
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
         else if (element.isJsonPrimitive())
         {

            JsonPrimitive primitive = element.getAsJsonPrimitive();

            TypedXPath typedXPath = xPathMap.getXPath(path);

            if (typedXPath != null)
            {

               switch (typedXPath.getType())
               {
                  case 0:
                     complexTypes.add(primitive.getAsBoolean());
                     break;
                  case 2:
                     complexTypes.add(primitive.getAsByte());
                     break;
                  case 3:
                     complexTypes.add(primitive.getAsShort());
                     break;
                  case 4:
                     complexTypes.add(primitive.getAsInt());
                     break;
                  case 5:
                     complexTypes.add(primitive.getAsLong());
                     break;
                  case 6:
                     complexTypes.add(primitive.getAsFloat());
                     break;
                  case 7:
                     complexTypes.add(primitive.getAsDouble());
                     break;
                  case 8:
                     complexTypes.add(primitive.getAsString());
                     break;
                  case 9:
                     complexTypes.add(primitive.getAsString());
                     break;
                  default:
                     complexTypes.add(primitive.getAsString());
                     break;
               }
            }
         }
      }
   }

   protected void processJsonObject(JsonObject jsonObject,
         Map<String, Object> complexType, String path, IXPathMap xPathMap)
   {

      for (Iterator<Entry<String, JsonElement>> _iterator = jsonObject.entrySet()
            .iterator(); _iterator.hasNext();)
      {

         Entry<String, JsonElement> entry = _iterator.next();

         if (entry.getValue().isJsonObject())
         {

            String xPath = "".equals(path) ? entry.getKey() : path + "/" + entry.getKey();

            Map<String, Object> nestedType = new HashMap<String, Object>();

            processJsonObject(entry.getValue().getAsJsonObject(), nestedType, xPath,
                  xPathMap);

            complexType.put(entry.getKey(), nestedType);

         }
         else if (entry.getValue().isJsonArray())
         {

            String xPath = "".equals(path) ? entry.getKey() : path + "/" + entry.getKey();

            List<Object> complexTypes = new ArrayList<Object>();

            processJsonArray(entry.getValue().getAsJsonArray(), complexTypes, xPath,
                  xPathMap);

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

   protected Object getTypeDeclaration(IModel model, String dataId)
   {
      IData data = model.findData(dataId);
      return StructuredTypeRtUtils.getTypeDeclaration(data, model);
   }

   protected Object getTypeDeclaration(IModel model, DataMapping mapping)
   {
      return getTypeDeclaration(model, mapping.getDataId());
      // IData data = model.findData(mapping.getDataId());
      // return StructuredTypeRtUtils.getTypeDeclaration(data, model);
   }

   protected Object getTypeDeclaration(Model model, DataMapping mapping)
   {
      TypeDeclaration td = null;
      String dataId = mapping.getDataId();
      Data data = model.getData(dataId);

      Reference ref = data.getReference();

      if (ref != null)
      {
         Model refModel = (Model) lookupModel(ref.getModelOid());
         td = refModel.getTypeDeclaration(ref.getId());
      }

      if (td == null)
      {
         String typeDeclarationId = (String) mapping.getApplicationAccessPoint()
               .getAttribute(StructuredDataConstants.TYPE_DECLARATION_ATT);

         if (typeDeclarationId != null && data.getModelOID() == model.getModelOID())
         {
            td = model.getTypeDeclaration(typeDeclarationId);
         }
      }

      return td;
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

   public boolean isStuctured(DataMapping mapping)
   {
      String typeDeclarationId = this.getTypeDeclarationId(mapping);
      return typeDeclarationId != null;
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

   protected static Object lookupModel(long modelOid)
   {
      BpmRuntimeEnvironment bpmRt = PropertyLayerProviderInterceptor.getCurrent();

      if (bpmRt != null)
      {
         ModelManager modelManager = ModelManagerFactory.getCurrent();
         return modelManager.findModel(modelOid);
      }
      else
      {
         ServiceFactory sf = ClientEnvironment.getCurrentServiceFactory();
         return sf.getWorkflowService().execute(
               RetrieveModelDetailsCommand.retrieveModelByOid(modelOid));
      }
   }

   protected class SDTConverter
   {
      private StructuredDataConverter converter;

      private XSDSchema xsdSchema;

      private IXPathMap xPathMap;

      protected SDTConverter(DataMapping dataMapping, long modelOid)
      {

         Object obj = lookupModel(modelOid);

         XSDNamedComponent component = null;

         if (obj instanceof IModel)
         {
            IModel iModel = ((IModel) obj);
            ITypeDeclaration iTypeDeclaration = (ITypeDeclaration) getTypeDeclaration(
                  iModel, dataMapping);
            this.xsdSchema = loadXsdSchema(iModel, iTypeDeclaration);
            component = StructuredTypeRtUtils.findElementOrTypeDeclaration(xsdSchema,
                  iTypeDeclaration.getId(), false);
         }
         else
         {
            Model model = ((Model) obj);
            TypeDeclaration typeDeclaration = (TypeDeclaration) getTypeDeclaration(model,
                  dataMapping);
            this.xsdSchema = loadXsdSchema(model, typeDeclaration);
            component = StructuredTypeRtUtils.findElementOrTypeDeclaration(xsdSchema,
                  typeDeclaration.getId(), false);
         }

         Set xPathSet = XPathFinder.findAllXPaths(xsdSchema, component);

         this.xPathMap = new ClientXPathMap(xPathSet);

         this.converter = new StructuredDataConverter(xPathMap);
      }

      protected SDTConverter(IModel iModel, String dataId)
      {
         XSDNamedComponent component = null;
         ITypeDeclaration iTypeDeclaration = (ITypeDeclaration) getTypeDeclaration(
               iModel, dataId);
         this.xsdSchema = loadXsdSchema(iModel, iTypeDeclaration);
         component = StructuredTypeRtUtils.findElementOrTypeDeclaration(xsdSchema,
               iTypeDeclaration.getId(), false);

         Set xPathSet = XPathFinder.findAllXPaths(xsdSchema, component);

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

      public Object toCollection(String xml, boolean namespaceAware)
            throws PublicException
      {
         return converter.toCollection(xml, "", namespaceAware);
      }

      public Node[] toDom(Object object, boolean namespaceAware)
      {
         return converter.toDom(object, "", namespaceAware, true);
      }
   }

   protected Map<String,Object> parseJson(SDTConverter converter,String json, String accessPointId){
      JsonParser parser = new JsonParser();
      JsonElement element = parser.parse(json);

      Map<String, Object> complexType = new HashMap<String, Object>();

      if (element.isJsonObject()) // is root already
      {

         JsonObject jObj = element.getAsJsonObject();
         processJsonObject(jObj, complexType, "", converter.getxPathMap());

      }
      else if (element.isJsonArray())
      {

         List<Object> list = new ArrayList<Object>();
         processJsonArray(element.getAsJsonArray(), list, accessPointId, converter.getxPathMap());
         complexType.put(accessPointId, list);

      }
      
      return complexType;
   }

}
