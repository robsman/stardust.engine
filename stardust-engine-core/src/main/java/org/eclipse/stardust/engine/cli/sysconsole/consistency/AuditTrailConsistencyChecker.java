package org.eclipse.stardust.engine.cli.sysconsole.consistency;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.cli.sysconsole.utils.Utils;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;

/**
 * AuditTrailConsistencyChecker runs all added consistency checks.
 *
 */
public class AuditTrailConsistencyChecker
{
   private static final String PARTITION = "partition";

   private List<AuditTrailConsistencyCheck> consistencyChecks = CollectionUtils.newList();

   private List<String> partitionIds;

   public AuditTrailConsistencyChecker(Map options)
   {
      // evaluate partition, fall back to default partition, if configured
      String partitionSpec = (String) options.get(PARTITION);
      if (StringUtils.isEmpty(partitionSpec))
      {
         partitionSpec = ParametersFacade.instance().getString(
               SecurityProperties.DEFAULT_PARTITION,
               PredefinedConstants.DEFAULT_PARTITION_ID);
      }
      partitionIds = CollectionUtils.newList();
      for (Iterator i = StringUtils.split(partitionSpec, ","); i.hasNext();)
      {
         String id = (String) i.next();
         if ((2 < id.length())
               && ((id.startsWith("\"") && id.endsWith("\"") || (id.startsWith("'") && id
                     .endsWith("'")))))
         {
            id = id.substring(1, id.length() - 2);
         }
         partitionIds.add(id);
      }
      if (partitionIds.isEmpty())
      {
         throw new PublicException("No audittrail partition specified.");
      }
   }

   public void addConsistencyCheck(AuditTrailConsistencyCheck consistencyCheck)
   {
      consistencyChecks.add(consistencyCheck);
   }

   public void run()
   {
      for (String partitionId : partitionIds)
      {
         Utils.initCarnotEngine(partitionId);
         for (AuditTrailConsistencyCheck consistencyCheck : consistencyChecks)
         {
            consistencyCheck.execute();
         }
      }
   }
}
