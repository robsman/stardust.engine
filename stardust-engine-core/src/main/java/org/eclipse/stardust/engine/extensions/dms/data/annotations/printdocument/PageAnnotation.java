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

import java.util.Date;

public abstract class PageAnnotation extends Identifiable
{

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private Date createDate;

   private String createdByAuthor;

   private Date modificationDate;

   private String modifiedByAuthor;

   private String color;

   private Integer xCoordinate;

   private Integer yCoordinate;

   private Integer height;

   private Integer width;

   private int pageNumber;

   private int pageRelativeRotation;

   public Date getCreateDate()
   {
      return createDate;
   }

   public void setCreateDate(Date createDate)
   {
      this.createDate = createDate;
   }

   public String getCreatedByAuthor()
   {
      return createdByAuthor;
   }

   public void setCreatedByAuthor(String createdByAuthor)
   {
      this.createdByAuthor = createdByAuthor;
   }

   public Date getModificationDate()
   {
      return modificationDate;
   }

   public void setModificationDate(Date modificationDate)
   {
      this.modificationDate = modificationDate;
   }

   public String getModifiedByAuthor()
   {
      return modifiedByAuthor;
   }

   public void setModifiedByAuthor(String modifiedByAuthor)
   {
      this.modifiedByAuthor = modifiedByAuthor;
   }

   public String getColor()
   {
      return color;
   }

   public void setColor(String color)
   {
      this.color = color;
   }

   public Integer getxCoordinate()
   {
      return xCoordinate;
   }

   public void setxCoordinate(Integer xCoordinate)
   {
      this.xCoordinate = xCoordinate;
   }

   public Integer getyCoordinate()
   {
      return yCoordinate;
   }

   public void setyCoordinate(Integer yCoordinate)
   {
      this.yCoordinate = yCoordinate;
   }

   public Integer getHeight()
   {
      return height;
   }

   public void setHeight(Integer height)
   {
      this.height = height;
   }

   public Integer getWidth()
   {
      return width;
   }

   public void setWidth(Integer width)
   {
      this.width = width;
   }

   public int getPageNumber()
   {
      return pageNumber;
   }

   public void setPageNumber(int pageNumber)
   {
      this.pageNumber = pageNumber;
   }

   public int getPageRelativeRotation()
   {
      return pageRelativeRotation;
   }

   public void setPageRelativeRotation(int pageRelativeRotation)
   {
      this.pageRelativeRotation = pageRelativeRotation;
   }

}
