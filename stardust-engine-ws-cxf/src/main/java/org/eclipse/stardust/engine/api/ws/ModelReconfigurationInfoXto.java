
package org.eclipse.stardust.engine.api.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 				The ModelReconfigurationInfo class is used to receive information about a model reconfiguration operation.
 * 				Model reconfiguration operations are all operations which modifies the models in audit trail, their attributes or behavior,
 * 				e.g. model deployment, configuration variable modification.
 * 			
 * 
 * <p>Java-Klasse für ModelReconfigurationInfo complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="ModelReconfigurationInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="modelOid" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="errors" type="{http://eclipse.org/stardust/ws/v2012a/api}Inconsistency" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="warnings" type="{http://eclipse.org/stardust/ws/v2012a/api}Inconsistency" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ModelReconfigurationInfo", propOrder = {
    "modelOid",
    "id",
    "errors",
    "warnings"
})
public class ModelReconfigurationInfoXto {

    protected int modelOid;
    @XmlElement(required = true)
    protected String id;
    protected List<InconsistencyXto> errors;
    protected List<InconsistencyXto> warnings;

    /**
     * Ruft den Wert der modelOid-Eigenschaft ab.
     * 
     */
    public int getModelOid() {
        return modelOid;
    }

    /**
     * Legt den Wert der modelOid-Eigenschaft fest.
     * 
     */
    public void setModelOid(int value) {
        this.modelOid = value;
    }

    /**
     * Ruft den Wert der id-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Legt den Wert der id-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the errors property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the errors property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getErrors().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link InconsistencyXto }
     * 
     * 
     */
    public List<InconsistencyXto> getErrors() {
        if (errors == null) {
            errors = new ArrayList<InconsistencyXto>();
        }
        return this.errors;
    }

    /**
     * Gets the value of the warnings property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the warnings property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getWarnings().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link InconsistencyXto }
     * 
     * 
     */
    public List<InconsistencyXto> getWarnings() {
        if (warnings == null) {
            warnings = new ArrayList<InconsistencyXto>();
        }
        return this.warnings;
    }

}
