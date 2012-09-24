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
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *          An evaluation policy selects (parts of) the strategy for query evaluation, i.e.
 *          which model version to use or what subset to deliver.
 *          
 * 
 * <p>Java class for Policy complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Policy">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="subsetPolicy" type="{http://eclipse.org/stardust/ws/v2012a/api/query}SubsetPolicy" minOccurs="0"/>
 *           &lt;element name="modelVersionPolicy" type="{http://eclipse.org/stardust/ws/v2012a/api/query}ModelVersionPolicy" minOccurs="0"/>
 *           &lt;element name="descriptorPolicy" type="{http://eclipse.org/stardust/ws/v2012a/api/query}DescriptorPolicy" minOccurs="0"/>
 *           &lt;element name="processInstanceDetailsPolicy" type="{http://eclipse.org/stardust/ws/v2012a/api/query}ProcessInstanceDetailsPolicy" minOccurs="0"/>
 *           &lt;element name="historicalStatesPolicy" type="{http://eclipse.org/stardust/ws/v2012a/api/query}HistoricalStatesPolicy" minOccurs="0"/>
 *           &lt;element name="historicalEventPolicy" type="{http://eclipse.org/stardust/ws/v2012a/api/query}HistoricalEventPolicy" minOccurs="0"/>
 *           &lt;element name="criticalExecutionTimePolicy" type="{http://eclipse.org/stardust/ws/v2012a/api/query}CriticalExecutionTimePolicy" minOccurs="0"/>
 *           &lt;element name="criticalCostPerExecutionPolicy" type="{http://eclipse.org/stardust/ws/v2012a/api/query}CriticalCostPerExecutionPolicy" minOccurs="0"/>
 *           &lt;element name="criticalProcessingTimePolicy" type="{http://eclipse.org/stardust/ws/v2012a/api/query}CriticalProcessingTimePolicy" minOccurs="0"/>
 *           &lt;element name="performanceCriticalityPolicy" type="{http://eclipse.org/stardust/ws/v2012a/api/query}PerformanceCriticalityPolicy" minOccurs="0"/>
 *           &lt;element name="processCumulationPolicy" type="{http://eclipse.org/stardust/ws/v2012a/api/query}ProcessCumulationPolicy" minOccurs="0"/>
 *           &lt;element name="timeoutPolicy" type="{http://eclipse.org/stardust/ws/v2012a/api/query}TimeoutPolicy" minOccurs="0"/>
 *           &lt;element name="userDetailsPolicy" type="{http://eclipse.org/stardust/ws/v2012a/api/query}UserDetailsPolicy" minOccurs="0"/>
 *           &lt;element name="casePolicy" type="{http://eclipse.org/stardust/ws/v2012a/api/query}CasePolicy" minOccurs="0"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Policy", propOrder = {
    "subsetPolicyOrModelVersionPolicyOrDescriptorPolicy"
})
public class PolicyXto {

    @XmlElements({
        @XmlElement(name = "modelVersionPolicy", type = ModelVersionPolicyXto.class),
        @XmlElement(name = "historicalEventPolicy", type = HistoricalEventPolicyXto.class),
        @XmlElement(name = "criticalExecutionTimePolicy", type = CriticalExecutionTimePolicyXto.class),
        @XmlElement(name = "userDetailsPolicy", type = UserDetailsPolicyXto.class),
        @XmlElement(name = "casePolicy", type = CasePolicyXto.class),
        @XmlElement(name = "criticalCostPerExecutionPolicy", type = CriticalCostPerExecutionPolicyXto.class),
        @XmlElement(name = "timeoutPolicy", type = TimeoutPolicyXto.class),
        @XmlElement(name = "subsetPolicy", type = SubsetPolicyXto.class),
        @XmlElement(name = "criticalProcessingTimePolicy", type = CriticalProcessingTimePolicyXto.class),
        @XmlElement(name = "processCumulationPolicy", type = ProcessCumulationPolicyXto.class),
        @XmlElement(name = "historicalStatesPolicy", type = HistoricalStatesPolicyXto.class),
        @XmlElement(name = "performanceCriticalityPolicy", type = PerformanceCriticalityPolicyXto.class),
        @XmlElement(name = "processInstanceDetailsPolicy", type = ProcessInstanceDetailsPolicyXto.class),
        @XmlElement(name = "descriptorPolicy", type = DescriptorPolicyXto.class)
    })
    protected List<EvaluationPolicyXto> subsetPolicyOrModelVersionPolicyOrDescriptorPolicy;

    /**
     * Gets the value of the subsetPolicyOrModelVersionPolicyOrDescriptorPolicy property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the subsetPolicyOrModelVersionPolicyOrDescriptorPolicy property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSubsetPolicyOrModelVersionPolicyOrDescriptorPolicy().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ModelVersionPolicyXto }
     * {@link HistoricalEventPolicyXto }
     * {@link CriticalExecutionTimePolicyXto }
     * {@link UserDetailsPolicyXto }
     * {@link CasePolicyXto }
     * {@link CriticalCostPerExecutionPolicyXto }
     * {@link TimeoutPolicyXto }
     * {@link SubsetPolicyXto }
     * {@link CriticalProcessingTimePolicyXto }
     * {@link ProcessCumulationPolicyXto }
     * {@link HistoricalStatesPolicyXto }
     * {@link PerformanceCriticalityPolicyXto }
     * {@link ProcessInstanceDetailsPolicyXto }
     * {@link DescriptorPolicyXto }
     * 
     * 
     */
    public List<EvaluationPolicyXto> getSubsetPolicyOrModelVersionPolicyOrDescriptorPolicy() {
        if (subsetPolicyOrModelVersionPolicyOrDescriptorPolicy == null) {
            subsetPolicyOrModelVersionPolicyOrDescriptorPolicy = new ArrayList<EvaluationPolicyXto>();
        }
        return this.subsetPolicyOrModelVersionPolicyOrDescriptorPolicy;
    }

}
