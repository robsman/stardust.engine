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
package org.eclipse.stardust.engine.core.runtime.internal;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.core.runtime.beans.ActionCarrier;


/**
 * @author rsauer
 * @version $Revision$
 */
public class SynchUserSessionsToDiskCarrier extends ActionCarrier
{
   /**
    * @deprecated WAS 5.x fails with this. Only used for backward compatibility. 
    */
   private static final String OLD_USER_OID_PREFIX = "user#";
   /**
    * @deprecated WAS 5.x fails with this. Only used for backward compatibility. 
    */
   private static final String OLD_USER_OID_PATTERN = "user'#'#0";

   private static final String USER_OID_PREFIX = "userOid_";
   private static final String USER_OID_PATTERN = "userOid'_'#0";

   /**
    * @deprecated WAS 5.x fails with this. Only used for backward compatibility. 
    */
   private final NumberFormat oldFmtUserOid;
   private final NumberFormat fmtUserOid;
   
   private final Map timestamps;
   
   public SynchUserSessionsToDiskCarrier()
   {
      this(CollectionUtils.newMap());
   }

   public SynchUserSessionsToDiskCarrier(Map timestamps)
   {
      super(SYSTEM_MESSAGE_TYPE_ID);

      this.oldFmtUserOid = new DecimalFormat(OLD_USER_OID_PATTERN);
      this.fmtUserOid = new DecimalFormat(USER_OID_PATTERN);
      
      this.timestamps = timestamps;
   }

   public Action doCreateAction()
   {
      return new SynchUserSessionToDiskAction(CollectionUtils.copyMap(timestamps));
   }

   protected void doExtract(Message message) throws JMSException
   {
      if (message instanceof MapMessage)
      {
         timestamps.clear();
         
         MapMessage mapMessage = (MapMessage) message;

         for (Enumeration e = mapMessage.getMapNames(); e.hasMoreElements();)
         {
            String name = (String) e.nextElement();
            if (name.startsWith(USER_OID_PREFIX))
            {
               try
               {
                  Number userOid = fmtUserOid.parse(name);
                  long timestamp = mapMessage.getLong(name);
                  
                  timestamps.put(new Long(userOid.longValue()), new Date(timestamp));
               }
               catch (ParseException pe)
               {
                  // TODO Auto-generated catch block
                  pe.printStackTrace();
               }
            }
            else if (name.startsWith(OLD_USER_OID_PREFIX))
            {
               try
               {
                  Number userOid = oldFmtUserOid.parse(name);
                  long timestamp = mapMessage.getLong(name);
                  
                  timestamps.put(new Long(userOid.longValue()), new Date(timestamp));
               }
               catch (ParseException pe)
               {
                  // TODO Auto-generated catch block
                  pe.printStackTrace();
               }
            }
         }
      }
   }

   protected void doFillMessage(Message message) throws JMSException
   {
      if (message instanceof MapMessage)
      {
         MapMessage mapMessage = (MapMessage) message;
         
         for (Iterator i = timestamps.entrySet().iterator(); i.hasNext();)
         {
            Map.Entry entry = (Map.Entry) i.next();
            
            Long userOid = (Long) entry.getKey();
            Date lastModificationTime = (Date) entry.getValue();
            
            mapMessage.setLong(fmtUserOid.format(userOid.longValue()),
                  lastModificationTime.getTime());
         }
      }
   }

}
