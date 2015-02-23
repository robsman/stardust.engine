
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 	        The client view of a workflow application.
 *  			Applications are software programs that interact with the Eclipse Process
 *  			Manager handling the processing required to support a particular activity in
 *  			whole or in part. Multiple activities may use the same application but only one
 *  			application may be executed within an activity.
 * 	        
 * 
 * <p>Java-Klasse für Application complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="Application">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api}ModelElement">
 *       &lt;sequence>
 *         &lt;element name="accessPoints" type="{http://eclipse.org/stardust/ws/v2012a/api}AccessPoints" minOccurs="0"/>
 *         &lt;element name="typeAttributes" type="{http://eclipse.org/stardust/ws/v2012a/api}Attributes" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Application", propOrder = {
    "accessPoints",
    "typeAttributes"
})
public class ApplicationXto
    extends ModelElementXto
{

    protected AccessPointsXto accessPoints;
    protected AttributesXto typeAttributes;

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
     * Ruft den Wert der typeAttributes-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link AttributesXto }
     *     
     */
    public AttributesXto getTypeAttributes() {
        return typeAttributes;
    }

    /**
     * Legt den Wert der typeAttributes-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link AttributesXto }
     *     
     */
    public void setTypeAttributes(AttributesXto value) {
        this.typeAttributes = value;
    }

}
