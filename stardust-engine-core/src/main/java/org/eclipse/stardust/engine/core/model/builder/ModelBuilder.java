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
package org.eclipse.stardust.engine.core.model.builder;

import java.util.Date;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.Period;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.core.extensions.actions.delegate.TargetWorklist;
import org.eclipse.stardust.engine.core.pojo.data.Type;
import org.eclipse.stardust.engine.extensions.jms.app.MessageAcceptor;
import org.eclipse.stardust.engine.extensions.jms.app.MessageProvider;
import org.eclipse.stardust.engine.extensions.jms.app.MessageType;
import org.eclipse.stardust.engine.extensions.mail.trigger.MailProtocol;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface ModelBuilder
{
   void createPredefinedConstants(IModel model);

   IModel createModel(String id, String name, String description);

   IModel createModel(String id);

   IApplication createSessionBeanApplication(IModel model, String home, String remote,
         String jndiName, String createMethod, String completeMethod, boolean localBinding);

   IApplication createSessionBeanApplication(IModel model, String id, String name,
         String home, String remote, String jndiName, String createMethod,
         String completeMethod, boolean localBinding);

   IApplication createPlainJavaApplication(IModel model, String id, String name,
         String className, String constructor, String method);

   IApplication createPlainJavaApplication(IModel model,
         String className, String constructor, String method);

   IApplication createJFCApplication(IModel model,
         String className, String method);

   IApplication createJFCApplication(IModel model, String id, String name,
         String className, String method);

   IApplication createJSPApplication(IModel model, String url);

   IApplication createJSPApplication(IModel model, String id, String name, String url);

   IApplication createJMSRequestApplication(IModel model,
         String connectionFactoryJNDI, String queueJNDI,
         MessageProvider provider, MessageType type, boolean includeOIDs);

   IApplication createJMSRequestApplication(IModel model, String id, String name,
         String connectionFactoryJNDI, String queueJNDI,
         MessageProvider provider, MessageType type, boolean includeOIDs);

   IApplication createJMSResponseApplication(IModel model,
         MessageAcceptor acceptor, MessageType type);

   IApplication createJMSResponseApplication(IModel model, String id, String name,
         MessageAcceptor acceptor, MessageType type);

   IApplication createJMSRequestResponseApplication(IModel model,
         String connectionFactoryJNDI, String queueJNDI,
         MessageProvider provider, MessageType requestType, boolean includeOIDs,
         MessageAcceptor acceptor, MessageType responseType);

   IApplication createJMSRequestResponseApplication(IModel model, String id, String name,
         String connectionFactoryJNDI, String queueJNDI,
         MessageProvider provider, MessageType requestType, boolean includeOIDs,
         MessageAcceptor acceptor, MessageType responseType);

   IProcessDefinition createProcessDefinition(IModel model, String id);

   IActivity createRouteActivity(IProcessDefinition process);

   IActivity createRouteActivity(IProcessDefinition process, String id, String name);

   IActivity createManualActivity(IProcessDefinition processDefinition,
         IModelParticipant performer);

   IActivity createManualActivity(IProcessDefinition processDefinition,
         String id, String name, IModelParticipant performer);

   IOrganization createOrganization(IModel model);

   IOrganization createOrganization(IModel model, String id, String name);

   IRole createRole(IModel model);

   IRole createRole(IModel model, String id, String name);

   IActivity createApplicationActivity(IProcessDefinition processDefinition,
         IApplication application);

   IActivity createApplicationActivity(IProcessDefinition processDefinition,
         String id, String name, IApplication application);

   IActivity createSubprocessActivity(IProcessDefinition processDefinition,
         IProcessDefinition subProcess);

   IActivity createSubprocessActivity(IProcessDefinition processDefinition,
         String id, String name, IProcessDefinition subProcess);

   ITransition createTransition(IActivity source, IActivity target,
         String condition);

   ITransition createJsTransition(IActivity source, IActivity target,
         String condition);

   ITransition[] split(IActivity source, IActivity[] targets, JoinSplitType type);

   ITransition[] join(IActivity[] sources, IActivity target, JoinSplitType type);

   ITransition createTransition(String id, String name, IActivity source,
         IActivity target, String condition);

   ITransition createJsTransition(String id, String name, IActivity source,
         IActivity target, String condition);

   IDataMapping createEngineMapping(IActivity activity,
         Direction direction, String applicationAP, String applicationPath, IData data,
         String dataPath);

   IDataMapping createNoninteractiveMapping(IActivity activity,
         Direction direction, String applicationAP, String applicationPath, IData data,
         String dataPath);

   IDataMapping createManualMapping(IActivity activity, String id, Direction direction,
         IData data, String dataPath);

   IDataMapping createJFCMapping(IActivity activity, Direction direction,
         String applicationAP, String applicationPath, IData data, String dataPath);

   IData createSerializableData(IModel model, String className);

   IData createSerializableData(IModel model, String id, String name, String className);

   IData createEntityBeanData(IModel model, String beanClassName,
         String jndiName, String homeClassName, String pkClassName);

   IData createEntityBeanData(IModel model, String id, String name, String beanClassName,
         String jndiName, String homeClassName, String pkClassName);

   IData createPrimitiveData(IModel model, Type type, String defaultValue);

   IData createPrimitiveData(IModel model, String id, String name,
         Type type, String defaultValue);

   IData createPlainXMLData(IModel model);

   IData createPlainXMLData(IModel model, String id, String name);

   IEventHandler createExceptionEventHandler(IActivity activity, String exception);

   IEventHandler createTimerEventHandler(EventHandlerOwner owner, Period period);

   IEventHandler createTimerEventHandler(EventHandlerOwner owner, IData data, String dataPath);

   IEventHandler createStatechangeEventHandler(EventHandlerOwner owner,
         ActivityInstanceState source, ActivityInstanceState target);

   IEventHandler createStatechangeEventHandler(EventHandlerOwner owner,
         ProcessInstanceState source, ProcessInstanceState target);

   IEventHandler createExternalEventHandler(EventHandlerOwner owner);

   IEventHandler createDataChangeEventHandler(EventHandlerOwner owner, String condition);

   IEventHandler createAssignmentEventHandler(EventHandlerOwner owner);

   IEventAction createDelegateAction(IEventHandler handler, TargetWorklist targetWorklist, String targetParticipant);
   IBindAction createDelegateBindAction(IEventHandler handler, TargetWorklist targetWorklist, String targetParticipant);
   IUnbindAction createDelegateUnbindAction(IEventHandler handler, TargetWorklist targetWorklist, String targetParticipant);

   IEventAction createScheduleAction(IEventHandler handler, ActivityInstanceState targetState);
   IBindAction createScheduleBindAction(IEventHandler handler, ActivityInstanceState targetState);
   IUnbindAction createScheduleUnbindAction(IEventHandler handler, ActivityInstanceState targetState);

   IEventAction createAbortProcessAction(IEventHandler handler);
   IBindAction createAbortProcessBindAction(IEventHandler handler);
   IUnbindAction createAbortProcessUnbindAction(IEventHandler handler);

   IEventAction createCompleteActivityAction(IEventHandler handler);
   IBindAction createCompleteActivityBindAction(IEventHandler handler);
   IUnbindAction createCompleteActivityUnbindAction(IEventHandler handler);

   IEventAction createActivateActivityAction(IEventHandler handler);
   IBindAction createActivateActivityBindAction(IEventHandler handler);
   IUnbindAction createActivateActivityUnbindAction(IEventHandler handler);

   IEventAction createTriggerProcessAction(IEventHandler handler, IProcessDefinition process);
   IBindAction createTriggerProcessBindAction(IEventHandler handler, IProcessDefinition process);
   IUnbindAction createTriggerProcessUnbindAction(IEventHandler handler, IProcessDefinition process);

   /**
    * Creates a new SendMailAction which sends a mail to the mail address given as parameter on
    * the passed EventHandler with a specific message text.
    *
    * @param handler the handler on which the SendMailAction will be created
    * @param email the mail address of the mail recipient
    * @param messageText  the body part of the mail
    * @return the new SendmailAction
    */
   IEventAction createSendMailAction(IEventHandler handler, String email, String messageText);
   IBindAction createSendMailBindAction(IEventHandler handler, String email, String messageText);
   IUnbindAction createSendMailUnbindAction(IEventHandler handler, String email, String messageText);

   /**
    * Creates a new SendMailAction which sends a mail to the mail address given as parameter on
    * the passed EventHandler.
    * <p>
    * The mail body will contain the current value of the passed data element.
    *
    * @param handler the handler on which the SendMailAction will be created
    * @param email the mail address of the mail recipient
    * @param data the model data element whose value will be put in the mail body
    * @param dataPath the access path the the data element
    * @return the new SendmailAction
    */
   IEventAction createSendMailAction(IEventHandler handler, String email, IData data, String dataPath);
   IBindAction createSendMailBindAction(IEventHandler handler, String email, IData data, String dataPath);
   IUnbindAction createSendMailUnbindAction(IEventHandler handler, String email, IData data, String dataPath);

   /**
    * Creates a new SendMailAction which sends a mail to the current user performer on
    * the passed EventHandler with a specific message text.
    *
    * @param handler the handler on which the SendMailAction will be created
    * @param messageText the body part of the mail
    * @return the new SendmailAction
    */
   IEventAction createSendMailAction(IEventHandler handler, String messageText);
   IBindAction createSendMailBindAction(IEventHandler handler, String messageText);
   IUnbindAction createSendMailUnbindAction(IEventHandler handler, String messageText);

   /**
    * Creates a new SendMailAction which sends a mail to the current user performer on
    * the passed EventHandler.
    * <p>
    * The mail body will contain the current value of the passed data element.
    *
    * @param handler the handler on which the SendMailAction will be created
    * @param data the model data element whose value will be put in the mail body
    * @param dataPath the access path the the data element
    * @return the new SendmailAction
    */
   IEventAction createSendMailAction(IEventHandler handler, IData data, String dataPath);
   IBindAction createSendMailBindAction(IEventHandler handler, IData data, String dataPath);
   IUnbindAction createSendMailUnbindAction(IEventHandler handler, IData data, String dataPath);

   /**
    * Creates a new SendMailAction which sends a mail to the member of the specified participant on
    * the passed EventHandler with a specific message text.
    *
    * @param handler the handler on which the SendMailAction will be created
    * @param receiver the receiving participant
    * @param messageText the body part of the mail
    * @return the new SendmailAction
    */
   IEventAction createSendMailAction(IEventHandler handler, IModelParticipant receiver, String messageText);
   IBindAction createSendMailBindAction(IEventHandler handler, IModelParticipant receiver, String messageText);
   IUnbindAction createSendMailUnbindAction(IEventHandler handler, IModelParticipant receiver, String messageText);

   /**
    * Creates a new SendMailAction which sends a mail to the members of the specified participant
    * the passed EventHandler.
    * <p>
    * The mail body will contain the current value of the passed data element.
    *
    * @param handler the handler on which the SendMailAction will be created
    * @param receiver the receiving participant
    * @param data the model data element whose value will be put in the mail body
    * @param dataPath the access path the the data element
    * @return the new SendmailAction
    */
   IEventAction createSendMailAction(IEventHandler handler, IModelParticipant receiver, IData data, String dataPath);
   IBindAction createSendMailBindAction(IEventHandler handler, IModelParticipant receiver, IData data, String dataPath);
   IUnbindAction createSendMailUnbindAction(IEventHandler handler, IModelParticipant receiver, IData data, String dataPath);

   IEventAction createSetDataAction(IEventHandler handler, String ap, String attPath, String dataPath, IData data);
   IBindAction createSetDataBindAction(IEventHandler handler, String ap, String attPath, String dataPath, IData data);
   IUnbindAction createSetDataUnbindAction(IEventHandler handler, String ap, String attPath, String dataPath, IData data);

   IEventAction createExcludeUserAction(IEventHandler handler, IData data, String dataPath);

   IDataPath createDataPath(IProcessDefinition process, String id, IData data,
         String path, Direction direction);

   ITrigger createManualTrigger(IProcessDefinition process, IModelParticipant participant);

   ITrigger createTimerTrigger(IProcessDefinition process, Date startTime);

   ITrigger createTimerTrigger(IProcessDefinition process, Date startTime, Date stopTime,
         Period period);

   ITrigger createMailTrigger(IProcessDefinition process, String user, String password,
         String host, String selector, MailProtocol protocol);

   IParameterMapping createParameterMapping(ITrigger trigger, String parameter,
         String parameterPath, IData data);
}
