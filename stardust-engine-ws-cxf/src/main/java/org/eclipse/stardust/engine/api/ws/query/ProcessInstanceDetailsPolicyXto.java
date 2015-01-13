
package org.eclipse.stardust.engine.api.ws.query;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.eclipse.stardust.engine.api.ws.ProcessInstanceDetailsOptionsXto;


/**
 * 
 *          Evaluation Policy for specifying details level of processInstances.
 *          
 * 
 * <p>Java-Klasse für ProcessInstanceDetailsPolicy complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="ProcessInstanceDetailsPolicy">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api/query}EvaluationPolicy">
 *       &lt;sequence>
 *         &lt;element name="detailsLevel" type="{http://eclipse.org/stardust/ws/v2012a/api/query}ProcessInstanceDetailsLevel"/>
 *         &lt;element name="detailsOptions" type="{http://eclipse.org/stardust/ws/v2012a/api}ProcessInstanceDetailsOptions"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProcessInstanceDetailsPolicy", propOrder = {
    "detailsLevel",
    "detailsOptions"
})
public class ProcessInstanceDetailsPolicyXto
    extends EvaluationPolicyXto
{

    @XmlElement(required = true)
    protected ProcessInstanceDetailsLevelXto detailsLevel;
    @XmlElement(required = true)
    protected ProcessInstanceDetailsOptionsXto detailsOptions;

    /**
     * Ruft den Wert der detailsLevel-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ProcessInstanceDetailsLevelXto }
     *     
     */
    public ProcessInstanceDetailsLevelXto getDetailsLevel() {
        return detailsLevel;
    }

    /**
     * Legt den Wert der detailsLevel-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ProcessInstanceDetailsLevelXto }
     *     
     */
    public void setDetailsLevel(ProcessInstanceDetailsLevelXto value) {
        this.detailsLevel = value;
    }

    /**
     * Ruft den Wert der detailsOptions-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ProcessInstanceDetailsOptionsXto }
     *     
     */
    public ProcessInstanceDetailsOptionsXto getDetailsOptions() {
        return detailsOptions;
    }

    /**
     * Legt den Wert der detailsOptions-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ProcessInstanceDetailsOptionsXto }
     *     
     */
    public void setDetailsOptions(ProcessInstanceDetailsOptionsXto value) {
        this.detailsOptions = value;
    }

}
