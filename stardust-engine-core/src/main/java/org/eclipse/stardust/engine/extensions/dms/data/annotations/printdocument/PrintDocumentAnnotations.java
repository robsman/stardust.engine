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
package org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument;

/**
 * Facade aggregating annotation types available for print documents.
 *
 * @author roland.stamm
 *
 */
public interface PrintDocumentAnnotations extends DocumentAnnotations, PageBookmarkable, NoteCapable, HighlightCapable, StampAware, PageOrientationAware, PageSequenceAware
{

}
