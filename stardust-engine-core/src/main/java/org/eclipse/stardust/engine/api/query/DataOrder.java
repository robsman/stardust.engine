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
package org.eclipse.stardust.engine.api.query;

/**
 * Criterion for ordering elements resulting from a query according to workflow data,
 * either with ascending or descending values.
 *
 * @author rsauer
 * @version $Revision$
 *
 * @see OrderCriteria
 */
public final class DataOrder extends AbstractSingleOrderCriterion
{
   private static final long serialVersionUID = 2L;

   private final String dataID;
   private final String attributeName;

   /**
    * Initializes the criterion to order according to ascending values of the given
    * workflow data.
    * <p />
    * The meaning of order depends on the type of the workflow data, i.e. arithmetic or
    * lexical order.
    *
    * @param dataID The ID of the workflow data to order according to
    *
    * @see #DataOrder(String, boolean)
    */
   public DataOrder(String dataID)
   {
      this(dataID, true);
   }

   /**
    * Initializes the criterion to order according to ascending values of the given
    * workflow data.
    * <p />
    * The meaning of order depends on the type of the workflow data, i.e. arithmetic or
    * lexical order.
    *
    * @param dataID The ID of the workflow data to order according to
    * @param attributeName The name of the data attribute to search for (XPath, etc.)
    *
    * @see #DataOrder(String, boolean)
    */
   public DataOrder(String dataID, String attributeName)
   {
      this(dataID, attributeName, true);
   }

   /**
    * Initializes the criterion to order according to ascending or descending values of
    * the given workflow data.
    * <p />
    * The meaning of order depends on the type of the workflow data, i.e. arithmetic or
    * lexical order.
    *
    * @param dataID The ID of the workflow data to according to.
    * @param ascending Flag indicating if ordering has to be performed along ascending or
    *                  descending values.
    *
    * @see #DataOrder(String)
    */
   public DataOrder(String dataID, boolean ascending)
   {
      this(dataID, null, ascending);
   }

   /**
    * Initializes the criterion to order according to ascending or descending values of
    * the given workflow data.
    * <p />
    * The meaning of order depends on the type of the workflow data, i.e. arithmetic or
    * lexical order.
    *
    * @param dataID The ID of the workflow data to according to.
    * @param attributeName The name of the data attribute to search for (XPath, etc.)
    * @param ascending Flag indicating if ordering has to be performed along ascending or
    *                  descending values.
    *
    * @see #DataOrder(String)
    */
   public DataOrder(String dataID, String attributeName, boolean ascending)
   {
      super(ascending);
      this.dataID = dataID;
      this.attributeName = attributeName;
   }

   /**
    * Returns the ID of the workflow data containing the values to be used for ordering.
    *
    * @return The workflow data ID.
    */
   public String getDataID()
   {
      return dataID;
   }

   /**
    * Returns
    * @return
    */
   public String getAttributeName()
   {
      return attributeName;
   }

   /**
    * Returns the name of the data attribute or an empty string if the name is null.
    * @return
    */
   public String getNormalizedAttributeName()
   {
      return attributeName == null ? "" : attributeName;
   }

   public Object accept(OrderEvaluationVisitor visitor, Object context)
   {
      return visitor.visit(this, context);
   }

   @Override
   public String toString()
   {
      return "DataOrder: [" + dataID + ", " + attributeName + "]";
   }
}
