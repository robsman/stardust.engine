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
package org.eclipse.stardust.engine.extensions.mail.web.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.runtime.*;


public class MailApplicationReceptionServlet extends HttpServlet
{
	public static final String PROCESS_INSTANCE_OID = "processInstanceOID";
	public static final String ACTIVITY_INSTANCE_OID = "activityInstanceOID";
	public static final String OUTPUT_VALUE = "outputValue";
	public static final String HASH_CODE = "hashCode";
	public static final String INVESTIGATE = "investigate";

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request, HttpServletResponse response)
				throws IOException, ServletException
	{
		ServiceFactory serviceFactory;

		// Parse request

		String processInstanceOIDString = request
					.getParameter(PROCESS_INSTANCE_OID);
		String activityInstanceOIDString = request
		.getParameter(ACTIVITY_INSTANCE_OID);
		String outputValue = request.getParameter(OUTPUT_VALUE);
//		String hashCode = request.getParameter(HASH_CODE);
		String investigate = request.getParameter(INVESTIGATE);
		long processInstanceOID = Long.parseLong(processInstanceOIDString);
		long activityInstanceOID = Long.parseLong(activityInstanceOIDString);

		// Complete activity

      try
      {
         String user = getInitParameter("user");
         String password = getInitParameter("password");
         
         serviceFactory = ServiceFactoryLocator.get(user, password);
      }
		catch (Exception e)
		{
			error(request, response, e, null, null, null);

			return;
		}

		if (investigate != null && investigate.compareToIgnoreCase("true") == 0)
		{
			investigate(request, response, serviceFactory, processInstanceOID, activityInstanceOID);
		}
		else
		{
			process(request, response, serviceFactory, processInstanceOID, activityInstanceOID, outputValue);
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
         out.println("<!--");
         out.println("body { background-color:#DBDBDB; font-weight:normal; font-family:Verdana; font-size:12px; }");
         out.println("-->");
         out.println("</style>");
         out.println("<title>Infinity Mail Confirmation</title>");
         out.println("</head>");
         out.println("<body>");
         out.println("<img src='images/logo.jpg'/>");
         out.println("<h1>Mail Confirmation</h1>");

         out.println("<p>You decided to proceed with \""
               + activityInstance.getActivity().getName() + "\" with ");
         out.println("<p>outputValue: " + outputValue + "</p>");
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
      out.println("<!--");
      out.println("body { background-color:#DBDBDB; font-weight:normal; font-family:Verdana; font-size:12px; }");
      out.println("-->");
      out.println("</style>");
		out.println("<title>Prozess History</title>");
		out.println("</head>");
		out.println("<body>");
		out.println("<img src='images/logo.jpg'/>");
		out.println("<h1>Process Status</h1>");
		
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
         response.sendRedirect(errorPage
               + requestParameters(activityInstance, processInstance, outputValue, e
                     .getMessage()));
      }
      else
      {
         response.setContentType("text/html");
         PrintWriter out = response.getWriter();

         out.println("<html>");
         out.println("<head>");
         out.println("<style type='text/css'>");
         out.println("<!--");
         out.println("body { background-color:#DBDBDB; font-weight:normal; font-family:Verdana; font-size:12px; }");
         out.println("-->");
         out.println("</style>");
         out.println("<title>Mail Confirmation Error</title>");
         out.println("</head>");
         out.println("<body>");
         out.println("<img src='images/logo.jpg'/>");
         out.println("<h1>Error</h1>");
         out.println("<p>You may have answered the request already.</p>");
         /*out.println("<div id='small'><p>");
         e.printStackTrace(out);
         out.println("</p><div>");*/
         out.println("</body>");
         out.println("</html>");
      }
   }
	
	private static String requestParameters(ActivityInstance activityInstance,
         ProcessInstance processInstance, String outputValue, String errorText)
   {
      String delimiter = "?";
      StringBuffer buffer = new StringBuffer(100);

      if (null != activityInstance)
      {
         buffer.append(delimiter).append("activity-name=").append(
               activityInstance.getActivity().getName());
         delimiter = "&";
      }

      if (null != processInstance)
      {
         buffer.append(delimiter).append("process-name=").append(
               processInstance.getProcessName());
         delimiter = "&";
         buffer.append(delimiter).append("process-id=").append(
               processInstance.getProcessID());
         buffer.append(delimiter).append("process-oid=").append(processInstance.getOID());
      }

      if ( !StringUtils.isEmpty(outputValue))
      {
         buffer.append(delimiter).append("output-value=").append(outputValue);
         delimiter = "&";
      }

      if ( !StringUtils.isEmpty(errorText))
      {
         buffer.append(delimiter).append("error-text=").append(errorText);
         delimiter = "&";
      }

      return buffer.toString();
   }
}
