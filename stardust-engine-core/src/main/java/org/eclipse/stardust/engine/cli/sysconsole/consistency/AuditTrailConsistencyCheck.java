package org.eclipse.stardust.engine.cli.sysconsole.consistency;


/**
 * The AuditTrailConsistencyCheck verifies if any problem instances exist in audittrail.
 *
 */
public interface AuditTrailConsistencyCheck
{
   
   /**
    * Runs the consistency check. 
    */
   void execute();
}
