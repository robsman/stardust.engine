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

import java.util.List;
import java.util.Map;

import org.eclipse.stardust.engine.api.model.ConditionalPerformer;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.ParticipantType;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;


public class ResolvedConditionalPerformerDetails implements ConditionalPerformer
{
   private static final long serialVersionUID = -2210183980349449318L;
   private final ConditionalPerformer delegate;
   private final Participant resolvedPerformer;
   
   public ResolvedConditionalPerformerDetails(ConditionalPerformer delegate,
         Participant resolvedPerformer)
   {
      this.delegate = delegate;
      this.resolvedPerformer = resolvedPerformer;
   }
   
   public Participant getResolvedPerformer()
   {
      return resolvedPerformer;
   }

   //
   // methods delegated to decorated conditional performer
   //

   public long getRuntimeElementOID()
   {
      return delegate.getRuntimeElementOID();
   }
   
   public Map getAllAttributes()
   {
      return delegate.getAllAttributes();
   }

   public List<Organization> getAllSuperOrganizations()
   {
      return delegate.getAllSuperOrganizations();
   }

   public Object getAttribute(String name)
   {
      return delegate.getAttribute(name);
   }

   public short getPartitionOID()
   {
      return delegate.getPartitionOID();
   }

   public String getPartitionId()
   {
      return delegate.getPartitionId();
   }
   
   public int getElementOID()
   {
      return delegate.getElementOID();
   }

   public String getId()
   {
      return delegate.getId();
   }

   public String getNamespace()
   {
      return delegate.getNamespace();
   }

   public int getModelOID()
   {
      return delegate.getModelOID();
   }

   public String getDescription()
   {
      return delegate.getDescription();
   }   
   
   public String getName()
   {
      return delegate.getName();
   }

   public String getQualifiedId()
   {
      return delegate.getQualifiedId();
   }

   public ParticipantType getPerformerKind()
   {
      return delegate.getPerformerKind();
   }

   public DepartmentInfo getDepartment()
   {
      return delegate.getDepartment();
   }

   public boolean definesDepartmentScope()
   {
      return delegate.definesDepartmentScope();
   }

   public boolean isDepartmentScoped()
   {
      return delegate.isDepartmentScoped();
   }
}