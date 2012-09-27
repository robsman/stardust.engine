/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.cli.sysconsole.patch;

public class RuntimeOidPatch {

   private final short partitionOid;
   private final long incorrectRuntimeOid;
   private final long fixedRuntimeOid;

   public RuntimeOidPatch(short partitionOid, long incorrectRuntimeOid, long fixedRuntimeOid)
   {
      this.partitionOid = partitionOid;
      this.incorrectRuntimeOid = incorrectRuntimeOid;
      this.fixedRuntimeOid = fixedRuntimeOid;   
   }

   public short getPartitionOid() {
      return partitionOid;
   }

   public long getIncorrectRuntimeOid() {
      return incorrectRuntimeOid;
   }

   public long getFixedRuntimeOid() {
      return fixedRuntimeOid;
   }
}