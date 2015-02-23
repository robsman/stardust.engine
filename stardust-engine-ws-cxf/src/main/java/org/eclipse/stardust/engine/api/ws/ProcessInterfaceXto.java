
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;


/**
 * 
 * 			Interface used to separate a process definition from its interface.
 * 			
 * 
 * <p>Java-Klasse für ProcessInterface complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="ProcessInterface">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="declaringProcessDefintionId" type="{http://www.w3.org/2001/XMLSchema}QName"/>
 *         &lt;element name="formalParameters" type="{http://eclipse.org/stardust/ws/v2012a/api}FormalParameters" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProcessInterface", propOrder = {
    "declaringProcessDefintionId",
    "formalParameters"
})
public class ProcessInterfaceXto {

    @XmlElement(required = true)
    protected QName declaringProcessDefintionId;
    protected FormalParametersXto formalParameters;

    /**
     * Ruft den Wert der declaringProcessDefintionId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link QName }
     *     
     */
    public QName getDeclaringProcessDefintionId() {
        return declaringProcessDefintionId;
    }

    /**
     * Legt den Wert der declaringProcessDefintionId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link QName }
     *     
     */
    public void setDeclaringProcessDefintionId(QName value) {
        this.declaringProcessDefintionId = value;
    }

    /**
     * Ruft den Wert der formalParameters-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link FormalParametersXto }
     *     
     */
    public FormalParametersXto getFormalParameters() {
        return formalParameters;
    }

    /**
     * Legt den Wert der formalParameters-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link FormalParametersXto }
     *     
     */
    public void setFormalParameters(FormalParametersXto value) {
        this.formalParameters = value;
    }

}
