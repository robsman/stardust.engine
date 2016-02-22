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
package org.eclipse.stardust.engine.core.runtime.utils;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author Florin.Herinean
 * @version $Revision: $
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface ExecutionPermission {

   public enum Default {
      ADMINISTRATOR,
      ALL,
      AUDITOR,
      OWNER
   }

   public enum Scope {
      activity,
      data,
      model,
      processDefinition,
      workitem
   }

   /**
    * @author Florin.Herinean
    * @version $Revision: $
    *
    * Class used as an identifier for the permission
    */
   public enum Id {
      /**
       * no permission is required
       */
      none,
      /**
       * permission to abort an activity
       */
      abortActivityInstances,
      /**
       * permission to abort a process
       */
      abortProcessInstances,
      /**
       * permission to flush the cache
       */
      controlProcessEngine,
      /**
       * permission to create process instance cases
       */
      createCase,
      /**
       * permission to switch activity to another department
       */
      delegateToDepartment,
      /**
       * permission to delegate activity to another participant
       */
      delegateToOther,
      /**
       * permission to deploy a process model to the engine
       */
      deployProcessModel,
      /**
       * permission to deploy and manage a runtime artifact.
       */
      deployRuntimeArtifact,
      /**
       * permission to suspend an activity of another user
       */
      forceSuspend,
      /**
       * permission to join a process instance
       */
      joinProcessInstance,
      /**
       * permission to change user grants
       */
      manageAuthorization,
      /**
       * permission to change user deputies
       */
      manageDeputies,
      /**
       * permission to start, stop and query the state of the daemons
       */
      manageDaemons,
      /**
       * permission to bind and unbind event handler
       */
      manageEventHandlers,
      /**
       * permission to modify activity instances
       */
      modifyActivityInstances,
      /**
       * permission to modify the attributes of activity and process instances
       */
      modifyAttributes,
      /**
       * permission to modify the AuditTrail database
       */
      modifyAuditTrail,
      /**
       * permission to modify the AuditTrail database
       */
      modifyAuditTrailStatistics,
      /**
       * permission to modify departments
       */
      modifyDepartments,
      /**
       * permission to modify process instance cases
       */
      modifyCase,
      /**
       * permission to modify process data values
       */
      modifyDataValues,
      /**
       * permission to modify any data via the document management service
       */
      modifyDmsData,
      /**
       * permission to modify process instances
       */
      modifyProcessInstances,
      /**
       * permission to modify user data such as email, account, etc.
       */
      modifyUserData,
      /**
       * permission to perform an activity
       */
      performActivity,
      /**
       * permission to access activity instances
       */
      @ReadOnly
      readActivityInstanceData,
      /**
       * permission to query statistics on the audittrail database
       */
      @ReadOnly
      readAuditTrailStatistics,
      /**
       * permission to read process data values
       */
      @ReadOnly
      readDataValues,
      /**
       * permission to read department information
       */
      @ReadOnly
      readDepartments,
      /**
       * permission to access data contained in the model
       */
      @ReadOnly
      readModelData,
      /**
       * permission to access the process instances
       */
      @ReadOnly
      readProcessInstanceData,
      /**
       * permission to read a deployed runtime artifact.
       */
      @ReadOnly
      readRuntimeArtifact,
      /**
       * readUserData - permission to access user data such as email, account, etc.
       */
      @ReadOnly
      readUserData,
      /**
       * permission to reset the password of an user
       */
      resetUserPassword,
      /**
       * runRecovery - permission to run the recovery
       */
      runRecovery,
      /**
       * saveOwnUserScopePreferences - permission to save preferences in own user scope
       */
      saveOwnUserScopePreferences,
      /**
       * saveOwnRealmScopePreferences - permission to save preferences in own realm scope
       */
      saveOwnRealmScopePreferences,
      /**
       * saveOwnPartitionScopePreferences - permission to save preferences in own partition scope
       */
      saveOwnPartitionScopePreferences,
      /**
       * permission to spawn a peer process instance
       */
      spawnPeerProcessInstance,
      /**
       * permission to spawn a sub process instance
       */
      spawnSubProcessInstance,
      /**
       * permission to start a new process instance
       */
      startProcesses
   }

   /**
    * Specifies the identifier of the permission.
    *
    * @return a string containing the identifier.
    */
   Id id() default Id.none;

   /**
    * Specifies the scope of the permission, which can be one of:
    * <ul>
    * <li>model - permission applies to the active model.</li>
    * <li>process - permission applies to the accessed process instance(s).</li>
    * <li>activity - permission applies to the accessed activity instance(s).</li>
    * <li>data - permission applies to the accessed data object(s).</li>
    * </ul>
    *
    * @return the scope of the permission.
    */
   Scope scope() default Scope.model;

   /**
    * Specifies which permissions are considered in the case that the model does not
    * specify any permission or changeable is false.
    *
    * @return the list of default permissions.
    */
   Default[] defaults() default Default.ADMINISTRATOR;

   /**
    * Specifies which permissions are always present in addition to the ones defined in the model.
    *
    * @return the list of default permissions.
    */
   Default[] fixed() default {};

   /**
    * Specifies that model permissions should be considered instead of the default ones
    * defined in the permission annotation.
    *
    * A value of false means that model permissions will be ignored and only the
    * default permissions defined in the annotation will be used.
    *
    * @return true if the permissions defined in the model should be considered.
    */
   boolean changeable() default true;

   /**
    * Specifies that an administrator can override the permission settings and
    * perform the method even if it is not explicitly present in the permission list.
    *
    * @return true if administrators are always allowed to perform this method.
    */
   boolean administratorOverride() default true;

   /**
    * Specifies that the permissions check will not be performed before invocation,
    * instead it will be deferred and performed in the called method.
    *
    * The engine will only set an Authorization2Predicate in the runtime environment
    * and it is the sole responsibility of the called method to use this predicate.
    *
    * @return true if the permission check should be deferred.
    */
   boolean defer() default false;

   /**
    * Specifies that the implied permission(s) could be used instead of this one.
    *
    * @return the implied Id
    */
   Id[] implied() default {};

   @Retention(RetentionPolicy.RUNTIME)
   public @interface ReadOnly {

   }
}
