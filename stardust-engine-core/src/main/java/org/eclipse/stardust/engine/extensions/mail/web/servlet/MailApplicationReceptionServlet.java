/*******************************************************************************
 * Copyright (c) 2011, 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.extensions.mail.web.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.extensions.mail.MailConstants;
import org.eclipse.stardust.engine.extensions.mail.utils.MailValidationUtils;
import org.eclipse.stardust.engine.extensions.mail.web.servlet.utils.HtmlUtils;


public class MailApplicationReceptionServlet extends HttpServlet
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   public void doGet(HttpServletRequest request, HttpServletResponse response)
         throws IOException, ServletException
   {
      ServiceFactory serviceFactory;

      // Parse request

      String processInstanceOIDString = request.getParameter(MailConstants.PROCESS_INSTANCE_OID);
      String activityInstanceOIDString = request.getParameter(MailConstants.ACTIVITY_INSTANCE_OID);
      String outputValue = request.getParameter(MailConstants.OUTPUT_VALUE);
      String investigateString = request.getParameter(MailConstants.INVESTIGATE);
      String requestHashCodeString = request.getParameter(MailConstants.HASH_CODE);
      String partition = request.getParameter(MailConstants.PARTITION);
      
      long processInstanceOID = Long.parseLong(processInstanceOIDString);
      long activityInstanceOID = Long.parseLong(activityInstanceOIDString);
      boolean investigate = Boolean.parseBoolean(investigateString);
      
      // compare hashCode retrieved from request with own computed hashCode and handle error if not equal
      int computedHashCode = MailValidationUtils.getQueryParametersHashCode(processInstanceOID,
            activityInstanceOID, partition, investigate, outputValue);
      
      if (!Integer.toString(computedHashCode).equals(requestHashCodeString))
      {
         error(request, response, new Exception("provided hashCode not valid: "
               + requestHashCodeString), null, null, null);

         return;
      }         
      
      // Complete activity

      try
      {
         if (StringUtils.isEmpty(partition))
         {
            partition = Parameters.instance().getString(
                  SecurityProperties.DEFAULT_PARTITION, "default");
         }
         
    	  Map<String, Object> properties = new HashMap<String, Object>();
    	  properties.put(SecurityProperties.PARTITION, partition);
    	  
    	  String user = getInitParameter("user");
    	  String password = getInitParameter("password");

    	  serviceFactory = ServiceFactoryLocator.get(user, password, properties);
      }
      catch (Exception e)
      {
         error(request, response, e, null, null, null);

         return;
      }

      if (investigate == true)
      {
         investigate(request, response, serviceFactory, processInstanceOID,
               activityInstanceOID);
      }
      else
      {
         process(request, response, serviceFactory, processInstanceOID,
               activityInstanceOID, outputValue);
      }
   }
	
	private void process(HttpServletRequest request,
				HttpServletResponse response, ServiceFactory serviceFactory, 
				long processInstanceOID, long activityInstanceOID, String outputValue)
				throws IOException
	{
	   ActivityInstance activityInstance = null;
	   ProcessInstance processInstance = null;
	   
		try
		{
         AdministrationService adminService = serviceFactory.getAdministrationService();
         WorkflowService workflowService = serviceFactory.getWorkflowService();

         Map map = new HashMap();
         map.put("returnValue", outputValue);

         activityInstance = adminService.forceCompletion(activityInstanceOID, map);
         processInstance = workflowService.getProcessInstance(activityInstance
               .getProcessInstanceOID());
      }
		catch (Exception e)
		{
			error(request, response, e, activityInstance, processInstance, outputValue);

			return;
		}

		// Create response
		String successPage = getInitParameter("successPage");
		if ( !StringUtils.isEmpty(successPage))
      {
         response.sendRedirect(successPage
               + requestParameters(activityInstance, processInstance, outputValue, null));
      }
      else
      {
         response.setContentType("text/html");
         PrintWriter out = response.getWriter();

         out.println("<html>");
         out.println("<head>");
         out.println("<style type='text/css'>");
         out.println("body { background-color:#FFFFFF; font-weight:normal; font-family:Verdana; font-size:12px; }");
         out.println("</style>");
         out.println("<title>E-Mail Confirmation</title>");
         out.println("</head>");
         out.println("<body>");
         out.println("<img src='plugins/common/images/banner.jpg'/>");
         out.println("<h2>E-Mail Confirmation</h2>");

         out.println("<p>You decided to proceed with <b>"
               + HtmlUtils.htmlEscape(activityInstance.getActivity().getName()) + "</b> and output <b>");
         
         out.println(HtmlUtils.htmlEscape(outputValue));
         
         out.println("</b>.</p><br>");
         out.println("<p>Thank you for your feedback.</p>");
         out.println("</body>");
         out.println("</html>");
      }
	}

	private void investigate(HttpServletRequest request,
				HttpServletResponse response, ServiceFactory serviceFactory,
				long processInstanceOID, long activityInstanceOID)
				throws IOException
	{
		ActivityInstances activityInstances;
		
		try
		{
			QueryService queryService = serviceFactory.getQueryService();			
			ActivityInstanceQuery activityInstanceQuery = ActivityInstanceQuery.findForProcessInstance(processInstanceOID);
			
			activityInstances = queryService.getAllActivityInstances(activityInstanceQuery);
		}
		catch (Exception e)
		{
			error(request, response, e, null, null, null);

			return;
		}

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		out.println("<html>");
		out.println("<head>");
        out.println("<style type='text/css'>");
        out.println("body { background-color:#FFFFFF; font-weight:normal; font-family:Verdana; font-size:12px; }");
        out.println("</style>");
		out.println("<title>Prozess Status</title>");
		out.println("</head>");
		out.println("<body>");
		out.println("<img src='plugins/common/images/banner.jpg'/>");
		out.println("<h2>Process Status</h2>");
		
		out.println("<table>");		
		out.println("<tr>");
		out.println("<td>Activity</td>");
		out.println("<td>Started</td>");
		out.println("<td>Last Modification</td>");
		out.println("<td>Status</td>");
		out.println("<td>Performer</td>");
		out.println("</tr>");
		
		for (int n = 0; n < activityInstances.size(); ++n)
		{
			ActivityInstance activityInstance = (ActivityInstance) activityInstances.get(n);
			
			out.println("<tr>");
			out.println("<td>");
			out.println(activityInstance.getActivity().getName());
			out.println("</td>");
			out.println("<td>");
			DateFormat dateFormat = DateFormat.getDateTimeInstance();

			out.println(dateFormat.format(activityInstance.getStartTime()));
			out.println("</td>");
			out.println("<td>");
			out.println(dateFormat.format(activityInstance.getLastModificationTime()));
			out.println("</td>");
			out.println("<td>");
			
			ActivityInstanceState state = activityInstance.getState();
			String stateString = "";
			
			switch (state.getValue())
			{
				case ActivityInstanceState.CREATED: stateString = "created";
				break;
				case ActivityInstanceState.ABORTED: stateString = "aborted";
				break;
				case ActivityInstanceState.COMPLETED: stateString = "completed";
				break;
				case ActivityInstanceState.APPLICATION: stateString = "in progress";
				break;
				case ActivityInstanceState.HIBERNATED: stateString = "waiting for confirmation";
				break;				
			}
			
			out.println(stateString);
			out.println("</td>");
			out.println("<td>");			
			out.println(activityInstance.getPerformedByName() != null ? activityInstance.getPerformedByName() : "");
			out.println("</td>");
			out.println("</tr>");
		}

		out.println("</table>");
		out.println("</body>");
		out.println("</html>");
	}

	private void error(HttpServletRequest request, HttpServletResponse response,
         Exception e, ActivityInstance activityInstance, ProcessInstance processInstance,
         String outputValue) throws IOException
   {
      String errorPage = getInitParameter("errorPage");
      if ( !StringUtils.isEmpty(errorPage))
      {
         response.sendRedirect((errorPage
               + requestParameters(activityInstance, processInstance, outputValue, e
                     .getMessage())));
      }
      else
      {
         response.setContentType("text/html");
         PrintWriter out = response.getWriter();

         out.println("<html>");
         out.println("<head>");
         out.println("<style type='text/css'>");
         out.println("body { background-color:#FFFFFF; font-weight:normal; font-family:Verdana; font-size:12px; }");
         out.println("</style>");
         out.println("<title>E-Mail Confirmation Error</title>");
         out.println("</head>");
         out.println("<body>");
         out.println("<img src='plugins/common/images/banner.jpg'/>");
         out.println("<h2>E-Mail Confirmation Error</h2>");
         out.println("<p>You may have answered the request already.</p>");       
         out.println("</body>");
         out.println("</html>");
      }
   }
	
	private static String requestParameters(ActivityInstance activityInstance,
         ProcessInstance processInstance, String outputValue, String errorText) throws UnsupportedEncodingException
   {
      String delimiter = "?";
      StringBuffer buffer = new StringBuffer(100);

      if (null != activityInstance)
      {
         buffer.append(delimiter).append("activity-name=").append(
               encodeString(activityInstance.getActivity().getName()));
         delimiter = "&";
      }

      if (null != processInstance)
      {
         buffer.append(delimiter).append("process-name=").append(
               encodeString(processInstance.getProcessName()));
         delimiter = "&";
         buffer.append(delimiter).append("process-id=").append(
               encodeString(processInstance.getProcessID()));
         buffer.append(delimiter).append("process-oid=").append(encodeString(String.valueOf(processInstance.getOID())));
      }

      if ( !StringUtils.isEmpty(outputValue))
      {
         buffer.append(delimiter).append("output-value=").append(encodeString(outputValue));
         delimiter = "&";
      }

      if ( !StringUtils.isEmpty(errorText))
      {
         buffer.append(delimiter).append("error-text=").append(encodeString(errorText));
         delimiter = "&";
      }

      return buffer.toString();
   }
	
	private static String encodeString(String inputString) throws UnsupportedEncodingException
	{
		String encoding = "UTF-8";
		

		return URLEncoder.encode(inputString, encoding);
	}
}
