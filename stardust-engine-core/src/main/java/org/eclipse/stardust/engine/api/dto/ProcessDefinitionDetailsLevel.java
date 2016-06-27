package org.eclipse.stardust.engine.api.dto;

/**
 * Represents the level of detail in which the 
 * {@link org.eclipse.stardust.engine.api.model.ProcessDefinition ProcessDefinition} 
 * object is initialized. <p>
 * The declared and implemented ProcessInterface is always contained.
 */
public enum ProcessDefinitionDetailsLevel
{
   /**
    * Contains no Activities, DataPaths, Triggers and EventHandlers.
    */
   CORE,
   /**
    * Contains no Activities but contains DataPaths, Triggers and EventHandlers.
    */
   WITHOUT_ACTIVITIES,
   /**
    * Contains Activities, DataPaths, Triggers and EventHandlers.
    */
   FULL
}
