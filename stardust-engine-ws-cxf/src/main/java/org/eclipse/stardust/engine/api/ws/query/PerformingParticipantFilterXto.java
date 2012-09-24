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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.eclipse.stardust.engine.api.ws.ParticipantInfoXto;


/**
 * 
 *         Restricts the resulting items to the ones that are performed by the specified user group or model participant.
 *         When using 'anyForUser' element the result is restricted to items performed by any participant (roles, organizations or user groups) associated with the calling user.
 *         Finding such participants will perform a deep search.
 *         
 * 
 * <p>Java class for PerformingParticipantFilter complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PerformingParticipantFilter">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api/query}PredicateBase">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element name="anyForUser">
 *             &lt;complexType>
 *               &lt;complexContent>
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;/restriction>
 *               &lt;/complexContent>
 *             &lt;/complexType>
 *           &lt;/element>
 *           &lt;element name="modelParticipant">
 *             &lt;complexType>
 *               &lt;complexContent>
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   &lt;sequence>
 *                     &lt;element name="participant" type="{http://eclipse.org/stardust/ws/v2012a/api}ParticipantInfo"/>
 *                   &lt;/sequence>
 *                   &lt;attribute name="recursively" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *                 &lt;/restriction>
 *               &lt;/complexContent>
 *             &lt;/complexType>
 *           &lt;/element>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PerformingParticipantFilter", propOrder = {
    "anyForUser",
    "modelParticipant"
})
public class PerformingParticipantFilterXto
    extends PredicateBaseXto
{

    protected PerformingParticipantFilterXto.AnyForUserXto anyForUser;
    protected PerformingParticipantFilterXto.ModelParticipantXto modelParticipant;

    /**
     * Gets the value of the anyForUser property.
     * 
     * @return
     *     possible object is
     *     {@link PerformingParticipantFilterXto.AnyForUserXto }
     *     
     */
    public PerformingParticipantFilterXto.AnyForUserXto getAnyForUser() {
        return anyForUser;
    }

    /**
     * Sets the value of the anyForUser property.
     * 
     * @param value
     *     allowed object is
     *     {@link PerformingParticipantFilterXto.AnyForUserXto }
     *     
     */
    public void setAnyForUser(PerformingParticipantFilterXto.AnyForUserXto value) {
        this.anyForUser = value;
    }

    /**
     * Gets the value of the modelParticipant property.
     * 
     * @return
     *     possible object is
     *     {@link PerformingParticipantFilterXto.ModelParticipantXto }
     *     
     */
    public PerformingParticipantFilterXto.ModelParticipantXto getModelParticipant() {
        return modelParticipant;
    }

    /**
     * Sets the value of the modelParticipant property.
     * 
     * @param value
     *     allowed object is
     *     {@link PerformingParticipantFilterXto.ModelParticipantXto }
     *     
     */
    public void setModelParticipant(PerformingParticipantFilterXto.ModelParticipantXto value) {
        this.modelParticipant = value;
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
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class AnyForUserXto {


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
     *         &lt;element name="participant" type="{http://eclipse.org/stardust/ws/v2012a/api}ParticipantInfo"/>
     *       &lt;/sequence>
     *       &lt;attribute name="recursively" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "participant"
    })
    public static class ModelParticipantXto {

        @XmlElement(required = true)
        protected ParticipantInfoXto participant;
        @XmlAttribute
        protected Boolean recursively;

        /**
         * Gets the value of the participant property.
         * 
         * @return
         *     possible object is
         *     {@link ParticipantInfoXto }
         *     
         */
        public ParticipantInfoXto getParticipant() {
            return participant;
        }

        /**
         * Sets the value of the participant property.
         * 
         * @param value
         *     allowed object is
         *     {@link ParticipantInfoXto }
         *     
         */
        public void setParticipant(ParticipantInfoXto value) {
            this.participant = value;
        }

        /**
         * Gets the value of the recursively property.
         * 
         * @return
         *     possible object is
         *     {@link Boolean }
         *     
         */
        public boolean isRecursively() {
            if (recursively == null) {
                return true;
            } else {
                return recursively;
            }
        }

        /**
         * Sets the value of the recursively property.
         * 
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *     
         */
        public void setRecursively(Boolean value) {
            this.recursively = value;
        }

    }

}
