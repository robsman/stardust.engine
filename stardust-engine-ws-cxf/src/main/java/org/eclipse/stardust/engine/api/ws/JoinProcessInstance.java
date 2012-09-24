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
import javax.xml.bind.annotation.XmlElement;
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
 *         &lt;element name="processInstanceOid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="targetProcessInstanceOid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="comment" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
    "processInstanceOid",
    "targetProcessInstanceOid",
    "comment"
})
@XmlRootElement(name = "joinProcessInstance")
public class JoinProcessInstance {

    protected long processInstanceOid;
    protected long targetProcessInstanceOid;
    @XmlElement(required = true)
    protected String comment;

    /**
     * Gets the value of the processInstanceOid property.
     * 
     */
    public long getProcessInstanceOid() {
        return processInstanceOid;
    }

    /**
     * Sets the value of the processInstanceOid property.
     * 
     */
    public void setProcessInstanceOid(long value) {
        this.processInstanceOid = value;
    }

    /**
     * Gets the value of the targetProcessInstanceOid property.
     * 
     */
    public long getTargetProcessInstanceOid() {
        return targetProcessInstanceOid;
    }

    /**
     * Sets the value of the targetProcessInstanceOid property.
     * 
     */
    public void setTargetProcessInstanceOid(long value) {
        this.targetProcessInstanceOid = value;
    }

    /**
     * Gets the value of the comment property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the value of the comment property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setComment(String value) {
        this.comment = value;
    }

}
