/*******************************************************************************
* Copyright (c) 2016 SunGard CSA LLC and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Barry.Grotjahn (SunGard CSA LLC) - initial API and implementation and/or initial documentation
*******************************************************************************/

package org.eclipse.stardust.test.workflow.application;

import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.core.runtime.beans.EmbeddedServiceFactory;

public class CaseProcessActivity 
{
	private static long processOid;
	private static String caseName;
	private static ProcessInstance caseProcessInstance;

	public CaseProcessActivity() 
	{
	}
		
	public static void setProcessOid(long processOid) 
	{
		CaseProcessActivity.processOid = processOid;
	}

	public static void setCaseName(String caseName) 
	{
		CaseProcessActivity.caseName = caseName;
	}
	
	public void createCase()
	{
      EmbeddedServiceFactory esf = EmbeddedServiceFactory.CURRENT_TX();
      WorkflowService ws = esf.getWorkflowService();
      
      ProcessInstanceQuery query = ProcessInstanceQuery.findCaseByName(caseName);      
      ProcessInstances allProcessInstances = esf.getQueryService().getAllProcessInstances(query);
      if(allProcessInstances != null && allProcessInstances.size() != 0)
      {
         caseProcessInstance = allProcessInstances.get(0);
      }
      if (caseProcessInstance == null
    		  || caseProcessInstance.getState() == ProcessInstanceState.Completed
    		  || caseProcessInstance.getState() == ProcessInstanceState.Aborted) 
      {
         long[] members2 = {processOid};         
         caseProcessInstance = ws.createCase(caseName, null, members2);    
      }
      else
      {
         long[] members2 = {processOid};                  
         caseProcessInstance = ws.joinCase(caseProcessInstance.getOID(), members2);
      }
	}
	
   public void leaveCase()
   {
      EmbeddedServiceFactory esf = EmbeddedServiceFactory.CURRENT_TX();
      WorkflowService ws = esf.getWorkflowService();
      
      if (caseProcessInstance != null
           && caseProcessInstance.getState() != ProcessInstanceState.Completed
           && caseProcessInstance.getState() != ProcessInstanceState.Aborted) 
      {
         long[] members2 = {processOid};         
         ws.leaveCase(caseProcessInstance.getOID(), members2);         
      }
   }	
}