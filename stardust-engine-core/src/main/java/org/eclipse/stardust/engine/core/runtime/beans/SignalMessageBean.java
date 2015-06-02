/***********************************************************************************
 * Copyright (c) 2011, 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 ***********************************************************************************/
package org.eclipse.stardust.engine.core.runtime.beans;

import static org.eclipse.stardust.engine.core.persistence.Predicates.andTerm;
import static org.eclipse.stardust.engine.core.persistence.Predicates.greaterOrEqual;
import static org.eclipse.stardust.engine.core.persistence.Predicates.isEqual;
import static org.eclipse.stardust.engine.core.persistence.QueryExtension.where;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.Session;

import org.eclipse.stardust.common.Base64;
import org.eclipse.stardust.common.Serialization;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.jdbc.IdentifiablePersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.JmsProperties;
import org.eclipse.stardust.engine.extensions.events.signal.SignalMessageAcceptor;
import org.eclipse.stardust.engine.extensions.jms.utils.JMSUtils;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;

/**
 * <p>
 * An {@code ISignalMessage} implementation being the <i>Java</i> class representation of an entry
 * of the table storing <i>JMS Signal Messages</i> in the <i>Audit Trail Database</i>.
 * </p>
 *
 * @author Nicolas.Werlein
 */
public class SignalMessageBean extends IdentifiablePersistentBean implements ISignalMessage
{
   private static final long serialVersionUID = -3964012480358288740L;

   /* ******************************* */
   /*     Database Table Metadata     */
   /* ******************************* */

   /* package-private */  static final String TABLE_NAME = "signal_message";
   /* package-private */ static final String DEFAULT_ALIAS = "sgn_msg";

   /* package-private */ static final String FIELD__OID = IdentifiablePersistentBean.FIELD__OID;
   /* package-private */ static final String FIELD__PARTITION_OID = "partitionOid";
   /* package-private */ static final String FIELD__SIGNAL_NAME = "signalName";
   /* package-private */ static final String FIELD__MESSAGE_CONTENT = "messageContent";
   /* package-private */ static final String FIELD__TIMESTAMP = "timestamp";

   /* package-private */ static final FieldRef FR__OID = new FieldRef(SignalMessageBean.class, FIELD__OID);
   /* package-private */ static final FieldRef FR__PARTITION_OID = new FieldRef(SignalMessageBean.class, FIELD__PARTITION_OID);
   /* package-private */ static final FieldRef FR__SIGNAL_NAME = new FieldRef(SignalMessageBean.class, FIELD__SIGNAL_NAME);
   /* package-private */ static final FieldRef FR__MESSAGE_CONTENT = new FieldRef(SignalMessageBean.class, FIELD__MESSAGE_CONTENT);
   /* package-private */ static final FieldRef FR__TIMESTAMP = new FieldRef(SignalMessageBean.class, FIELD__TIMESTAMP);

   /* package-private */ static final String PK_FIELD = FIELD__OID;
   /* package-private */ static final String PK_SEQUENCE = "signal_message_seq";

   /* package-private */ static final boolean TRY_DEFERRED_INSERT = true;

   /* package-private */ static final String[] msg_store_idx1_UNIQUE_INDEX = new String[] { FIELD__OID };


   /* ****************************** */
   /*     Database Table Columns     */
   /* ****************************** */

   private long partitionOid;

   private String signalName;

   /* package-private */ static final int message_COLUMN_LENGTH = Integer.MAX_VALUE;
   private String messageContent;

   private Date timestamp;


   public SignalMessageBean()
   {
      /* default ctor needed for persistence framework: nothing to do */
   }

   public SignalMessageBean(final long partitionOid, final MapMessage message)
   {
      this.partitionOid = partitionOid;
      this.signalName = getSignalNameFrom(message);
      this.messageContent = serialize(message);
      this.timestamp = TimestampProviderUtils.getTimeStamp();

      SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).cluster(this);
   }

   @Override
   public long getPartitionOid()
   {
      fetch();

      return partitionOid;
   }

   @Override
   public String getSignalName()
   {
      fetch();

      return signalName;
   }

   @Override
   public MapMessage getMessage()
   {
      fetch();

      final MapMessage result = deserialize(messageContent);
      setSignalNameOn(result);
      return result;
   }

   @Override
   public Date getTimestamp()
   {
      fetch();

      return timestamp;
   }

   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      final StringBuilder sb = new StringBuilder();

      sb.append("Signal Message { ");
      sb.append("OID = ").append(getOID()).append(", ");
      sb.append("partition OID = ").append(getOID()).append(", ");
      sb.append("signal name = ").append(getSignalName()).append(", ");
      sb.append("timestamp = ").append(getTimestamp());
      sb.append(" }");

      return sb.toString();
   }

   /**
    * @param oid the OID criterion
    *
    * @return the {@link SignalMessageBean} satisfying the given criterion
    */
   public static SignalMessageBean findByOid(final long oid)
   {
      return SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).findByOID(SignalMessageBean.class, oid);
   }

   /**
    * @param partitionOid the partition criterion
    * @param signalName the signal name criterion
    * @param validFrom the timestamp criterion: signal message must have been fired after (or at) the given point in time
    *
    * @return an iterator containing all {@code SignalMessageBean}s satisfying the given criteria
    */
   public static Iterator<SignalMessageBean> findFor(final long partitionOid, final String signalName, final Date validFrom)
   {
      return SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).getIterator(SignalMessageBean.class,
            where(andTerm(isEqual(FR__PARTITION_OID, partitionOid), isEqual(FR__SIGNAL_NAME, signalName), greaterOrEqual(FR__TIMESTAMP, validFrom.getTime()))));
   }

   private String getSignalNameFrom(final MapMessage msg)
   {
      try
      {
         return msg.getStringProperty(SignalMessageAcceptor.BPMN_SIGNAL_PROPERTY_KEY);
      }
      catch (final JMSException e)
      {
         // TODO - bpmn-2-events - review exception handling
         throw new PublicException(e.getMessage(), e);
      }
   }

   private void setSignalNameOn(final MapMessage msg)
   {
      try
      {
         msg.setStringProperty(SignalMessageAcceptor.BPMN_SIGNAL_PROPERTY_KEY, getSignalName());
      }
      catch (final JMSException e)
      {
         // TODO - bpmn-2-events - review exception handling
         throw new PublicException(e.getMessage(), e);
      }
   }

   // TODO - bpmn-2-events - review serialization
   private String serialize(final MapMessage msg)
   {
      try
      {
         final Map<String, Object> map = JMSUtils.toMap(msg);
         final byte[] serializedMap = Serialization.serializeObject((Serializable) map);
         return new String(Base64.encode(serializedMap));
      }
      catch (final IOException e)
      {
         // TODO - bpmn-2-events - review exception handling
         throw new PublicException(e.getMessage(), e);
      }
   }

   // TODO - bpmn-2-events - review deserialization
   private MapMessage deserialize(final String msg)
   {
      try
      {
         final byte[] serializedMap = Base64.decode(msg.getBytes());
         final Map<String, Object> map = (Map<String, Object>) Serialization.deserializeObject(serializedMap);
         return JMSUtils.toMapMessage(map, getSession());
      }
      catch (final ClassNotFoundException e)
      {
         // TODO - bpmn-2-events - review exception handling
         throw new PublicException(e.getMessage(), e);
      }
      catch (final IOException e)
      {
         // TODO - bpmn-2-events - review exception handling
         throw new PublicException(e.getMessage(), e);
      }
   }

   private Session getSession()
   {
      try
      {
         final BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();
         final QueueConnectionFactory connectionFactory = rtEnv.retrieveQueueConnectionFactory(JmsProperties.QUEUE_CONNECTION_FACTORY_PROPERTY);
         final QueueConnection connection = rtEnv.retrieveQueueConnection(connectionFactory);
         return rtEnv.retrieveQueueSession(connection);
      }
      catch (final JMSException e)
      {
         // TODO - bpmn-2-events - review exception handling
         throw new PublicException(e.getMessage(), e);
      }
   }
}
