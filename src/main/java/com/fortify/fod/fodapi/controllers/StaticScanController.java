package com.fortify.fod.fodapi.controllers;

import com.fortify.fod.fodapi.FodApi;
import com.fortify.fod.fodapi.models.GenericErrorResponse;
import com.fortify.fod.fodapi.models.PostStartScanResponse;
import com.fortify.fod.fodapi.models.ReleaseAssessmentTypeDTO;
import com.fortify.fod.parser.FortifyCommands;
import com.google.gson.Gson;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;

import java.io.FileInputStream;
import java.net.URLEncoder;
import java.util.Arrays;

public class StaticScanController extends ControllerBase {
    private final int CHUNK_SIZE = 1024 * 1024;

    /**
     * Constructor
     *
     * @param api api object with client info
     */
    public StaticScanController(final FodApi api) {
        super(api);
    }

    /**
     * Starts a scan based on the V3 API
     * @param fc fortify command object
     * @return true if scan successfully started
     */
    public boolean StartStaticScan(final FortifyCommands fc) {
        PostStartScanResponse scanStartedResponse;

        try (FileInputStream fs = new FileInputStream(fc.payload)) {

            byte[] readByteArray = new byte[CHUNK_SIZE];
            byte[] sendByteArray;
            int fragmentNumber = 0;
            int byteCount;
            long offset = 0;

            // Get entitlement info
            ReleaseAssessmentTypeDTO assessment = api.getReleaseController()
                    .getAssessmentType(fc);

            // Build 'static' portion of url
            String fragUrl = api.getBaseUrl() + "/api/v3/releases/" + fc.bsiToken.getProjectVersionId() +
                    "/static-scans/start-scan?";
            fragUrl += "assessmentTypeId=" + fc.bsiToken.getAssessmentTypeId();
            fragUrl += "&technologyStack=" + URLEncoder.encode(fc.bsiToken.getTechnologyStack(), "UTF-8");
            fragUrl += "&entitlementId=" + assessment.getEntitlementId();
            fragUrl += "&entitlementFrequencyType=" + assessment.getFrequencyTypeId();
            fragUrl += "&isBundledAssessment=" + assessment.isBundledAssessment();
            if (assessment.getParentAssessmentTypeId() != 0 && assessment.isBundledAssessment())
                fragUrl += "&parentAssessmentTypeId=" + assessment.getParentAssessmentTypeId();
            if (fc.bsiToken.getLanguageLevel() != null)
                fragUrl += "&languageLevel=" + fc.bsiToken.getLanguageLevel();
            if (fc.hasScanPreferenceType())
                fragUrl += "&scanPreferenceType=" + fc.scanPreferenceType.toString();
            if (fc.hasAuditPreferenceType())
                fragUrl += "&auditPreferenceType=" + fc.auditPreferenceType.toString();
            fragUrl += "&doSonatypeScan=" + fc.runSonatypeScan;
            fragUrl += "&isRemediationScan=" + fc.isRemediationScan;
            fragUrl += "&excludeThirdPartyLibs=" + !fc.includeThirdPartyLibs;

            Gson gson = new Gson();

            // Loop through chunks
            while ((byteCount = fs.read(readByteArray)) != -1) {

                if (byteCount < CHUNK_SIZE) {
                    sendByteArray = Arrays.copyOf(readByteArray, byteCount);
                    fragmentNumber = -1;
                } else {
                    sendByteArray = readByteArray;
                }

                MediaType byteArray = MediaType.parse("application/octet-stream");
                Request request = new Request.Builder()
                        .addHeader("Authorization", "Bearer " + api.getToken())
                        .addHeader("Content-Type", "application/octet-stream")
                        // Add offsets
                        .url(fragUrl + "&fragNo=" + fragmentNumber++ + "&offset=" + offset)
                        .post(RequestBody.create(byteArray, sendByteArray))
                        .build();
                // Get the response
                Response response = api.getClient().newCall(request).execute();

                if (response.code() == HttpStatus.SC_FORBIDDEN) {  // got logged out during polling so log back in
                    // Re-authenticate
                    api.authenticate();

                    // if you had to re-authenticate here, would the loop and request not need to be resubmitted?
                    // possible continue?
                }

                offset += byteCount;

                if (fragmentNumber % 5 == 0) {
                    System.out.println("Upload Status - Bytes sent:" + offset);
                }

                if (response.code() != 202) {
                    String responseJsonStr = IOUtils.toString(response.body().byteStream(), "utf-8");

                    if (response.code() == 200) {
                        scanStartedResponse = gson.fromJson(responseJsonStr, PostStartScanResponse.class);
                        System.out.println("Scan " + scanStartedResponse.getScanId() +
                                " uploaded successfully. Total bytes sent: " + offset);

                        return true;
                    } else if (!response.isSuccessful()) {
                        GenericErrorResponse errors = gson.fromJson(responseJsonStr, GenericErrorResponse.class);
                        System.out.println("Package upload failed for the following reasons: " +
                        errors.toString());
                        return false;
                    }
                }
                response.body().close();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }
}
