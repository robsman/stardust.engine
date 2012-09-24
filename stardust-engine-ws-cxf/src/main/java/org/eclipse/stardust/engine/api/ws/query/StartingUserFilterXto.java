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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *         Restricts the resulting items to the ones that were started by the specified user.
 *         (The currently logged in user is mapped to userOid = -1 for this filter.)
 *         
 * 
 * <p>Java class for StartingUserFilter complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="StartingUserFilter">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api/query}PredicateBase">
 *       &lt;sequence>
 *         &lt;element name="userOid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StartingUserFilter", propOrder = {
    "userOid"
})
public class StartingUserFilterXto
    extends PredicateBaseXto
{

    protected long userOid;

    /**
     * Gets the value of the userOid property.
     * 
     */
    public long getUserOid() {
        return userOid;
    }

    /**
     * Sets the value of the userOid property.
     * 
     */
    public void setUserOid(long value) {
        this.userOid = value;
    }

}
