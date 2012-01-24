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
package org.eclipse.stardust.engine.core.compatibility.extensions.dms;

import org.eclipse.stardust.engine.api.model.PredefinedConstants;

/**
 * @author rsauer
 * @version $Revision$
 */
public interface DmsConstants
{
   // TODO (ab) since old dms types where removed, source cleanup is needed. 
   // For example, this constant interface should not be there anymore
   
   String DMS_SCOPE = "carnot:integration:dms:";

   String DMS_KIND_ATT = PredefinedConstants.ENGINE_SCOPE + "dmsKind"; //$NON-NLS-1$
   
   String DATA_TYPE_ID_DOCUMENT = "dms-document";

   String DATA_TYPE_ID_DOCUMENT_SET = "dms-document-set";

   String PATH_ID_ATTACHMENTS = org.eclipse.stardust.engine.extensions.dms.data.DmsConstants.PATH_ID_ATTACHMENTS;

   String DATA_ID_ATTACHMENTS = org.eclipse.stardust.engine.extensions.dms.data.DmsConstants.DATA_ID_ATTACHMENTS;

}
