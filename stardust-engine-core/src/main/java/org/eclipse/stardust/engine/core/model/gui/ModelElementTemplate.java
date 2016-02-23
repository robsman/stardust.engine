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
package org.eclipse.stardust.engine.core.model.gui;

import java.io.Serializable;

import org.eclipse.stardust.common.AttributeHolderImpl;
import org.eclipse.stardust.common.AttributeManager;
import org.eclipse.stardust.common.IAttributeManager;
import org.eclipse.stardust.engine.core.compatibility.gui.ItemStateListener;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElement;
import org.eclipse.stardust.engine.core.model.utils.ModelElement;
import org.eclipse.stardust.engine.core.model.utils.RootElement;


public class ModelElementTemplate extends AttributeHolderImpl implements IdentifiableElement
{
   private static final long serialVersionUID = -5573734929247230203L;

   private String id;
   private String name;
   private String description;
   private int oid;
   private ItemStateListener listener;
   private boolean predefined;

   private IAttributeManager runtimeAttributes;

   public ModelElementTemplate()
   {
   }

   public ModelElementTemplate(ModelElement element)
   {
      oid = element.getElementOID();
      description = element.getDescription();
      setAllAttributes(element.getAllAttributes());
   }

   public ModelElementTemplate(IdentifiableElement element)
   {
      id = element.getId();
      name = element.getName();
      oid = element.getElementOID();
      description = element.getDescription();
      setAllAttributes(element.getAllAttributes());
   }

   public String getId()
   {
      return id;
   }

   public void setId(String id)
   {
      this.id = id;
      firePropertyChanged();
   }

   public String getUniqueId()
   {
      return getId();
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
      firePropertyChanged();
   }

   public String getDescription()
   {
      return description;
   }

   public void setDescription(String description)
   {
      this.description = description;
      firePropertyChanged();
   }

   public void markModified()
   {
   }

   public void delete()
   {
   }

   public RootElement getModel()
   {
      return null;
   }

   public void setParent(ModelElement parent)
   {
   }

   public ModelElement getParent()
   {
      return null;
   }

   public int getElementOID()
   {
      return oid;
   }

   public void register(int oid)
   {
   }

   public long getOID()
   {
      return 0;
   }

   public void setElementOID(int elementOID)
   {
      this.oid = elementOID;
   }

   // @todo (france, ub): ????
   public boolean isTransient()
   {
      return predefined;
   }

   public boolean isPredefined()
   {
      return predefined;
   }

   public void setPredefined(boolean predefined)
   {
      this.predefined = predefined;
   }

   public void setTransient(boolean predefined)
   {
      this.predefined = predefined;
   }

   protected void firePropertyChanged()
   {
      if (listener != null)
      {
         listener.updateItem(this);
      }
   }

   public ItemStateListener getListener()
   {
      return listener;
   }

   public void setItemStateListener(ItemStateListener listener)
   {
      this.listener = listener;
   }

   public String toString()
   {
      return name == null ? id == null ? super.toString() : id : name;
   }

   public Object getRuntimeAttribute(String name)
   {
      return (null != runtimeAttributes) ? runtimeAttributes.getAttribute(name) : null;
   }

   public synchronized Object setRuntimeAttribute(String name, Object value)
   {
      if (null == runtimeAttributes)
      {
         this.runtimeAttributes = new AttributeManager();
      }
      
      return runtimeAttributes.setAttribute(name, (Serializable) value);
   }
   
}
