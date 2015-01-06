package org.eclipse.stardust.engine.api.pojo;

import java.sql.SQLException;

import org.eclipse.stardust.engine.cli.sysconsole.utils.Utils;
import org.eclipse.stardust.engine.core.runtime.beans.SchemaHelper;

public class AuditTrailPartitionManager
{

   /**
    * Creates a new partition with the given partitionId, if no partition having this id
    * currently exists.
    * 
    * @param partitionId - The id of the partition to be created.
    * @param password - The password of the sysconsole.
    * @throws SQLException
    */
   public static void createAuditTrailPartition(String partitionId, String password)
         throws SQLException
   {
      SchemaHelper.alterAuditTrailCreatePartition(password, partitionId, null, null);
   }

   /**
    * Deletes the partition identified by the given partitionId.
    * 
    * @param partitionId - The id of the partition to be deleted.
    * @param password - The password of the sysconsole.
    */
   public static void dropAuditTrailPartition(String partitionId, String password)
   {
      Utils.initCarnotEngine(partitionId);
      SchemaHelper.alterAuditTrailDropPartition(partitionId, password);
   }

}
