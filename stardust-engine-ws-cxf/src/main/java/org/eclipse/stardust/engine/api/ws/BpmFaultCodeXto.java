
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for BpmFaultCode.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="BpmFaultCode">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="UnknownError"/>
 *     &lt;enumeration value="InvalidName"/>
 *     &lt;enumeration value="ItemDoesNotExist"/>
 *     &lt;enumeration value="ItemAlreadyExists"/>
 *     &lt;enumeration value="NameAlreadyExists"/>
 *     &lt;enumeration value="MissingConfiguration"/>
 *     &lt;enumeration value="InvalidConfiguration"/>
 *     &lt;enumeration value="CapacityExceeded"/>
 *     &lt;enumeration value="InternalException"/>
 *     &lt;enumeration value="AssertionFailedException"/>
 *     &lt;enumeration value="PublicException"/>
 *     &lt;enumeration value="AccessForbiddenException"/>
 *     &lt;enumeration value="BindingException"/>
 *     &lt;enumeration value="ConcurrencyException"/>
 *     &lt;enumeration value="DeploymentException"/>
 *     &lt;enumeration value="DocumentManagementServiceException"/>
 *     &lt;enumeration value="ExpectedFailureException"/>
 *     &lt;enumeration value="IllegalOperationException"/>
 *     &lt;enumeration value="IllegalStateChangeException"/>
 *     &lt;enumeration value="InvalidArgumentException"/>
 *     &lt;enumeration value="InvalidEncodingException"/>
 *     &lt;enumeration value="InvalidValueException"/>
 *     &lt;enumeration value="LoginFailedException"/>
 *     &lt;enumeration value="ModelParsingException"/>
 *     &lt;enumeration value="ObjectNotFoundException"/>
 *     &lt;enumeration value="ServiceException"/>
 *     &lt;enumeration value="ServiceCommandException"/>
 *     &lt;enumeration value="UnsupportedFilterException"/>
 *     &lt;enumeration value="UserExistsException"/>
 *     &lt;enumeration value="UserGroupExistsException"/>
 *     &lt;enumeration value="UserRealmExistsException"/>
 *     &lt;enumeration value="ValidationException"/>
 *     &lt;enumeration value="WfxmlException"/>
 *     &lt;enumeration value="ResourceException"/>
 *     &lt;enumeration value="TransactionFreezedException"/>
 *     &lt;enumeration value="UniqueConstraintViolatedException"/>
 *     &lt;enumeration value="ServiceNotAvailableException"/>
 *     &lt;enumeration value="UnrecoverableExecutionException"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "BpmFaultCode")
@XmlEnum
public enum BpmFaultCodeXto {

    @XmlEnumValue("UnknownError")
    UNKNOWN_ERROR("UnknownError"),
    @XmlEnumValue("InvalidName")
    INVALID_NAME("InvalidName"),
    @XmlEnumValue("ItemDoesNotExist")
    ITEM_DOES_NOT_EXIST("ItemDoesNotExist"),
    @XmlEnumValue("ItemAlreadyExists")
    ITEM_ALREADY_EXISTS("ItemAlreadyExists"),
    @XmlEnumValue("NameAlreadyExists")
    NAME_ALREADY_EXISTS("NameAlreadyExists"),
    @XmlEnumValue("MissingConfiguration")
    MISSING_CONFIGURATION("MissingConfiguration"),
    @XmlEnumValue("InvalidConfiguration")
    INVALID_CONFIGURATION("InvalidConfiguration"),
    @XmlEnumValue("CapacityExceeded")
    CAPACITY_EXCEEDED("CapacityExceeded"),
    @XmlEnumValue("InternalException")
    INTERNAL_EXCEPTION("InternalException"),
    @XmlEnumValue("AssertionFailedException")
    ASSERTION_FAILED_EXCEPTION("AssertionFailedException"),
    @XmlEnumValue("PublicException")
    PUBLIC_EXCEPTION("PublicException"),
    @XmlEnumValue("AccessForbiddenException")
    ACCESS_FORBIDDEN_EXCEPTION("AccessForbiddenException"),
    @XmlEnumValue("BindingException")
    BINDING_EXCEPTION("BindingException"),
    @XmlEnumValue("ConcurrencyException")
    CONCURRENCY_EXCEPTION("ConcurrencyException"),
    @XmlEnumValue("DeploymentException")
    DEPLOYMENT_EXCEPTION("DeploymentException"),
    @XmlEnumValue("DocumentManagementServiceException")
    DOCUMENT_MANAGEMENT_SERVICE_EXCEPTION("DocumentManagementServiceException"),
    @XmlEnumValue("ExpectedFailureException")
    EXPECTED_FAILURE_EXCEPTION("ExpectedFailureException"),
    @XmlEnumValue("IllegalOperationException")
    ILLEGAL_OPERATION_EXCEPTION("IllegalOperationException"),
    @XmlEnumValue("IllegalStateChangeException")
    ILLEGAL_STATE_CHANGE_EXCEPTION("IllegalStateChangeException"),
    @XmlEnumValue("InvalidArgumentException")
    INVALID_ARGUMENT_EXCEPTION("InvalidArgumentException"),
    @XmlEnumValue("InvalidEncodingException")
    INVALID_ENCODING_EXCEPTION("InvalidEncodingException"),
    @XmlEnumValue("InvalidValueException")
    INVALID_VALUE_EXCEPTION("InvalidValueException"),
    @XmlEnumValue("LoginFailedException")
    LOGIN_FAILED_EXCEPTION("LoginFailedException"),
    @XmlEnumValue("ModelParsingException")
    MODEL_PARSING_EXCEPTION("ModelParsingException"),
    @XmlEnumValue("ObjectNotFoundException")
    OBJECT_NOT_FOUND_EXCEPTION("ObjectNotFoundException"),
    @XmlEnumValue("ServiceException")
    SERVICE_EXCEPTION("ServiceException"),
    @XmlEnumValue("ServiceCommandException")
    SERVICE_COMMAND_EXCEPTION("ServiceCommandException"),
    @XmlEnumValue("UnsupportedFilterException")
    UNSUPPORTED_FILTER_EXCEPTION("UnsupportedFilterException"),
    @XmlEnumValue("UserExistsException")
    USER_EXISTS_EXCEPTION("UserExistsException"),
    @XmlEnumValue("UserGroupExistsException")
    USER_GROUP_EXISTS_EXCEPTION("UserGroupExistsException"),
    @XmlEnumValue("UserRealmExistsException")
    USER_REALM_EXISTS_EXCEPTION("UserRealmExistsException"),
    @XmlEnumValue("ValidationException")
    VALIDATION_EXCEPTION("ValidationException"),
    @XmlEnumValue("WfxmlException")
    WFXML_EXCEPTION("WfxmlException"),
    @XmlEnumValue("ResourceException")
    RESOURCE_EXCEPTION("ResourceException"),
    @XmlEnumValue("TransactionFreezedException")
    TRANSACTION_FREEZED_EXCEPTION("TransactionFreezedException"),
    @XmlEnumValue("UniqueConstraintViolatedException")
    UNIQUE_CONSTRAINT_VIOLATED_EXCEPTION("UniqueConstraintViolatedException"),
    @XmlEnumValue("ServiceNotAvailableException")
    SERVICE_NOT_AVAILABLE_EXCEPTION("ServiceNotAvailableException"),
    @XmlEnumValue("UnrecoverableExecutionException")
    UNRECOVERABLE_EXECUTION_EXCEPTION("UnrecoverableExecutionException");
    private final String value;

    BpmFaultCodeXto(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static BpmFaultCodeXto fromValue(String v) {
        for (BpmFaultCodeXto c: BpmFaultCodeXto.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
