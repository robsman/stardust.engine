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
package org.eclipse.stardust.engine.core.spi.extensions.runtime;

import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.core.compatibility.el.SymbolTable;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;


/**
 * Context object for {@link ExtendedAccessPathEvaluator}
 */
public class AccessPathEvaluationContext
{
   private SymbolTable symbolTable;
   private AccessPoint targetAccessPointDefinition;
   private String targetPath;
   private IActivity activity;
   private boolean ignoreUnknownValueParts;
   private long scopeProcessInstanceOid;

   /**
    * @param processInstance
    * @param targetAccessPointDefinition specifies optional target access point
    * where the evaluation result will be set, it can be used as a hint considering 
    * the result of evaluation 
    */
   public AccessPathEvaluationContext(SymbolTable symbolTable,
         AccessPoint targetAccessPointDefinition)
   {
      this(symbolTable, targetAccessPointDefinition, null, null);
   }

   public AccessPathEvaluationContext(SymbolTable symbolTable,
         AccessPoint targetAccessPointDefinition, String targetPath)
   {
      this(symbolTable, targetAccessPointDefinition, targetPath, null);
   }

   public AccessPathEvaluationContext(SymbolTable symbolTable,
         AccessPoint targetAccessPointDefinition, String targetPath, 
         IActivity activity)
   {
      this.symbolTable = symbolTable;
      this.targetAccessPointDefinition = targetAccessPointDefinition;
      this.targetPath = targetPath;
      this.activity = activity;
      this.ignoreUnknownValueParts = false;
   }

   public AccessPathEvaluationContext(SymbolTable symbolTable, long scopeProcessOid)
   {
      this(symbolTable, null);
      this.scopeProcessInstanceOid = scopeProcessOid;
   }

   public IProcessInstance getProcessInstance()
   {
      if (symbolTable instanceof IProcessInstance)
      {
         return (IProcessInstance) symbolTable;
      }
      ActivityInstance activityInstance = (ActivityInstance) symbolTable.lookupSymbol(PredefinedConstants.ACTIVITY_INSTANCE_ACCESSPOINT);
      return activityInstance == null ? null : ProcessInstanceBean.findByOID(activityInstance.getProcessInstanceOID());
   }

   public AccessPoint getTargetAccessPointDefinition()
   {
      return targetAccessPointDefinition;
   }
   
   public String getTargetPath()
   {
      return targetPath;
   }

   public IActivity getActivity()
   {
      return activity;
   }

   public void setIgnoreUnknownValueParts(boolean ignoreUnknownValueParts)
   {
      this.ignoreUnknownValueParts = ignoreUnknownValueParts;
   }
   
   public boolean isIgnoreUnknownValueParts()
   {
      return this.ignoreUnknownValueParts;
   }

   public long getScopeProcessInstanceOID()
   {
      if (scopeProcessInstanceOid > 0)
      {
         return scopeProcessInstanceOid;
      }
      IProcessInstance pi = getProcessInstance();
      return pi == null ? 0 : pi.getScopeProcessInstanceOID();
   }
}
