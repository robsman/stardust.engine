package org.eclipse.stardust.engine.api;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.core.runtime.command.Configurable;
import org.eclipse.stardust.engine.core.runtime.command.ServiceCommand;

public class ProcessInterfaceCommand implements ServiceCommand,  Configurable{

	private static final long serialVersionUID = 1L;
	
	private Map<String, Object> options;
	private Map<String, ? > parameters; 
	private String processId;
	private boolean synchronous;
	
	public ProcessInterfaceCommand(String processId, Map<String, ? > parameters, boolean synchronous)
	{
		this.options = new HashMap<String, Object>();
		this.options.put("autoFlush", Boolean.FALSE);
		this.processId = processId;
		this.parameters = parameters;
		this.synchronous = synchronous;
	}

	@Override
	public Map<String, Object> getOptions() {
		return this.options;
	}

	@Override
	public Serializable execute(ServiceFactory sf) {

		ProcessInstance pi = sf.getWorkflowService().startProcess(this.processId, this.parameters, synchronous);
		
		Map<String, Serializable> result = null;
		
		if (synchronous && pi.getState().equals(ProcessInstanceState.COMPLETED))
		{
			result = sf.getWorkflowService().getProcessResults(pi.getOID());
		}
		
		return new Result(pi, result);
	}
	
	public class Result implements Serializable {

		private static final long serialVersionUID = 1L;
		
		private ProcessInstance processInstance;
		private Map<String, Serializable> processResults;

		private Result(ProcessInstance processInstance, Map<String, Serializable> processResults) {
			this.processInstance = processInstance;
			this.processResults = processResults;
		}

		public ProcessInstance getProcessInstance() {
			return processInstance;
		}

		public void setProcessInstance(ProcessInstance processInstance) {
			this.processInstance = processInstance;
		}

		public Map<String, Serializable> getProcessResults() {
			return processResults;
		}

		public void setProcessResult(Map<String, Serializable> processResults) {
			this.processResults = processResults;
		}

	}

}
