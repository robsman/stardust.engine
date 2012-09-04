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
package org.eclipse.stardust.engine.api.model;

// @todo (france, ub): not nicely placed here

/**
 * Contains statics to name the available CARNOT modules.
 */
public enum Modules
{
   ENGINE ("ProcessEngine"),
   DMS ("DmsIntegration"),
   CALENDAR ("WorktimeCalendar"),
   WEBSERVICES ("WebServicesAdapter"),
   WFXML ("Wf-XML"),
   JCA ("JCAAdapter"),
   PARTITIONS ("MultiPartitions"),
   DOMAINS ("MultiDomains"),
   WAREHOUSE ("ProcessWarehouse"),

   DEVELOPER ("ProcessWorkbenchDeveloperEdition"),
   MODELLING ("ProcessDefinitionDesktop"), // deprecated, same as DEVELOPER

   REPORT_DESIGNER ("ReportDesigner"),
   REPORT_RUNTIME ("ReportingRuntime"),

   ARIS_IMPORT ("ARISImport"),
   TOPEASE_IMPORT ("TopEaseImport"),
   INCOME_IMPORT ("IncomeImport"),

   EFFORT ("EffortCalculation"),

   SIMULATION ("SimulationModeller"),
   SIMULATION_CONNECTION ("SimulationAuditTrailConnection");

   private String id;

   private Modules(String id)
   {
      this.id = id;
   }

   public String getId()
   {
      return id;
   }
}
