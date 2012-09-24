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

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="activityOid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="abortScope" type="{http://eclipse.org/stardust/ws/v2012a/api}AbortScope" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "activityOid",
    "abortScope"
})
@XmlRootElement(name = "abortActivity")
public class AbortActivity {

    protected long activityOid;
    @XmlElementRef(name = "abortScope", namespace = "http://eclipse.org/stardust/ws/v2012a/api", type = JAXBElement.class)
    protected JAXBElement<AbortScopeXto> abortScope;

    /**
     * Gets the value of the activityOid property.
     * 
     */
    public long getActivityOid() {
        return activityOid;
    }

    /**
     * Sets the value of the activityOid property.
     * 
     */
    public void setActivityOid(long value) {
        this.activityOid = value;
    }

    /**
     * Gets the value of the abortScope property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link AbortScopeXto }{@code >}
     *     
     */
    public JAXBElement<AbortScopeXto> getAbortScope() {
        return abortScope;
    }

    /**
     * Sets the value of the abortScope property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link AbortScopeXto }{@code >}
     *     
     */
    public void setAbortScope(JAXBElement<AbortScopeXto> value) {
        this.abortScope = ((JAXBElement<AbortScopeXto> ) value);
    }

}
