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

import java.io.Serializable;
import java.util.Map;

/**
 * A client side view of a model element.
 * <p>Model elements are workflow relevant objects that are created during workflow modelling
 * and which have a representation in the CARNOT runtime after model deployment.
 * Model elements have a persistent representation in the audit trail database by being
 * part of a deployed model stored in the <code>MODEL</code> table of the audit trail
 * database.</p>
 * <p>Client side views of CARNOT model elements are exposed to a client as
 * readonly detail objects which contain a copy of the state of the corresponding server
 * object.</p>
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface ModelElement extends Serializable
{
   /**
    * Provides the OID of the partition this model element is deployed to.
    *
    * @return The partition OID.
    */
   short getPartitionOID();
   
   /**
    * Provides the ID of the partition this model element is deploed to.
    *
    * @return The partition ID.
    */
   String getPartitionId();
   
   /**
    * Gets the ID of this model element.
    * <p>Model elements such as process definitions or roles are identified by their ID.
    * The IDs of model elements are Strings being unique
    * inside the containing scope of the model element and the model version.
    * The meaning of containing scope can vary for different model element types, but
    * is usually given by the corresponding factory method.</p>
    *
    * @return the ID of the model element.
    */
   String getId();
   
   /**
    * Gets the (optional) description of this model element.
    *
    * @return the description of the model element.
    */
   String getDescription();

   /**
    * Gets the name of this model element.
    * <p>Model elements have names which can be used to identify them in visual user interfaces.</p>
    *
    * @return the name of the model element.
    */
   String getName();

   /**
    * Gets the OID of the model.
    * <p>The model OID is used to identify the model defining that model element in the
    * scope of the CARNOT runtime.<p>
    *
    * @return the OID of the model.
    *
    * @see #getElementOID
    */
   int getModelOID();

   /**
    * Gets the OID of the model element.
    * <p>Each model element has a unique OID in the scope of the defining model.</p>
    *
    * @return the model element OID
    *
    * @see #getModelOID
    */
   int getElementOID();

   /**
    * Gets all the attributes defined for this model element.
    *
    * @return a Map with name-value pairs containing the attributes defined at modelling time.
    */
   Map getAllAttributes();

   /**
    * Gets a specified attribute.
    *
    * @param name the name of the attribute.
    *
    * @return the value of the attribute.
    */
   Object getAttribute(String name);
   
   /**
    * Gets the namespace of the model element.
    *
    * @return the namespace (the Id of the containing model).
    */
   String getNamespace();

   /**
    * Gets the qualified ID of the model element.
    * 
    * @return the qualified id in the form "{<modelId>}<elementId>"
    */
   String getQualifiedId();
}