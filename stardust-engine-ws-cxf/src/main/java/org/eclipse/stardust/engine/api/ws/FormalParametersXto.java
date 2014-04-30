
package org.eclipse.stardust.engine.api.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for FormalParameters complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="FormalParameters">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="formalParameter" type="{http://eclipse.org/stardust/ws/v2012a/api}FormalParameter" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FormalParameters", propOrder = {
    "formalParameter"
})
public class FormalParametersXto {

    protected List<FormalParameterXto> formalParameter;

    /**
     * Gets the value of the formalParameter property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the formalParameter property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFormalParameter().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FormalParameterXto }
     *
     *
     */
    public List<FormalParameterXto> getFormalParameter() {
        if (formalParameter == null) {
            formalParameter = new ArrayList<FormalParameterXto>();
        }
        return this.formalParameter;
    }

}
