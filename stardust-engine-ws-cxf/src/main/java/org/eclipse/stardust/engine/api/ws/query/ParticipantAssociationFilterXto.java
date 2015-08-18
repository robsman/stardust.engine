
package org.eclipse.stardust.engine.api.ws.query;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.eclipse.stardust.engine.api.ws.DepartmentInfoXto;
import org.eclipse.stardust.engine.api.ws.ParticipantInfoXto;
import org.eclipse.stardust.engine.api.ws.RoleInfoXto;


/**
 * 
 *         Filter criterion for restricting results of UserQuery to users having granted
 *  		specific roles/organizations or being members of specific user groups.
 *  		A usage examples is to retrieve all users being administrators.
 *         
 * 
 * <p>Java-Klasse für ParticipantAssociationFilter complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="ParticipantAssociationFilter">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api/query}PredicateBase">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element name="department" type="{http://eclipse.org/stardust/ws/v2012a/api}DepartmentInfo"/>
 *           &lt;element name="teamLeader" type="{http://eclipse.org/stardust/ws/v2012a/api}RoleInfo"/>
 *           &lt;element name="modelParticipant">
 *             &lt;complexType>
 *               &lt;complexContent>
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   &lt;sequence>
 *                     &lt;element name="participant" type="{http://eclipse.org/stardust/ws/v2012a/api}ParticipantInfo"/>
 *                   &lt;/sequence>
 *                   &lt;attribute name="recursively" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *                 &lt;/restriction>
 *               &lt;/complexContent>
 *             &lt;/complexType>
 *           &lt;/element>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ParticipantAssociationFilter", propOrder = {
    "department",
    "teamLeader",
    "modelParticipant"
})
public class ParticipantAssociationFilterXto
    extends PredicateBaseXto
{

    protected DepartmentInfoXto department;
    protected RoleInfoXto teamLeader;
    protected ParticipantAssociationFilterXto.ModelParticipantXto modelParticipant;

    /**
     * Ruft den Wert der department-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link DepartmentInfoXto }
     *     
     */
    public DepartmentInfoXto getDepartment() {
        return department;
    }

    /**
     * Legt den Wert der department-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link DepartmentInfoXto }
     *     
     */
    public void setDepartment(DepartmentInfoXto value) {
        this.department = value;
    }

    /**
     * Ruft den Wert der teamLeader-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link RoleInfoXto }
     *     
     */
    public RoleInfoXto getTeamLeader() {
        return teamLeader;
    }

    /**
     * Legt den Wert der teamLeader-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link RoleInfoXto }
     *     
     */
    public void setTeamLeader(RoleInfoXto value) {
        this.teamLeader = value;
    }

    /**
     * Ruft den Wert der modelParticipant-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ParticipantAssociationFilterXto.ModelParticipantXto }
     *     
     */
    public ParticipantAssociationFilterXto.ModelParticipantXto getModelParticipant() {
        return modelParticipant;
    }

    /**
     * Legt den Wert der modelParticipant-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ParticipantAssociationFilterXto.ModelParticipantXto }
     *     
     */
    public void setModelParticipant(ParticipantAssociationFilterXto.ModelParticipantXto value) {
        this.modelParticipant = value;
    }


    /**
     * <p>Java-Klasse für anonymous complex type.
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="participant" type="{http://eclipse.org/stardust/ws/v2012a/api}ParticipantInfo"/>
     *       &lt;/sequence>
     *       &lt;attribute name="recursively" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "participant"
    })
    public static class ModelParticipantXto {

        @XmlElement(required = true)
        protected ParticipantInfoXto participant;
        @XmlAttribute(name = "recursively")
        protected Boolean recursively;

        /**
         * Ruft den Wert der participant-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link ParticipantInfoXto }
         *     
         */
        public ParticipantInfoXto getParticipant() {
            return participant;
        }

        /**
         * Legt den Wert der participant-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link ParticipantInfoXto }
         *     
         */
        public void setParticipant(ParticipantInfoXto value) {
            this.participant = value;
        }

        /**
         * Ruft den Wert der recursively-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link Boolean }
         *     
         */
        public boolean isRecursively() {
            if (recursively == null) {
                return false;
            } else {
                return recursively;
            }
        }

        /**
         * Legt den Wert der recursively-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *     
         */
        public void setRecursively(Boolean value) {
            this.recursively = value;
        }

    }

}
