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
package org.eclipse.stardust.engine.core.model.beans;

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.CardinalityKey;
import org.eclipse.stardust.engine.api.model.ILinkType;
import org.eclipse.stardust.engine.core.compatibility.diagram.ArrowKey;
import org.eclipse.stardust.engine.core.compatibility.diagram.ColorKey;
import org.eclipse.stardust.engine.core.compatibility.diagram.LineKey;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElementBean;


/**
 * Parameterizes  generic links.
 */
public class LinkTypeBean extends IdentifiableElementBean
      implements ILinkType
{
   public static ArrowKey DEFAULT_FIRST_ARROW_TYPE = ArrowKey.NO_ARROW;
   public static ArrowKey DEFAULT_SECOND_ARROW_TYPE = ArrowKey.OPEN_TRIANGLE;
   public static ColorKey DEFAULT_LINE_COLOR = ColorKey.DARK_BLUE;
   public static LineKey DEFAULT_LINE_TYPE = LineKey.NORMAL;

   private static final String FIRST_ROLE_ATT = "First Role";
   private String firstRole;

   private static final String SECOND_ROLE_ATT = "Second Role";
   private String secondRole;

   private static final String FIRST_CLASS_NAME_ATT = "First Class";
   private String firstClassName;

   private static final String SECOND_CLASS_NAME_ATT = "Second Class";
   private String secondClassName;

   private static final String FIRST_CARDINALITY_ATT = "First Cardinality";
   private int firstCardinality;

   private static final String SECOND_CARDINALITY_ATT = "Second Cardinality";
   private int secondCardinality;

   private static final String FIRST_ARROW_TYPE_ATT = "First Arrow Type";
   private int firstArrowType;

   private static final String SECOND_ARROW_TYPE_ATT = "Second Arrow Type";
   private int secondArrowType;

   private static final String SHOW_LINK_TYPE_NAME_ATT = "Show Link Type Name";
   private boolean showLinkTypeName = false;

   private static final String SHOW_ROLE_NAMES_ATT = "Show Role Names";
   private boolean showRoleNames = false;

   private static final String LINE_TYPE_ATT = "Line Type";
   private int lineType;

   private static final String LINE_COLOR_ATT = "Line Color";
   private int lineColor;

   LinkTypeBean()
   {
   }

   // @todo (france, ub): add id to signature
   public LinkTypeBean(String name
         , Class firstClass, Class secondClass
         , String firstRole, String secondRole
         , CardinalityKey firstCardinality, CardinalityKey secondCardinality
         , ArrowKey firstArrowType, ArrowKey secondArrowType
         , ColorKey lineColor
         , LineKey lineType
         , boolean showLinkTypeName
         , boolean showRoleNames)
   {
      super(name, name);
      this.firstClassName = firstClass == null ? null : firstClass.getName();
      this.secondClassName = secondClass == null ? null : secondClass.getName();
      this.firstRole = firstRole;
      this.secondRole = secondRole;
      this.firstCardinality = firstCardinality.getValue();
      this.secondCardinality = secondCardinality.getValue();
      this.firstArrowType = firstArrowType.getValue();
      this.secondArrowType = secondArrowType.getValue();
      this.lineColor = lineColor.getValue();
      this.lineType = lineType.getValue();
      this.showLinkTypeName = showLinkTypeName;
      this.showRoleNames = showRoleNames;
   }

   public String toString()
   {
      return "Link Type: " + getId();
   }


   /**
    * // @todo (france, ub): remove when adding id to signature
    */
   public String getId()
   {
      return "Link Type: " + getName();
   }

   /**
    *
    */
   public String getFirstRole()
   {
      return firstRole;
   }

   /**
    *
    */
   public void setFirstRole(String firstRole)
   {
      markModified();

      this.firstRole = firstRole;

   }

   /**
    *
    */
   public String getSecondRole()
   {
      return secondRole;
   }

   /**
    *
    */
   public void setSecondRole(String secondRole)
   {
      markModified();

      this.secondRole = secondRole;
   }

   /**
    *
    */
   public Class getFirstClass()
   {
      return Reflect.getClassFromClassName(firstClassName);
   }

   /**
    *
    */
   public void setFirstClass(Class firstClass)
   {
      markModified();

      firstClassName = firstClass.getName();
   }

   /**
    *
    */
   public Class getSecondClass()
   {
      try
      {
         return Reflect.getClassFromClassName(secondClassName);
      }
      catch (Exception x)
      {
         throw new InternalException(x);
      }
   }

   /**
    *
    */
   public void setSecondClass(Class secondClass)
   {
      markModified();

      secondClassName = secondClass.getName();
   }

   /**
    *
    */
   public CardinalityKey getFirstCardinality()
   {
      return new CardinalityKey(firstCardinality);
   }

   /**
    *
    */
   public void setFirstCardinality(CardinalityKey firstCardinality)
   {
      markModified();

      this.firstCardinality = firstCardinality.getValue();
   }

   /**
    *
    */
   public CardinalityKey getSecondCardinality()
   {
      return new CardinalityKey(secondCardinality);
   }

   /**
    *
    */
   public void setSecondCardinality(CardinalityKey secondCardinality)
   {
      markModified();

      this.secondCardinality = secondCardinality.getValue();
   }

   /**
    * Retrieves the class of the other "end" of the link type.
    */
   public Class getOtherClass(String role)
   {
      if (role.equals(getFirstRole()))
      {
         return getSecondClass();
      }
      else
      {
         return getFirstClass();
      }
   }

   /**
    * Retrieves the role of the other "end" of the link type.
    */
   public String getOtherRole(String role)
   {
      if (role.equals(getFirstRole()))
      {
         return getSecondRole();
      }
      else
      {
         return getFirstRole();
      }
   }

   public ArrowKey getFirstArrowType()
   {
      return new ArrowKey(firstArrowType);
   }

   public void setFirstArrowType(ArrowKey arrowKey)
   {
      firstArrowType = arrowKey.getValue();
   }

   public ArrowKey getSecondArrowType()
   {
      return new ArrowKey(secondArrowType);
   }

   /** */
   public void setSecondArrowType(ArrowKey arrowKey)
   {
      secondArrowType = arrowKey.getValue();
   }

   /** */
   public boolean getShowLinkTypeName()
   {
      return showLinkTypeName;
   }

   /** */
   public void setShowLinkTypeName(boolean isVisible)
   {
      showLinkTypeName = isVisible;
   }

   /** */
   public boolean getShowRoleNames()
   {
      return showRoleNames;
   }

   /** */
   public void setShowRoleNames(boolean isVisible)
   {
      showRoleNames = isVisible;
   }

   /** */
   public LineKey getLineType()
   {
      return new LineKey(lineType);
   }

   /** */
   public void setLineType(LineKey lineKey)
   {
      lineType = lineKey.getValue();
   }

   /** */
   public ColorKey getLineColor()
   {
      return new ColorKey(lineColor);
   }

   /** */
   public void setLineColor(ColorKey colorKey)
   {
      lineColor = colorKey.getValue();
   }
}
