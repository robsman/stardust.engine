
package org.eclipse.stardust.engine.api.ws.query;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for EvaluationPolicy complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="EvaluationPolicy">
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
@XmlType(name = "EvaluationPolicy")
@XmlSeeAlso({
    EvaluateByWorkitemsPolicyXto.class,
    ProcessInstanceDetailsPolicyXto.class,
    TimeoutPolicyXto.class,
    ProcessDefinitionDetailsPolicyXto.class,
    UserDetailsPolicyXto.class,
    SubFolderPolicyXto.class,
    CasePolicyXto.class,
    PerformanceCriticalityPolicyXto.class,
    ModelVersionPolicyXto.class,
    ExcludeUserPolicyXto.class,
    DescriptorPolicyXto.class,
    HistoricalEventPolicyXto.class,
    SubsetPolicyXto.class,
    HistoricalStatesPolicyXto.class,
    ProcessCumulationPolicyXto.class,
    AbstractCriticalityPolicyXto.class,
    AbstractStoplightPolicyXto.class
})
public abstract class EvaluationPolicyXto {


}
