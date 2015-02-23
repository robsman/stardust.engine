
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 	        XPDL specific type declaration definition object.
 * 	        
 * 
 * <p>Java-Klasse für XpdlType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="XpdlType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="externalReference" type="{http://eclipse.org/stardust/ws/v2012a/api}ExternalReference" minOccurs="0"/>
 *         &lt;element name="schemaType" type="{http://eclipse.org/stardust/ws/v2012a/api}SchemaType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "XpdlType", propOrder = {
    "externalReference",
    "schemaType"
})
public class XpdlTypeXto {

    protected ExternalReferenceXto externalReference;
    protected SchemaTypeXto schemaType;

    /**
     * Ruft den Wert der externalReference-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ExternalReferenceXto }
     *     
     */
    public ExternalReferenceXto getExternalReference() {
        return externalReference;
    }

    /**
     * Legt den Wert der externalReference-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ExternalReferenceXto }
     *     
     */
    public void setExternalReference(ExternalReferenceXto value) {
        this.externalReference = value;
    }

    /**
     * Ruft den Wert der schemaType-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link SchemaTypeXto }
     *     
     */
    public SchemaTypeXto getSchemaType() {
        return schemaType;
    }

    /**
     * Legt den Wert der schemaType-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link SchemaTypeXto }
     *     
     */
    public void setSchemaType(SchemaTypeXto value) {
        this.schemaType = value;
    }

}
