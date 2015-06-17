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

import static org.eclipse.stardust.common.CollectionUtils.newHashSet;
import static org.eclipse.stardust.engine.core.persistence.Predicates.andTerm;
import static org.eclipse.stardust.engine.core.persistence.Predicates.isEqual;
import static org.eclipse.stardust.engine.core.persistence.QueryExtension.where;

import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.jdbc.PersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;

/**
 * <p>
 * An {@code ISignalMessageLookup} implementation being the <i>Java</i> class representation of an entry
 * of the table holding lookup data for <i>JMS Signal Messages</i> in the <i>Audit Trail Database</i>.
 * </p>
 *
 * @author Nicolas.Werlein
 */
public class SignalMessageLookupBean extends PersistentBean implements ISignalMessageLookup
{
   /* ******************************* */
   /*     Database Table Metadata     */
   /* ******************************* */

   /* package-private */  static final String TABLE_NAME = "signal_message_lookup";
   /* package-private */ static final String DEFAULT_ALIAS = "sgn_msg_lookup";

   /* package-private */ static final String FIELD__PARTITION_OID = "partitionOid";
   /* package-private */ static final String FIELD__SIGNAL_DATA_HASH = "signalDataHash";
   /* package-private */ static final String FIELD__SIGNAL_MESSAGE_OID = "signalMessageOid";

   /* package-private */ static final FieldRef FR__PARTITION_OID = new FieldRef(SignalMessageLookupBean.class, FIELD__PARTITION_OID);
   /* package-private */ static final FieldRef FR__SIGNAL_DATA_HASH = new FieldRef(SignalMessageLookupBean.class, FIELD__SIGNAL_DATA_HASH);
   /* package-private */ static final FieldRef FR__SIGNAL_MESSAGE_OID = new FieldRef(SignalMessageLookupBean.class, FIELD__SIGNAL_MESSAGE_OID);

   // TODO - bpmn-2-events - review primary key: in theory, it may be possible that the following compound key is *not* unique
   /* package-private */ static final String[] PK_FIELD = new String[] { FIELD__PARTITION_OID, FIELD__SIGNAL_DATA_HASH, FIELD__SIGNAL_MESSAGE_OID };

   /* package-private */ static final boolean TRY_DEFERRED_INSERT = true;

   /* package-private */ static final String[] signal_message_lookup_idx1_INDEX = new String[] { FIELD__PARTITION_OID, FIELD__SIGNAL_DATA_HASH };

   /* ****************************** */
   /*     Database Table Columns     */
   /* ****************************** */

   private long partitionOid;

   private String signalDataHash;

   private long signalMessageOid;

   public SignalMessageLookupBean()
   {
      /* default ctor needed for persistence framework: nothing to do */
   }

   public SignalMessageLookupBean(final long partitionOid, final String signalDataHash, final long signalMessageOid)
   {
      this.partitionOid = partitionOid;
      this.signalDataHash = signalDataHash;
      this.signalMessageOid = signalMessageOid;

      SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).cluster(this);
   }

   @Override
   public long getPartitionOid()
   {
      fetch();

      return partitionOid;
   }

   @Override
   public String getSignalDataHash()
   {
      fetch();

      return signalDataHash;
   }

   @Override
   public long getSignalMessageOid()
   {
      fetch();

      return signalMessageOid;
   }

   /**
    * @param partitionOid the partition criterion
    * @param signalDataHash the signal data hash criterion
    * @param validFrom the timestamp criterion: signal message must have been fired after (or at) the given point in time
    *
    * @return an iterator containing all {@code SignalMessageBean}s satisfying the given criteria
    */
   public static Iterator<SignalMessageBean> findFor(final long partitionOid, final String signalDataHash, final Date validFrom)
   {
      final Iterator<SignalMessageLookupBean> iter = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).getIterator(SignalMessageLookupBean.class,
                                                         where(andTerm(isEqual(FR__PARTITION_OID, partitionOid), isEqual(FR__SIGNAL_DATA_HASH, signalDataHash))));

      final Set<SignalMessageBean> result = newHashSet();
      while (iter.hasNext())
      {
         final SignalMessageBean signalMsg = SignalMessageBean.findByOid(iter.next().getSignalMessageOid());
         if (signalMsg.getTimestamp().getTime() >= validFrom.getTime())
         {
            result.add(signalMsg);
         }
      }
      return result.iterator();
   }
}
