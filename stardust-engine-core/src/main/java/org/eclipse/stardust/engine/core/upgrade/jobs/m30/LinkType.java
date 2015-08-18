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
package org.eclipse.stardust.engine.core.upgrade.jobs.m30;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class LinkType extends IdentifiableElement
{
   private String sourceClassName;
   private String targetClassName;
   private String sourceRoleName;
   private String targetRoleName;
   private String sourceCardinality;
   private String targetCardinality;
   private String sourceSymol;
   private String targetSymbol;
   private String lineColor;
   private String lineType;
   private boolean showLinkTypeName;
   private boolean showRoleNames;

   public LinkType(String name, String sourceClassName, String targetClassName,
         String sourceRoleName, String targetRoleName,
         String sourceCardinality, String targetCardinality,
         String sourceSymol, String targetSymbol,
         String lineColor, String lineType,
         boolean showLinkTypeName, boolean showRoleNames, int oid, Model model)
   {
      // @todo (france, ub): class names have to be converted here from 2.7 to 3.0
      super(name, name, null);
      this.sourceClassName = sourceClassName;
      this.targetClassName = targetClassName;
      this.sourceRoleName = sourceRoleName;
      this.targetRoleName = targetRoleName;
      this.sourceCardinality = sourceCardinality;
      this.targetCardinality = targetCardinality;
      this.sourceSymol = sourceSymol;
      this.targetSymbol = targetSymbol;
      this.lineColor = lineColor;
      this.lineType = lineType;
      this.showLinkTypeName = showLinkTypeName;
      this.showRoleNames = showRoleNames;
      model.register(this, oid);
   }

   public String getLineColor()
   {
      return lineColor;
   }

   public String getLineType()
   {
      return lineType;
   }

   public boolean isShowLinkTypeName()
   {
      return showLinkTypeName;
   }

   public boolean isShowRoleNames()
   {
      return showRoleNames;
   }

   public String getSourceCardinality()
   {
      return sourceCardinality;
   }

   public String getSourceClassName()
   {
      return sourceClassName;
   }

   public String getSourceRoleName()
   {
      return sourceRoleName;
   }

   public String getSourceSymol()
   {
      return sourceSymol;
   }

   public String getTargetCardinality()
   {
      return targetCardinality;
   }

   public String getTargetClassName()
   {
      return targetClassName;
   }

   public String getTargetRoleName()
   {
      return targetRoleName;
   }

   public String getTargetSymbol()
   {
      return targetSymbol;
   }
}
