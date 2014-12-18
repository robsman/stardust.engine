
package org.eclipse.stardust.engine.api.ws.query;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 		    Contains various elements which work as filters for a query.
 * 		    The elements 'and' and 'or' can contain a nested PredicateTerm for more complex filtering.
 * 		    
 * 
 * <p>Java class for PredicateTerm complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PredicateTerm">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api/query}PredicateBase">
 *       &lt;sequence>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="and" type="{http://eclipse.org/stardust/ws/v2012a/api/query}AndTerm"/>
 *           &lt;element name="or" type="{http://eclipse.org/stardust/ws/v2012a/api/query}OrTerm"/>
 *           &lt;element name="isNull" type="{http://eclipse.org/stardust/ws/v2012a/api/query}IsNullPredicate"/>
 *           &lt;element name="notNull" type="{http://eclipse.org/stardust/ws/v2012a/api/query}NotNullPredicate"/>
 *           &lt;element name="isEqual" type="{http://eclipse.org/stardust/ws/v2012a/api/query}IsEqualPredicate"/>
 *           &lt;element name="notEqual" type="{http://eclipse.org/stardust/ws/v2012a/api/query}NotEqualPredicate"/>
 *           &lt;element name="lessThan" type="{http://eclipse.org/stardust/ws/v2012a/api/query}LessThanPredicate"/>
 *           &lt;element name="lessOrEqual" type="{http://eclipse.org/stardust/ws/v2012a/api/query}LessOrEqualPredicate"/>
 *           &lt;element name="greaterOrEqual" type="{http://eclipse.org/stardust/ws/v2012a/api/query}GreaterOrEqualPredicate"/>
 *           &lt;element name="greaterThan" type="{http://eclipse.org/stardust/ws/v2012a/api/query}GreaterThanPredicate"/>
 *           &lt;element name="isLike" type="{http://eclipse.org/stardust/ws/v2012a/api/query}IsLikePredicate"/>
 *           &lt;element name="inList" type="{http://eclipse.org/stardust/ws/v2012a/api/query}InListPredicate"/>
 *           &lt;element name="notInList" type="{http://eclipse.org/stardust/ws/v2012a/api/query}NotInListPredicate"/>
 *           &lt;element name="notAnyOf" type="{http://eclipse.org/stardust/ws/v2012a/api/query}NotAnyOfPredicate"/>
 *           &lt;element name="between" type="{http://eclipse.org/stardust/ws/v2012a/api/query}BetweenPredicate"/>
 *           &lt;element name="forProcessDefinition" type="{http://eclipse.org/stardust/ws/v2012a/api/query}ProcessDefinitionFilter"/>
 *           &lt;element name="forProcess" type="{http://eclipse.org/stardust/ws/v2012a/api/query}ProcessInstanceFilter"/>
 *           &lt;element name="processesInState" type="{http://eclipse.org/stardust/ws/v2012a/api/query}ProcessStateFilter"/>
 *           &lt;element name="processesInHierarchy" type="{http://eclipse.org/stardust/ws/v2012a/api/query}ProcessInstanceHierarchyFilter"/>
 *           &lt;element name="processesHavingLink" type="{http://eclipse.org/stardust/ws/v2012a/api/query}ProcessInstanceLinkFilter"/>
 *           &lt;element name="forActivityDefinition" type="{http://eclipse.org/stardust/ws/v2012a/api/query}ActivityDefinitionFilter"/>
 *           &lt;element name="forActivity" type="{http://eclipse.org/stardust/ws/v2012a/api/query}ActivityInstanceFilter"/>
 *           &lt;element name="activitiesInState" type="{http://eclipse.org/stardust/ws/v2012a/api/query}ActivityStateFilter"/>
 *           &lt;element name="performingUser" type="{http://eclipse.org/stardust/ws/v2012a/api/query}PerformingUserFilter"/>
 *           &lt;element name="performedByUser" type="{http://eclipse.org/stardust/ws/v2012a/api/query}PerformedByUserFilter"/>
 *           &lt;element name="performingParticipant" type="{http://eclipse.org/stardust/ws/v2012a/api/query}PerformingParticipantFilter"/>
 *           &lt;element name="performingOnBehalfOf" type="{http://eclipse.org/stardust/ws/v2012a/api/query}PerformingOnBehalfOfFilter"/>
 *           &lt;element name="startingUser" type="{http://eclipse.org/stardust/ws/v2012a/api/query}StartingUserFilter"/>
 *           &lt;element name="userState" type="{http://eclipse.org/stardust/ws/v2012a/api/query}UserStateFilter"/>
 *           &lt;element name="participantAssociation" type="{http://eclipse.org/stardust/ws/v2012a/api/query}ParticipantAssociationFilter"/>
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
@XmlType(name = "PredicateTerm", propOrder = {
    "andOrOrOrIsNull"
})
@XmlSeeAlso({
    OrTermXto.class,
    AndTermXto.class
})
public abstract class PredicateTermXto
    extends PredicateBaseXto
{

    @XmlElements({
        @XmlElement(name = "userState", type = UserStateFilterXto.class),
        @XmlElement(name = "participantAssociation", type = ParticipantAssociationFilterXto.class),
        @XmlElement(name = "performingParticipant", type = PerformingParticipantFilterXto.class),
        @XmlElement(name = "notInList", type = NotInListPredicateXto.class),
        @XmlElement(name = "processesInState", type = ProcessStateFilterXto.class),
        @XmlElement(name = "startingUser", type = StartingUserFilterXto.class),
        @XmlElement(name = "forProcess", type = ProcessInstanceFilterXto.class),
        @XmlElement(name = "and", type = AndTermXto.class),
        @XmlElement(name = "forActivity", type = ActivityInstanceFilterXto.class),
        @XmlElement(name = "activitiesInState", type = ActivityStateFilterXto.class),
        @XmlElement(name = "lessThan", type = LessThanPredicateXto.class),
        @XmlElement(name = "notNull", type = NotNullPredicateXto.class),
        @XmlElement(name = "greaterOrEqual", type = GreaterOrEqualPredicateXto.class),
        @XmlElement(name = "processesHavingLink", type = ProcessInstanceLinkFilterXto.class),
        @XmlElement(name = "isEqual", type = IsEqualPredicateXto.class),
        @XmlElement(name = "lessOrEqual", type = LessOrEqualPredicateXto.class),
        @XmlElement(name = "between", type = BetweenPredicateXto.class),
        @XmlElement(name = "performingOnBehalfOf", type = PerformingOnBehalfOfFilterXto.class),
        @XmlElement(name = "or", type = OrTermXto.class),
        @XmlElement(name = "inList", type = InListPredicateXto.class),
        @XmlElement(name = "isLike", type = IsLikePredicateXto.class),
        @XmlElement(name = "performedByUser", type = PerformedByUserFilterXto.class),
        @XmlElement(name = "forActivityDefinition", type = ActivityDefinitionFilterXto.class),
        @XmlElement(name = "performingUser", type = PerformingUserFilterXto.class),
        @XmlElement(name = "forProcessDefinition", type = ProcessDefinitionFilterXto.class),
        @XmlElement(name = "notEqual", type = NotEqualPredicateXto.class),
        @XmlElement(name = "notAnyOf", type = NotAnyOfPredicateXto.class),
        @XmlElement(name = "processesInHierarchy", type = ProcessInstanceHierarchyFilterXto.class),
        @XmlElement(name = "greaterThan", type = GreaterThanPredicateXto.class),
        @XmlElement(name = "isNull", type = IsNullPredicateXto.class)
    })
    protected List<PredicateBaseXto> andOrOrOrIsNull;

    /**
     * Gets the value of the andOrOrOrIsNull property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the andOrOrOrIsNull property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAndOrOrOrIsNull().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link UserStateFilterXto }
     * {@link ParticipantAssociationFilterXto }
     * {@link PerformingParticipantFilterXto }
     * {@link NotInListPredicateXto }
     * {@link ProcessStateFilterXto }
     * {@link StartingUserFilterXto }
     * {@link ProcessInstanceFilterXto }
     * {@link AndTermXto }
     * {@link ActivityInstanceFilterXto }
     * {@link ActivityStateFilterXto }
     * {@link LessThanPredicateXto }
     * {@link NotNullPredicateXto }
     * {@link GreaterOrEqualPredicateXto }
     * {@link ProcessInstanceLinkFilterXto }
     * {@link IsEqualPredicateXto }
     * {@link LessOrEqualPredicateXto }
     * {@link BetweenPredicateXto }
     * {@link PerformingOnBehalfOfFilterXto }
     * {@link OrTermXto }
     * {@link InListPredicateXto }
     * {@link IsLikePredicateXto }
     * {@link PerformedByUserFilterXto }
     * {@link ActivityDefinitionFilterXto }
     * {@link PerformingUserFilterXto }
     * {@link ProcessDefinitionFilterXto }
     * {@link NotEqualPredicateXto }
     * {@link NotAnyOfPredicateXto }
     * {@link ProcessInstanceHierarchyFilterXto }
     * {@link GreaterThanPredicateXto }
     * {@link IsNullPredicateXto }
     * 
     * 
     */
    public List<PredicateBaseXto> getAndOrOrOrIsNull() {
        if (andOrOrOrIsNull == null) {
            andOrOrOrIsNull = new ArrayList<PredicateBaseXto>();
        }
        return this.andOrOrOrIsNull;
    }

}
