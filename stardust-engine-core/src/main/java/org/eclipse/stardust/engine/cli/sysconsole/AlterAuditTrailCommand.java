/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.cli.sysconsole;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.utils.console.Options;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryExtension;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.*;


/**
 * @author sborn
 * @version $Revision$
 */
public class AlterAuditTrailCommand extends AuditTrailCommand
{
   private static final Logger trace = LogManager.getLogger(AlterAuditTrailCommand.class);

   private static final String LOCKTABLE_ENABLE = "enableLockTables";
   private static final String LOCKTABLE_VERIFY = "verifyLockTables";
   private static final String LOCKTABLE_DROP = "dropLockTables";

   private static final String PARTITION_CREATE = "createPartition";
   private static final String PARTITIONS_LIST = "listPartitions";
   private static final String PARTITION_DROP = "dropPartition";

   private static final String DATACLUSTER_ENABLE = "enableDataClusters";
   private static final String DATACLUSTER_VERIFY = "verifyDataClusters";
   private static final String DATACLUSTER_DROP = "dropDataClusters";
   private static final String DATACLUSTER_CONFIG_FILE = "configFile";
   
   private static final String AUDITTRAIL_SKIPDDL = "skipDDL";
   private static final String AUDITTRAIL_SKIPDML = "skipDML";
   private static final String AUDITTRAIL_SQL = "sql";
   private static final String STATEMENT_DELIMITER = "statementDelimiter";

   private static final Options argTypes = new Options();

   private PrintStream spoolDevice = null;
   private boolean isListPartitionsOption = false;

   static
   {
      argTypes.register("-" + LOCKTABLE_ENABLE, "-elt", LOCKTABLE_ENABLE,
            "Creates missing proxy locking tables and synchronizes table content.", false);
      argTypes.register("-" + LOCKTABLE_VERIFY, "-vlt", LOCKTABLE_VERIFY,
            "Verifies existence of proxy locking tables and their consistency.", false);
      argTypes.register("-" + LOCKTABLE_DROP, "-dlt", LOCKTABLE_DROP,
            "Drops any existing proxy locking tables.", false);

      argTypes.register("-" + PARTITION_CREATE, "-cp", PARTITION_CREATE,
            "Creates a new partition with the given ID, if no partition having this ID currently exists.", true);
      argTypes.register("-" + PARTITION_DROP, "-dp", PARTITION_DROP,
            "Deletes the partition identified by the given ID and any contained data from the AuditTrail.", true);
      argTypes.register("-" + PARTITIONS_LIST, "-lp", PARTITIONS_LIST,
            "Lists all existing partitions.", false);

      argTypes.register("-" + DATACLUSTER_ENABLE, "-edc", DATACLUSTER_ENABLE,
            "Creates missing data cluster tables and synchronizes table content.", false);
      argTypes.register("-" + DATACLUSTER_VERIFY, "-vdc", DATACLUSTER_VERIFY,
            "Verifies existence of data cluster tables and their consistency.", false);
      argTypes.register("-" + DATACLUSTER_DROP, "-ddc", DATACLUSTER_DROP,
            "Drops any existing data cluster tables.", false);
      argTypes.register("-" + DATACLUSTER_CONFIG_FILE, Options.NO_SHORTNAME, DATACLUSTER_CONFIG_FILE,
            "Specifies the name of the config file which shall be deployed to audit trail.", true);
      argTypes.register("-" + AUDITTRAIL_SKIPDDL, Options.NO_SHORTNAME, AUDITTRAIL_SKIPDDL,
            "Skips the execution of schema changing commands like 'create' or 'drop'.", false);
      argTypes.register("-" + AUDITTRAIL_SKIPDML, Options.NO_SHORTNAME, AUDITTRAIL_SKIPDML,
            "Skips the execution of data changing commands like 'insert' or 'update'.", false);
      argTypes.register("-" + AUDITTRAIL_SQL, Options.NO_SHORTNAME, AUDITTRAIL_SQL,
            "Spools SQL statements to file instead of executing them on audit trail.", true);
      argTypes.register("-" + STATEMENT_DELIMITER, "-sd", STATEMENT_DELIMITER,
            "Specifies the delimiter applied after each SQL statement.", true);
      
      argTypes.addExclusionRule(//
            new String[] { LOCKTABLE_ENABLE, LOCKTABLE_VERIFY, LOCKTABLE_DROP,//
                  DATACLUSTER_ENABLE, DATACLUSTER_VERIFY, DATACLUSTER_DROP,//
                  PARTITION_CREATE, PARTITION_DROP, PARTITIONS_LIST }, true);
      argTypes.addExclusionRule(//
            new String[] { LOCKTABLE_ENABLE, LOCKTABLE_VERIFY, LOCKTABLE_DROP,//
                  DATACLUSTER_VERIFY, DATACLUSTER_DROP,//
                  PARTITION_CREATE, PARTITION_DROP, PARTITIONS_LIST,//
                  DATACLUSTER_CONFIG_FILE }, false);
      argTypes.addExclusionRule(//
            new String[] { LOCKTABLE_VERIFY, DATACLUSTER_VERIFY, AUDITTRAIL_SQL }, false);
   }

   private boolean doRunLockingTableOptions(Map options)
   {
      final String password = (String) globalOptions.get("password");
      final String statementDelimiter = (String) options.get(STATEMENT_DELIMITER);

      boolean optionHandled = false;
      if (options.containsKey(LOCKTABLE_ENABLE))
      {
         optionHandled = true;
         print("Creating missing proxy locking tables and synchronizing their table content for Infinity schema.");
         
         boolean skipDdl = options.containsKey(AUDITTRAIL_SKIPDDL);
         boolean skipDml = options.containsKey(AUDITTRAIL_SKIPDML);
         
         try
         {
            SchemaHelper.alterAuditTrailCreateLockingTables(password, skipDdl, skipDml,
                  spoolDevice, statementDelimiter);
         }
         catch (SQLException e)
         {
            trace.warn("", e);
            throw new PublicException("SQL Exception occured: " + e.getMessage());
         }
         catch (InternalException e)
         {
            trace.warn("", e);
            throw new PublicException(e.getMessage());
         }

         print("Proxy locking tables created and synchronized.");
      }
      else if (options.containsKey(LOCKTABLE_VERIFY))
      {
         optionHandled = true;
         print("Verifying existence of proxy locking tables and their consistency.");
         
         try
         {
            SchemaHelper.alterAuditTrailVerifyLockingTables(password);
         }
         catch (SQLException e)
         {
            trace.warn("", e);
            throw new PublicException("SQL Exception occured: " + e.getMessage());
         }
         catch (InternalException e)
         {
            trace.warn("", e);
            throw new PublicException(e.getMessage());
         }
         
         print("Verification of proxy locking tables and their consistency done.");
      }
      else if (options.containsKey(LOCKTABLE_DROP))
      {
         optionHandled = true;
         print("Dropping any proxy locking tables from Infinity schema.");
         SchemaHelper.alterAuditTrailDropLockingTables(password, spoolDevice,
               statementDelimiter);
         print("Proxy locking tables dropped.");
      }
      
      return optionHandled;
   }
   
   private boolean doRunDataClusterOptions(Map options)
   {
      final String password = (String) globalOptions.get("password");
      final String statementDelimiter = (String) options.get(STATEMENT_DELIMITER);

      
      boolean optionHandled = false;
      if (options.containsKey(DATACLUSTER_ENABLE))
      {
         optionHandled = true;
         print("Creating missing data cluster tables and synchronizing their table content for Infinity schema.");
         
         String configFileName = (String) options.get(DATACLUSTER_CONFIG_FILE);
         boolean skipDdl = options.containsKey(AUDITTRAIL_SKIPDDL);
         boolean skipDml = options.containsKey(AUDITTRAIL_SKIPDML);
         
         try
         {
            SchemaHelper.alterAuditTrailCreateDataClusterTables(password, configFileName,
                  skipDdl, skipDml, spoolDevice, statementDelimiter);
         }
         catch (SQLException e)
         {
            trace.warn("", e);
            throw new PublicException("SQL Exception occured: " + e.getMessage());
         }
         catch (InternalException e)
         {
            trace.warn("", e);
            throw new PublicException(e.getMessage());
         }
         
         print("Data cluster tables created and synchronized.");
      }
      else if (options.containsKey(DATACLUSTER_VERIFY))
      {
         optionHandled = true;
         print("Verifying existence of data cluster tables and their consistency.");
         
         try
         {
            SchemaHelper.alterAuditTrailVerifyDataClusterTables(password);
         }
         catch (SQLException e)
         {
            trace.warn("", e);
            throw new PublicException("SQL Exception occured: " + e.getMessage());
         }
         catch (InternalException e)
         {
            trace.warn("", e);
            throw new PublicException(e.getMessage());
         }
         
         print("Verification of data cluster tables and their consistency done.");
      }
      else if (options.containsKey(DATACLUSTER_DROP))
      {
         optionHandled = true;
         print("Dropping any data cluster tables from CARNOT schema.");

         SchemaHelper.alterAuditTrailDropDataClusterTables(password, spoolDevice,
               statementDelimiter);
         print("Data cluster tables dropped.");
      }
      
      return optionHandled;
   }

   private boolean doRunPartitionOptions(Map options)
   {
      final String password = (String) globalOptions.get("password");
      final String statementDelimiter = (String) options.get(STATEMENT_DELIMITER);


      boolean optionHandled = false;
      if (options.containsKey(PARTITION_CREATE))
      {
         optionHandled = true;
         print("Creating a new partition in AuditTrail.");
         
         String partitionId = (String) options.get(PARTITION_CREATE);
         
         try
         {
            SchemaHelper.alterAuditTrailCreatePartition(password, partitionId,
                  spoolDevice, statementDelimiter);
         }
         catch (SQLException e)
         {
            trace.warn("", e);
            throw new PublicException("SQL Exception occured: " + e.getMessage());
         }
         catch (InternalException e)
         {
            trace.warn("", e);
            throw new PublicException(e.getMessage());
         }

         print("Creation of partition in AuditTrail done.");
      }
      else if (options.containsKey(PARTITION_DROP))
      {
         optionHandled = true;
         print("Deletes the partition and any contained data from the AuditTrail.");
         
         String partitionId = (String) options.get(PARTITION_DROP);
         Utils.initCarnotEngine(partitionId);

         IAuditTrailPartition partition = AuditTrailPartitionBean.findById(partitionId);
         Session session = (Session) SessionFactory
               .getSession(SessionFactory.AUDIT_TRAIL);
         
         // Delete for all models in given partition the runtime data (process instances, ...).
         Iterator iter = session.getIterator(ModelPersistorBean.class, QueryExtension
               .where(Predicates.isEqual(ModelPersistorBean.FR__PARTITION, partition
                     .getOID())));
         while (iter.hasNext())
         {
            ModelPersistorBean model = (ModelPersistorBean) iter.next();
            AdminServiceUtils.deleteModelRuntimePart(model.getOID(), session);
         }

         // Delete runtime data which does not depend on any model in given partition.
         // loginUserOid can be 0 because keepLoginUser = false.
         AdminServiceUtils.deleteModelIndependentRuntimeData(false, false, session, 0,
               partition.getOID());

         // Delete for all model the definition data (process definition, ...).
         iter = session.getIterator(ModelPersistorBean.class, QueryExtension
               .where(Predicates.isEqual(ModelPersistorBean.FR__PARTITION, partition
                     .getOID())));
         while (iter.hasNext())
         {
            ModelPersistorBean model = (ModelPersistorBean) iter.next();
            AdminServiceUtils.deleteModelModelingPart(model.getOID(), session);
            model.delete();
         }
         
         // Delete partition scope preferences
         AdminServiceUtils.deletePartitionPreferences(partition.getOID(), session);

         // There should only be one for this partition. But to be on the save side...
         iter = session.getIterator(UserDomainBean.class, QueryExtension.where(Predicates
               .isEqual(UserDomainBean.FR__PARTITION, partition.getOID())));
         while (iter.hasNext())
         {
            IUserDomain domain = (IUserDomain) iter.next();
            domain.delete();
         }

         // There should only be one for this partition. But to be on the save side...
         iter = session.getIterator(UserRealmBean.class, QueryExtension.where(Predicates
               .isEqual(UserRealmBean.FR__PARTITION, partition.getOID())));
         while (iter.hasNext())
         {
            IUserRealm realm = (IUserRealm) iter.next();

            session.delete(UserBean.class, Predicates.isEqual(UserBean.FR__REALM, realm
                  .getOID()), false);

            realm.delete();
         }

         partition.delete();

         session.save(true);

         print("Deletion of partition and contained data from AuditTrail done.");
      }
      else if (options.containsKey(PARTITIONS_LIST))
      {
         optionHandled = true;
         print("Lists all existing partitions.");
         try
         {
            SchemaHelper.alterAuditTrailListPartitions(password);
            print("List completed.");
         }
         catch (SQLException e)
         {
            trace.warn("", e);
            throw new PublicException("SQL Exception occured: " + e.getMessage());
         }
         catch (InternalException e)
         {
            trace.warn("", e);
            throw new PublicException(e.getMessage());
         }
      }
      
      return optionHandled;
   }
   
   public int doRun(Map options)
   {
      if (globalOptions.containsKey("dbtype"))
      {
         Parameters.instance().set(SessionFactory.AUDIT_TRAIL + ".Type",
               globalOptions.get("dbtype"));
      }

      if (options.containsKey(AUDITTRAIL_SQL))
      {
         String fileName = (String) options.get(AUDITTRAIL_SQL);
         if ( !StringUtils.isEmpty(fileName))
         {
            File ddlFile = new File(fileName);
            
            try
            {
               spoolDevice = new PrintStream(new FileOutputStream(ddlFile));
            }
            catch(FileNotFoundException x)
            {
               trace.warn("", x);
               throw new PublicException("Could not initialize ddl spool file: "
                     + x.getMessage());
            }
         }
      }
      
      if ( !doRunLockingTableOptions(options) && !doRunDataClusterOptions(options)
            && !doRunPartitionOptions(options))
      {
         print("Unknown option for command auditTrail.");
      }

      return 0;
   }

   public void printCommand(Map options)
   {
      if (options.containsKey(LOCKTABLE_VERIFY)
            || options.containsKey(DATACLUSTER_VERIFY))
      {
         print("Verifies existing Infinity schema:\n");
      }
      else if (options.containsKey(PARTITIONS_LIST))
      {
         print("Lists elements from existing Infinity schema:\n");
      }
      else if (options.containsKey(PARTITION_CREATE))
      {
         print("Creates new partition in Infinity schema:\n");
      }
      else if (options.containsKey(PARTITION_DROP))
      {
         print("Drops existing partition in Infinity schema:\n");
      }
      else
      {
         print("Alters existing Infinity schema:\n");
      }
   }
   
   public void preprocessOptions(Map options)
   {
      if (options.containsKey(PARTITIONS_LIST))
      {
         isListPartitionsOption = true;
      }
      
      super.preprocessOptions(options);
   }
   
   public boolean force()
   {
      if (isListPartitionsOption)
      {
         return true;
      }
      
      return super.force();
   }

   public Options getOptions()
   {
      return argTypes;
   }

   public String getSummary()
   {
      return "Allows altering of the existing Infinity schema.";
   }

}
