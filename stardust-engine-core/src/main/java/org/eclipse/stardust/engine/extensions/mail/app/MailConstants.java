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
package org.eclipse.stardust.engine.extensions.mail.app;

import org.eclipse.stardust.engine.api.model.PredefinedConstants;

/**
 * 
 */
public interface MailConstants
{
	String QUERY_PARAMETER_PREFIX = "mailParam";

	final String SCOPE_MAIL_RT = PredefinedConstants.ENGINE_SCOPE + "mail"; //$NON-NLS-1$
	
	final String PLAIN_TEXT_TEMPLATE = MailConstants.SCOPE_MAIL_RT + ":plainTextTemplate"; //$NON-NLS-1$
	final String USE_HTML = MailConstants.SCOPE_MAIL_RT + ":useHTML"; //$NON-NLS-1$
	final String HTML_HEADER = MailConstants.SCOPE_MAIL_RT + ":htmlHeader"; //$NON-NLS-1$
	final String HTML_TEMPLATE = MailConstants.SCOPE_MAIL_RT + ":htmlTemplate"; //$NON-NLS-1$
	final String HTML_FOOTER = MailConstants.SCOPE_MAIL_RT + ":htmlFooter"; //$NON-NLS-1$
	final String DEFAULT_MAIL_SERVER = MailConstants.SCOPE_MAIL_RT + ":defaultMailServer"; //$NON-NLS-1$
   final String DEFAULT_JNDI_SESSION = MailConstants.SCOPE_MAIL_RT + ":defaultJNDISession"; //$NON-NLS-1$
	final String URL_PREFIX = MailConstants.SCOPE_MAIL_RT + ":urlPrefix"; //$NON-NLS-1$
	final String DEFAULT_MAIL_FROM = MailConstants.SCOPE_MAIL_RT + ":defaultMailFrom"; //$NON-NLS-1$
	final String DEFAULT_MAIL_TO = MailConstants.SCOPE_MAIL_RT + ":defaultMailTo"; //$NON-NLS-1$
	final String DEFAULT_MAIL_CC = MailConstants.SCOPE_MAIL_RT + ":defaultMailCC"; //$NON-NLS-1$
	final String DEFAULT_MAIL_BCC = MailConstants.SCOPE_MAIL_RT + ":defaultMailBCC"; //$NON-NLS-1$
	final String DEFAULT_MAIL_PRIORITY = MailConstants.SCOPE_MAIL_RT + ":defaultMailPriority"; //$NON-NLS-1$
	final String DEFAULT_MAIL_SUBJECT = MailConstants.SCOPE_MAIL_RT + ":defaultMailSubject"; //$NON-NLS-1$
	final String SUBJECT_INCLUDE_UNIQUE_IDENTIFIED = MailConstants.SCOPE_MAIL_RT + ":subjectIncludeUniqueIdentified"; //$NON-NLS-1$
	final String OUTPUT_VALUES = MailConstants.SCOPE_MAIL_RT + ":outputValues"; //$NON-NLS-1$
	final String CREATE_PROCESS_HISTORY_LINK = MailConstants.SCOPE_MAIL_RT + ":createProcessHistoryLink"; //$NON-NLS-1$
	final String MAIL_RESPONSE = MailConstants.SCOPE_MAIL_RT + ":mailResponse"; //$NON-NLS-1$	
	final String MAIL_SUBJECT_ENHANCED_WITH_DETAILS = "Mail.SubjectEnhancedWithDetails"; //$NON-NLS-1$
}
