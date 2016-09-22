package com.fortify.fod.parser;

import org.apache.commons.cli.*;
import org.apache.commons.cli.CommandLine;

import java.io.PrintWriter;
import java.util.Comparator;
import java.util.regex.Pattern;

public class FortifyParser {
    static final String USERNAME = "username";
    static final String API = "api";
    static final String ZIP_LOCATION = "zipLocation";
    static final String BSI_URL = "bsiUrl";
    private static final String HELP = "help";
    private static final String VERSION = "version";
    static final String POLLING_INTERVAL = "pollingInterval";
    static final String RUN_SONATYPE_SCAN = "runSonatypeScan";
    static final String AUDIT_PREFERENCE_ID = "auditPreferenceId";
    static final String SCAN_PREFERENCE_ID = "scanPreferenceId";
    static final String PROXY = "proxy";
    static final String LEGACY = "legacy";

    private Options options = new Options();
    private CommandLineParser parser = new DefaultParser();
    private boolean legacy = false;

    /**
     * Argument paring wrapper for the Fod Uploader.
     */
    public FortifyParser() {
        // creates 2 arguments which aren't required. #documentation
        Option help = new       Option(HELP, "print this message");
        Option version = new    Option(VERSION, "print the version information and exit");

        // Creates the polling interval argument ( -pollingInterval <<minutes> required=false interval between
        // checking scan status
        Option pollingInterval = Option.builder(POLLING_INTERVAL)
                .hasArg(true).argName("minutes")
                .desc("interval between checking scan status")
                .required(false).build();

        // Creates the run sonatype scan argument ( -runSonatypeScan <true | false> required=false whether to run a
        // Sonatype Scan
        Option runSonatypeScan = Option.builder(RUN_SONATYPE_SCAN)
                .hasArg(true).argName("true|false")
                .desc("whether to run a Sonatype Scan")
                .required(false).build();

        // Creates the audit preference id argument ( -auditPreferenceId <1 | 2> required=false false positive audit
        // type (Manual or Automated) )
        Option auditPreferenceId = Option.builder(AUDIT_PREFERENCE_ID)
                .hasArg(true).argName("1|2")
                .desc("false positive audit type (Manual or Automated)")
                .required(false).build();

        // Creates the scan preference id argument ( -scanPreferenceId <1 | 2> required=false scan mode (Standard or
        // Express) )
        Option scanPreferenceId = Option.builder(SCAN_PREFERENCE_ID)
                .hasArg(true).argName("1|2")
                .desc("scan mode (Standard or Express)")
                .required(false).build();

        // Creates the bsi url argument ( -bsiUrl <url> required=true build server url )
        Option bsiUrl = Option.builder(BSI_URL)
                .hasArg(true).argName("url")
                .desc("build server url")
                .required(true).build();

        // Creates the zip location argument ( -zipLocation <file> required=true location of scan )
        Option zipLocation = Option.builder(ZIP_LOCATION)
                .hasArg(true).argName("file")
                .desc("location of scan")
                .required(true).build();

        // Add the options to the options list
        options.addOption(help);
        options.addOption(version);
        options.addOption(bsiUrl);
        options.addOption(zipLocation);
        options.addOption(pollingInterval);
        options.addOption(runSonatypeScan);
        options.addOption(auditPreferenceId);
        options.addOption(scanPreferenceId);

        // This one is so dirty I separated it from the rest of the pack.
        // I put all proxy settings into one option with **up to** 5 arguments. Then I do a little cheese
        // for the argName so that it will display with "-help"
        Option proxy = Option.builder(PROXY)
                .hasArgs().numberOfArgs(5).argName("proxyUrl> <username> <password> <ntDomain> <ntWorkstation")
                .desc("credentials for accessing the proxy")
                .required(false).build();
        proxy.setOptionalArg(true);
        options.addOption(proxy);

        // I've put the log-in credentials into a special group to denote that either can be used.
        // Similar build as Proxy, but I won't be using a custom class for these.
        Option username = Option.builder(USERNAME)
                .hasArg().numberOfArgs(2).argName("username> <password")
                .desc("login credentials")
                .build();
        Option api = Option.builder(API)
                .hasArg().numberOfArgs(2).argName("key> <secret")
                .desc("api credentials")
                .build();

        OptionGroup credentials = new OptionGroup();
        credentials.setRequired(true);
        credentials.addOption(username);
        credentials.addOption(api);

        options.addOptionGroup(credentials);
    }

    /**
     * Gets the various arguments and handles them accordingly.
     * @param args arguments to parse
     */
    public FortifyCommandLine parse(String[] args) {
        try {
            CommandLine cmd = parser.parse(options, args);

            // Put args into an object for easy handling.
            return new FortifyCommandLine(cmd);

        // Throws if username, password, zip location and bsi url aren't all present.
        } catch (ParseException e) {
            // If the user types just -help or just -version, then it will handle that command.
            // Regex is used here since cmd isn't accessible.
            if(args.length > 0) {
                if (Pattern.matches("(-{1,2})" + HELP, args[0])) {
                    help();
                    return new FortifyCommandLine(null);
                } else if (Pattern.matches("(-{1,2})" + VERSION, args[0])) {
                    System.out.println("upload version FodUploader 5.3.0");
                    return new FortifyCommandLine(null);
                }
                if (Pattern.matches("(-{1,2})" + LEGACY, args[0])) {
                    System.out.println("Using legacy argument parsing....");
                    legacy = true;
                    return new FortifyCommandLine(null);
                }
            }
            // I can no longer hope to imagine the command you intended.
            System.err.println(e.getMessage());
            System.err.println("try \"-" + HELP + "\" for info");

            return new FortifyCommandLine(null);
        } catch(Exception e) {
            e.printStackTrace();
            return new FortifyCommandLine(null);
        }
    }

    /**
     * Displays help dialog.
     */
    private void help() {
        final String header = "FodUpload is a command-line tool for uploading a static scan. \n\nConnect to the api with " +
                "either \"-username\" or \"-api\".";
        final int width = 120;
        final int padding = 5;
        HelpFormatter formatter = new HelpFormatter();
        PrintWriter out = new PrintWriter(System.out, true);

        formatter.setDescPadding(padding);
        formatter.setOptionComparator(HelpComparator);

        formatter.printWrapped(out, width, header);
        formatter.printUsage(out, width, "FodUpload-5.3.jar", options);
        formatter.printWrapped(out, width, ""); // New line
        formatter.printOptions(out, width, options, formatter.getLeftPadding(), formatter.getDescPadding());
    }

    /**
     * Compares options so that they are ordered:
     * 1.) by required, then by
     * 2.) short operator.
     * Used for sorting the results of the Help command.
     */
    private static Comparator<Option> HelpComparator = (o1, o2) -> {
        String required1 = o1.isRequired() ? "1" : "0";
        String required2 = o2.isRequired() ? "1" : "0";

        int result = required2.compareTo(required1);
        if (result == 0) {
            // will try to sort by short Operator but if it doesn't exist then it'll use long operator
            String comp1 = o1.getOpt() == null ? o1.getLongOpt() : o1.getOpt();
            String comp2 = o2.getOpt() == null ? o2.getLongOpt() : o2.getOpt();

            result = comp1.compareToIgnoreCase(comp2);
        }
        return result;
    };

    public boolean useLegacy() {
        return legacy;
    }
}
