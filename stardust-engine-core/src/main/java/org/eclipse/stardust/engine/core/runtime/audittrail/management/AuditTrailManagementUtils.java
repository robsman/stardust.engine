package org.eclipse.stardust.engine.core.runtime.audittrail.management;

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.engine.core.persistence.Predicates.isEqual;
import static org.eclipse.stardust.engine.core.persistence.Predicates.notEqual;
import static org.eclipse.stardust.engine.core.persistence.QueryExtension.where;
import static org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils.closeResultSet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.persistence.Column;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.Persistent;
import org.eclipse.stardust.engine.core.persistence.PredicateTerm;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryDescriptor;
import org.eclipse.stardust.engine.core.persistence.QueryExtension;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.runtime.beans.AdminServiceUtils;
import org.eclipse.stardust.engine.core.runtime.beans.Constants;
import org.eclipse.stardust.engine.core.runtime.beans.IUserRealm;
import org.eclipse.stardust.engine.core.runtime.beans.ModelPersistorBean;
import org.eclipse.stardust.engine.core.runtime.beans.ModelRefBean;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceLinkTypeBean;
import org.eclipse.stardust.engine.core.runtime.beans.UserBean;
import org.eclipse.stardust.engine.core.runtime.beans.UserDomainBean;
import org.eclipse.stardust.engine.core.runtime.beans.UserRealmBean;
import org.eclipse.stardust.engine.core.runtime.logging.RuntimeLog;

public class AuditTrailManagementUtils
{
   private static final Logger trace = LogManager.getLogger(AuditTrailManagementUtils.class);

   public static void deleteAllContentFromPartition(short partitionOid, Session session)
   {
      // Delete all runtime data in given partition (process instances, ...).
      deleteAllProcessInstancesFromPartition(partitionOid, session);

      // Delete runtime data which does not depend on any model in given partition.
      // loginUserOid can be 0 because keepLoginUser = false.
      AdminServiceUtils.deleteModelIndependentRuntimeData(false, false, session, 0,
            partitionOid);

      // Delete for all models the definition data (process definition, ...).
      Iterator<ModelPersistorBean> models = session.getIterator(ModelPersistorBean.class, //
            where(isEqual(ModelPersistorBean.FR__PARTITION, partitionOid)));
      while (models.hasNext())
      {
         ModelPersistorBean model = models.next();
         AdminServiceUtils.deleteModelModelingPart(model.getOID(), session);
         ModelRefBean.deleteForModel(model.getOID(), session);
         model.delete();
      }

      // Delete ProcessInstanceLinkTypes
      session.delete(ProcessInstanceLinkTypeBean.class,
            Predicates.isEqual(ProcessInstanceLinkTypeBean.FR__PARTITION, partitionOid),
            false);

      // Delete partition scope preferences
      AdminServiceUtils.deletePartitionPreferences(partitionOid, session);

      // Delete partition runtime artifacts
      AdminServiceUtils.deletePartitionRuntimeArtifacts(partitionOid, session);

      // There should only be one for this partition. But to be on the safe side...
      Iterator<UserDomainBean> domains = session.getIterator(UserDomainBean.class, //
            where(isEqual(UserDomainBean.FR__PARTITION, partitionOid)));
      while (domains.hasNext())
      {
         domains.next().delete();
      }

      // There should only be one for this partition. But to be on the save side...
      Iterator<UserRealmBean> realms = session.getIterator(UserRealmBean.class, //
            where(isEqual(UserRealmBean.FR__PARTITION, partitionOid)));
      while (realms.hasNext())
      {
         IUserRealm realm = realms.next();

         session.delete(UserBean.class, isEqual(UserBean.FR__REALM, realm.getOID()), false);

         realm.delete();
      }
   }

   /**
    * <p>
    * Checks if the runtime Audit Trail contains any Process Instance related information or not.
    * </p>
    *
    * @param partitionOid the OID of the partition to validate
    * @param session the JDBC session to be used for accessing the Audit Trail DB
    * @return {@code true} if the Audit Trail is clean, otherwise {@code false}.
    */
   public static boolean isPartitionCleanOfProcessInstances(short partitionOid, Session session)
   {
      for (Class< ? extends Persistent> prc : Constants.PERSISTENT_RUNTIME_PI_CLASSES)
      {
         if (session.exists(prc, new QueryExtension()))
         {
            trace.info("Found stale records for persistent type " + prc);

            return false;
         }
      }

      return true;
   }

   public static void deleteAllProcessInstancesFromPartition(short partitionOid,
         Session session)
   {
      deleteAllProcessInstancesFromPartition(partitionOid, session, false);
   }
      
   public static void deleteAllProcessInstancesFromPartition(short partitionOid,
         Session session, boolean keepBO)
   {
      PredicateTerm predicate;
      if (keepBO)
      {
         predicate = Predicates.andTerm(isEqual(ModelPersistorBean.FR__PARTITION, partitionOid),
                        notEqual(ProcessInstanceBean.FR__PROCESS_DEFINITION, -1));         
      }
      else
      {
         predicate = isEqual(ModelPersistorBean.FR__PARTITION, partitionOid);         
      }      
      
      // select PIs from the current partition
      QueryDescriptor qd = QueryDescriptor //
            .from(ProcessInstanceBean.class) //
            .select(ProcessInstanceBean.FR__OID) //
            .where(predicate);
      
      // ensure the model table is properly joined, required for filtering against
      // partition OID
      qd.innerJoin(ModelPersistorBean.class) //
            .on(ProcessInstanceBean.FR__MODEL, ModelPersistorBean.FIELD__OID);

      // order by descending PI OID to ensure PI hierarchies are being deleted leaf to
      // root
      qd.getQueryExtension().addOrderBy(ProcessInstanceBean.FR__OID, false);

      try
      {
         deleteProcessInstancesByPredicate(qd, session);
      }
      catch (SQLException sqle)
      {
         RuntimeLog.SQL.warn("Failed collecting process instances for partition "
               + partitionOid, sqle);

         throw new PublicException(BpmRuntimeError.BPMRT_GENERIC_ERROR.raise(), sqle);
      }
   }

   public static void deleteAllProcessInstancesForModel(long modelOid, Session session)
   {
      // select PIs from the current partition
      QueryDescriptor qd = QueryDescriptor //
            .from(ProcessInstanceBean.class) //
            .select(ProcessInstanceBean.FR__OID) //
            .where(isEqual(ProcessInstanceBean.FR__MODEL, modelOid));

      // order by descending PI OID to ensure PI hierarchies are being deleted leaf to
      // root
      qd.getQueryExtension().addOrderBy(ProcessInstanceBean.FR__OID, false);

      try
      {
         deleteProcessInstancesByPredicate(qd, session);
      }
      catch (SQLException sqle)
      {
         RuntimeLog.SQL.warn("Failed collecting process instances for model " + modelOid,
               sqle);

         throw new PublicException(BpmRuntimeError.BPMRT_GENERIC_ERROR.raise(), sqle);
      }
   }

   public static void deleteProcessInstancesByPredicate(QueryDescriptor qd,
         Session session) throws SQLException
   {
      Assert.condition(0 < qd.getQueryExtension().getSelection().length,
            "Query must select PI OIDs in first column.");
      Column firstColumn = qd.getQueryExtension().getSelection()[0];
      Assert.condition(firstColumn instanceof FieldRef,
            "Query must select PI OIDs in first column.");
      Assert.condition(
            ProcessInstanceBean.FIELD__OID.equals(((FieldRef) firstColumn).fieldName),
            "Query must select PI OIDs in first column.");
      Assert.condition(ProcessInstanceBean.TABLE_NAME.equals(((FieldRef) firstColumn)
            .getType().getTableName()), "Query must select PI OIDs in first column.");

      // in terms of a long[] this defaults to about 1MB
      // TODO configurable?
      final int piPrefetchSize = 120000;

      List<Long> piOids = newArrayList(piPrefetchSize);
      do
      {
         piOids.clear();
         ResultSet rs = session.executeQuery(qd);
         try
         {
            // collect PI OIDs
            while (rs.next())
            {
               piOids.add(rs.getLong(1));
            }
         }
         finally
         {
            closeResultSet(rs);
         }

         if (!piOids.isEmpty())
         {
            // delete current slice of PIs
            ProcessInstanceUtils.deleteProcessInstances(piOids, session);
         }
         else
         {
            // done deleting
            break;
         }
      }
      while (true);
   }

   private AuditTrailManagementUtils()
   {
      // utility class
   }
}
