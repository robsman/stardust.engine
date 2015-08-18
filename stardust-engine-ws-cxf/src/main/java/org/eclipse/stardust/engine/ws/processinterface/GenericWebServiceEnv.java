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
package org.eclipse.stardust.engine.ws.processinterface;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * This class maintains information that is
 * local to a particular WS request.
 * </p>
 * 
 * <p>
 * This information is needed to distinguish between
 * different WS operations since all of them
 * are handled by the same Web Service Provider.
 * </p>
 * 
 * @author Nicolas.Werlein
 */
public class GenericWebServiceEnv
{
   private static GenericWebServiceEnv instance;
   
   private final ThreadLocal<String> processId;
   private final ThreadLocal<String> soapAction;
   private final ThreadLocal<String> modelId;
   private final ThreadLocal<String> partitionId;
   
   /**
    * Singleton ctor
    */
   private GenericWebServiceEnv()
   {
      processId = new ThreadLocal<String>();
      soapAction = new ThreadLocal<String>();
      modelId = new ThreadLocal<String>();
      partitionId = new ThreadLocal<String>();
   }

   public static synchronized GenericWebServiceEnv instance()
   {
      return (instance == null) ? (instance = new GenericWebServiceEnv()) : instance;
   }
   
   public void setEnv(final HttpServletRequest request)
   {
      /* from the JavaDoc of HttpServletRequest#getHeader(): The header name is case insensitive. */
//      this.request.set(request);
      
      final String soapActionStr = request.getHeader("SOAPAction");
      if (soapActionStr != null)
      {
         final String requestURI = request.getRequestURI();
         final String relativeUrl = extractLastUrlPart(requestURI);
         WsUtils.ensureNeitherNullNorEmpty(relativeUrl, "Relative Endpoint URL");
         
         String soapActionWithoutBackSlashes = soapActionStr.replaceAll("\"", "");
         if (soapActionWithoutBackSlashes.startsWith("startProcess"))
         {
            processId.set(soapActionWithoutBackSlashes.replace("startProcess", ""));
         }
         else if (soapActionWithoutBackSlashes.startsWith("getProcessResults"))
         {
            processId.set(soapActionWithoutBackSlashes.replace("getProcessResults", ""));
         }
         
         if (processId.get() != null)
         {
            soapAction.set(soapActionWithoutBackSlashes.replace(processId.get(), ""));
         }
      }
   }
   
   public void removeEnv()
   {
      processId.remove();
      soapAction.remove();
      modelId.remove();
      partitionId.remove();
   }

   public String processId()
   {
      return processId.get();
   }
   
   public String soapAction()
   {
      return soapAction.get();
   }
   
   private String extractLastUrlPart(final String url)
   {
      return url.substring(url.lastIndexOf("/") + 1);
   }
   
   public String getModelId()
   {
      return modelId.get();
   }
   
   public void setModelId(String modelId)
   {
      this.modelId.set(modelId);
   }
   
   public String getPartitionId()
   {
      return partitionId.get();
   }
   
   public void setPartitionId(String partitionId)
   {
      this.partitionId.set(partitionId);
   }
}
