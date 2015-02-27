/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Florin.Herinean (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.api.runtime;

import java.io.Serializable;
import java.util.List;

import javax.xml.namespace.QName;

import org.eclipse.stardust.engine.core.runtime.beans.BigData;

/**
 * A business object view of a Process Data.
 *
 * @author Florin.Herinean
 * @version $Revision: $
 */
public interface BusinessObject extends Serializable
{
   /**
    * The definition of a business object structure.
    *
    * @author Florin.Herinean
    * @version $Revision: $
    */
   public static interface Definition
   {
      /**
       * Retrieves the name of the structure.
       *
       * @return the name of the corresponding xsd element.
       */
      String getName();

      /**
       * The type of the structure.
       *
       * @return
       * <ul>
       *    <li>{@link BigData#NULL} if this structure contains a complex content.</li>
       *    <li>one of the {@link BigData} primitive constants if this structure contains a (java) primitive value.</li>
       * </ul>
       */
      int getType();

      /**
       * Retrieves the xsd type name.
       *
       * @return the xsd type name or null if it's an anonymous definition.
       */
      QName getTypeName();

      /**
       * Retrieves if this structure is a list (array).
       *
       * @return true if the structure is a list, false otherwise.
       */
      boolean isList();

      /**
       * Retrieves if this structure can be used in searches.
       *
       * @return true if the structure is searchable, false otherwise.
       */
      boolean isKey();

      /**
       * Retrieves if this structure represents the primary key.
       *
       * @return true if this structure represents the primary key, false otherwise.
       */
      boolean isPrimaryKey();

      /**
       * Retrieves the child items composing this structure.
       *
       * @return a list of child definitions, or null if this is a leaf node (no children).
       */
      List<Definition> getItems();
   }

   /**
    * A concrete business object instance value.
    *
    * @author Florin.Herinean
    * @version $Revision: $
    */
   public static interface Value
   {
      /**
       * Retrieves the process instance oid containing the actual data value.
       *
       * @return the synthetic process instance oid.
       */
      long getProcessInstanceOid();

      /**
       * Retrieves the actual value of the business object instance.
       *
       * @return the value object (an instance of a map).
       */
      Object getValue();
   }

   /**
    * Retrieves the oid of the model defining the business object.
    *
    * @return the oid of the defining model.
    */
   long getModelOid();

   /**
    * Retrieves the id of the model defining the business object.
    *
    * @return the id of the defining model.
    */
   String getModelId();

   /**
    * Retrieves the id of the business object.
    *
    * @return the id of the business object.
    */
   String getId();

   /**
    * Retrieves the name of the business object.
    *
    * @return the name of the business object.
    */
   String getName();

   /**
    * Retrieves the definition of the business object.
    *
    * @return the definition of the business object.
    * May be <tt>null</tt> if the query did not include the
    *   {@link org.eclipse.stardust.engine.api.query.BusinessObjectQuery.Option#WITH_DESCRIPTION WITH_DESCRIPTION} option.
    */
   List<Definition> getItems();

   /**
    * Retrieves the instance values of the business object.
    *
    * @return the instance values of the business object.
    * May be <tt>null</tt> if the query did not include the
    *   {@link org.eclipse.stardust.engine.api.query.BusinessObjectQuery.Option#WITH_VALUES WITH_VALUES} option.
    */
   List<Value> getValues();
}