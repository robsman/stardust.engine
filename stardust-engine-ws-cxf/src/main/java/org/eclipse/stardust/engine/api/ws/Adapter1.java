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

package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.eclipse.stardust.common.Direction;

public class Adapter1
    extends XmlAdapter<String, Direction>
{


    public Direction unmarshal(String value) {
        return (org.eclipse.stardust.engine.ws.XmlAdapterUtils.parseDirection(value));
    }

    public String marshal(Direction value) {
        return (org.eclipse.stardust.engine.ws.XmlAdapterUtils.printDirection(value));
    }

}
