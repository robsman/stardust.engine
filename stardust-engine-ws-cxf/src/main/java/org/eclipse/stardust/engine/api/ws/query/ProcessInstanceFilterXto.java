
package org.eclipse.stardust.engine.api.ws.query;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *         Filter criterion for matching specific process instances.
 *         
 * 
 * <p>Java-Klasse für ProcessInstanceFilter complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="ProcessInstanceFilter">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api/query}PredicateBase">
 *       &lt;sequence>
 *         &lt;element name="processOid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *       &lt;/sequence>
 *       &lt;attribute name="includingSubprocesses" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProcessInstanceFilter", propOrder = {
    "processOid"
})
public class ProcessInstanceFilterXto
    extends PredicateBaseXto
{

    protected long processOid;
    @XmlAttribute(name = "includingSubprocesses")
    protected Boolean includingSubprocesses;

    /**
     * Ruft den Wert der processOid-Eigenschaft ab.
     * 
     */
    public long getProcessOid() {
        return processOid;
    }

    /**
     * Legt den Wert der processOid-Eigenschaft fest.
     * 
     */
    public void setProcessOid(long value) {
        this.processOid = value;
    }

    /**
     * Ruft den Wert der includingSubprocesses-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isIncludingSubprocesses() {
        if (includingSubprocesses == null) {
            return true;
        } else {
            return includingSubprocesses;
        }
    }

    /**
     * Legt den Wert der includingSubprocesses-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIncludingSubprocesses(Boolean value) {
        this.includingSubprocesses = value;
    }

}
