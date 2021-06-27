package com.mojagap.backenstarter.infrastructure;

public class ErrorMessages {
    public static final String USER_REQUIRED_WHEN_CREATING_ACCOUNT = "Please provide user details for account creation";
    public static final String MISSING_APPLICATION_PLATFORM = "Please provide the application platform that is performing the request";
    public static final String INVALID_PLATFORM_ID = "Invalid Platform ID provided";
    public static final String INVALID_SECURITY_CREDENTIAL = "Invalid Security Credentials Provided!!";
    public static final String FORBIDDEN_INSUFFICIENT_PERMISSION = "Insufficient permissions to perform the action";
    public static final String VALID_ACCOUNT_TYPE_REQUIRED = "Account type is required";
    public static final String VALID_COUNTRY_REQUIRED = "Please provide a valid country";
    public static final String COMPANY_DETAILS_REQUIRED = "Please provide your company information";
    public static final String INVALID_COMPANY_NAME = "Please provide a valid company name of length 5 to 255";
    public static final String INVALID_BRANCH_NAME = "Please provide a valid branch name of length 5 to 255";
    public static final String INVALID_FIRST_NAME = "Please provide a first name of length 5 to 100";
    public static final String INVALID_LAST_NAME = "Please provide a first name of length 5 to 100";
    public static final String INVALID_ID_NUMBER_PROVIDED = "Please provide a ID number of length 5 to 100";
    public static final String INVALID_LOCATION_ADDRESS = "Please provide a valid address of length 5 to 255";
    public static final String INVALID_EMAIL_ADDRESS = "Please provide a valid email address";
    public static final String INVALID_PHONE_NUMBER = "Please a valid number phone number";
    public static final String INVALID_PASSWORD = "Password must have at least 1 digit, lowercase, uppercase, special character, length(8 - 20) and no white spaces";
    public static final String VALID_COMPANY_TYPE = "Please provide a valid company type";
    public static final String INVALID_ID_TYPE = "Please a correct ID category";
    public static final String COMPANY_REGISTRATION_DATE_REQUIRED = "Company registration date is needed";
    public static final String DATE_OF_BIRTH_REQUIRED = "Date of birth is mandatory";
    public static final String INVALID_ROLE_NAME = "Role name should be between 5 to 100 characters";
    public static final String INVALID_ROLE_DESCRIPTION = "Role description should be between 10 to 100 characters";
    public static final String PERMISSIONS_REQUIRED_FOR_ROLE = "Attach at least one permission when creating or updating role";
    public static final String ENTITY_REQUIRED = "%s is required";
    public static final String ENTITY_ALREADY_EXISTS = "%s with that %s is already exists";
    public static final String ENTITY_DOES_NOT_EXISTS = "%s with that %s does not exists";
    public static final String ROLE_IS_IN_USE = "There is an active user with that role";
    public static final String ACCOUNT_TYPE_NOT_PERMITTED = "%s account type is not permitted here";
    public static final String CANNOT_CREATE_USER_IN_BRANCH = "You are not permitted to create user under this branch";
    public static final String CANNOT_CREATE_USER_UNDER_SUB_COMPANY = "You are not permitted to create user under this sub-company";
    public static final String NOT_PERMITTED_TO_CREATE_BRANCH_UNDER_PARENT = "You are not allowed to create a branch sub-branch under provided parent";
    public static final String NOT_PERMITTED_TO_PERFORM_ACTION_ON_COMPANY = "You are not permitted perform any action on this company";
    public static final String NOT_PERMITTED_TO_CLOSE_COMPANY = "You are not permitted to close this company";
    public static final String NOT_PERMITTED_TO_MIGRATE_COMPANY = "You are not permitted to migrate this company to a different parent";
}
