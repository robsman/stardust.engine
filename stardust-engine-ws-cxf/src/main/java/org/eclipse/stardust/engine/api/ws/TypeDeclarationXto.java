
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 	        A client side view of a type declaration.
 * 	        
 * 
 * <p>Java-Klasse für TypeDeclaration complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="TypeDeclaration">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api}ModelElement">
 *       &lt;sequence>
 *         &lt;element name="xpdlType" type="{http://eclipse.org/stardust/ws/v2012a/api}XpdlType"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TypeDeclaration", propOrder = {
    "xpdlType"
})
public class TypeDeclarationXto
    extends ModelElementXto
{

    @XmlElement(required = true)
    protected XpdlTypeXto xpdlType;

    /**
     * Ruft den Wert der xpdlType-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XpdlTypeXto }
     *     
     */
    public XpdlTypeXto getXpdlType() {
        return xpdlType;
    }

    /**
     * Legt den Wert der xpdlType-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XpdlTypeXto }
     *     
     */
    public void setXpdlType(XpdlTypeXto value) {
        this.xpdlType = value;
    }

}
