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

import static org.eclipse.stardust.common.StringUtils.isEmpty;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.transform.stream.StreamResult;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.utils.XmlUtils;
import org.eclipse.stardust.engine.ws.WebServiceEnv;
import org.eclipse.stardust.engine.ws.servlet.DynamicServletConfiguration;
import org.w3c.dom.Node;



public class WsUtils
{
   public static final String URI_PREFIX_GENERATED_MODELS = "http://eclipse.org/stardust/models/generated/";

   public static void ensureNeitherNullNorEmpty(final String string, final String name)
   {
      if (string == null)
      {
         throw new NullPointerException(name + " must not be null.");
      }
      if ("".equals(string))
      {
         throw new IllegalArgumentException(name + " must not be empty.");
      }
   }

   public static String createQualifiedProcessId(final String modelId,
         final String processId)
   {
      final StringBuffer sb = new StringBuffer();
      sb.append("{");
      sb.append(modelId);
      sb.append("}");
      sb.append(processId);
      return sb.toString();
   }

   public static String dom2String(final Node node, final int indent)
   {
      final ByteArrayOutputStream out = new ByteArrayOutputStream();
      final StreamResult result = new StreamResult(out);
      XmlUtils.serialize(node, result, indent);
      return out.toString();
   }

   private WsUtils()
   {
      /* utility class */
   }

   public static String getNamespaceSafeModelID(String modelId)
   {
      String namespaceSafeModelId = modelId;
      if ( !modelId.startsWith("http://") && !modelId.startsWith("https://") && !modelId.startsWith("file://"))
      {
         namespaceSafeModelId = URI_PREFIX_GENERATED_MODELS + modelId;
      }
      return namespaceSafeModelId;
   }

   public static String extractModelId(String namespaceSafeModelId)
   {
      String modelId = namespaceSafeModelId;
      if (namespaceSafeModelId.startsWith(URI_PREFIX_GENERATED_MODELS))
      {
         modelId = modelId.substring(URI_PREFIX_GENERATED_MODELS.length());
      }
      return modelId;
   }

   public static void ensureModelIdExists(String modelId)
   {
      if (StringUtils.isEmpty(modelId))
      {
         throw new PublicException("ModelId was not found in request and no default value is defined.");
      }
   }

   public static String encodeUrl(String string)
   {
      try
      {
         return URLEncoder.encode(string, "UTF-8");
      }
      catch (UnsupportedEncodingException e)
      {
         throw new PublicException(e);
      }
   }
   
   public static String decodeUrl(String string)
   {
      try
      {
         return URLDecoder.decode(string, "UTF-8");
      }
      catch (UnsupportedEncodingException e)
      {
         throw new PublicException(e);
      }
   }

   public static String encodeInternalEndpointPath(String contextPath ,String partitionId, String modelId, String endpointName)
   {
      String encodedModelId = WsUtils.encodeUrl(modelId); //WsUtils.getNamespaceSafeModelID(
      String encodedPartitionId = WsUtils.encodeUrl(partitionId);
     
      return contextPath + "/" + encodedPartitionId
            + "/" + encodedModelId + "/" + endpointName;
   }

   public static String getDefaultModelId(String partitionId)
   {
//      Parameters parameters = Parameters.instance();
//      return parameters.getString("WebService.ProcessService.DefaultModelId", null);
      
      return DynamicServletConfiguration.getCurrentInstance().getDefaultModelId(partitionId);
   }
   
   public static long getEndpointSyncPeriod()
   {
      Parameters parameters = Parameters.instance();
      long syncPeriod = parameters.getLong(
            "WebService.ProcessService.EndpointSyncPeriod", 10);
      return syncPeriod;
   }

   public static List<String> getEnabledPartitions()
   {
      List<String> partitions = null;

      Parameters parameters = Parameters.instance();
      String partitionsString = parameters.getString(
            "WebService.ProcessService.EnabledPartitions", null);
      if (partitionsString != null)
      {
         Iterator<String> split = StringUtils.split(partitionsString, ',');

         partitions = new LinkedList<String>();
         while (split.hasNext())
         {
            partitions.add(split.next().trim());
         }
      }
      return partitions;
   }
   
}
