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
package org.eclipse.stardust.engine.extensions.jms.app;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.Stateless;
import org.eclipse.stardust.common.StringKey;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.query.ActivityFilter;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQueryEvaluator;
import org.eclipse.stardust.engine.api.query.EvaluationContext;
import org.eclipse.stardust.engine.api.query.ProcessQueryPostprocessor;
import org.eclipse.stardust.engine.api.query.RawQueryResult;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.core.persistence.ResultIterator;
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.extensions.jms.app.ResponseHandlerImpl.Match;

/**
 * @author rsauer
 * @author Simon Nikles
 *
 */
public class SignalMessageAcceptor implements MessageAcceptor, Stateless
{
	private static final Logger trace = LogManager.getLogger(DefaultMessageAcceptor.class);

	public boolean isStateless()
	{
		return true;
	}

	public Collection getAccessPoints(StringKey key)
	{
		return DefaultMessageHelper.getIntrinsicAccessPoints(key, Direction.OUT);
	}

	public Iterator<IActivityInstance> getMatchingActivityInstances(Message message)
	{
		try
		{
			if (!message.propertyExists("stardust.bpmn.signal")) return Collections.EMPTY_LIST.iterator();

			String signal = message.getStringProperty("stardust.bpmn.signal");

			if (trace.isDebugEnabled())
			{
				trace.debug("Getting matching activity instances for incoming signal " + signal);
			}
			if (null != signal)
			{
//				ActivityInstanceQuery query = findInStateHavingSignal(signal, ActivityInstanceState.Hibernated);
//
//				return executeRawQuery(query);
			}
		}
		catch (JMSException e) {
			throw new PublicException(e);
		}
		// no matches
		return Collections.EMPTY_LIST.iterator();
	}

	private ActivityInstanceQuery findInStateHavingSignal(String activityID, String processID, ActivityInstanceState state) {
		ActivityInstanceQuery query = ActivityInstanceQuery.findInState(state);
		query.where(ActivityFilter.forProcess(activityID, processID));
		return query;
	}

	private Iterator<IActivityInstance> executeRawQuery(ActivityInstanceQuery query)
	{
		ResultIterator rawResult = new ActivityInstanceQueryEvaluator(query,
				new EvaluationContext(ModelManagerFactory.getCurrent(), null))
		.executeFetch();
		try
		{
			RawQueryResult filteredResult = ProcessQueryPostprocessor
					.findMatchingActivityInstances(query, rawResult);

			List<IActivityInstance> matchingInstances = CollectionUtils.newList();
			for (Iterator<IActivityInstance> i = filteredResult.iterator(); i.hasNext();)
			{
				matchingInstances.add(i.next());
			}
			return matchingInstances.iterator();
		}
		finally
		{
			rawResult.close();
		}
	}

	public Map<String, Object> getData(Message message, StringKey id, Iterator accessPoints)
	{
		if (trace.isDebugEnabled())
		{
			trace.debug("Getting data from incoming message");
		}

		return DefaultMessageHelper.getData(message, accessPoints);
	}

	public String getName()
	{
		return "Signal event acceptor";
	}

	public boolean hasPredefinedAccessPoints(StringKey id)
	{
		return DefaultMessageHelper.hasPredefinedAccessPoints(id);
	}

	public Collection getMessageTypes()
	{
		return DefaultMessageHelper.getMessageIds();
	}

   @Override
   public Match finalizeMatch(IActivityInstance activityInstance)
   {
      // TODO create match
      return null;
   }
}
