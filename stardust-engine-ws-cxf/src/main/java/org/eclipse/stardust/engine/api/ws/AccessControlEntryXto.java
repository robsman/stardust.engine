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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 			An access control entry consists of a principal that is associated with privileges.
 * 			
 * 
 * <p>Java class for AccessControlEntry complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AccessControlEntry">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="principal" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="privileges" type="{http://eclipse.org/stardust/ws/v2012a/api}Privileges" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AccessControlEntry", propOrder = {
    "principal",
    "privileges"
})
public class AccessControlEntryXto {

    protected String principal;
    protected PrivilegesXto privileges;

    /**
     * Gets the value of the principal property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPrincipal() {
        return principal;
    }

    /**
     * Sets the value of the principal property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPrincipal(String value) {
        this.principal = value;
    }

    /**
     * Gets the value of the privileges property.
     * 
     * @return
     *     possible object is
     *     {@link PrivilegesXto }
     *     
     */
    public PrivilegesXto getPrivileges() {
        return privileges;
    }

    /**
     * Sets the value of the privileges property.
     * 
     * @param value
     *     allowed object is
     *     {@link PrivilegesXto }
     *     
     */
    public void setPrivileges(PrivilegesXto value) {
        this.privileges = value;
    }

}
