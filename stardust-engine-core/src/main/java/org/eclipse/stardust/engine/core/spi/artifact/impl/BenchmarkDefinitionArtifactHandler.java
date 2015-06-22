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
package org.eclipse.stardust.engine.core.spi.artifact.impl;

import org.eclipse.stardust.engine.api.runtime.ArtifactType;
import org.eclipse.stardust.engine.api.runtime.DeployedRuntimeArtifact;
import org.eclipse.stardust.engine.api.runtime.RuntimeArtifact;
import org.eclipse.stardust.engine.core.benchmark.BenchmarkUtils;
import org.eclipse.stardust.engine.core.spi.artifact.IArtifactHandler;

/**
 * This {@link IArtifactHandler} handles artifacts of type
 * {@link BenchmarkDefinitionArtifactType}.
 * <p>
 * The handled artifacts are all of content type
 * {@link BenchmarkDefinitionArtifactHandler#MIME_TYPE}.<br>
 * Internal caches for benchmark definitions are flushed if a benchmark definition runtime
 * artifact is overwritten or deleted.
 *
 * @author Roland.Stamm
 */
public class BenchmarkDefinitionArtifactHandler
      implements IArtifactHandler, IArtifactHandler.Factory
{

   public static final ArtifactType ARTIFACT_TYPE = new BenchmarkDefinitionArtifactType();

   public static final String MIME_TYPE = "application/json";

   @Override
   public IArtifactHandler getInstance()
   {
      return new BenchmarkDefinitionArtifactHandler();
   }

   @Override
   public ArtifactType getArtifactType()
   {
      return ARTIFACT_TYPE;
   }

   @Override
   public String getArtifactContentType(RuntimeArtifact runtimeArtifact)
   {
      // all benchmarks are in json format.
      return MIME_TYPE;
   }

   @Override
   public RuntimeArtifact preProcess(RuntimeArtifact runtimeArtifact)
   {
      return runtimeArtifact;
   }

   @Override
   public void afterOverwrite(DeployedRuntimeArtifact deployedRuntimeArtifact)
   {
      BenchmarkUtils.removeBenchmarkFromCache(deployedRuntimeArtifact.getOid());
   }

   @Override
   public void beforeDelete(DeployedRuntimeArtifact deployedRuntimeArtifact)
   {}

   @Override
   public void afterDelete(long oid)
   {
      BenchmarkUtils.removeBenchmarkFromCache(oid);
   }

}
