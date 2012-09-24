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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *         Filter criterion for matching process instances having links from or to other process instances.
 *         
 * 
 * <p>Java class for ProcessInstanceLinkFilter complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ProcessInstanceLinkFilter">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api/query}PredicateBase">
 *       &lt;sequence>
 *         &lt;element name="processOid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="direction" type="{http://eclipse.org/stardust/ws/v2012a/api/query}LinkDirection"/>
 *         &lt;element name="linkTypes">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="typeId" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProcessInstanceLinkFilter", propOrder = {
    "processOid",
    "direction",
    "linkTypes"
})
public class ProcessInstanceLinkFilterXto
    extends PredicateBaseXto
{

    protected long processOid;
    @XmlElement(required = true)
    protected LinkDirectionXto direction;
    @XmlElement(required = true)
    protected ProcessInstanceLinkFilterXto.LinkTypesXto linkTypes;

    /**
     * Gets the value of the processOid property.
     * 
     */
    public long getProcessOid() {
        return processOid;
    }

    /**
     * Sets the value of the processOid property.
     * 
     */
    public void setProcessOid(long value) {
        this.processOid = value;
    }

    /**
     * Gets the value of the direction property.
     * 
     * @return
     *     possible object is
     *     {@link LinkDirectionXto }
     *     
     */
    public LinkDirectionXto getDirection() {
        return direction;
    }

    /**
     * Sets the value of the direction property.
     * 
     * @param value
     *     allowed object is
     *     {@link LinkDirectionXto }
     *     
     */
    public void setDirection(LinkDirectionXto value) {
        this.direction = value;
    }

    /**
     * Gets the value of the linkTypes property.
     * 
     * @return
     *     possible object is
     *     {@link ProcessInstanceLinkFilterXto.LinkTypesXto }
     *     
     */
    public ProcessInstanceLinkFilterXto.LinkTypesXto getLinkTypes() {
        return linkTypes;
    }

    /**
     * Sets the value of the linkTypes property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProcessInstanceLinkFilterXto.LinkTypesXto }
     *     
     */
    public void setLinkTypes(ProcessInstanceLinkFilterXto.LinkTypesXto value) {
        this.linkTypes = value;
    }


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
     *         &lt;element name="typeId" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
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
        "typeId"
    })
    public static class LinkTypesXto {

        protected List<String> typeId;

        /**
         * Gets the value of the typeId property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the typeId property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getTypeId().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * 
         * 
         */
        public List<String> getTypeId() {
            if (typeId == null) {
                typeId = new ArrayList<String>();
            }
            return this.typeId;
        }

    }

}
