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
package org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument;

import java.util.LinkedHashSet;
import java.util.Set;

public class Note extends PageAnnotation
{
   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private Integer fontSize;

   private String text;

   private Set<String> textStyle = new LinkedHashSet<String>();

   private transient Set<TextStyle> textStyleCache;

   public Integer getFontSize()
   {
      return fontSize;
   }

   public void setFontSize(Integer fontSize)
   {
      this.fontSize = fontSize;
   }

   public String getText()
   {
      return text;
   }

   public void setText(String text)
   {
      this.text = text;
   }

   public Set<TextStyle> getTextStyle()
   {
      if (this.textStyleCache != null)
      {
         return textStyleCache;
      }

      Set<TextStyle> enumSet = new LinkedHashSet<TextStyle>();

      for (String textStyleString : this.textStyle)
      {
         enumSet.add(TextStyle.valueOf(textStyleString));
      }

      if (this.textStyleCache == null)
      {
         textStyleCache = enumSet;
      }

      return enumSet;
   }

   public void setTextStyle(Set<TextStyle> textStyle)
   {
      this.textStyleCache = null;

      this.textStyle.clear();
      for (TextStyle textStyleEnum : textStyle)
      {
         this.textStyle.add(textStyleEnum.name());
      }
   }

}
