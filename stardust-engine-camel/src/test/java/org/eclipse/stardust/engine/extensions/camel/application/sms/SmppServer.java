/*
 * Copyright 2002-2013 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.eclipse.stardust.engine.extensions.camel.application.sms;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.BasicConfigurator;
import org.jsmpp.PDUStringException;
import org.jsmpp.SMPPConstant;
import org.jsmpp.bean.*;
import org.jsmpp.extra.ProcessRequestException;
import org.jsmpp.session.*;
import org.jsmpp.util.MessageIDGenerator;
import org.jsmpp.util.MessageId;
import org.jsmpp.util.RandomMessageIDGenerator;

/**
 * This is mock SMPP server connection which copied from jsmpp-examples (git clone https://github.com/otnateos/jsmpp.git)
 * with additional functionality related to SMS Application
 * @author Sabri.Bousselmi
 * @version $Revision: $
 */
public class SmppServer {
   private int port;
   private static String acceptedAccountName = "smppclient1";
   private static String acceptedAccountPw = "testPW";
   private static SubmitSm camelSubmitSm;
   private static BindRequest camelRequest ;
   
   private static final SmppServer  smppServer = new SmppServer();
   
   private SmppServer()
   {
      super();
   }
   
   public static SmppServer getInstance(){
      return smppServer;
   }

   public void start() {
        BasicConfigurator.configure();
        try {
            
            // prepare generator of Message ID
            final MessageIDGenerator messageIdGenerator = new RandomMessageIDGenerator();
            
            // prepare the message receiver
            ServerMessageReceiverListener messageReceiverListener = new ServerMessageReceiverListener() {
                public MessageId onAcceptSubmitSm(SubmitSm submitSm,
                        SMPPServerSession source)
                        throws ProcessRequestException {
                    System.out.println("Receiving message : " + new String(submitSm.getShortMessage()));
                    // store camel application sent message fo Assert Test
                    camelSubmitSm = submitSm;
                    // need message_id to response submit_sm
                    return messageIdGenerator.newMessageId();
                }
                
                public QuerySmResult onAcceptQuerySm(QuerySm querySm,
                        SMPPServerSession source)
                        throws ProcessRequestException {
                   System.out.println("");
                    return null;
                }
                
                public SubmitMultiResult onAcceptSubmitMulti(
                        SubmitMulti submitMulti, SMPPServerSession source)
                        throws ProcessRequestException {
                    System.out.println("");
                    return null;
                }
                
                public DataSmResult onAcceptDataSm(DataSm dataSm, Session source)
                        throws ProcessRequestException {
                    System.out.println("");
                    return null;
                }
                
                public void onAcceptCancelSm(CancelSm cancelSm,
                        SMPPServerSession source)
                        throws ProcessRequestException {
                   System.out.println("");
                }
                
                public void onAcceptReplaceSm(ReplaceSm replaceSm,
                        SMPPServerSession source)
                        throws ProcessRequestException {
                   System.out.println("");
                }
            };
            
            System.out.println("Listening ...");
            SMPPServerSessionListener sessionListener = new SMPPServerSessionListener(SmppServer.getInstance().port);
            // set all default ServerMessageReceiverListener for all accepted SMPPServerSessionListener
            sessionListener.setMessageReceiverListener(messageReceiverListener);
            
            // accepting connection, session still in OPEN state
            SMPPServerSession session = sessionListener.accept();
            // or we can set for each accepted session session.setMessageReceiverListener(messageReceiverListener)
            System.out.println("Accept connection");
            
            try {
                BindRequest request = session.waitForBind(5000);
                System.out.println("Receive bind request");
                
                // store request fo Assert Test
                camelRequest = request;
                if (request.getSystemId().equals(acceptedAccountName) && 
                        request.getPassword().equals(acceptedAccountPw)) {
                    
                    // accepting request and send bind response immediately
                    System.out.println("Accepting bind request");
                    request.accept(acceptedAccountName);
                    
                    
                    try { Thread.sleep(20000); } catch (InterruptedException e) {}
                } else {
                    System.out.println("Rejecting bind request");
                    request.reject(SMPPConstant.STAT_ESME_RINVPASWD);
                }
            } catch (TimeoutException e) {
                System.out.println("No binding request made after 5000 millisecond");
                e.printStackTrace();
            }
            
            System.out.println("Closing session");
            session.unbindAndClose();
            System.out.println("Closing session listener");
            sessionListener.close();
        } catch (PDUStringException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }   
   
   public SubmitSm getCamelSubmitSm()
   {
      return camelSubmitSm;
   }
   
   public BindRequest getCamelRequest()
   {
      return camelRequest;
   }
   
   
   public int getPort()
   {
      return port;
   }

   public void setPort(int port)
   {
      this.port = port;
   }
   
}
