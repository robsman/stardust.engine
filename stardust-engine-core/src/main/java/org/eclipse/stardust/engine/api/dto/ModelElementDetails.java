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
package org.eclipse.stardust.engine.api.dto;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.stardust.engine.core.model.utils.IdentifiableElement;
import org.eclipse.stardust.engine.core.model.utils.ModelElement;
import org.eclipse.stardust.engine.core.model.utils.ModelUtils;
import org.eclipse.stardust.engine.core.runtime.beans.IAuditTrailPartition;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class ModelElementDetails implements org.eclipse.stardust.engine.api.model.ModelElement
{
   private static final long serialVersionUID = -6777155032013205956L;
   
   private final short partitionOid;
   private final String partitionId;
   
   private final int modelOID;
   private final int elementOID;
   
   private final String namespace;
   private final String id;
   private final String name;
   private final String qualifiedId;
   private final Map attributes;
   private String description;

   public ModelElementDetails(int modelOID, int elementOID, String id, String name, String namespace, String description)
   {
      IAuditTrailPartition partition = SecurityProperties.getPartition();
      this.partitionOid = partition.getOID();
      this.partitionId = partition.getId();
      
      this.modelOID = modelOID;
      this.elementOID = elementOID;
      this.qualifiedId = '{' + namespace + '}' + id;
      this.namespace = namespace;
      this.id = id;
      this.name = name;
      this.description = description;

      this.attributes = new HashMap();
   }

   public ModelElementDetails(ModelElement element, String id, String name, String description)
   {
      this(ModelUtils.nullSafeGetModelOID(element), element.getElementOID(), id, name,
           ModelUtils.nullSafeGetModelNamespace(element), description);

      // @todo (france, ub): deepcopy
      attributes.putAll(element.getAllAttributes());
   }

   public ModelElementDetails(IdentifiableElement element)
   {
      this(element, element.getId(), element.getName(), element.getDescription());
   }

   protected ModelElementDetails(ModelElementDetails template)
   {
      this.partitionOid = template.partitionOid;
      this.partitionId = template.partitionId;
      
      this.modelOID = template.modelOID;
      this.elementOID = template.elementOID;
      this.namespace = template.namespace;
      this.id = template.id;
      this.name = template.name;
      this.description = template.description;
      
      this.attributes = template.attributes;
      this.qualifiedId = template.qualifiedId;
   }

   public short getPartitionOID()
   {
      return partitionOid;
   }

   public String getPartitionId()
   {
      return partitionId;
   }
   
   public int getModelOID()
   {
      return modelOID;
   }

   public int getElementOID()
   {
      return elementOID;
   }

   public String getNamespace()
   {
      return namespace;
   }

   public String getId()
   {
      return id;
   }

   public String getName()
   {
      return name;
   }
   
   public String getQualifiedId()
   {
      return qualifiedId;
   }

   public String getDescription()
   {
      return description;
   }   
   
   public Map getAllAttributes()
   {
      return Collections.unmodifiableMap(attributes);
   }

   public Object getAttribute(String name)
   {
      return attributes.get(name);
   }

   protected void injectAttributes(Map attributes)
   {
      this.attributes.putAll(attributes);
   }

   protected void clearAttributes()
   {
      attributes.clear();
   }

   public boolean equals(Object other)
   {
      if (other == null)
      {
         return false;
      }
      if (! (other instanceof org.eclipse.stardust.engine.api.model.ModelElement))
      {
         return false;
      };
      org.eclipse.stardust.engine.api.model.ModelElement otherElement = (org.eclipse.stardust.engine.api.model.ModelElement) other;
      if (otherElement.getElementOID() == getElementOID()
          && otherElement.getModelOID() == getModelOID())
      {
         return true;
      }
      return false;
   }
}