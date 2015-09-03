package org.eclipse.stardust.engine.core.upgrade.framework;

import java.io.PrintStream;
import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;

/**
 * The RuntimeUpgradeTaskExecutor executes all added upgrade tasks (upgrade schema,
 * migrate data, finalize schema) of a runtime upgrade job and displays the progress
 * during execution.
 * 
 */
public class RuntimeUpgradeTaskExecutor
{
   private List<UpgradeTask> upgradeSchemaTasks = CollectionUtils.newArrayList();

   private List<UpgradeTask> migrateDataTasks = CollectionUtils.newArrayList();

   private List<UpgradeTask> finalizeSchemaTasks = CollectionUtils.newArrayList();

   private final boolean upgradeData;

   private int currentTaskNumber = 1;

   private final String jobName;

   private final PrintStream console;

   public RuntimeUpgradeTaskExecutor(String jobName, boolean upgradeData)
   {
      this(jobName, upgradeData, System.out);
   }

   RuntimeUpgradeTaskExecutor(String jobName, boolean upgradeData, PrintStream print)
   {
      this.console = print;
      this.jobName = jobName;
      this.upgradeData = upgradeData;
   }

   public void addUpgradeSchemaTask(UpgradeTask task)
   {
      upgradeSchemaTasks.add(task);
   }

   public void addMigrateDataTask(UpgradeTask task)
   {
      migrateDataTasks.add(task);
   }

   public void addFinalizeSchemaTask(UpgradeTask task)
   {
      finalizeSchemaTasks.add(task);
   }

   public void executeUpgradeSchemaTasks()
   {
      execute(upgradeSchemaTasks);
   }

   public void executeMigrateDataTasks()
   {
      execute(migrateDataTasks);
   }

   public void executeFinalizeSchemaTasks()
   {
      execute(finalizeSchemaTasks);
   }
   
   public void printUpgradeSchemaInfo()
   {
      printInfo(upgradeSchemaTasks);
   }
   

   public void printMigrateDataInfo()
   {
      printInfo(migrateDataTasks);
   }

   public void printFinalizeSchemaInfo()
   {
      printInfo(finalizeSchemaTasks);
   }


   private void printInfo(List<UpgradeTask> upgradeTasks)
   {
      for (UpgradeTask upgradeTask : upgradeTasks)
      {
         upgradeTask.printInfo();
      }
   }

   private void execute(List<UpgradeTask> upgradeTasks)
   {
      if (currentTaskNumber == 1)
      {
         console.println("Upgrade Job " + jobName + ":");
      }
      if (getUpgradeTaskCount() == 0)
      {
         console.println("No upgrade tasks available.");
      }
      for (UpgradeTask upgradeTask : upgradeTasks)
      {
         console.println("Upgrading data block: " + currentTaskNumber + " of "
               + getUpgradeTaskCount() + ". " + getProgressInPercent() + " % completed.");
         upgradeTask.execute();
         currentTaskNumber++;
      }
      if (getUpgradeTaskCount() > 0 && currentTaskNumber > getUpgradeTaskCount())
      {
         console.println("Upgrade Job " + jobName + ": " + getProgressInPercent()
               + " % completed.");
      }
   }

   private int getProgressInPercent()
   {
      int percent = 0;
      if (currentTaskNumber > 1)
      {
         percent = ((currentTaskNumber - 1) * 100) / getUpgradeTaskCount();
      }
      return percent;
   }

   private int getUpgradeTaskCount()
   {
      int count = 0;
      if (upgradeData)
      {
         count = migrateDataTasks.size();
      }
      else
      {
         count = upgradeSchemaTasks.size() + migrateDataTasks.size()
               + finalizeSchemaTasks.size();
      }
      return count;
   }

}
