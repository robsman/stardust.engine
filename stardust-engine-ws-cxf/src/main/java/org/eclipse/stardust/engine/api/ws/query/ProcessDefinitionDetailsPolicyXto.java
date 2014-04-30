
package org.eclipse.stardust.engine.api.ws.query;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 *
 *          Evaluation Policy for specifying details level of processDefinitions.
 *
 *
 * <p>Java class for ProcessDefinitionDetailsPolicy complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ProcessDefinitionDetailsPolicy">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api/query}EvaluationPolicy">
 *       &lt;sequence>
 *         &lt;element name="detailsLevel" type="{http://eclipse.org/stardust/ws/v2012a/api/query}ProcessDefinitionDetailsLevel"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProcessDefinitionDetailsPolicy", propOrder = {
    "detailsLevel"
})
public class ProcessDefinitionDetailsPolicyXto
    extends EvaluationPolicyXto
{

    @XmlElement(required = true)
    protected ProcessDefinitionDetailsLevelXto detailsLevel;

    /**
     * Gets the value of the detailsLevel property.
     *
     * @return
     *     possible object is
     *     {@link ProcessDefinitionDetailsLevelXto }
     *
     */
    public ProcessDefinitionDetailsLevelXto getDetailsLevel() {
        return detailsLevel;
    }

    /**
     * Sets the value of the detailsLevel property.
     *
     * @param value
     *     allowed object is
     *     {@link ProcessDefinitionDetailsLevelXto }
     *
     */
    public void setDetailsLevel(ProcessDefinitionDetailsLevelXto value) {
        this.detailsLevel = value;
    }

}
