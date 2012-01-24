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
 * Contains statics to name the available CARNOT modules for usage e.g. in
 * licensing stuff.
 */
public class Modules
{
   public static final String ENGINE="ProcessEngine";
   public static final String DMS_INTEGRATION="DmsIntegration";
   public static final String MODELLING = "ProcessDefinitionDesktop";
   public static final String CALENDAR = "WorktimeCalendar";
   public static final String WEBSERVICES = "WebServicesAdapter";
   public static final String WFXML = "Wf-XML";
   public static final String SAP_R3= "JCAAdapter";
   public static final String PARTITIONS = "MultiPartitions";
   public static final String DOMAINS = "MultiDomains";
   public static final String WAREHOUSE = "ProcessWarehouse";
   
   public static final String PROCESS_WORKBENCH_4_ANALYSTS = "ProcessWorkbenchAnalystEdition";
   public static final String PROCESS_WORKBENCH_4_DEVELOPERS = "ProcessWorkbenchDeveloperEdition";

   public static final String REPORTING_DESIGNER = "ReportDesigner";
   public static final String REPORTING_RUNTIME = "ReportingRuntime";
   
   public static final String IMPORT__ARIS = "ARISImport";
   public static final String IMPORT__TOP_EASE = "TopEaseImport";
   public static final String IMPORT__INCOME = "IncomeImport";

   public static final String EFFORT_CALCULATION = "EffortCalculation";
}
