package org.eclipse.stardust.engine.ws.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.stardust.engine.ws.processinterface.WsUtils;

public class WsdlListingHandler
{
   public static void handleWsdlListingResponse(HttpServletRequest request,
         HttpServletResponse response, String currentPartitionId,
         Set<String> destinationPaths, String listingPath) throws ServletException
   {
      TreeMap<String, UrlInfo> urls = new TreeMap<String, UrlInfo>();
      for (String address : destinationPaths)
      {
         int idxLastSpacer = address.lastIndexOf("/");
         int idx2ndLastSpacer = address.lastIndexOf("/", idxLastSpacer - 1);
         int idx3rdLastSpacer = address.lastIndexOf("/", idx2ndLastSpacer - 1);

         if ( -1 < idxLastSpacer && -1 < idx2ndLastSpacer && -1 < idx3rdLastSpacer)
         {
            String endpointName = address.substring(idxLastSpacer + 1, address.length());
            String modelId = WsUtils.extractModelId(WsUtils.decodeUrl(address.substring(
                  idx2ndLastSpacer + 1, idxLastSpacer)));
            String partitionId = WsUtils.decodeUrl(address.substring(
                  idx3rdLastSpacer + 1, idx2ndLastSpacer));
            String requestUrl = request.getRequestURL().toString();
            StringBuffer url = new StringBuffer(50);
            url.append(requestUrl.substring(0,
                  requestUrl.toLowerCase().lastIndexOf(listingPath)));
            url.append(address.substring(0, idx3rdLastSpacer));
            url.append("/");
            url.append(WsUtils.encodeUrl(partitionId));
            url.append("/");
            url.append(WsUtils.encodeUrl(modelId));
            url.append("/");
            url.append(endpointName);

            if (partitionId.equals(currentPartitionId))
            {
               urls.put(partitionId + modelId + endpointName, new UrlInfo(modelId, url.toString()));
            }
         }

      }
      try
      {
         response.setContentType("text/html");
         PrintWriter writer = response.getWriter();
         writer.println("<H2>Process Interface Endpoints WSDL Listing.</H2>");
         writer.println("<p>Enpoints of other partitions can be listed by adding the partition as query parameter. Example: (&lt;URL&gt;?partition=&lt;partitionId&gt;).</p>");
         writer.println("<p>Please find the WSDL URL of the corresponding model below.</p>");

         writer.println("<table border=\"1\" cellpadding=\"5\">");
         writer.println("<tr><th>Process Interface Endpoints for partition: "
               + currentPartitionId + "</th></tr>");
         for (UrlInfo urlEntry : urls.values())
         {
            String modelId = urlEntry.getModelId();
            String wsdlUrl = urlEntry.getUrl()+ "?wsdl";

            writer.println("<tr><td>ModelId: " + modelId + "<br/>");
            writer.println("Endpoint: " + urlEntry.getUrl() + "<br/>");
            writer.println("WSDL: <a href=\"" + wsdlUrl + "\">" + wsdlUrl
                  + "</a></td></tr>");
         }
         writer.println("</table>");
         writer.close();
      }
      catch (IOException e)
      {
         throw new ServletException("Write failed.", e);
      }

   }

   private static class UrlInfo
   {
      private final String modelId;

      private final String url;

      public UrlInfo(String modelId, String url)
      {
         this.modelId = modelId;
         this.url = url;
      }

      public String getModelId()
      {
         return modelId;
      }

      public String getUrl()
      {
         return url;
      }

   }

}
