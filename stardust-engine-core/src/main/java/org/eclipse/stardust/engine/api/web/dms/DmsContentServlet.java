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
package org.eclipse.stardust.engine.api.web.dms;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.stardust.common.Base64;
import org.eclipse.stardust.common.Function;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.ExtensionProviderUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.config.PropertyLayer;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingService;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingServiceFactory;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.SynchronizationService;
import org.eclipse.stardust.engine.core.runtime.beans.UserBean;
import org.eclipse.stardust.engine.core.runtime.beans.UserSessionBean;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryService;
import org.eclipse.stardust.engine.core.spi.dms.RepositoryManager;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;
import org.eclipse.stardust.vfs.RepositoryOperationFailedException;
import org.eclipse.stardust.vfs.impl.jcr.web.AbstractVfsContentServlet;


/**
 * @author sauer
 * @version $Revision$
 */
public class DmsContentServlet extends AbstractVfsContentServlet
{
   static final long serialVersionUID = 1L;

   public static final String OP_DOWNLOAD = "dl";

   public static final String OP_UPLOAD = "ul";

   public static final String CLIENT_CONTEXT_PARAM = "clientContext";

   public static String encodeDmsServletToken(String resourceId, String opcode,
         long userOid, long timestamp)
   {
      // TODO encode sessionId/fileId

      // as concrete session is currently not available, encode userOid/time/fileId
      // and later verify that at least one of the sessions started by the user
      // before time is still alive

      StringBuffer buffer = new StringBuffer(100);
      buffer.append(opcode);
      buffer.append("/").append(userOid);
      buffer.append("/").append(timestamp);
      buffer.append("/").append(resourceId);

      return new String(Base64.encode(buffer.toString().getBytes()));
   }

   private String context;

   protected int doDownloadFileContent(final String fileUri,
         final ContentDownloadController downloadManager) throws IOException
   {
      Integer status = (Integer) getForkingService().isolate(new Function<Integer>()
      {
         protected Integer invoke()
         {
            int result;

            final DecodedRequest request = decodeRequest(fileUri);
            if ((null != request) && OP_DOWNLOAD.equals(request.opcode))
            {
               if (isAuthorized(request))
               {
                  IUser user = findUser(request);

                  if (user != null)
                  {
                     pushUserPropertyLayer(user);
                  }
                  try
                  {

                     // as token still seems to be valid, verify file exists and if yes,
                     // provide its content
                     try
                     {
                        RepositoryManager provider = RepositoryManager.getInstance();
                        IRepositoryService service = provider.getImplicitService();
                        Document document = service.getDocument(request.resourceId);
                        if (null != document)
                        {
                           downloadManager.setContentLength((int) document.getSize());
                           downloadManager.setContentType(document.getContentType());

                           if ( !StringUtils.isEmpty(document.getEncoding()))
                           {
                              downloadManager.setContentEncoding(document.getEncoding());
                           }
                           downloadManager.setFilename(document.getName());

                           service.retrieveDocumentContentStream(request.resourceId,
                                 downloadManager.getContentOutputStream());

                           result = HttpServletResponse.SC_OK;
                        }
                        else
                        {
                           // file not found
                           result = HttpServletResponse.SC_NOT_FOUND;
                        }
                     }
                     catch (RepositoryOperationFailedException rofe)
                     {
                        throw new PublicException(
                              BpmRuntimeError.DMS_FAILED_RETRIEVING_CONTENT_FOR_DOCUMENT
                                    .raise(request.resourceId), rofe);
                     }
                     catch (IOException ioe)
                     {
                        throw new PublicException(
                              BpmRuntimeError.DMS_FAILED_RETRIEVING_CONTENT_FOR_DOCUMENT
                                    .raise(request.resourceId), ioe);
                     }

                  }
                  finally
                  {
                     if (user != null)
                     {
                        ParametersFacade.popLayer();
                     }
                  }
               }
               else
               {
                  // no qualifying session is active
                  result = HttpServletResponse.SC_FORBIDDEN;
               }
            }
            else
            {
               // request can not be decoded
               result = HttpServletResponse.SC_BAD_REQUEST;
            }

            return result;
         }



      });

      // report outcome
      return status.intValue();
   }
         protected int doUploadFileContent(final String fileUri,
         final InputStream contentStream, final int contentLength,
         final String contentType, final String contentEncoding) throws IOException
   {
      Integer status = (Integer) getForkingService().isolate(new Function<Integer>()
      {
         protected Integer invoke()
         {
            int result;

            final DecodedRequest request = decodeRequest(fileUri);
            if ((null != request) && OP_UPLOAD.equals(request.opcode))
            {
               if (isAuthorized(request))
               {

                  IUser user = findUser(request);

                  if (user != null)
                  {
                     pushUserPropertyLayer(user);
                  }
                  try
                  {

                     try
                     {
                        RepositoryManager provider = RepositoryManager.getInstance();
                        IRepositoryService service = provider.getImplicitService();
                        Document document = service.getDocument(request.resourceId);
                        if (null != document)
                        {
                           service.uploadDocumentContentStream(request.resourceId, contentStream, contentType, contentEncoding);

                           result = HttpServletResponse.SC_OK;
                        }
                        else
                        {
                           // file not found
                           result = HttpServletResponse.SC_NOT_FOUND;
                        }
                     }
                     catch (RepositoryOperationFailedException rofe)
                     {
                        throw new PublicException(
                              BpmRuntimeError.DMS_FAILED_UPDATING_CONTENT_FOR_DOCUMENT
                                    .raise(request.resourceId), rofe);
                     }
                  }
                  finally
                  {
                     if (user != null)
                     {
                        ParametersFacade.popLayer();
                     }
                  }
               }
               else
               {
                  // no qualifying session is active
                  result = HttpServletResponse.SC_FORBIDDEN;
               }
            }
            else
            {
               // request can not be decoded
               result = HttpServletResponse.SC_BAD_REQUEST;
            }

            return result;
         }
      });

      // report outcome
      return status.intValue();
   }

   private void pushUserPropertyLayer(IUser user)
   {
      PropertyLayer pushLayer = ParametersFacade.pushLayer(Collections.singletonMap(
            SecurityProperties.CURRENT_USER, user));
      pushLayer.setProperty(SecurityProperties.CURRENT_PARTITION_OID, user.getRealm()
            .getPartition()
            .getOID());
      pushLayer.setProperty(SecurityProperties.CURRENT_DOMAIN_OID, user.getDomainOid());

      pushLayer.setProperty(SecurityProperties.CURRENT_PARTITION, user.getRealm().getPartition());
      pushLayer.setProperty(SecurityProperties.CURRENT_PARTITION_OID, user.getRealm().getPartition().getOID());

      pushLayer.setProperty(SynchronizationService.PRP_DISABLE_SYNCHRONIZATION, true);
      pushLayer.setProperty(SecurityProperties.AUTHORIZATION_SYNC_LOAD_PROPERTY, false);
   }

   private ForkingService getForkingService()
   {
      ForkingServiceFactory factory = null;
      ForkingService forkingService = null;
      factory = (ForkingServiceFactory) Parameters.instance().get(
            EngineProperties.FORKING_SERVICE_HOME);
      if (factory == null)
      {
         List<ExecutionServiceProvider> exProviderList = ExtensionProviderUtils.getExtensionProviders(ExecutionServiceProvider.class);
         for (ExecutionServiceProvider executionServiceProvider : exProviderList)
         {
            try
            {
               forkingService = executionServiceProvider.getExecutionService(context);
            }
            catch (Exception e)
            {
               continue;
            }
            if(forkingService != null)
            {
               break;
            }
         }
      }
      else
      {
         forkingService = factory.get();
      }
      return forkingService;
   }

   public void init(ServletConfig config) throws ServletException
   {
      super.init(config);

      context = config.getInitParameter(CLIENT_CONTEXT_PARAM);
      context = context != null ? context.toLowerCase() : null;
   }

   private static DecodedRequest decodeRequest(String uri)
   {
      DecodedRequest result = new DecodedRequest();

      String decodedToken;
      try
      {
         decodedToken = new String(Base64.decode(uri.getBytes()));

         int splitIdx = decodedToken.indexOf("/");
         if ( -1 != splitIdx)
         {
            result.opcode = decodedToken.substring(0, splitIdx);
            decodedToken = decodedToken.substring(splitIdx + 1);

            splitIdx = decodedToken.indexOf("/");
            if ( -1 != splitIdx)
            {
               result.userOid = Long.parseLong(decodedToken.substring(0, splitIdx));
               decodedToken = decodedToken.substring(splitIdx + 1);

               splitIdx = decodedToken.indexOf("/");
               if ( -1 != splitIdx)
               {
                  result.timestamp = Long.parseLong(decodedToken.substring(0, splitIdx));
                  result.resourceId = decodedToken.substring(splitIdx + 1);
               }
            }
         }
      }
      catch (InternalException ie)
      {
         // invald URI
         result = null;
      }

      return result;
   }

   private static boolean isAuthorized(DecodedRequest request)
   {
      QueryDescriptor query = QueryDescriptor.from(UserSessionBean.class).where(
            Predicates.andTerm(Predicates.isEqual(UserSessionBean.FR__USER,
                  request.userOid), Predicates.lessOrEqual(
                  UserSessionBean.FR__START_TIME, request.timestamp),
                  Predicates.greaterOrEqual(UserSessionBean.FR__EXPIRATION_TIME,
                        TimestampProviderUtils.getTimeStampValue())));

      long nSessions = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).getCount(
            query.getType(), query.getQueryExtension());

      return 0 < nSessions;
   }

   private static IUser findUser(DecodedRequest request)
   {
      QueryDescriptor query = QueryDescriptor.from(UserBean.class)//
            .where(Predicates.isEqual(UserBean.FR__OID, request.userOid));


      IUser user;
      try
      {
         PropertyLayer pushLayer = ParametersFacade.pushLayer(new HashMap());

         pushLayer.setProperty(SynchronizationService.PRP_DISABLE_SYNCHRONIZATION, true);
         pushLayer.setProperty(SecurityProperties.AUTHORIZATION_SYNC_LOAD_PROPERTY, false);

         user = (IUser) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).findFirst(
               query.getType(), query.getQueryExtension());
      }
      catch (ObjectNotFoundException e)
      {
         user = null;
      }
      finally
      {
         ParametersFacade.popLayer();
      }

      return user;
   }

   private static class DecodedRequest
   {
      public String opcode;

      public long userOid;

      public long timestamp;

      public String resourceId;
   }

   public static interface ExecutionServiceProvider
   {
      ForkingService getExecutionService(String clientContext);
   }
}