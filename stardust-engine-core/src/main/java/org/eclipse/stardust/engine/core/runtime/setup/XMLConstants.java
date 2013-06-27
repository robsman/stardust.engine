/*******************************************************************************
 * Copyright (c) 2011, 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.setup;

/**
 * @author sborn
 * @version $Revision$
 */
public interface XMLConstants
{
   String XMLNS_ATTR = "xmlns";
   
   String NS_XSI = "http://www.w3.org/2001/XMLSchema-instance";
   String NS_CARNOT_RUNTIME_SETUP = "http://www.carnot.ag/carnot-runtime-setup";

   String RUNTIME_SETUP_XSD = "carnot-runtime-setup.xsd";

   String RUNTIME_SETUP_XSD_URL = NS_CARNOT_RUNTIME_SETUP + "/" + RUNTIME_SETUP_XSD;

   // Element names
   String RUNTIME_SETUP = "runtime-setup";
   String AUDIT_TRAIL = "audit-trail";
   String DATA_CLUSTERS = "data-clusters";
   String DATA_CLUSTER = "data-cluster";
   String DATA_SLOTS = "data-slots";
   String DATA_SLOT = "data-slot";
   String DATA_CLUSTER_INDEXES = "data-cluster-indexes";
   String DATA_CLUSTER_INDEX = "data-cluster-index";
   String DATA_CLUSTER_INDEX_COLUMN = "column";
   

   // Attribute names
   String DATA_CLUSTER_TABNAME_ATT = "tableName";
   String DATA_CLUSTER_PICOLUMN_ATT = "processInstanceColumn";
   String DATA_CLUSTER_ENABLED_PI_STATE = "enabledForProcessInstanceState";
   
   String DATA_SLOT_MODELID_ATT = "modelId";
   String DATA_SLOT_DATAID_ATT = "dataId";
   String DATA_SLOT_ATTRIBUTENAME_ATT = "attributeName";
   String DATA_SLOT_OIDCOLUMN_ATT = "oidColumn";
   String DATA_SLOT_TYPECOLUMN_ATT = "typeColumn";
   String DATA_SLOT_NVALCOLUMN_ATT = "nValueColumn";
   String DATA_SLOT_IGNORE_PREPARED_STATEMENTS_ATT = "ignorePreparedStatements";
   String DATA_SLOT_SVALCOLUMN_ATT = "sValueColumn";
   String DATA_SLOT_DVALCOLUMN_ATT = "dValueColumn";
   
   String INDEX_NAME_ATT = "indexName";
   String INDEX_UNIQUE_ATT = "unique";
   
   String INDEX_COLUMN_NAME_ATT = "name";

   // Attribute values
}
