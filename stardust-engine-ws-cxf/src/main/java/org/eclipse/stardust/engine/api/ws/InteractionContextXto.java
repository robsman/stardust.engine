
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 			The InteractionContext represents the execution context of an activity.
 *  			An activity may have multiple execution contexts, depending on the implementation type.
 * 			
 * 
 * <p>Java-Klasse für InteractionContext complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="InteractionContext">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api}ModelElement">
 *       &lt;sequence>
 *         &lt;element name="inDataFlows" type="{http://eclipse.org/stardust/ws/v2012a/api}DataFlows"/>
 *         &lt;element name="outDataFlows" type="{http://eclipse.org/stardust/ws/v2012a/api}DataFlows"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InteractionContext", propOrder = {
    "inDataFlows",
    "outDataFlows"
})
public class InteractionContextXto
    extends ModelElementXto
{

    @XmlElement(required = true)
    protected DataFlowsXto inDataFlows;
    @XmlElement(required = true)
    protected DataFlowsXto outDataFlows;

    /**
     * Ruft den Wert der inDataFlows-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link DataFlowsXto }
     *     
     */
    public DataFlowsXto getInDataFlows() {
        return inDataFlows;
    }

    /**
     * Legt den Wert der inDataFlows-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link DataFlowsXto }
     *     
     */
    public void setInDataFlows(DataFlowsXto value) {
        this.inDataFlows = value;
    }

    /**
     * Ruft den Wert der outDataFlows-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link DataFlowsXto }
     *     
     */
    public DataFlowsXto getOutDataFlows() {
        return outDataFlows;
    }

    /**
     * Legt den Wert der outDataFlows-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link DataFlowsXto }
     *     
     */
    public void setOutDataFlows(DataFlowsXto value) {
        this.outDataFlows = value;
    }

}
