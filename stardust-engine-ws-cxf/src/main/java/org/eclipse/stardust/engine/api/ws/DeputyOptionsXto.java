
package org.eclipse.stardust.engine.api.ws;

import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.eclipse.stardust.engine.api.ws.xsd.Adapter1;


/**
 * <p>Java class for DeputyOptions complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DeputyOptions">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="fromDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="toDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="modelParticipantInfos" type="{http://eclipse.org/stardust/ws/v2012a/api}ModelParticipantInfos"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DeputyOptions", propOrder = {
    "fromDate",
    "toDate",
    "modelParticipantInfos"
})
public class DeputyOptionsXto {

    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Date fromDate;
    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Date toDate;
    @XmlElement(required = true)
    protected ModelParticipantInfosXto modelParticipantInfos;

    /**
     * Gets the value of the fromDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Date getFromDate() {
        return fromDate;
    }

    /**
     * Sets the value of the fromDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFromDate(Date value) {
        this.fromDate = value;
    }

    /**
     * Gets the value of the toDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Date getToDate() {
        return toDate;
    }

    /**
     * Sets the value of the toDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setToDate(Date value) {
        this.toDate = value;
    }

    /**
     * Gets the value of the modelParticipantInfos property.
     * 
     * @return
     *     possible object is
     *     {@link ModelParticipantInfosXto }
     *     
     */
    public ModelParticipantInfosXto getModelParticipantInfos() {
        return modelParticipantInfos;
    }

    /**
     * Sets the value of the modelParticipantInfos property.
     * 
     * @param value
     *     allowed object is
     *     {@link ModelParticipantInfosXto }
     *     
     */
    public void setModelParticipantInfos(ModelParticipantInfosXto value) {
        this.modelParticipantInfos = value;
    }

}
