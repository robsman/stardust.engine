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

import org.eclipse.stardust.engine.core.compatibility.diagram.ArrowKey;
import org.eclipse.stardust.engine.core.compatibility.diagram.ColorKey;
import org.eclipse.stardust.engine.core.compatibility.diagram.LineKey;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElement;


/**
 * Parameterizes a generic links.
 */
public interface ILinkType extends IdentifiableElement
{
   /**
    *
    */
   public String getFirstRole();

   /**
    *
    */
   public void setFirstRole(String firstRole);

   /**
    *
    */
   public String getSecondRole();

   /**
    *
    */
   public void setSecondRole(String firstRole);

   /**
    *
    */
   public Class getFirstClass();

   /**
    *
    */
   public void setFirstClass(Class firstClass);

   /**
    *
    */
   public Class getSecondClass();

   /**
    *
    */
   public void setSecondClass(Class firstClass);

   /**
    *
    */
   public CardinalityKey getFirstCardinality();

   /**
    *
    */
   public void setFirstCardinality(CardinalityKey firstCardinality);

   /**
    *
    */
   public CardinalityKey getSecondCardinality();

   /**
    *
    */
   public void setSecondCardinality(CardinalityKey secondCardinality);

   /**
    * Retrieves the class of the other "end" of the link type.
    */
   public Class getOtherClass(String role);

   /**
    * Retrieves the role of the other "end" of the link type.
    */
   public String getOtherRole(String role);

   // Attributes for the visualisation
   public ArrowKey getFirstArrowType();

   public void setFirstArrowType(ArrowKey arrowKey);

   public ArrowKey getSecondArrowType();

   public void setSecondArrowType(ArrowKey arrowKey);

   public boolean getShowLinkTypeName();

   public void setShowLinkTypeName(boolean isVisible);

   public boolean getShowRoleNames();

   public void setShowRoleNames(boolean isVisible);

   public LineKey getLineType();

   public void setLineType(LineKey lineKey);

   public ColorKey getLineColor();

   public void setLineColor(ColorKey colorKey);

}
