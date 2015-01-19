package org.eclipse.stardust.engine.core.persistence.archive;

import java.io.Serializable;

import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.command.ServiceCommand;

/**
 * This class allows a request to import archived processes instances. The processes are
 * imported from a byte[]. The class returns the number of processes imported.
 * 
 * @author Jolene.Saayman
 * @version $Revision: $
 */
public class ImportProcessesCommand implements ServiceCommand
{
   private static final long serialVersionUID = 1L;

   private final byte[] rawData;

   /**
    * @param rawData This contains the data that needs to be imported in byte[] format
    */
   public ImportProcessesCommand(byte[] rawData)
   {
      super();
      this.rawData = rawData;
   }

   @Override
   public Serializable execute(ServiceFactory sf)
   {
      int importCount;
      if (rawData != null)
      {

         final Session session = (Session) SessionFactory
               .getSession(SessionFactory.AUDIT_TRAIL);

         importCount = ExportImportSupport.loadProcessInstanceGraph(rawData, session);

      } else {
         importCount = 0;
      }
      return importCount;
   }

}
