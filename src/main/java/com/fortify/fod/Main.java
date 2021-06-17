package com.fortify.fod;

import com.beust.jcommander.JCommander;
import com.fortify.fod.fodapi.FodApi;
import com.fortify.fod.parser.FortifyCommands;
import com.fortify.fod.parser.Proxy;
import com.fortify.fod.fodapi.controllers.*;
import com.fortify.fod.parser.converters.BsiTokenConverter;
import com.fortify.fod.parser.BsiToken;

public class Main {

    /**
     * @param args Required: zip location, bsi url, username/password or api key/secret
     */
    public static void main(String[] args) {
        FortifyCommands fc = new FortifyCommands();
        JCommander jc = new JCommander(fc);

        try {
            jc.parse(args);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            jc.usage();
            System.exit(1);
        }

        if (fc.version) {
            fc.version();
            System.exit(1);
        }

        if (fc.help) {
            jc.usage();
            System.exit(1);
        }

        if (fc.bsiToken == null && (fc.isEmptyParameter(fc.portalUrl) || !fc.isValidUrl(fc.portalUrl)))
        {
            System.err.println("Please provide Valid Fortify Portal Url Parameter ( Example : -portalurl https://ams.fortify.com/ )");
            System.exit(1);
        }

        if(fc.bsiToken == null && (fc.isEmptyParameter(fc.apiUrl) || !fc.isValidUrl(fc.apiUrl)))
        {
            System.err.println("Please provide Valid Fortify Api Url Parameter ( Example : -apiurl https://api.ams.fortify.com/ )");
            System.exit(1);
        }

        if (!fc.hasApiCredentials() && !fc.hasUserCredentials()) {
            System.err.println("The following options are required: -apiCredentials, -ac or -userCredentials, -uc");
            System.exit(1);
        }

        if (fc.hasUserCredentials() && fc.bsiToken == null && fc.isEmptyParameter(fc.tenantCode)) {
            System.err.println("The following options are required: -tenantCode -tc if using -userCredentials -uc for API Authentication");
            jc.usage();
            System.exit(1);
        }

        if (fc.isBundledAssessment || fc.includeThirdPartyLibs || fc.scanPreferenceType != null) {
            System.out.println("The following parameters are deprecated and will be ignored: -scanPreferenceId -p, -includeThirdPartyApps -itp, -isBundledAssessment -b");
        }

        if (fc.entitlementPreference == null) {
            System.err.println("The entitlement preference option needs to be have the following values");
            jc.usage();
            System.exit(1);
        }
      
        if (fc.isRemediationScan && fc.remediationScanPreference != null) {
            System.err.println("Both --r and -rp cannot be used at the same time , --r is deprecated instead we would prefer you to use -rp for selecting Remediation Scan Preference ");
            System.exit(1);
        }

        // Both bsiToken and ReleaseId are not provided - Exit
        if (fc.bsiToken == null && fc.releaseId == 0) {
            System.out.println("Release ID or BSI token should be required. The Release ID is HIGHLY recommended for CI usage as the BSI token is being sunset in 2020");
            System.exit(1);
        }

        //Both BsiToken and ReleaseId ar provided , call is executed but with a warning message
        if (fc.bsiToken != null && fc.releaseId != 0) {
            System.out.println("Warning: Both the BSI and Release ID Token were provided; The BSI Token is being ignored.");
        }

        boolean uploadSucceeded;
        int pollExitCode = 0;
        int triggeredscanId;

        try {
            Proxy proxy = new Proxy(fc.proxy);
            BsiToken bsiToken = fc.bsiToken != null ? new BsiTokenConverter().convert(fc.bsiToken) : null;
            FodApi fodApi = new FodApi(fc.apiUrl != null ? fc.apiUrl : bsiToken.getApiUri(), proxy.getProxyUri() == null ? null : proxy, fc.portalUrl != null ? fc.portalUrl : bsiToken.getPortalUri());
            System.out.println("Authenticating");
            // Has username/password
            String username, password, grantType;
            if (fc.hasUserCredentials()) {
                username = fc.userCredentials.get(0);
                password = fc.userCredentials.get(1);
                grantType = fodApi.GRANT_TYPE_PASSWORD;
                // Has key/secret
            } else {
                username = fc.apiCredentials.get(0);
                password = fc.apiCredentials.get(1);
                grantType = fodApi.GRANT_TYPE_CLIENT_CREDENTIALS;
            }

            String tenantCode = bsiToken != null ? bsiToken.getTenantCode() : fc.tenantCode;
            fodApi.authenticate(tenantCode, username, password, grantType);
            ReleaseController r = fodApi.getReleaseController();
            boolean proccedWithScan = r.UpdateScanSettings(r.getReleaseScanSettings(fc.releaseId),fc);
            if(proccedWithScan) {
                System.out.println("Beginning upload");
                StaticScanController s = fodApi.getStaticScanController();
                uploadSucceeded = s.StartStaticScan(fc);
                triggeredscanId = s.getTriggeredScanId();
                //check success status exit appropriately
                if (uploadSucceeded) {
                    // Why do we need to poll for this?
                    if (fc.pollingInterval > 0) {
                        PollStatus listener = new PollStatus(fodApi, fc.pollingInterval);
                        // Until status is complete or cancelled
                        pollExitCode = listener.releaseStatus((fc.bsiToken != null) ? bsiToken.getProjectVersionId() : fc.releaseId, triggeredscanId);
                    }
                    fodApi.retireToken();
                    switch (pollExitCode) {
                        case 0:
                            System.exit(0);
                            break;
                        case 4:
                            System.exit(4);
                            break; // On Scan Paused
                        case 3:
                            System.exit(3);
                            break; // On Scan Cancelled
                        case 1:
                            System.exit(fc.allowPolicyFail ? 0 : 1);
                            break;
                        default:
                            throw new IllegalStateException("Unexpected value: " + pollExitCode);
                    }
                } else {
                    fodApi.retireToken();
                    System.exit(1);
                }
            }

            fodApi.retireToken();
            System.exit(proccedWithScan ? 0 : 1) ;
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

    }


}
