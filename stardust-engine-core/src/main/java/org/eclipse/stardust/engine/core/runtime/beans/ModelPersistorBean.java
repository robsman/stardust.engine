/*******************************************************************************
 * Copyright (c) 2011, 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.beans;

import java.io.StringReader;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;

import javax.xml.transform.OutputKeys;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.IModelPersistor;
import org.eclipse.stardust.engine.core.model.beans.DefaultXMLReader;
import org.eclipse.stardust.engine.core.model.beans.IConfigurationVariablesProvider;
import org.eclipse.stardust.engine.core.model.beans.PrefStoreAwareConfigurationVariablesProvider;
import org.eclipse.stardust.engine.core.model.beans.XMLConstants;
import org.eclipse.stardust.engine.core.model.utils.Identifiable;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryExtension;
import org.eclipse.stardust.engine.core.persistence.jdbc.IdentifiablePersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.utils.XmlUtils;
import org.eclipse.stardust.engine.core.thirdparty.encoding.Text;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;

import org.w3c.dom.Document;



/**
 * Stores the model information in a single XML clob.
 */
public class ModelPersistorBean extends IdentifiablePersistentBean implements IModelPersistor, Identifiable
{
   public static final String FIELD__OID = IdentifiablePersistentBean.FIELD__OID;
   public static final String FIELD__ID = "id";
   public static final String FIELD__NAME = "name";
   public static final String FIELD__VALID_FROM = "validFrom";
   public static final String FIELD__VALID_TO = "validTo";
   public static final String FIELD__PREDECESSOR = "predecessor";
   public static final String FIELD__DEPLOYMENT_COMMENT = "deploymentComment";
   public static final String FIELD__DEPLOYMENT_STAMP = "deploymentStamp";
   public static final String FIELD__VERSION = "version";
   public static final String FIELD__REVISION = "revision";
   public static final String FIELD__DISABLED = "disabled";
   public static final String FIELD__PARTITION = "partition";

   public static final FieldRef FR__OID = new FieldRef(ModelPersistorBean.class, FIELD__OID);
   public static final FieldRef FR__ID = new FieldRef(ModelPersistorBean.class, FIELD__ID);
   public static final FieldRef FR__NAME = new FieldRef(ModelPersistorBean.class, FIELD__NAME);
   public static final FieldRef FR__VALID_FROM = new FieldRef(ModelPersistorBean.class, FIELD__VALID_FROM);
   public static final FieldRef FR__VALID_TO = new FieldRef(ModelPersistorBean.class, FIELD__VALID_TO);
   public static final FieldRef FR__PREDECESSOR = new FieldRef(ModelPersistorBean.class, FIELD__PREDECESSOR);
   public static final FieldRef FR__DEPLOYMENT_COMMENT = new FieldRef(ModelPersistorBean.class, FIELD__DEPLOYMENT_COMMENT);
   public static final FieldRef FR__DEPLOYMENT_STAMP = new FieldRef(ModelPersistorBean.class, FIELD__DEPLOYMENT_STAMP);
   public static final FieldRef FR__VERSION = new FieldRef(ModelPersistorBean.class, FIELD__VERSION);
   public static final FieldRef FR__REVISION = new FieldRef(ModelPersistorBean.class, FIELD__REVISION);
   public static final FieldRef FR__DISABLED = new FieldRef(ModelPersistorBean.class, FIELD__DISABLED);
   public static final FieldRef FR__PARTITION = new FieldRef(ModelPersistorBean.class, FIELD__PARTITION);

   public static final String TABLE_NAME = "model";
   public static final String DEFAULT_ALIAS = "m";
   public static final String PK_FIELD = FIELD__OID;
   private static final String PK_SEQUENCE = "model_seq";
   public static final String[] model_idx1_UNIQUE_INDEX = new String[]{FIELD__OID};

   private static final int id_COLUMN_LENGTH = 50;
   private String id;
   private String name;
   private Date validFrom;
   private Date validTo;
   private long predecessor;
   private String deploymentComment;
   private Date deploymentStamp;
   private String version;
   private long revision;
   private long disabled;
   private AuditTrailPartitionBean partition;

   /**
    * Used for caching the model loaded by fetchModel(), so that subsequent invocations in the same transaction
    * will return the already loaded model.
    */
   private transient IModel model;
   private transient IConfigurationVariablesProvider confVarProvider;

   /**
    * Finds a model version deployed as a model persistor by its OID.
    */
   public static ModelPersistorBean findByModelOID(long oid)
   {
      ModelPersistorBean result = (ModelPersistorBean) SessionFactory.getSession(
            SessionFactory.AUDIT_TRAIL).findFirst(
                  ModelPersistorBean.class,
                  QueryExtension.where(Predicates.isEqual(FR__OID, oid)));

      if (result == null)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.MDL_UNKNOWN_MODEL_OID.raise(oid), oid);
      }
      return result;
   }

   /**
    * Finds all versions of a model deployed as a model persistors.
    */
   public static Iterator findAll(short partitionOid)
   {
      return SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).getVector(
            ModelPersistorBean.class, //
            QueryExtension.where( //
                  Predicates.isEqual(ModelPersistorBean.FR__PARTITION, partitionOid))) //
            .iterator();
   }

   public static int getMaxIdLength()
   {
      return id_COLUMN_LENGTH;
   }

   public ModelPersistorBean(Date validFrom, String comment, int predecessorOID, AuditTrailPartitionBean partition)
   {
      this.validFrom = validFrom;
      this.validTo = null;
      this.deploymentComment = comment;
      this.deploymentStamp = TimestampProviderUtils.getTimeStamp();
      this.disabled = 0;
      this.predecessor = predecessorOID;
      this.partition = partition;
      SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).cluster(this);
   }

   /**
    * Default constructor for persistence framework.
    */
   public ModelPersistorBean()
   {
   }

   public void modify(String comment)
   {
      this.deploymentComment = comment;
      this.deploymentStamp = TimestampProviderUtils.getTimeStamp();
   }

   public long getPredecessorOID()
   {
      fetch();
      return predecessor;
   }

   public long getModelOID()
   {
      return getOID();
   }

   public String getId()
   {
      return fetchModel().getId();
   }

   /**
    * This will return the ID as it is persisted in table. This prevents bootstrapping of complete model.
    * In order to get ID from model definition {@link getId()} should be called.
    *
    * @return the ID as it persisted in table.
    */
   public String getPersistedId()
   {
      fetch();
      return id;
   }

   public IModel fetchModel()
   {
      if (model == null)
      {
         String xmlString =  LargeStringHolder.getLargeString(getOID(), ModelPersistorBean.class);
         DefaultXMLReader reader = new DefaultXMLReader(false, getConfigurationVariablesProvider(), getModelOID());
         model = reader.importFromXML(new StringReader(xmlString));
         injectTo(model);
      }
      return model;
   }

   public IConfigurationVariablesProvider getConfigurationVariablesProvider()
   {
      // use configuration variable provider if configured by properties
      Object object = Parameters.instance().get(
            IConfigurationVariablesProvider.CONFIGURATION_VAR_PROVIDER);
      if (object instanceof IConfigurationVariablesProvider)
      {
         return (IConfigurationVariablesProvider) object;
      }

      // otherwise use statically configured configuration variable provider
      if (confVarProvider == null)
      {
         confVarProvider = new PrefStoreAwareConfigurationVariablesProvider();
      }
      return confVarProvider;
   }

   public void setConfVarProvider(IConfigurationVariablesProvider confVarProvider)
   {
      if (this.confVarProvider != null)
      {
         throw new InternalException("ConfigurationVariablesProvider already set.");
      }
      this.confVarProvider = confVarProvider;
   }

   /**
    * Updates the model information in the audit trail.
    *
    * @param model The in memory representation of the model.
    * @param modelXml The string representation of the model, assumed to be in CWM,
    *       format encoded in ISO-8859-1.
    */
   public void setModel(IModel model, String modelXml)
   {
      markModified();

      this.id = StringUtils.cutString(model.getId(), id_COLUMN_LENGTH);
      this.name =model.getName();
      this.version = (String) model.getAttribute(PredefinedConstants.VERSION_ATT);

      injectTo(model);

      if (null != modelXml)
      {
         if ( !ParametersFacade.instance().getBoolean(
               KernelTweakingProperties.XPDL_MODEL_DEPLOYMENT, true))
         {
            Document parsedModel = XmlUtils.parseString(modelXml,
                  DefaultXMLReader.getCarnotModelEntityResolver());

            // convert into ASCII encoding to prevent model corruption as of DBMS charset
            // constraints
            Properties properties = new Properties();
            properties.put(OutputKeys.ENCODING, XMLConstants.ENCODING_ISO_8859_1);
            modelXml = XmlUtils.toString(parsedModel, properties);
         }

         // TODO inject attributes into XML

         // model is assumed to be in ISO8859-1 encoding, so it can be safely stored in
         // most databases
         LargeStringHolder.setLargeString(getOID(), ModelPersistorBean.class, modelXml);
      }
   }

   public IAuditTrailPartition getPartition()
   {
      fetchLink(FIELD__PARTITION);
      return partition;
   }

   private void injectTo(IModel model)
   {
      model.setModelOID((int) getOID());
      model.setAttribute(PredefinedConstants.VALID_FROM_ATT, validFrom);
      model.setAttribute(PredefinedConstants.VALID_TO_ATT, validTo);
      model.setAttribute(PredefinedConstants.DEPLOYMENT_TIME_ATT, deploymentStamp);
      model.setAttribute(PredefinedConstants.DEPLOYMENT_COMMENT_ATT, deploymentComment);
      model.setAttribute(PredefinedConstants.PREDECESSOR_ATT, new Integer((int) predecessor));
      model.setAttribute(PredefinedConstants.IS_DISABLED_ATT, disabled == 0 ? Boolean.FALSE: Boolean.TRUE);
      model.setAttribute(PredefinedConstants.REVISION_ATT, new Integer((int) revision));
      model.setAttribute(PredefinedConstants.VERSION_ATT, version);
   }

   public void setPredecessor(long predecessorOID)
   {
      markModified();
      this.predecessor = predecessorOID;
   }

   public void incrementRevision()
   {
      markModified();
      revision++;
   }

   public void delete()
   {
      super.delete();
      LargeStringHolder.deleteAllForOID(getOID(), ModelPersistorBean.class);
   }
}
