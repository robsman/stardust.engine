/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.engine.api.ws.query;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;

public class Adapter1
    extends XmlAdapter<String, ActivityInstanceState>
{


    public ActivityInstanceState unmarshal(String value) {
        return (org.eclipse.stardust.engine.ws.XmlAdapterUtils.parseActivityInstanceState(value));
    }

    public String marshal(ActivityInstanceState value) {
        return (org.eclipse.stardust.engine.ws.XmlAdapterUtils.printActivityInstanceState(value));
    }

}
