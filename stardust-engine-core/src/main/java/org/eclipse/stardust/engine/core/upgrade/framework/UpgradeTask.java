package org.eclipse.stardust.engine.core.upgrade.framework;

/**
 * The UpgradeTask provides a separate task of a runtime upgrade job.
 *
 */
public interface UpgradeTask
{
   
   /**
    * The upgrade task will be executed.
    */
   void execute();
}
