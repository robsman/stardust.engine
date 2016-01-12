/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Roland.Stamm (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.beans;

import java.util.Date;

import org.eclipse.stardust.engine.core.persistence.IdentifiablePersistent;

public interface IRuntimeArtifact extends IdentifiablePersistent
{

   public String getReferenceId();

   public void setReferenceId(String referenceId);

   public String getArtifactTypeId();

   public String getArtifactId();

   public String getArtifactName();

   public Date getValidFrom();

   public short getPartition();

}