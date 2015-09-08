package org.eclipse.stardust.engine.core.upgrade.framework;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.PrintStream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class RuntimeUpgradeTaskExecutorTest
{
   private RuntimeUpgradeTaskExecutor upgradeTaskExecutor;

   @Mock
   private PrintStream console;

   @Before
   public void setup()
   {
      console = mock(PrintStream.class);
   }

   @Test
   public void upgradeTaskProgress()
   {
      upgradeTaskExecutor = new RuntimeUpgradeTaskExecutor("R7_0_0from6_x_xRuntimeJob",
            false, console);
      for (int i = 0; i < 7; i++)
      {
         upgradeTaskExecutor.addUpgradeSchemaTask(createTask());
      }
      upgradeTaskExecutor.addMigrateDataTask(createTask());
      upgradeTaskExecutor.addMigrateDataTask(createTask());
      upgradeTaskExecutor.addMigrateDataTask(createTask());
      upgradeTaskExecutor.addMigrateDataTask(createTask());
      upgradeTaskExecutor.addFinalizeSchemaTask(createTask());
      upgradeTaskExecutor.addFinalizeSchemaTask(createTask());
      upgradeTaskExecutor.executeUpgradeSchemaTasks();
      verify(console).println("Upgrade Job R7_0_0from6_x_xRuntimeJob:");
      verify(console).println("Upgrading data block: 1 of 13. 0 % completed.");
      verify(console).println("Upgrading data block: 2 of 13. 7 % completed.");
      verify(console).println("Upgrading data block: 3 of 13. 15 % completed.");
      verify(console).println("Upgrading data block: 4 of 13. 23 % completed.");
      verify(console).println("Upgrading data block: 5 of 13. 30 % completed.");
      verify(console).println("Upgrading data block: 6 of 13. 38 % completed.");
      verify(console).println("Upgrading data block: 7 of 13. 46 % completed.");
      upgradeTaskExecutor.executeMigrateDataTasks();
      verify(console).println("Upgrading data block: 8 of 13. 53 % completed.");
      verify(console).println("Upgrading data block: 9 of 13. 61 % completed.");
      verify(console).println("Upgrading data block: 10 of 13. 69 % completed.");
      verify(console).println("Upgrading data block: 11 of 13. 76 % completed.");
      upgradeTaskExecutor.executeFinalizeSchemaTasks();
      verify(console).println("Upgrading data block: 12 of 13. 84 % completed.");
      verify(console).println("Upgrading data block: 13 of 13. 92 % completed.");
      verify(console).println("Upgrade Job R7_0_0from6_x_xRuntimeJob: 100 % completed.");
   }

   @Test
   public void upgradeTaskProgressDataOnly()
   {
      upgradeTaskExecutor = new RuntimeUpgradeTaskExecutor("R7_0_0from6_x_xRuntimeJob",
            true, console);
      for (int i = 0; i < 5; i++)
      {
         upgradeTaskExecutor.addUpgradeSchemaTask(createTask());
      }
      upgradeTaskExecutor.addMigrateDataTask(createTask());
      upgradeTaskExecutor.addMigrateDataTask(createTask());
      upgradeTaskExecutor.addMigrateDataTask(createTask());
      upgradeTaskExecutor.addFinalizeSchemaTask(createTask());
      upgradeTaskExecutor.executeMigrateDataTasks();
      verify(console).println("Upgrade Job R7_0_0from6_x_xRuntimeJob:");
      verify(console).println("Upgrading data block: 1 of 3. 0 % completed.");
      verify(console).println("Upgrading data block: 2 of 3. 33 % completed.");
      verify(console).println("Upgrading data block: 3 of 3. 66 % completed.");
      verify(console).println("Upgrade Job R7_0_0from6_x_xRuntimeJob: 100 % completed.");
   }

   @Test
   public void upgradeTaskProgressSchemaOnly()
   {
      upgradeTaskExecutor = new RuntimeUpgradeTaskExecutor("R7_0_0from6_x_xRuntimeJob",
            false, console);
      for (int i = 0; i < 6; i++)
      {
         upgradeTaskExecutor.addUpgradeSchemaTask(createTask());
      }
      upgradeTaskExecutor.executeUpgradeSchemaTasks();
      verify(console).println("Upgrade Job R7_0_0from6_x_xRuntimeJob:");
      verify(console).println("Upgrading data block: 1 of 6. 0 % completed.");
      verify(console).println("Upgrading data block: 2 of 6. 16 % completed.");
      verify(console).println("Upgrading data block: 3 of 6. 33 % completed.");
      verify(console).println("Upgrading data block: 4 of 6. 50 % completed.");
      verify(console).println("Upgrading data block: 5 of 6. 66 % completed.");
      verify(console).println("Upgrading data block: 6 of 6. 83 % completed.");
      verify(console).println("Upgrade Job R7_0_0from6_x_xRuntimeJob: 100 % completed.");
      upgradeTaskExecutor.executeMigrateDataTasks();
      upgradeTaskExecutor.executeFinalizeSchemaTasks();
   }

   @Test
   public void upgradeTaskProgressDataOnlyNoTasks()
   {
      upgradeTaskExecutor = new RuntimeUpgradeTaskExecutor("R7_0_0from6_x_xRuntimeJob",
            true, console);
      for (int i = 0; i < 5; i++)
      {
         upgradeTaskExecutor.addUpgradeSchemaTask(createTask());
      }
      upgradeTaskExecutor.executeMigrateDataTasks();
      verify(console).println("Upgrade Job R7_0_0from6_x_xRuntimeJob:");
      verify(console).println("No upgrade tasks available.");
   }

   private UpgradeTask createTask()
   {
      return new UpgradeTask()
      {
         @Override
         public void execute()
         {}

         @Override
         public void printInfo()
         {
         }
      };
   }

}
