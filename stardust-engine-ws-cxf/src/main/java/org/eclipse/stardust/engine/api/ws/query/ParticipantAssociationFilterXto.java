
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
 * <p>Java class for ParticipantAssociationFilter complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
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
     * Gets the value of the department property.
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
     * Sets the value of the department property.
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
     * Gets the value of the teamLeader property.
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
     * Sets the value of the teamLeader property.
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
     * Gets the value of the modelParticipant property.
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
     * Sets the value of the modelParticipant property.
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
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
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
        @XmlAttribute
        protected Boolean recursively;

        /**
         * Gets the value of the participant property.
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
         * Sets the value of the participant property.
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
         * Gets the value of the recursively property.
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
         * Sets the value of the recursively property.
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
