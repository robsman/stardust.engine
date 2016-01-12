/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    roland.stamm (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.spi.artifact.impl;

import org.eclipse.stardust.engine.api.runtime.ArtifactType;

/**
 * The ArtifactType of a benchmark definition.
 * <p>
 * Benchmark definitions contain categories and conditions for process and activity
 * instances.
 *
 * @author Roland.Stamm
 */
public class BenchmarkDefinitionArtifactType implements ArtifactType
{

   private static final long serialVersionUID = 5661588569220278066L;

   public static final String TYPE_ID = "benchmark-definition";

   @Override
   public String getId()
   {
      return TYPE_ID;
   }

}
