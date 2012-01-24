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
package org.eclipse.stardust.engine.extensions.transformation;

/**
 * TODO Merge with modeling
 * 
 * @author Marc Gille
 *
 */
public interface Constants
{
	public static final String SCOPE = "messageTransformation:"; //$NON-NLS-1$
	public static final String FIELD_MAPPING = SCOPE + "FieldMapping"; //$NON-NLS-1$
	public static final String MESSAGE_FORMAT = SCOPE + "MessageFormat";
	public static final String FORMAT_MODEL_FILE_PATH = SCOPE + "FormatModelFilePath";
	public static final String XSL_STRING = SCOPE + "XSLCode";
	public static final String SOURCE_TYPE = SCOPE + "SourceType";
	public static final String TARGET_TYPE = SCOPE + "TargetType";
	public static final String TRANSFORMATION_PROPERTY = SCOPE + "TransformationProperty";
	
}
