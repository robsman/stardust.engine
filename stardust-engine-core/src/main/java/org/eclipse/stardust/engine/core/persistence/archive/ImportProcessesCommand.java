package org.eclipse.stardust.engine.core.persistence.archive;

import java.io.Serializable;

import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.command.ServiceCommand;

/**
 * 
 * @author Jolene.Saayman
 * @version $Revision: $
 */
public class ImportProcessesCommand implements ServiceCommand
{
   private static final long serialVersionUID = 1L;
   
   private final byte[] rawData;
   
   /**
    * @param processInstanceOids Oids of process instances to export
    */
   public ImportProcessesCommand(byte[] rawData)
   {
      super();
      this.rawData = rawData;
   }

   @Override
   public Serializable execute(ServiceFactory sf)
   {
      if (rawData != null) {
         
         final Session session = (Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
         
         ExportImportSupport.loadProcessInstanceGraph(rawData, session);
        
      }
      return null;
   }

}
