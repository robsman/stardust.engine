/*******************************************************************************
 * Copyright (c) 2012, 2014 SunGard CSA LLC and others.
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
 * (C) 2000 - 2009 CARNOT AG
 */
package org.eclipse.stardust.engine.rest.interactions;

import static java.util.Arrays.asList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_XML_TYPE;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.engine.ws.DataFlowUtils.isPrimitiveType;
import static org.eclipse.stardust.engine.ws.DataFlowUtils.isStructuredType;
import static org.eclipse.stardust.engine.ws.DataFlowUtils.marshalPrimitiveValue;
import static org.eclipse.stardust.engine.ws.DataFlowUtils.marshalStructValue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.utils.io.CloseableUtil;
import org.eclipse.stardust.engine.api.model.AccessPoint;
import org.eclipse.stardust.engine.api.model.ApplicationContext;
import org.eclipse.stardust.engine.api.model.Data;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.ws.ParameterXto;
import org.eclipse.stardust.engine.core.interactions.Interaction;
import org.eclipse.stardust.engine.core.interactions.InteractionRegistry;
import org.eclipse.stardust.engine.core.runtime.utils.XmlUtils;



/**
 * @author Robert.Sauer
 * @version $Revision: $
 */
@Path("interactions/{interactionId}")
public class UiInteractionsRestlet extends AbstractUiInteractionsRestlet
{
   static final Logger trace = LogManager.getLogger(UiInteractionsRestlet.class);

   @Context
   protected ServletContext servletContext;

   @Override
   protected InteractionRegistry getInteractionRegistry()
   {
      InteractionRegistry registry = (InteractionRegistry) servletContext.getAttribute(InteractionRegistry.BEAN_ID);

      if (null == registry)
      {
         trace.warn("There is no interactions registry defined.");

         throw new WebApplicationException(Status.NOT_FOUND);
      }

      return registry;
   }

   @GET
   @Produces(MediaType.TEXT_HTML)
   public String getOverview(@Context UriInfo uriInfo)
   {
      return super.getOverview(uriInfo);
   }

   @Path("definition")
   @GET
   @Produces(MediaType.APPLICATION_XML)
   public InteractionDefinition getDefinition()
   {
      return super.getDefinition();
   }

   @Path("owner")
   @GET
   @Produces(MediaType.APPLICATION_XML)
   public InteractionOwner getOwner()
   {
      return super.getOwner();
   }

   @Path("embeddedMarkup")
   @GET
   @Produces(MediaType.TEXT_HTML)
   public Response getEmbeddedMarkup()
   {
      Interaction interaction = findInteraction();
      ApplicationContext context = interaction.getDefinition();
      String markup = (String)context.getAttribute("carnot:engine:ui:externalWebApp:markup");
      if (StringUtils.isNotEmpty(markup))
      {
         return Response.ok(markup, MediaType.TEXT_HTML_TYPE).build();
      }
      else
      {
         throw new WebApplicationException(Status.NO_CONTENT);
      }
   }

   @Path("ippMashupAuthProxy.html")
   @GET
   @Produces(MediaType.TEXT_HTML)
   public Response getAuthProxyPage()
   {
      final URL authProxyUri = getClass().getClassLoader().getResource("META-INF/xhtml/common/controller/mashup/ippMashupAuthProxy.html");
      if (null != authProxyUri)
      {
         return Response.ok(new StreamingOutput()
         {
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException
            {
               InputStream is = authProxyUri.openStream();
               try
               {
                  int nBytesRead = 0;
                  byte[] buffer = new byte[4096];
                  while (0 < (nBytesRead = is.read(buffer)))
                  {
                     os.write(buffer, 0, nBytesRead);
                  }

                  os.flush();
               }
               finally
               {
                  CloseableUtil.closeQuietly(is);
               }
            }
         }).build();
      }
      else
      {
         return Response.status(Status.NOT_FOUND).build();
      }
   }

   @Path("inData")
   @GET
   @Produces(MediaType.APPLICATION_XML)
   public InDataValues getInDataValuesAsXml()
   {
      return getInDataValues();
   }

   @Path("inData/{parameterId}")
   @GET
   @Produces( {
         MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
   public Response getInDataValueRepresentation(
         @PathParam("parameterId") String parameterId, @Context Request jaxRsRequest,
         @Context HttpServletRequest httpRequest)
   {
      Interaction interaction = findInteraction();

      AccessPoint inParam = findParameterDefinition(interaction, parameterId, Direction.IN);
      if (null == inParam)
      {
         if (InteractionDataFlowUtils.supportDataMappingIds())
         {
            return getInDataValueRepresentationUsingMapping(parameterId, jaxRsRequest, httpRequest);
         }
         else
         {
            throw new WebApplicationException(Status.NOT_FOUND);
         }
      }

      Response response = null;

      if (isPrimitiveType(interaction.getModel(), inParam))
      {
         ParameterXto result = marshalPrimitiveValue(interaction.getModel(), inParam,
               interaction.getInDataValue(parameterId));

         response = Response.ok(result.getPrimitive(), TEXT_PLAIN_TYPE).build();
      }
      else if (isStructuredType(interaction.getModel(), inParam))
      {
         List<Variant> providedMediaTypes = asList( //
               new Variant(APPLICATION_XML_TYPE, (String) null, null), //
               new Variant(APPLICATION_JSON_TYPE, (String) null, null) //
         );

         Variant selectedVariant = jaxRsRequest.selectVariant(providedMediaTypes);

         if (null != selectedVariant)
         {
            if (APPLICATION_XML_TYPE == selectedVariant.getMediaType())
            {
               // provide XML representation of structured data
               ParameterXto result = marshalStructValue(interaction.getModel(), inParam,
                     interaction.getCrossModelReference(), interaction.getInDataValue(parameterId),
                     interaction.getResolver());

               // Workaround, only first element used
               Element e = result.getXml().getAny().size() > 0
                     ? result.getXml().getAny().get(0)
                     : null;
               response = Response.ok(XmlUtils.toString(e),
                     APPLICATION_XML_TYPE).build();
            }
            else if (APPLICATION_JSON_TYPE == selectedVariant.getMediaType())
            {
               // provide JSON representation of structured data
               String jsonpCallback = httpRequest.getParameter("callback");

               String result = getInDataValueAsJson(parameterId, jsonpCallback);

               if ( !isEmpty(result))
               {
                  response = Response.ok(result, APPLICATION_JSON_TYPE).build();
               }
               else
               {
                  throw new WebApplicationException(Status.NO_CONTENT);
               }
            }
         }
      }

      if (null != response)
      {
         return response;
      }
      else
      {
         throw new WebApplicationException(Status.UNSUPPORTED_MEDIA_TYPE);
      }
   }

   public Response getInDataValueRepresentationUsingMapping(
         @PathParam("parameterId") String parameterId, @Context Request jaxRsRequest,
         @Context HttpServletRequest httpRequest)
   {
      Interaction interaction = findInteraction();

      DataMapping dm = findDataFlow(interaction, parameterId, Direction.IN);

      trace.warn("Retrieving IN data using data mapping ID \"" + parameterId
            + "\", this is only supported for a transition period.");

      Response response = null;

      String inParamId = dm.getApplicationAccessPoint().getId();
      Serializable inParamValue = interaction.getInDataValue(inParamId);

      if (isPrimitiveType(interaction.getModel(), dm))
      {
         ParameterXto result = marshalPrimitiveValue(interaction.getModel(),dm,
               inParamValue);

         response = Response.ok(result.getPrimitive(), TEXT_PLAIN_TYPE).build();
      }
      else if (isStructuredType(interaction.getModel(), dm))
      {
         Model model = interaction.getModel();
         Data data = model.getData(dm.getDataId());
         if (data.getModelOID() != model.getModelOID())
         {
            model = interaction.getServiceFactory()
                  .getQueryService()
                  .getModel(data.getModelOID(), false);
         }

         List<Variant> providedMediaTypes = asList( //
               new Variant(APPLICATION_XML_TYPE, (String) null, null), //
               new Variant(APPLICATION_JSON_TYPE, (String) null, null) //
         );

         Variant selectedVariant = jaxRsRequest.selectVariant(providedMediaTypes);

         if (null != selectedVariant)
         {
            if (APPLICATION_XML_TYPE == selectedVariant.getMediaType())
            {
               // provide XML representation of structured data
               ParameterXto result = marshalStructValue(model, dm, inParamValue, interaction.getResolver());
               // Workaround, only first element used
               Element e = result.getXml().getAny().size() > 0 ? result.getXml()
                     .getAny()
                     .get(0) : null;
               response = Response.ok(XmlUtils.toString(e), APPLICATION_XML_TYPE).build();
            }
            else if (APPLICATION_JSON_TYPE == selectedVariant.getMediaType())
            {
               // provide JSON representation of structured data
               String jsonpCallback = httpRequest.getParameter("callback");

               String result = getInDataValueAsJson(inParamId, jsonpCallback);

               if ( !isEmpty(result))
               {
                  response = Response.ok(result, APPLICATION_JSON_TYPE).build();
               }
               else
               {
                  throw new WebApplicationException(Status.NO_CONTENT);
               }
            }
         }
      }

      if (null != response)
      {
         return response;
      }
      else
      {
         throw new WebApplicationException(Status.UNSUPPORTED_MEDIA_TYPE);
      }
   }

   @Path("outData")
   @PUT
   @Consumes(MediaType.APPLICATION_XML)
   public void setOutDataValuesFromXml(OutDataValues outDataValues)
   {
      setOutDataValues(outDataValues);
   }

   @Path("outData/{parameterId}")
   @PUT
   @Consumes(MediaType.TEXT_PLAIN)
   public void setOutDataValueFromText(@PathParam("parameterId") String parameterId,
         String value)
   {
      try
      {
         setOutDataValue(parameterId, value);
      }
      catch(Exception e)
      {
         trace.error(e);
         if (e instanceof WebApplicationException)
         {
            throw (WebApplicationException)e;
         }
         else
         {
            throw new WebApplicationException(e, Status.BAD_REQUEST);
         }
      }
   }

   @Path("outData/{parameterId}")
   @PUT
   @Consumes(MediaType.APPLICATION_XML)
   public Response setOutDataValueFromXml(@PathParam("parameterId") String parameterId,
         InputStream xmlContent)
   {
      Document document = XmlUtils.parseSource(new InputSource(xmlContent), null);

      setOutDataValue(parameterId, document.getDocumentElement());

      return Response.ok("", MediaType.TEXT_PLAIN_TYPE).status(Status.ACCEPTED).build();
   }

}
