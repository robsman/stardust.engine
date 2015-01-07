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
package org.eclipse.stardust.engine.api.runtime;

import org.eclipse.stardust.engine.core.persistence.jdbc.PersistentBean;

/**
 * @author Sebastian Woelk
 * @version $Revision: 31061 $
 */
public class LSHTestPersistent extends PersistentBean
{
   public static final String TABLE_NAME = "LSH_TEST_PERSISTENT";
   public static final String PK_FIELD = "oid";
   public static final String PK_SEQUENCE = "LSH_TEST_PERSISTENT_SEQ";
}
