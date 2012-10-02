package org.eclipse.stardust.engine.cli.sysconsole.consistency;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.cli.sysconsole.Archiver;
import org.eclipse.stardust.engine.core.persistence.Column;
import org.eclipse.stardust.engine.core.persistence.Join;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.DataValueBean;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.SchemaHelper;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;

/**
 * SharedDocumentDataConsistencyCheck checks wether there are legacy document data types
 * that are shared between super- and subprocess
 * 
 */
public class SharedDocumentDataConsistencyCheck implements AuditTrailConsistencyCheck
{
   @Override
   public void execute()
   {
      Set docDataOids = Archiver.findAllDocumentDataOids();
      if ((!docDataOids.isEmpty()) && checkSharedDataExists(docDataOids))
      {
         SchemaHelper.setAuditTrailProperty(
               KernelTweakingProperties.INFINITY_DMS_SHARED_DATA_EXIST, "true");
      }
      else
      {
         SchemaHelper.setAuditTrailProperty(
               KernelTweakingProperties.INFINITY_DMS_SHARED_DATA_EXIST, "false");
      }
   }

   private boolean checkSharedDataExists(Set docDataOids)
   {
      Session session = (Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      QueryDescriptor q = buildQuery(docDataOids);

      List stillRunningRootPiOids = new ArrayList();
      ResultSet rs = null;
      try
      {
         rs = session.executeQuery(q);
         while (rs.next())
         {
            stillRunningRootPiOids.add(new Long(rs.getLong(1)));
         }
      }
      catch (SQLException e)
      {
         throw new PublicException(e);
      }
      finally
      {
         QueryUtils.closeResultSet(rs);
      }
      return !stillRunningRootPiOids.isEmpty();
      
   }

   private QueryDescriptor buildQuery(Set docDataOids)
   {
      /*
       * SELECT DISTINCT spi.ROOTPROCESSINSTANCE from data_value dv 
       * INNER JOIN process_instance spi ON (dv.processInstance = spi.oid) 
       * left outer join data_value dv2 on (dv.NUMBER_VALUE = dv2.NUMBER_VALUE and dv.DATA = dv2.DATA and dv.MODEL = dv2.MODEL) 
       * left outer join PROCESS_INSTANCE spi2 on (dv2.PROCESSINSTANCE = spi2.OID) 
       * where dv.DATA in (12) and dv.TYPE_KEY != -1 and dv.oid != dv2.oid and spi2.TERMINATIONTIME = 0
       */
      
      QueryDescriptor q = QueryDescriptor.from(DataValueBean.class, "dv");
      Join spiJoin = new Join(ProcessInstanceBean.class, "spi").on(
            DataValueBean.FR__PROCESS_INSTANCE, ProcessInstanceBean.FIELD__OID);

      Join dv2Join = new Join(DataValueBean.class, "dv2")
            .on(DataValueBean.FR__NUMBER_VALUE, DataValueBean.FIELD__NUMBER_VALUE)
            .andOn(DataValueBean.FR__DATA, DataValueBean.FIELD__DATA)
            .andOn(DataValueBean.FR__MODEL, DataValueBean.FIELD__MODEL);
      dv2Join.setRequired(false);

      Join spi2Join = new Join(ProcessInstanceBean.class, "spi2").on(
            dv2Join.fieldRef(DataValueBean.FIELD__PROCESS_INSTANCE),
            ProcessInstanceBean.FIELD__OID);
      spi2Join.setRequired(false);
      spi2Join.setDependency(dv2Join);

      q.getQueryExtension().addJoin(spiJoin).addJoin(dv2Join).addJoin(spi2Join);

      q.getQueryExtension().setDistinct(true);
      q.getQueryExtension().setSelection(
            new Column[] {spiJoin
                  .fieldRef(ProcessInstanceBean.FIELD__ROOT_PROCESS_INSTANCE)});

      q.where(Predicates.andTerm(Archiver.splitUpOidsSubList(new ArrayList(docDataOids),
            DataValueBean.FR__DATA), Predicates.notEqual(DataValueBean.FR__TYPE_KEY, -1),
            Predicates.notEqual(DataValueBean.FR__OID,
                  dv2Join.fieldRef(DataValueBean.FIELD__OID)), Predicates.isEqual(
                  spi2Join.fieldRef(ProcessInstanceBean.FIELD__TERMINATION_TIME), 0)));
      return q;
   }

}
