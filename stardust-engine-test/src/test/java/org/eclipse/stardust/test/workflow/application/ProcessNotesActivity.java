package org.eclipse.stardust.test.workflow.application;

import org.eclipse.stardust.engine.api.dto.ProcessInstanceAttributes;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.core.runtime.beans.EmbeddedServiceFactory;

/**
 * Plain Java class using by test class <code>TransientUsersWorkflowTest</code>.
 * 
 * @author Antje.Fuhrmann
 * @version $Revision: $
 */
public class ProcessNotesActivity
{
   private long processOid;

   public void addNote()
   {
      EmbeddedServiceFactory esf = EmbeddedServiceFactory.CURRENT_TX();
      WorkflowService ws = esf.getWorkflowService();
      ProcessInstance pi = ws.getProcessInstance(processOid);
      ProcessInstanceAttributes pia = pi.getAttributes();
      pia.addNote("Sample note");
      // persist notes
      ws.setProcessInstanceAttributes(pia);
   }

   public void setProcessOID(long processOid)
   {
      this.processOid = processOid;

   }
}
