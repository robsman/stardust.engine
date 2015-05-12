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
package org.eclipse.stardust.engine.core.spi.artifact;

import java.text.MessageFormat;
import java.util.*;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailLogger;
import org.eclipse.stardust.engine.core.runtime.beans.IRuntimeArtifact;
import org.eclipse.stardust.engine.core.runtime.beans.RuntimeArtifactBean;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;

/**
 * Manages everything around {@link RuntimeArtifact} deploying, overwriting, retrieving
 * and deleting.
 * <p>
 * New {@link ArtifactType} can be supported by implementing the SPI {@link IArtifactHandler}.
 *
 * @author Roland.Stamm
 */
public class ArtifactManager
{
   private static final String DELETE_ARTIFACT_MESSAGE = "Deleted runtime artifact ''{0}'' (oid: {1}, artifactTypeId: {2}, validFrom: {3})";

   private static final String DEPLOY_ARTIFACT_MESSAGE = "Deployed runtime artifact ''{0}'' (oid: {1}, artifactTypeId: {2}, validFrom: {3})";

   private Map<String, IArtifactHandler> handlers = CollectionUtils.newHashMap();

   private ArrayList<ArtifactType> artifactTypes = CollectionUtils.newArrayList();

   private IArtifactStore artifactStore = new DmsArtifactStore();

   public ArtifactManager()
   {
      ServiceLoader<IArtifactHandler.Factory> loader = ServiceLoader
            .load(IArtifactHandler.Factory.class);
      Iterator<IArtifactHandler.Factory> loaderIterator = loader.iterator();
      while (loaderIterator.hasNext())
      {
         IArtifactHandler.Factory handlerFactory = (IArtifactHandler.Factory) loaderIterator
               .next();
         IArtifactHandler handler = handlerFactory.getInstance();
         handlers.put(handler.getArtifactType().getId(), handler);
         artifactTypes.add(handler.getArtifactType());
      }
   }

   /**
    * Returns a list of supported artifact types.
    * <p>
    *
    * The {@link ArtifactType#getId()} is used to identify the {@link ArtifactType} for a
    * {@link RuntimeArtifact}.
    *
    * @return The supported artifact types.
    */
   public List<ArtifactType> getSupportedArtifactTypes()
   {
      return Collections.unmodifiableList(artifactTypes);
   }

   /**
    * Deploys a new artifact with a new oid.
    * <p>
    * If an artifact with the same validFrom date already exists,
    * the newly deployed artifact takes priority when querying for active artifacts.
    *
    * @param runtimeArtifact The new artifact.
    * @return The deployed artifact including an assigned oid.
    */
   public DeployedRuntimeArtifact deployArtifact(RuntimeArtifact runtimeArtifact)
   {
      IArtifactHandler handler = getHandler(runtimeArtifact.getArtifactTypeId());

      // pre process
      RuntimeArtifact processedRuntimeArtifact = handler.preProcess(runtimeArtifact);

      RuntimeArtifactBean runtimeArtifactBean = new RuntimeArtifactBean(
            processedRuntimeArtifact);

      String contentType = handler.getArtifactContentType(processedRuntimeArtifact);
      String referenceId = artifactStore.storeContent(runtimeArtifactBean, processedRuntimeArtifact.getContent(), contentType);

      runtimeArtifactBean.setReferenceId(referenceId);

      logArtifactOperation(DEPLOY_ARTIFACT_MESSAGE, runtimeArtifactBean);

      return new DeployedRuntimeArtifactDetails(runtimeArtifactBean);
   }

   /**
    * Overwrites only content of a specified already deployed artifact.
    * Other fields cannot be changed.
    *
    * @param oid The oid of the artifact.
    * @param runtimeArtifact The new artifact.
    * @return The updated artifact.
    */
   public DeployedRuntimeArtifact overwriteArtifact(long oid,
         RuntimeArtifact runtimeArtifact)
   {
      IArtifactHandler handler = getHandler(runtimeArtifact.getArtifactTypeId());

      RuntimeArtifactBean runtimeArtifactBean = RuntimeArtifactBean.findByOid(oid);

      if (runtimeArtifactBean == null)
      {
         throw new RuntimeException("RuntimeArtifact with oid '"+ oid +"' not found.");
      }

      // pre process
      RuntimeArtifact processedRuntimeArtifact = handler.preProcess(runtimeArtifact);

      String contentType = handler.getArtifactContentType(processedRuntimeArtifact);
      String referenceId = artifactStore.storeContent(runtimeArtifactBean, processedRuntimeArtifact.getContent(), contentType);

      runtimeArtifactBean.setReferenceId(referenceId);

      logArtifactOperation(DEPLOY_ARTIFACT_MESSAGE, runtimeArtifactBean);

      return new DeployedRuntimeArtifactDetails(runtimeArtifactBean);
   }

   /**
    * Retrieves the artifact by the unique oid.
    *
    * @param oid The oid of the artifact.
    * @return The artifact.
    */
   public RuntimeArtifact getArtifact(long oid)
   {
      // retrieve by oid
      IRuntimeArtifact runtimeArtifactBean = RuntimeArtifactBean.findByOid(oid);

      return getArtifactWithContent(runtimeArtifactBean);
   }

   /**
    * Retrieves the artifact that is currently active.
    *
    * @param artifactTypeId The type of the artifact.
    * @param artifactId The id of the artifact.
    * @return The artifact that is currently active.
    */
   public RuntimeArtifact getActiveArtifact(String artifactTypeId, String artifactId)
   {
      // retrieve currently valid
      IRuntimeArtifact runtimeArtifactBean = RuntimeArtifactBean.findActive(
            artifactTypeId, artifactId, TimestampProviderUtils.getTimeStampValue());

      return getArtifactWithContent(runtimeArtifactBean);
   }

   /**
    * Retrieves the artifact that is active at the specified point in time.
    *
    * @param artifactTypeId The type of the artifact.
    * @param artifactId The id of the artifact.
    * @param date The point in time where the artifact is active at.
    * @return The artifact that is active at the specified point in time.
    */
   public RuntimeArtifact getActiveArtifactAt(String artifactTypeId, String artifactId, Date date)
   {
      // retrieve valid at date
      IRuntimeArtifact runtimeArtifactBean = RuntimeArtifactBean.findActive(
            artifactTypeId, artifactId, date.getTime());

      return getArtifactWithContent(runtimeArtifactBean);
   }

   /**
    * Deleted a deployed artifact by oid.
    *
    * @param oid The oid of the artifact
    */
   public void deleteArtifact(long oid)
   {

      // delete by oid
      RuntimeArtifactBean runtimeArtifactBean = RuntimeArtifactBean.findByOid(oid);

      if (runtimeArtifactBean == null)
      {
         throw new RuntimeException("RuntimeArtifact with oid '"+ oid +"' not found.");
      }

      IArtifactHandler handler = getHandler(runtimeArtifactBean.getArtifactTypeId());

      // should throw exception if delete is not allowed.
      handler.beforeDelete(new DeployedRuntimeArtifactDetails(runtimeArtifactBean));

      artifactStore.removeContent(runtimeArtifactBean.getReferenceId());

      logArtifactOperation(DELETE_ARTIFACT_MESSAGE, runtimeArtifactBean);

      runtimeArtifactBean.delete();
   }

   private IArtifactHandler getHandler(String artifactType)
   {
      IArtifactHandler artifacthandler = handlers.get(artifactType);
      if (artifacthandler == null)
      {
         throw new RuntimeException("ArtifactType not supported.");
      }
      return artifacthandler;
   }

   private RuntimeArtifact getArtifactWithContent(
         IRuntimeArtifact runtimeArtifactBean)
   {
      RuntimeArtifact deployedArtifact = null;
      if (runtimeArtifactBean != null)
      {
         // resolve content
         byte[] content = artifactStore.retrieveContent(runtimeArtifactBean.getReferenceId());

         deployedArtifact = new RuntimeArtifact(runtimeArtifactBean.getArtifactTypeId(),
               runtimeArtifactBean.getArtifactId(),
               runtimeArtifactBean.getArtifactName(), content,
               runtimeArtifactBean.getValidFrom());
      }
      return deployedArtifact;
   }

   private static void logArtifactOperation(String pattern, RuntimeArtifactBean runtimeArtifactBean)
   {
      final String logMessage = MessageFormat.format(pattern, runtimeArtifactBean.getArtifactId(),
            runtimeArtifactBean.getOID(),
            runtimeArtifactBean.getArtifactTypeId(),
            runtimeArtifactBean.getValidFrom().getTime());
      AuditTrailLogger.getInstance(LogCode.ENGINE).info(logMessage);
   }

}
