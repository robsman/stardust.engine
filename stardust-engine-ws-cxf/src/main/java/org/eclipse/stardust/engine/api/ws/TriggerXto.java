
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 		    A client side view of a process trigger.
 *  			A trigger is responsible for starting the process instance corresponding to the
 *  			process definition containing the trigger.
 * 		    
 * 
 * <p>Java-Klasse für Trigger complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="Trigger">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api}ModelElement">
 *       &lt;sequence>
 *         &lt;element name="runtimeElementOid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="type" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="synchronous" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="accessPoints" type="{http://eclipse.org/stardust/ws/v2012a/api}AccessPoints" minOccurs="0"/>
 *         &lt;element name="parameterMappings" type="{http://eclipse.org/stardust/ws/v2012a/api}ParameterMappings" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Trigger", propOrder = {
    "runtimeElementOid",
    "type",
    "synchronous",
    "accessPoints",
    "parameterMappings"
})
public class TriggerXto
    extends ModelElementXto
{

    protected long runtimeElementOid;
    protected String type;
    protected boolean synchronous;
    protected AccessPointsXto accessPoints;
    protected ParameterMappingsXto parameterMappings;

    /**
     * Ruft den Wert der runtimeElementOid-Eigenschaft ab.
     * 
     */
    public long getRuntimeElementOid() {
        return runtimeElementOid;
    }

    /**
     * Legt den Wert der runtimeElementOid-Eigenschaft fest.
     * 
     */
    public void setRuntimeElementOid(long value) {
        this.runtimeElementOid = value;
    }

    /**
     * Ruft den Wert der type-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Legt den Wert der type-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Ruft den Wert der synchronous-Eigenschaft ab.
     * 
     */
    public boolean isSynchronous() {
        return synchronous;
    }

    /**
     * Legt den Wert der synchronous-Eigenschaft fest.
     * 
     */
    public void setSynchronous(boolean value) {
        this.synchronous = value;
    }

    /**
     * Ruft den Wert der accessPoints-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link AccessPointsXto }
     *     
     */
    public AccessPointsXto getAccessPoints() {
        return accessPoints;
    }

    /**
     * Legt den Wert der accessPoints-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link AccessPointsXto }
     *     
     */
    public void setAccessPoints(AccessPointsXto value) {
        this.accessPoints = value;
    }

    /**
     * Ruft den Wert der parameterMappings-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ParameterMappingsXto }
     *     
     */
    public ParameterMappingsXto getParameterMappings() {
        return parameterMappings;
    }

    /**
     * Legt den Wert der parameterMappings-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ParameterMappingsXto }
     *     
     */
    public void setParameterMappings(ParameterMappingsXto value) {
        this.parameterMappings = value;
    }

}
