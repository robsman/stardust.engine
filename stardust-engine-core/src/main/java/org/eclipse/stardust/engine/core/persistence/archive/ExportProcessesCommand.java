package org.eclipse.stardust.engine.core.persistence.archive;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.ProcessElementExporter;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.ProcessElementPurger;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.ProcessElementsVisitor;
import org.eclipse.stardust.engine.core.runtime.command.ServiceCommand;

/**
 * 
 * @author Jolene.Saayman
 * @version $Revision: $
 */
public class ExportProcessesCommand implements ServiceCommand
{
   private static final long serialVersionUID = 1L;
   
   private final List<Long> processInstanceOids;
   
   /**
    * @param processInstanceOids Oids of process instances to export
    */
   public ExportProcessesCommand(List<Long> processInstanceOids)
   {
      super();
      this.processInstanceOids = processInstanceOids;
   }

   @Override
   public Serializable execute(ServiceFactory sf)
   {

      if (CollectionUtils.isNotEmpty(processInstanceOids)) {
         final Session session = (Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);

        
         ProcessElementExporter exporter = new ProcessElementExporter();
         ProcessElementsVisitor processVisitor = new ProcessElementsVisitor(exporter);

         List<Long> uniqueOids = new ArrayList<Long>();
         for (Long oid : processInstanceOids) {
            if (!uniqueOids.contains(oid)) {
               uniqueOids.add(oid);
            }
         }
         
         processVisitor.visitProcessInstances(uniqueOids, session);
         
         ProcessElementPurger purger = new ProcessElementPurger();
         processVisitor = new ProcessElementsVisitor(purger);
         processVisitor.visitProcessInstances(uniqueOids, session);
         
         return exporter.getBlob();
         
      }
      return null;
   }

}
