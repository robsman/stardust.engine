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
package org.eclipse.stardust.engine.core.persistence.jms;

import java.util.List;

import javax.jms.BytesMessage;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.PublicException;


/**
 * @author sauer
 * @version $Revision$
 */
public class JmsBytesMessagesReader extends AbstractJmsBytesMessageReader
{

   private final List/*<BytesMessage>*/ messages;
   
   private int i;
   
   public JmsBytesMessagesReader(List messages)
   {
      this.messages = messages;
   }
   
   protected BytesMessage nextBlobContainer() throws PublicException
   {
      return (i < messages.size()) ? (BytesMessage) messages.get(i) : null;
   }

   public void init(Parameters params) throws PublicException
   {
      super.init(params);
      
      this.i = 0;
   }

}