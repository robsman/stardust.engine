
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
 *           &lt;element name="subFolderPolicy" type="{http://eclipse.org/stardust/ws/v2012a/api/query}SubFolderPolicy" minOccurs="0"/>
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
 *           &lt;element name="evaluateByWorkitemsPolicy" type="{http://eclipse.org/stardust/ws/v2012a/api/query}EvaluateByWorkitemsPolicy"/>
 *           &lt;element name="excludeUserPolicy" type="{http://eclipse.org/stardust/ws/v2012a/api/query}ExcludeUserPolicy"/>
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
    "subsetPolicyOrSubFolderPolicyOrModelVersionPolicy"
})
public class PolicyXto {

    @XmlElements({
        @XmlElement(name = "performanceCriticalityPolicy", type = PerformanceCriticalityPolicyXto.class),
        @XmlElement(name = "descriptorPolicy", type = DescriptorPolicyXto.class),
        @XmlElement(name = "criticalExecutionTimePolicy", type = CriticalExecutionTimePolicyXto.class),
        @XmlElement(name = "processInstanceDetailsPolicy", type = ProcessInstanceDetailsPolicyXto.class),
        @XmlElement(name = "modelVersionPolicy", type = ModelVersionPolicyXto.class),
        @XmlElement(name = "excludeUserPolicy", type = ExcludeUserPolicyXto.class),
        @XmlElement(name = "historicalStatesPolicy", type = HistoricalStatesPolicyXto.class),
        @XmlElement(name = "criticalCostPerExecutionPolicy", type = CriticalCostPerExecutionPolicyXto.class),
        @XmlElement(name = "userDetailsPolicy", type = UserDetailsPolicyXto.class),
        @XmlElement(name = "historicalEventPolicy", type = HistoricalEventPolicyXto.class),
        @XmlElement(name = "criticalProcessingTimePolicy", type = CriticalProcessingTimePolicyXto.class),
        @XmlElement(name = "subFolderPolicy", type = SubFolderPolicyXto.class),
        @XmlElement(name = "evaluateByWorkitemsPolicy", type = EvaluateByWorkitemsPolicyXto.class),
        @XmlElement(name = "subsetPolicy", type = SubsetPolicyXto.class),
        @XmlElement(name = "processCumulationPolicy", type = ProcessCumulationPolicyXto.class),
        @XmlElement(name = "casePolicy", type = CasePolicyXto.class),
        @XmlElement(name = "timeoutPolicy", type = TimeoutPolicyXto.class)
    })
    protected List<EvaluationPolicyXto> subsetPolicyOrSubFolderPolicyOrModelVersionPolicy;

    /**
     * Gets the value of the subsetPolicyOrSubFolderPolicyOrModelVersionPolicy property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the subsetPolicyOrSubFolderPolicyOrModelVersionPolicy property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSubsetPolicyOrSubFolderPolicyOrModelVersionPolicy().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PerformanceCriticalityPolicyXto }
     * {@link DescriptorPolicyXto }
     * {@link CriticalExecutionTimePolicyXto }
     * {@link ProcessInstanceDetailsPolicyXto }
     * {@link ModelVersionPolicyXto }
     * {@link ExcludeUserPolicyXto }
     * {@link HistoricalStatesPolicyXto }
     * {@link CriticalCostPerExecutionPolicyXto }
     * {@link UserDetailsPolicyXto }
     * {@link HistoricalEventPolicyXto }
     * {@link CriticalProcessingTimePolicyXto }
     * {@link SubFolderPolicyXto }
     * {@link EvaluateByWorkitemsPolicyXto }
     * {@link SubsetPolicyXto }
     * {@link ProcessCumulationPolicyXto }
     * {@link CasePolicyXto }
     * {@link TimeoutPolicyXto }
     * 
     * 
     */
    public List<EvaluationPolicyXto> getSubsetPolicyOrSubFolderPolicyOrModelVersionPolicy() {
        if (subsetPolicyOrSubFolderPolicyOrModelVersionPolicy == null) {
            subsetPolicyOrSubFolderPolicyOrModelVersionPolicy = new ArrayList<EvaluationPolicyXto>();
        }
        return this.subsetPolicyOrSubFolderPolicyOrModelVersionPolicy;
    }

}
