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

import java.util.Date;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
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
 *         &lt;element name="modelOid" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="validFrom" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="validTo" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="comment" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="disabled" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="ignoreWarnings" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="configuration" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="xml" type="{http://eclipse.org/stardust/ws/v2012a/api}XmlValue"/>
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
    "modelOid",
    "validFrom",
    "validTo",
    "comment",
    "disabled",
    "ignoreWarnings",
    "configuration",
    "xml"
})
@XmlRootElement(name = "overwriteModel")
public class OverwriteModel {

    protected int modelOid;
    @XmlElementRef(name = "validFrom", namespace = "http://eclipse.org/stardust/ws/v2012a/api", type = JAXBElement.class)
    protected JAXBElement<Date> validFrom;
    @XmlElementRef(name = "validTo", namespace = "http://eclipse.org/stardust/ws/v2012a/api", type = JAXBElement.class)
    protected JAXBElement<Date> validTo;
    @XmlElementRef(name = "comment", namespace = "http://eclipse.org/stardust/ws/v2012a/api", type = JAXBElement.class)
    protected JAXBElement<String> comment;
    @XmlElementRef(name = "disabled", namespace = "http://eclipse.org/stardust/ws/v2012a/api", type = JAXBElement.class)
    protected JAXBElement<Boolean> disabled;
    @XmlElementRef(name = "ignoreWarnings", namespace = "http://eclipse.org/stardust/ws/v2012a/api", type = JAXBElement.class)
    protected JAXBElement<Boolean> ignoreWarnings;
    @XmlElementRef(name = "configuration", namespace = "http://eclipse.org/stardust/ws/v2012a/api", type = JAXBElement.class)
    protected JAXBElement<String> configuration;
    @XmlElement(required = true, nillable = true)
    protected XmlValueXto xml;

    /**
     * Gets the value of the modelOid property.
     * 
     */
    public int getModelOid() {
        return modelOid;
    }

    /**
     * Sets the value of the modelOid property.
     * 
     */
    public void setModelOid(int value) {
        this.modelOid = value;
    }

    /**
     * Gets the value of the validFrom property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Date }{@code >}
     *     
     */
    public JAXBElement<Date> getValidFrom() {
        return validFrom;
    }

    /**
     * Sets the value of the validFrom property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Date }{@code >}
     *     
     */
    public void setValidFrom(JAXBElement<Date> value) {
        this.validFrom = ((JAXBElement<Date> ) value);
    }

    /**
     * Gets the value of the validTo property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Date }{@code >}
     *     
     */
    public JAXBElement<Date> getValidTo() {
        return validTo;
    }

    /**
     * Sets the value of the validTo property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Date }{@code >}
     *     
     */
    public void setValidTo(JAXBElement<Date> value) {
        this.validTo = ((JAXBElement<Date> ) value);
    }

    /**
     * Gets the value of the comment property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getComment() {
        return comment;
    }

    /**
     * Sets the value of the comment property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setComment(JAXBElement<String> value) {
        this.comment = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the disabled property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public JAXBElement<Boolean> getDisabled() {
        return disabled;
    }

    /**
     * Sets the value of the disabled property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public void setDisabled(JAXBElement<Boolean> value) {
        this.disabled = ((JAXBElement<Boolean> ) value);
    }

    /**
     * Gets the value of the ignoreWarnings property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public JAXBElement<Boolean> getIgnoreWarnings() {
        return ignoreWarnings;
    }

    /**
     * Sets the value of the ignoreWarnings property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public void setIgnoreWarnings(JAXBElement<Boolean> value) {
        this.ignoreWarnings = ((JAXBElement<Boolean> ) value);
    }

    /**
     * Gets the value of the configuration property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getConfiguration() {
        return configuration;
    }

    /**
     * Sets the value of the configuration property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setConfiguration(JAXBElement<String> value) {
        this.configuration = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the xml property.
     * 
     * @return
     *     possible object is
     *     {@link XmlValueXto }
     *     
     */
    public XmlValueXto getXml() {
        return xml;
    }

    /**
     * Sets the value of the xml property.
     * 
     * @param value
     *     allowed object is
     *     {@link XmlValueXto }
     *     
     */
    public void setXml(XmlValueXto value) {
        this.xml = value;
    }

}
