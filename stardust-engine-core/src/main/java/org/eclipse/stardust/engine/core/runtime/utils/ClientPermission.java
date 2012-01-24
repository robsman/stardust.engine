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
package org.eclipse.stardust.engine.core.runtime.utils;

import org.eclipse.stardust.engine.core.runtime.utils.ExecutionPermission.Default;
import org.eclipse.stardust.engine.core.runtime.utils.ExecutionPermission.Id;
import org.eclipse.stardust.engine.core.runtime.utils.ExecutionPermission.Scope;

public class ClientPermission
{
   static final ClientPermission NULL = new ClientPermission();
   
   private String id;
   private Scope scope = Scope.model;
   private Default[] defaults = {Default.ADMINISTRATOR};
   private Default[] fixed = {};
   private boolean changeable = true;
   private boolean administratorOverride = true;
   private boolean defer = false;
   private String[] implied = {};
   
   private ClientPermission()
   {
   }
   
   public ClientPermission(ExecutionPermission permission)
   {
      this.id = permission.id().name();
      this.scope = permission.scope();
      this.defaults = permission.defaults();
      this.fixed = permission.fixed();
      this.changeable = permission.changeable();
      this.administratorOverride = permission.administratorOverride();
      this.defer = permission.defer();
      Id[] implied = permission.implied();
      this.implied = new String[implied.length];
      for (int i = 0; i < implied.length; i++)
      {
         this.implied[i] = implied[i].name();
      }
   }

   public ClientPermission(String permission)
   {
      if (permission.startsWith(Permissions.PREFIX))
      {
         permission = permission.substring(Permissions.PREFIX.length());
      }
      int firstDot = permission.indexOf('.');
      this.id = permission.substring(firstDot + 1);
      
      this.scope = Scope.valueOf(permission.substring(0, firstDot));
   }

   public String id()
   {
      return id;
   }

   public Scope scope()
   {
      return scope;
   }

   public Default[] defaults()
   {
      return defaults;
   }

   public Default[] fixed()
   {
      return fixed;
   }

   public boolean changeable()
   {
      return changeable;
   }

   public boolean administratorOverride()
   {
      return administratorOverride;
   }

   public boolean defer()
   {
      return defer;
   }

   public String[] implied()
   {
      return implied;
   }

   @Override
   public String toString()
   {
      return scope.name() + '.' + id;
   }
}
