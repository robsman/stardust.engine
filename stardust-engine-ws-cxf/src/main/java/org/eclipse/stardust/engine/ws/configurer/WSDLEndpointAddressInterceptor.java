/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.ws.configurer;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.common.util.UrlUtils;
import org.apache.cxf.frontend.WSDLGetInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.http.AbstractHTTPDestination;

/**
 * This class intercepts messages requesting wsdl files. A relative endpoint address will
 * be updated to an absolute address for all services in wsdl file if property
 * <code>autoRewriteSoapAddressForAllServices</code> is set in spring context file. But
 * only the http base path will be considered. This interceptor class adds the servlet
 * path (if available) to the base path.
 * 
 * @author Antje.Fuhrmann
 * @version $Revision: $
 */
public class WSDLEndpointAddressInterceptor extends AbstractPhaseInterceptor<Message>
{
   private static final String HTTP_BASE_PATH = "http.base.path";

   public WSDLEndpointAddressInterceptor()
   {
      super(Phase.READ);
      addBefore(WSDLGetInterceptor.class.getName());
   }

   @Override
   public void handleMessage(Message message) throws Fault
   {
      String method = (String) message.get(Message.HTTP_REQUEST_METHOD);
      String query = (String) message.get(Message.QUERY_STRING);

      if (!"GET".equals(method) || StringUtils.isEmpty(query))
      {
         return;
      }

      Map<String, String> map = UrlUtils.parseQueryString(query);

      if (map.containsKey("wsdl"))
      {
         HttpServletRequest request = (HttpServletRequest) message
               .get(AbstractHTTPDestination.HTTP_REQUEST);
         String servletPath = request.getServletPath();
         if (org.eclipse.stardust.common.StringUtils.isNotEmpty(servletPath))
         {
            String basePath = (String) message.get(HTTP_BASE_PATH);
            message.put(HTTP_BASE_PATH, basePath + servletPath);
         }
      }
   }
}
