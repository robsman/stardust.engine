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

import static org.eclipse.stardust.engine.rest.processinterface.TypeDeclarationRestletPathParameters.TYPE_DECL_ID;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.SchemaType;
import org.eclipse.stardust.engine.api.model.TypeDeclaration;
import org.eclipse.stardust.engine.api.model.XpdlType;
import org.eclipse.stardust.engine.ws.WebServiceEnv;
import org.w3c.dom.Document;

/**
 * <p>
 * This class represents a RESTful Web Service that exposes the XML Schema Definition for
 * a particular Type Declaration in an XPDL Model.
 * </p>
 *
 * @author Nicolas.Werlein
 * @version $Revision: $
 */
@Path("/typeDeclarations/{" + TYPE_DECL_ID + "}")
public class TypeDeclarationsRestlet extends EnvironmentAware
{
   @PathParam(TYPE_DECL_ID)
   private String typeDeclarationId;

   @GET
   @Produces(MediaType.APPLICATION_XML)
   public Document get()
   {
      try
      {
         if (StringUtils.isEmpty(getModelId()) || StringUtils.isEmpty(typeDeclarationId))
         {
            final String errorMsg = "No model ID and/or type declaration ID specified.";
            throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(
                  errorMsg).build());
         }

         Model model = getModel();
         final TypeDeclaration typeDeclaration = model.getTypeDeclaration(typeDeclarationId);
         if (typeDeclaration == null)
         {
            final String errorMsg = "Type Declaration not found.";
            throw new WebApplicationException(Response.status(Status.NOT_FOUND).entity(
                  errorMsg).build());
         }

         return getXSDDocument(typeDeclaration.getXpdlType());
      }
      finally
      {
         WebServiceEnv.removeCurrent();
      }
   }

   private Document getXSDDocument(final XpdlType xpdlType)
   {
      if ( !(xpdlType instanceof SchemaType))
      {
         final String errorMsg = "Type Declaration not found.";
         throw new WebApplicationException(Response.status(Status.NOT_FOUND).entity(
               errorMsg).build());
      }

      final SchemaType schemaType = (SchemaType) xpdlType;
      return schemaType.getSchema().getDocument();
   }
}

class TypeDeclarationRestletPathParameters
{
   static final String TYPE_DECL_ID = "typeDeclarationId";

   private TypeDeclarationRestletPathParameters()
   {
      /* constants class */
   }
}
