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
package org.eclipse.stardust.engine.api.runtime;


/**
 * Reports information about the last migration batch and the migration jobs in general.
 * 
 * @author Roland.Stamm
 *
 */
public interface RepositoryMigrationReport
{
   /**
    * Total count of resources which need migration from the current version to the next repository structure version.
    * @return Total count of resources to be migrated.
    */
   long getTotalCount();
   
   /**
    * @return Fraction of resources that are finished for this migration step.
    */
   long getResourcesDone();
   
   /**
    * @return The target version which is the highest version the repository supports.
    */
   int getTargetRepositoryVersion();
   
   /**
    * @return The version of the repository structure currently used.
    */
   int getCurrentRepositoryVersion();
   
   /**
    * @return information about the current migration job.
    */
   RepositoryMigrationJobInfo getCurrentMigrationJob();
   
}
