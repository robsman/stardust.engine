
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 			Result of an ProcessDefinitionQuery execution.
 * 			
 * 
 * <p>Java-Klasse für ProcessDefinitionQueryResult complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="ProcessDefinitionQueryResult">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api}QueryResult">
 *       &lt;sequence>
 *         &lt;element name="processDefinitions" type="{http://eclipse.org/stardust/ws/v2012a/api}ProcessDefinitions" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProcessDefinitionQueryResult", propOrder = {
    "processDefinitions"
})
public class ProcessDefinitionQueryResultXto
    extends QueryResultXto
{

    protected ProcessDefinitionsXto processDefinitions;

    /**
     * Ruft den Wert der processDefinitions-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ProcessDefinitionsXto }
     *     
     */
    public ProcessDefinitionsXto getProcessDefinitions() {
        return processDefinitions;
    }

    /**
     * Legt den Wert der processDefinitions-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ProcessDefinitionsXto }
     *     
     */
    public void setProcessDefinitions(ProcessDefinitionsXto value) {
        this.processDefinitions = value;
    }

}
