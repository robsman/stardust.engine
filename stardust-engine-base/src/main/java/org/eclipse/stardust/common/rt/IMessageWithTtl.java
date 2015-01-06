package org.eclipse.stardust.common.rt;

public interface IMessageWithTtl
{

   void setTimeToLive(long ttl);
   
   long getTimeToLive();
   
}
