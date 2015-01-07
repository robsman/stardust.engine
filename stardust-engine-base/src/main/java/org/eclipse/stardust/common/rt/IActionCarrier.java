/*
 * $Id$
 * (C) 2000 - 2012 CARNOT AG
 */
package org.eclipse.stardust.common.rt;

import javax.jms.JMSException;
import javax.jms.Message;

import org.eclipse.stardust.common.Action;

public interface IActionCarrier<T>
{
   Action<T> createAction();

   void fillMessage(Message message) throws JMSException;

   int getMessageType();
}
