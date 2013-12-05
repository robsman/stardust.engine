package org.eclipse.stardust.engine.extensions.camel.runtime;

import javax.jms.Destination;

public interface JmsMessage extends Message
{
   public abstract String getJMSCorrelationID();

   public abstract int getJMSDeliveryMode();

   public abstract Destination getJMSDestination();

   public abstract long getJMSExpiration();

   public abstract String getJMSMessageID();

   public abstract int getJMSPriority();

   public abstract boolean getJMSRedelivered();

   public abstract Destination getJMSReplyTo();

   public abstract long getJMSTimestamp();

   public abstract String getJMSType();

   public abstract String getJMSXGroupID();
}
