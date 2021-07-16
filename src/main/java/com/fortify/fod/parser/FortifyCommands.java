package com.fortify.fod.parser;

import com.beust.jcommander.Parameter;
import com.fortify.fod.fodapi.FodEnums;
import com.fortify.fod.parser.converters.*;
import com.fortify.fod.parser.validators.BsiTokenValidator;
import com.fortify.fod.parser.validators.FileValidator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FortifyCommands {
    private static final String HELP = "-help";
    private static final String HELP_SHORT = "-h";
    @Parameter(names = {HELP, HELP_SHORT},
            description = "print this message",
            help = true)
    public boolean help = false;


    private static final String VERSION = "-version";
    private static final String VERSION_SHORT = "-v";
    @Parameter(names = {VERSION, VERSION_SHORT},
            description = "print the version information and exit",
            help = true)
    public boolean version = false;

    private static final String BSI_TOKEN = "-bsiToken";
    private static final String BSI_TOKEN_SHORT = "-bsi";
    private static final String BSI_URL = "-bsiUrl";
    private static final String BSI_URL_SHORT = "-u";
    @Parameter(names = {BSI_TOKEN, BSI_TOKEN_SHORT, BSI_URL, BSI_URL_SHORT},
            description = "build server url")
    public String bsiToken;

    private static final String ZIP_LOCATION = "-zipLocation";
    private static final String ZIP_LOCATION_SHORT = "-z";
    @Parameter(names = {ZIP_LOCATION, ZIP_LOCATION_SHORT},
            description = "location of scan",
            required = true,
            converter = FileConverter.class,
            validateWith = FileValidator.class)
    public File payload;

    private static final String POLLING_INTERVAL = "-pollingInterval";
    private static final String POLLING_INTERVAL_SHORT = "-I";
    @Parameter(names = {POLLING_INTERVAL, POLLING_INTERVAL_SHORT},
            description = "interval between checking scan status")
    public int pollingInterval = 0;

    private static final String IN_PROGRESS_SCAN_ACTION_TYPE = "-inProgressScanActionType";
    private static final String IN_PROGRESS_SCAN_ACTION_TYPE_SHORT = "-pp";
    @Parameter(names = {IN_PROGRESS_SCAN_ACTION_TYPE, IN_PROGRESS_SCAN_ACTION_TYPE_SHORT},
            description = "in progress scan type (Do not start scan , Cancel In Progress Scan or Queue)",
            converter = InProgressScanActionTypeConverter.class)
    public FodEnums.InProgressScanActionType inProgressScanPreferenceType = null;

    private static final String REMEDIATION_SCAN_PREFERENCE = "-remdiationScanPreferenceType";
    private static final String REMEDIATION_SCAN_PREFERENCE_SHORT = "-rp";
    @Parameter(names = {REMEDIATION_SCAN_PREFERENCE, REMEDIATION_SCAN_PREFERENCE_SHORT},
            description = "preferred remediation scan type (Remediation Scan if Available or Remediation Scan Only or Non Remediation Scan Only)",
            converter = RemediationScanPreferenceTypeConverter.class)
    public FodEnums.RemediationScanPreferenceType remediationScanPreference = null;

    private static final String ENTITLEMENT_PREFERENCE = "-entitlementPreferenceType";
    private static final String ENTITLEMENT_PREFERENCE_SHORT = "-ep";
    @Parameter(names = {ENTITLEMENT_PREFERENCE, ENTITLEMENT_PREFERENCE_SHORT},
            description = "preferred entitlement type (SingleScan , Subscription , First Single Scan Then Subscription Or First Subscription Then Single Scan)",
            converter = EntitlementPreferenceTypeConverter.class,
            required = true)
    public FodEnums.EntitlementPreferenceType entitlementPreference = null;

    private static final String PURCHASE_ENTITLEMENT = "-purchaseEntitlement";
    private static final String PURCHASE_ENTITLEMENT_SHORT = "-purchase";
    @Parameter(names = {PURCHASE_ENTITLEMENT, PURCHASE_ENTITLEMENT_SHORT},
            description = "whether to purchase an entitlement if available")
    public boolean purchaseEntitlement = false;

    public String scanTool = "FoDUploader";
    public String scanMethodType = "CICD";

    private static final String API_CREDENTIALS = "-apiCredentials";
    private static final String API_CREDENTIALS_SHORT = "-ac";
    @Parameter(names = {API_CREDENTIALS, API_CREDENTIALS_SHORT},
            description = "api credentials",
            arity = 2)
    public List<String> apiCredentials = new ArrayList<>();

    public boolean hasApiCredentials() {
        return apiCredentials != null &&
                !apiCredentials.isEmpty() &&
                apiCredentials.size() == 2;
    }

    private static final String USER_CREDENTIALS = "-userCredentials";
    private static final String USER_CREDENTIALS_SHORT = "-uc";
    @Parameter(names = {USER_CREDENTIALS, USER_CREDENTIALS_SHORT},
            description = "user credentials",
            arity = 2)
    public List<String> userCredentials = new ArrayList<>();

    public boolean hasUserCredentials() {
        return userCredentials != null &&
                !userCredentials.isEmpty() &&
                userCredentials.size() == 2;
    }

    private static final String PROXY = "-proxy";
    private static final String PROXY_SHORT = "-P";
    @Parameter(names = {PROXY, PROXY_SHORT},
            description = "credentials for accessing the proxy",
            arity = 5,
            variableArity = true)
    public List<String> proxy = new ArrayList<>();

    private static final String NOTES = "-notes";
    private static final String NOTES_SHORT = "-n";
    @Parameter(names = {NOTES, NOTES_SHORT},
            description = "the notes about the scan.")
    public String notes = "";

    // Deprecated Options
    private static final String AUDIT_PREFERENCE_ID = "-auditPreferenceId";
    private static final String AUDIT_PREFERENCE_ID_SHORT = "-a";
    @Parameter(names = { AUDIT_PREFERENCE_ID, AUDIT_PREFERENCE_ID_SHORT },
            description = "false positive audit type (Manual or Automated)",
            converter = AuditPreferenceConverter.class)
    public FodEnums.AuditPreferenceTypes auditPreferenceType = null;

    private static final String RUN_OPEN_SOURCE_SCAN = "-runOpenSourceScan";
    private static final String RUN_OPEN_SOURCE_SCAN_SHORT = "-os";
    @Parameter(names = { RUN_OPEN_SOURCE_SCAN, RUN_OPEN_SOURCE_SCAN_SHORT},
            description = "whether to run an Open Source Scan")
    public boolean runOpenSourceScan = false;

    private static final String SCAN_PREFERENCE_ID = "-scanPreferenceId";
    private static final String SCAN_PREFERENCE_ID_SHORT = "-p";
    @Parameter(names = { SCAN_PREFERENCE_ID, SCAN_PREFERENCE_ID_SHORT },
            description = "scan mode (Standard or Express)")
    public String scanPreferenceType = null;

    private static final String INCLUDE_THIRD_PARTY_LIBS = "-includeThirdPartyApps";
    private static final String INCLUDE_THIRD_PARTY_LIBS_SHORT = "-itp";
    @Parameter(names = { INCLUDE_THIRD_PARTY_LIBS, INCLUDE_THIRD_PARTY_LIBS_SHORT },
            description = "whether to include third party libraries")
    public boolean includeThirdPartyLibs = false;

    private static final String IS_REMEDIATION_SCAN = "-isRemediationScan";
    private static final String IS_REMEDIATION_SCAN_SHORT = "--r";
    @Parameter(names = { IS_REMEDIATION_SCAN, IS_REMEDIATION_SCAN_SHORT },
            description = "whether the scan is in remediation")
    public boolean isRemediationScan = false;

    private static final String IS_BUNDLED_ASSESSMENT = "-isBundledAssessment";
    private static final String IS_BUNDLED_ASSESSMENT_SHORT = "-b";
    @Parameter(names = { IS_BUNDLED_ASSESSMENT, IS_BUNDLED_ASSESSMENT_SHORT },
            description = "whether the scan is a bundled assessment")
    public boolean isBundledAssessment = false;

    private static final String RELEASE_ID = "-releaseId";
    private static final String RELEASE_ID_SHORT = "-rid";
    @Parameter(names = {RELEASE_ID, RELEASE_ID_SHORT},
            description = "Release id provides scan details that are used for the scan")
    public int releaseId = 0;

    private static final String PORTALURI = "-portalurl";
    private static final String PORTAL_URI_SHORT = "-purl";
    @Parameter(names = {PORTALURI,PORTAL_URI_SHORT},
            description = "URL provides an environment URL.",
            required = false)
    public String portalUrl;

    private static final String APIURI = "-apiurl";
    private static final String API_URI_SHORT = "-aurl";
    @Parameter(names = {APIURI,API_URI_SHORT},
            description = "URL provides an environment URL for which API to call",
            required = false)
    public String apiUrl;

    private static final String TENANT_CODE = "-tenantCode";
    private static final String TENANT_CODE_SHORT = "-tc";
    @Parameter(names = {TENANT_CODE,TENANT_CODE_SHORT},
            description = "Tenant identifier ",
            required = false)
    public String tenantCode;

    private static final String IS_POLICY_FAILURE = "-allowPolicyFail";
    private static final String IS_POLICY_FAILURE_SHORT = "-apf";
    @Parameter(names = {IS_POLICY_FAILURE, IS_POLICY_FAILURE_SHORT },
            description = "whether the scan is a bundled assessment")
    public boolean allowPolicyFail = false;

    private static final String ASSESSMENT_TYPE_ID = "-assessmentTypeId";
    private static final String ASSESSMENT_TYPE_ID_SHORT = "-at";
    @Parameter(names = { ASSESSMENT_TYPE_ID, ASSESSMENT_TYPE_ID_SHORT },
            description = "The Assessment Type")
    public int assessmentType = 0;

    private static final String TECHNOLOGY_STACK_ID = "-technologyStackId";
    private static final String TECHNOLOGY_STACK__ID_SHORT = "-ts";
    @Parameter(names = { TECHNOLOGY_STACK_ID, TECHNOLOGY_STACK__ID_SHORT },
            description = "The Technology Stack Id")
    public int technologyStack = 0;

    private static final String LANGUAGE_LEVEL_ID = "-languageLevelId";
    private static final String LANGUAGE_LEVEL_ID_SHORT = "-l";
    @Parameter(names = { LANGUAGE_LEVEL_ID, LANGUAGE_LEVEL_ID_SHORT },
            description = "Language version based on the technology stack.")
    public int languageLevel = 0;

    private static final String IS_BINARY_SCAN = "-isBinaryScan";
    private static final String IS_BINARY_SCAN_SHORT = "-bs";
    @Parameter(names = {IS_BINARY_SCAN, IS_BINARY_SCAN_SHORT },
            description = "Allows Binary Files to be scanned")
    public boolean isBinaryScan = false;

    private static final String ENTITLEMENT_ID = "-entitlementId";
    private static final String ENTITLEMENT_ID_SHORT = "-eid";
    @Parameter(names = {ENTITLEMENT_ID, ENTITLEMENT_ID_SHORT },
            description = "Entitlement Id. Note: Purchase Entitlements option will no longer be valid if entitlement Id is specified.")
    public int entitlementId = -1;

    public void version() {
        Package p = getClass().getPackage();
        System.out.println("Version " + p.getImplementationVersion());
    }

    public String getImplementedVersion() {
        return getClass().getPackage().getImplementationVersion();
    }

    public boolean isEmptyParameter(String param){
        return param == null || param.isEmpty();
    }

    public boolean isValidUrl(String url) {
        return (url.contains("http://") || url.contains("https://"));
    }
}
