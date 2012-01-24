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
package org.eclipse.stardust.engine.extensions.dms.data;

import java.io.Serializable;

import org.eclipse.stardust.common.StringKey;
import org.eclipse.stardust.engine.api.runtime.Privilege;



/**
 * @author rsauer
 * @version $Revision: 24736 $
 */
public class DmsPrivilege extends StringKey implements Privilege, Serializable 
{
   private static final long serialVersionUID = 1L;
   
   public static final DmsPrivilege READ_PRIVILEGE = new DmsPrivilege("READ");
   public static final DmsPrivilege MODIFY_PRIVILEGE = new DmsPrivilege("MODIFY");
   public static final DmsPrivilege CREATE_PRIVILEGE = new DmsPrivilege("CREATE");
   public static final DmsPrivilege DELETE_PRIVILEGE = new DmsPrivilege("DELETE");
   public static final DmsPrivilege DELETE_CHILDREN_PRIVILEGE = new DmsPrivilege("DELETE_CHILDREN");
   public static final DmsPrivilege READ_ACL_PRIVILEGE = new DmsPrivilege("READ_ACL");
   public static final DmsPrivilege MODIFY_ACL_PRIVILEGE = new DmsPrivilege("MODIFY_ACL");
   public static final DmsPrivilege ALL_PRIVILEGES = new DmsPrivilege("ALL");
   
   private DmsPrivilege(String name)
   {
      super(name, name);
   }

   public String getName()
   {
      return getId();
   }
}
