
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 			Result of an ProcessQuery execution.
 * 			
 * 
 * <p>Java-Klasse für ProcessInstanceQueryResult complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="ProcessInstanceQueryResult">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api}QueryResult">
 *       &lt;sequence>
 *         &lt;element name="processInstances" type="{http://eclipse.org/stardust/ws/v2012a/api}ProcessInstances" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProcessInstanceQueryResult", propOrder = {
    "processInstances"
})
public class ProcessInstanceQueryResultXto
    extends QueryResultXto
{

    protected ProcessInstancesXto processInstances;

    /**
     * Ruft den Wert der processInstances-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ProcessInstancesXto }
     *     
     */
    public ProcessInstancesXto getProcessInstances() {
        return processInstances;
    }

    /**
     * Legt den Wert der processInstances-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ProcessInstancesXto }
     *     
     */
    public void setProcessInstances(ProcessInstancesXto value) {
        this.processInstances = value;
    }

}
