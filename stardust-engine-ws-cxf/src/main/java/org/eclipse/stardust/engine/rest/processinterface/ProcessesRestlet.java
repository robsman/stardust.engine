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
 * (C) 2000 - 2011 SunGard CSA LLC
 */
package org.eclipse.stardust.engine.rest.processinterface;

import static javax.ws.rs.core.MediaType.APPLICATION_XML_TYPE;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static org.eclipse.stardust.engine.rest.processinterface.PathParameters.APPLICATION_WADL;
import static org.eclipse.stardust.engine.rest.processinterface.PathParameters.PROCESS_ID;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Map;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.ProcessInterfaceCommand;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.core.runtime.utils.XmlUtils;
import org.eclipse.stardust.engine.ws.WebServiceEnv;
import org.w3c.dom.Document;


/**
 * <p>
 * This class represents a RESTful Web Service with two operations:
 * <ul>
 * <li><i>Starting a process</i> and</li>
 * <li><i>Retrieving the result of a completed process instance</i>.</li>
 * </ul>
 * </p>
 *
 * @author Nicolas.Werlein
 * @version $Revision: $
 */
@Path("/processes/{" + PROCESS_ID + "}")
public class ProcessesRestlet extends EnvironmentAware
{
   static final String ARGS_ELEMENT_NAME = "Args";

   static final String RESULTS_ELEMENT_NAME = "Results";

   static final String TYPES_NS = "http://eclipse.org/stardust/rest/v2012a/types";

   @PathParam(PROCESS_ID)
   private String processId;

   @POST
   @Consumes(MediaType.APPLICATION_XML)
   @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN})
   public Response startProcess(
         /*
          * cannot use Document directly as DocumentProvider is unable to cope with empty
          * payloads (JERSEY-154)
          */
         final InputStream in,
         @QueryParam("synchronously") @DefaultValue(value = "false") final boolean synchronously,
         @Context final UriInfo uriInfo)
   {
      try
      {
         checkModelId();
         ProcessDefinition pd = checkProcessId();

         String modelId = getModelId();

         final String qualifiedProcessId = createQualifiedProcessId(modelId, processId);
         final FormalParameterTransformer transformer = new FormalParameterTransformer(environment(), pd);
         final Map<String, Serializable> parameters = transformer.unmarshalParameters(in);

         ProcessInterfaceCommand command = new ProcessInterfaceCommand(qualifiedProcessId, parameters, synchronously);

         ProcessInterfaceCommand.Result result = null;
         try
         {
   	        result = (ProcessInterfaceCommand.Result) environment().getServiceFactory().getWorkflowService().execute(command);
         }
         catch(ObjectNotFoundException e)
         {
            String errorMsg = e.getMessage();
       	    throw new WebApplicationException(Response.status(Status.NOT_FOUND).entity(errorMsg).build());
         }

         if (ProcessInstanceState.Completed.equals(result.getProcessInstance().getState()) && result.getProcessResults() != null)
         {
            Map<String, Serializable> returnValues = result.getProcessResults();
            String response = XmlUtils.toString(transformer.marshalDocument(result.getProcessInstance(), returnValues));
            return Response.ok(response, APPLICATION_XML_TYPE).build();
         }
         else
         {
            StringBuilder linkString = new StringBuilder(uriInfo.getAbsolutePath().toString())
               .append("?piOID=")
               .append(result.getProcessInstance().getOID());
            String partitionId = getPartitionId();
            if (!PredefinedConstants.DEFAULT_PARTITION_ID.equals(partitionId))
            {
               linkString.append("&stardust-bpm-partition=")
                         .append(partitionId);
            }
            linkString.append("&stardust-bpm-model=")
                      .append(modelId);
            return Response.ok(linkString.toString(), TEXT_PLAIN_TYPE).build();
         }
      }
      finally
      {
         WebServiceEnv.removeCurrent();
      }
   }

   @GET
   @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN})
   public Document getProcessResults(
         @QueryParam("piOID") @DefaultValue("-1") final long piOID)
   {
      try
      {
         checkModelId();
         ProcessDefinition pd = checkProcessId();

         if (piOID < 0)
         {
            String errorMsg = "No Process Instance OID specified.";
            throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(errorMsg).build());
         }

         Map<String, Serializable> returnValues = getProcessResultsInternal(piOID);
         ProcessInstance processInstance = environment().getServiceFactory().getWorkflowService().getProcessInstance(piOID);
         FormalParameterTransformer transformer = new FormalParameterTransformer(environment(), pd);
         return transformer.marshalDocument(processInstance, returnValues);
      }
      finally
      {
         WebServiceEnv.removeCurrent();
      }
   }

   @OPTIONS
   @Produces(MediaType.APPLICATION_XML)
   public String getWADL(@Context final UriInfo uriInfo)
   {
      try
      {
         checkModelId();
         checkProcessId();
         return new WADLGenerator(uriInfo).generateWADL();
      }
      finally
      {
         WebServiceEnv.removeCurrent();
      }
   }

   @GET
   @Path(APPLICATION_WADL)
   @Produces(MediaType.APPLICATION_XML)
   public String getWADLExplicitly(@Context final UriInfo uriInfo)
   {
      try
      {
         return getWADL(uriInfo);
      }
      finally
      {
         WebServiceEnv.removeCurrent();
      }
   }

   @GET
   @Path("WebAppTypes.xsd")
   @Produces(MediaType.APPLICATION_XML)
   public Document getWebAppTypesXsd(@Context final UriInfo uriInfo)
   {
      try
      {
         return new WADLGenerator(uriInfo).generateWebAppTypesXSD(getModel(), processId);
      }
      finally
      {
         WebServiceEnv.removeCurrent();
      }
   }

   private void checkModelId()
   {
      if (StringUtils.isEmpty(getModelId()))
      {
         final String errorMsg = "No model ID specified. Please append the query parameter 'stardust-bpm-model' to specify a modelId";
         throw new WebApplicationException(Response.status(Status.BAD_REQUEST)
               .entity(errorMsg)
               .build());
      }
   }

   private ProcessDefinition checkProcessId()
   {
      if (StringUtils.isEmpty(processId))
      {
         final String errorMsg = "No process ID specified.";
         throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(errorMsg).build());
      }
      else
      {
         ProcessDefinition processDefinition = getModel().getProcessDefinition(processId);
         if (processDefinition == null)
         {
            final String errorMsg = "No such process ID found in model.";
            throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(errorMsg).build());
         }
         return processDefinition;
      }
   }

   private Map<String, Serializable> getProcessResultsInternal(final long piOID)
   {
      final Map<String, Serializable> result;
      try
      {
         result = environment().getServiceFactory().getWorkflowService().getProcessResults(piOID);
      }
      catch (final ObjectNotFoundException e)
      {
         final String errorMsg = "Process Instance with OID '" + piOID + "' not found.";
         throw new WebApplicationException(Response.status(Status.NOT_FOUND)
               .entity(errorMsg)
               .build());
      }
      catch (final AccessForbiddenException e)
      {
         final String errorMsg = "Process Instance with OID '" + piOID
               + "' hasn't been completed yet.";
         throw new WebApplicationException(Response.status(Status.SERVICE_UNAVAILABLE)
               .entity(errorMsg)
               .build());
      }
      return result;
   }

   private String createQualifiedProcessId(final String modelId, final String processId)
   {
      final StringBuilder sb = new StringBuilder();
      sb.append("{");
      sb.append(modelId);
      sb.append("}");
      sb.append(processId);
      return sb.toString();
   }
}

class PathParameters
{
   static final String PROCESS_ID = "processId";

   static final String APPLICATION_WADL = "application.wadl";

   private PathParameters()
   {
      /* constants class */
   }
}