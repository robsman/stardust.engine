
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
 * <p>Java class for ProcessInterface complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
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
     * Gets the value of the declaringProcessDefintionId property.
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
     * Sets the value of the declaringProcessDefintionId property.
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
     * Gets the value of the formalParameters property.
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
     * Sets the value of the formalParameters property.
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
