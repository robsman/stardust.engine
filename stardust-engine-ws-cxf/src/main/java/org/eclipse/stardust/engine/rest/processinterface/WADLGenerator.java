/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
/*
 * $Id: $
 * (C) 2000 - 2011 SunGard CSA LLC
 */
package org.eclipse.stardust.engine.rest.processinterface;

import static org.eclipse.stardust.engine.rest.processinterface.PathParameters.APPLICATION_WADL;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;
import javax.xml.XMLConstants;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.utils.xml.StaticNamespaceContext;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.core.pojo.data.Type;
import org.eclipse.stardust.engine.core.runtime.utils.XmlUtils;
import org.eclipse.stardust.engine.core.struct.StructuredDataConstants;
import org.eclipse.stardust.engine.ws.processinterface.DomUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



/**
 * <p>
 * This class is responsible for generating a WADL file as well as the needed XML
 * Schema Definitions for the types that are exposed as formal parameters. The generation
 * is based on templates.
 * </p>
 *
 * @author Nicolas.Werlein
 * @version $Revision: $
 */
public class WADLGenerator
{
   private static final String WADL_TEMPLATE_PATH = "/META-INF/wadl/templates/ProcessesWADL.template.xml";
   private static final String WEB_APP_TYPES_TEMPLATE_PATH = "/META-INF/wadl/templates/WebAppTypes.template.xsd";

   private static final String ABSOLUTE_PATH_TEMPLATE = "{AbsolutePath}";
   private static final String BASE_URI_TEMPLATE = "{BaseURL}";
   private static final String RESOURCE_PATH_TEMPLATE = "{ResourcePath}";
   private static final String QUERY_PARAMETERS_TEMPLATE = "{QueryParameters}";

   public static final String W3C_XML_SCHEMA =
      "http://www.w3.org/2001/XMLSchema";

   private static final StaticNamespaceContext XPATH_CONTEXT = new StaticNamespaceContext(Collections.singletonMap("xsd", W3C_XML_SCHEMA));

   private final UriInfo uriInfo;

   public WADLGenerator(final UriInfo uriInfo)
   {
      if (uriInfo == null)
      {
         throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
      }
      this.uriInfo = uriInfo;
   }

   public String generateWADL()
   {
      String baseUri = uriInfo.getBaseUri().toString();      
      final String requestPath = uriInfo.getPath();
      final Map<String,List<String>> queryParameters = uriInfo.getQueryParameters();
      final String path;
      if (requestPath.endsWith("/" + APPLICATION_WADL))
      {
         final int startOfPostfix = requestPath.indexOf("/" + APPLICATION_WADL);
         path = requestPath.substring(0, startOfPostfix);
      }
      else
      {
         path = requestPath;
      }
      
      if (baseUri != null && !baseUri.endsWith("/") && path != null && !path.startsWith("/"))
      {
         baseUri = baseUri + "/";
      }

      final StringBuilder sb = loadWADLTemplate();
      replaceWithActualValue(sb, BASE_URI_TEMPLATE, baseUri);
      replaceWithActualValue(sb, RESOURCE_PATH_TEMPLATE, path);
      replaceWithActualValue(sb, ABSOLUTE_PATH_TEMPLATE, baseUri + path);
      replaceWithActualValue(sb, QUERY_PARAMETERS_TEMPLATE, buildQueryParameters(queryParameters));
      return sb.toString();
   }

   private String buildQueryParameters(Map<String, List<String>> queryParameters)
   {
      String queryString = "";
      if (null != queryParameters && !queryParameters.isEmpty())
      {
         List<String> list = queryParameters.get("stardust-bpm-partition");

         if (list != null && !list.isEmpty())
         {
            String value = list.get(0);
            if (value != null && !PredefinedConstants.DEFAULT_PARTITION_ID.equals(value))
            {
               queryString = "?" + "stardust-bpm-partition=" + value;
            }
         }

         list = queryParameters.get("stardust-bpm-model");

         if (list != null && !list.isEmpty())
         {
            String value = list.get(0);

            if (queryString.startsWith("?"))
            {
               queryString += "&amp;" + "stardust-bpm-model=" + value;
            }
            else
            {
               queryString += "?" + "stardust-bpm-model=" + value;
            }
         }
      }

      return  queryString;
   }

   public Document generateWebAppTypesXSD(final Model model, final String processId)
   {
      final Document doc = createTemplateDocument(WEB_APP_TYPES_TEMPLATE_PATH);
      final ProcessDefinition pd = model.getProcessDefinition(processId);
      final ProcessInterface pi = pd.getDeclaredProcessInterface();
      if (pi == null)
      {
         final String errorMsg = "Process Definition '" + pd.getId() + "' does not expose an interface.";
         throw new WebApplicationException(Response.status(Status.NOT_FOUND).entity(errorMsg).build());
      }
      final List<FormalParameter> formalParameters = pi.getFormalParameters();
      assembleWebAppType(doc, formalParameters, model);
      return doc;
   }

   private StringBuilder loadWADLTemplate()
   {
      final InputStream in = WADLGenerator.class.getResourceAsStream(WADL_TEMPLATE_PATH);
      final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
      final StringBuilder sb = new StringBuilder();

      try
      {
         String line;
         while ((line = reader.readLine()) != null)
         {
            sb.append(line).append("\n");
         }
      }
      catch (final IOException e)
      {
         throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
      }

      return sb;
   }

   private Document createTemplateDocument(final String templatePath)
   {
      final InputStream in = WADLGenerator.class.getResourceAsStream(templatePath);

      return XmlUtils.parseStream(in);
   }

   private void assembleWebAppType(final Document doc, final List<FormalParameter> formalParameters, final Model model)
   {

      final List<Pair<String, FormalParameter>> prefixAwareParameters = assignNSPrefixes(formalParameters);
      assembleNSDecls(doc, prefixAwareParameters, model);
      assembleXSDImports(doc, formalParameters, model);

      final List<Pair<String, FormalParameter>> in = getFormalParameters(prefixAwareParameters, Direction.IN);
      final List<Pair<String, FormalParameter>> out = getFormalParameters(prefixAwareParameters, Direction.OUT);

      assembleWebAppType(doc, ProcessesRestlet.ARGS_ELEMENT_NAME, in);
      assembleWebAppType(doc, ProcessesRestlet.RESULTS_ELEMENT_NAME, out);
   }

   private List<Pair<String, FormalParameter>> assignNSPrefixes(final List<FormalParameter> formalParameters)
   {
      final List<Pair<String, FormalParameter>> result = CollectionUtils.newArrayList();
      int nsCount = 0;

      for (final FormalParameter f : formalParameters)
      {
         if (isStructType(f))
         {
            boolean found = false;
            for (final Pair<String, FormalParameter> p : result)
            {
               if (getStructTypeDeclarationId(f).equals(getStructTypeDeclarationId(p.getSecond())))
               {
                  result.add(new Pair<String, FormalParameter>(p.getFirst(), f));
                  found = true;
                  break;
               }
            }

            if ( !found)
            {
               result.add(new Pair<String, FormalParameter>("ns" + nsCount++, f));
            }
         }
         else
         {
            result.add(new Pair<String, FormalParameter>("xsd", f));
         }
      }

      return result;
   }

   private void assembleNSDecls(final Document doc, final List<Pair<String, FormalParameter>> formalParameters, final Model model)
   {
      final Map<String, String> nsDecls = CollectionUtils.newHashMap();
      for (final Pair<String, FormalParameter> p : formalParameters)
      {
         if (isStructType(p.getSecond()))
         {
            final String typeDeclId = getStructTypeDeclarationId(p.getSecond());
            final TypeDeclaration typeDecl = model.getTypeDeclaration(typeDeclId);
            final String targetNS = getTargetNamespace(typeDecl);
            nsDecls.put(p.getFirst(), targetNS);
         }
      }

      final Element root = doc.getDocumentElement();
      for (final Entry<String, String> e : nsDecls.entrySet())
      {
         root.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:"+e.getKey(), e.getValue());
      }
   }

   private void assembleXSDImports(final Document doc, final List<FormalParameter> formalParameters, final Model model)
   {
      final Map<String, String> xsdImports = CollectionUtils.newHashMap();
      for (final FormalParameter f : formalParameters)
      {
         if (isStructType(f))
         {
            determineXSDImport(xsdImports, model, f);
         }
      }

      final Element root = doc.getDocumentElement();
      for (final Entry<String, String> e : xsdImports.entrySet())
      {
         final Element importElement = doc.createElementNS(W3C_XML_SCHEMA, "import");
         importElement.setAttribute("namespace", e.getKey());
         importElement.setAttribute("schemaLocation", e.getValue());
         root.insertBefore(importElement, root.getFirstChild());
         root.insertBefore(doc.createTextNode("\n"), root.getFirstChild());
      }
   }

   private void determineXSDImport(final Map<String, String> xsdImports, final Model model, final FormalParameter f)
   {
      final String typeDeclId = getStructTypeDeclarationId(f);
      final TypeDeclaration typeDecl = model.getTypeDeclaration(typeDeclId);
      final XpdlType xpdlType = typeDecl.getXpdlType();

      final String targetNS;
      final String schemaLocation;
      if (xpdlType instanceof SchemaType)
      {
         targetNS = ((SchemaType) xpdlType).getSchema().getTargetNamespace();
         schemaLocation = buildSchemaLocationPath(model.getId(), typeDeclId, model.getPartitionId());
      }
      else if (xpdlType instanceof ExternalReference)
      {
         final ExternalReference externalRef = (ExternalReference) xpdlType;
         targetNS = externalRef.getNamespace();
         schemaLocation = externalRef.getLocation();
      }
      else
      {
         throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
      }
      xsdImports.put(targetNS, schemaLocation);
   }

   private String getTargetNamespace(final TypeDeclaration typeDecl)
   {
      final XpdlType xpdlType = typeDecl.getXpdlType();
      if (xpdlType instanceof SchemaType)
      {
         final SchemaType schemaType = (SchemaType) xpdlType;
         return schemaType.getSchema().getTargetNamespace();

      }
      else if (xpdlType instanceof ExternalReference)
      {
         final ExternalReference extRef = (ExternalReference) xpdlType;
         return extRef.getNamespace();
      }
      else
      {
         throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
      }
   }

   private String buildSchemaLocationPath(final String modelId, final String typeDeclId,
         String partitionId)
   {
      final StringBuilder sb = new StringBuilder();
      sb.append(uriInfo.getBaseUri().toString());
      String baseUri = uriInfo.getBaseUri().toString();
      
      if (baseUri != null && !baseUri.endsWith("/") && uriInfo.getPath() != null
            && !uriInfo.getPath().startsWith("/"))
      {
         sb.append("/");
      }

      sb.append("typeDeclarations/");
      sb.append(typeDeclId);
      if ( !PredefinedConstants.DEFAULT_PARTITION_ID.equals(partitionId))
      {
         sb.append("?stardust-bpm-partition=");
         sb.append(partitionId);
         sb.append("&stardust-bpm-model=");
         sb.append(modelId);
      }
      else
      {
         sb.append("?stardust-bpm-model=");
         sb.append(modelId);
      }

      return sb.toString();
   }

   private List<Pair<String, FormalParameter>> getFormalParameters(final List<Pair<String, FormalParameter>> formalParameters, final Direction direction)
   {
      final List<Pair<String, FormalParameter>> result = CollectionUtils.newArrayList();
      for (final Pair<String, FormalParameter> p : formalParameters)
      {
         if (p.getSecond().getDirection().isCompatibleWith(direction))
         {
            result.add(p);
         }
      }
      return result;
   }

   private void assembleWebAppType(final Document doc, final String elementName, final List<Pair<String, FormalParameter>> formalParameters)
   {
      final NodeList nodes = DomUtils.retrieveElementsByXPath(doc, "/xsd:schema/xsd:element[@name='" + elementName + "']/xsd:complexType/xsd:sequence", XPATH_CONTEXT);
      if (nodes.getLength() < 0 || nodes.getLength() > 1)
      {
         throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
      }
      final Node node = nodes.item(0);
      if ( !(node instanceof Element))
      {
         throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
      }
      final Element typeElement = (Element) node;
      for (final Pair<String, FormalParameter> p : formalParameters)
      {
         final StringBuilder type = new StringBuilder();
         type.append(p.getFirst()).append(":");
         
         final Element formalParameterElement = doc.createElementNS(W3C_XML_SCHEMA, "xsd:element");
         formalParameterElement.setAttribute("name", p.getSecond().getId());

         typeElement.appendChild(formalParameterElement);
         typeElement.appendChild(doc.createTextNode("\n\t"));
         

         if (isStructType(p.getSecond()))
         {
            type.append(getStructTypeDeclarationId(p.getSecond()));

            final Element nestedComplexTypeElement = doc.createElementNS(W3C_XML_SCHEMA, "xsd:complexType");
            formalParameterElement.appendChild(nestedComplexTypeElement);
            
            final Element nestedSequenceElement = doc.createElementNS(W3C_XML_SCHEMA, "xsd:sequence");
            nestedComplexTypeElement.appendChild(nestedSequenceElement);
            
            final Element nestedStructElement = doc.createElementNS(W3C_XML_SCHEMA, "xsd:element");
            nestedSequenceElement.appendChild(nestedStructElement);         
             

            nestedStructElement.setAttribute("name", getStructTypeDeclarationId(p.getSecond()));
            nestedStructElement.setAttribute("type", type.toString());
         }
         else
         {
            type.append(getPrimitiveType(p.getSecond()).getId());
            
            formalParameterElement.setAttribute("type", type.toString());
         }
         
      }
   }

   private boolean isStructType(final FormalParameter formalParameter)
   {
      return getStructTypeDeclarationId(formalParameter) != null;
   }

   private String getStructTypeDeclarationId(final FormalParameter formalParameter)
   {
      return (String) formalParameter.getAttribute(StructuredDataConstants.TYPE_DECLARATION_ATT);
   }

   private Type getPrimitiveType(final FormalParameter formalParameter)
   {
      return (Type) formalParameter.getAttribute(PredefinedConstants.TYPE_ATT);
   }

   private void replaceWithActualValue(final StringBuilder sb, final String template, final String actualValue)
   {
      final int index = sb.indexOf(template);
      if (index < 0)
      {
         throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
      }
      sb.replace(index, index + template.length(), actualValue);
   }
}
