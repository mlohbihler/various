/*
 * Copyright (c) 2015, Matthew Lohbihler
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package ai.serotonin.backup;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.spec.KeySpec;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.model.DescribeJobRequest;
import com.amazonaws.services.glacier.model.DescribeJobResult;
import com.amazonaws.services.glacier.model.GetJobOutputRequest;
import com.amazonaws.services.glacier.model.GetJobOutputResult;
import com.amazonaws.services.glacier.model.InitiateJobRequest;
import com.amazonaws.services.glacier.model.InitiateJobResult;
import com.amazonaws.services.glacier.model.JobParameters;
import com.amazonaws.services.glacier.model.ResourceNotFoundException;
import com.amazonaws.util.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.ISO8601Utils;
import com.serotonin.io.MulticastOutputStream;
import com.serotonin.web.mail.EmailSender;

abstract public class Base {
    private static final Log LOG = LogFactory.getLog(Backup.class);

    /**
     * Used to store in memory the messages that are being written to the log. Allows the log messages to be written
     * to the content of a result email.
     */
    ByteArrayOutputStream memoryLog;

    final String suffix;
    ObjectNode configRoot;
    EmailSender emailSender;
    AWSCredentials credentials;
    AmazonGlacierClient client;

    Base(final String[] args) throws Exception {
        if (args.length > 0)
            suffix = args[0];
        else
            suffix = "";

        // Initialize
        loadConfig();
        createSmtpClient();
        createGlacierClient();
    }

    void execute() {
        final File processLock = new File(".lock" + suffix);
        if (processLock.exists()) {
            sendEmail("Backup/restore failure!", "Another process is already running. Aborting new run.");
            return;
        }

        String emailSubject = null;
        try (MulticastOutputStream mos = new MulticastOutputStream();
                final FileOutputStream logOut = new FileOutputStream("log" + suffix + ".txt", true);
                final PrintStream out = new PrintStream(mos)) {
            lock(processLock);

            // Redirect output to file and memory log.
            memoryLog = new ByteArrayOutputStream();

            //            mos.setExceptionHandler(new IOExceptionHandler() {
            //                @Override
            //                public void ioException(OutputStream stream, IOException e) {
            //                    e.printStackTrace(s);
            //                }
            //            });
            mos.addStream(logOut);
            mos.addStream(memoryLog);

            System.setOut(out);
            System.setErr(out);

            executeImpl();
            emailSubject = "Backup/restore completion";
        } catch (final Exception e) {
            LOG.error("An error occurred", e);

            final StringWriter sw = new StringWriter();
            final PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            // TODO sw isn't used?
            emailSubject = "Backup/restore failure!";
        } finally {
            // Send the result email
            final String content = new String(memoryLog.toByteArray());
            sendEmail(emailSubject, content);

            unlock(processLock);
        }
    }

    abstract void executeImpl() throws Exception;

    private void loadConfig() throws Exception {
        final String name = "/config" + suffix + ".json";
        try (final InputStream in = Backup.class.getResourceAsStream(name)) {
            final ObjectMapper mapper = new ObjectMapper();
            configRoot = mapper.readValue(in, ObjectNode.class);
        } catch (final Exception e) {
            throw new Exception("Failed to load " + name, e);
        }
    }

    private void createSmtpClient() {
        final JsonNode smtp = configRoot.get("smtp");
        emailSender = new EmailSender( //
                smtp.get("host").asText(), //
                smtp.get("port").asInt(), //
                smtp.get("useAuth").asBoolean(), //
                smtp.get("username").asText(), //
                smtp.get("password").asText(), //
                smtp.get("tls").asBoolean() //
        );
    }

    private void createGlacierClient() {
        final String accessKey = configRoot.get("glacier").get("accessKey").asText();
        final String secretKey = configRoot.get("glacier").get("secretKey").asText();
        final String endpoint = configRoot.get("glacier").get("endpoint").asText();
        credentials = new BasicAWSCredentials(accessKey, secretKey);
        client = new AmazonGlacierClient(credentials) //
                .withEndpoint(endpoint);
    }

    private void sendEmail(final String subject, final String content) {
        final String prefix = configRoot.get("prefix").asText();

        emailSender.send( //
                configRoot.get("smtp").get("from").asText(), //
                configRoot.get("smtp").get("to").asText(), //
                subject + " (" + prefix + ")", //
                content, //
                (String) null);
        LOG.info("Sent job completion email");
    }

    private static void lock(final File lockFile) throws IOException {
        try (final FileWriter fw = new FileWriter(lockFile)) {
            fw.write("Running...");
        }
    }

    private static void unlock(final File lockFile) {
        lockFile.delete();
    }

    List<Archive> getInventory() throws Exception {
        final String vaultName = getVaultName();

        final InitiateJobRequest initJobRequest = new InitiateJobRequest() //
                .withVaultName(vaultName) //
                .withJobParameters(new JobParameters() //
                        .withType("inventory-retrieval") //
        //.withSNSTopic("*** provide SNS topic ARN ****") //
        );

        String jobId;
        try {
            LOG.info("Initiating inventory job...");
            final InitiateJobResult initJobResult = client.initiateJob(initJobRequest);
            jobId = initJobResult.getJobId();
        } catch (final ResourceNotFoundException e) {
            // Ignore. No inventory is available.
            LOG.warn("Inventory not available: " + e.getErrorMessage());
            return null;
        }

        // Wait for the inventory job to complete.
        waitForJob(vaultName, jobId);

        // Get the output of the inventory job.
        LOG.info("Inventory job completed. Getting output...");
        final GetJobOutputRequest jobOutputRequest = new GetJobOutputRequest().withVaultName(vaultName)
                .withJobId(jobId);
        final GetJobOutputResult jobOutputResult = client.getJobOutput(jobOutputRequest);

        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode rootNode = mapper.readValue(jobOutputResult.getBody(), ObjectNode.class);

        final List<Archive> archives = new ArrayList<>();
        for (final JsonNode archiveNode : rootNode.get("ArchiveList"))
            archives.add(new Archive(archiveNode));
        Collections.sort(archives);

        return archives;
    }

    String getVaultName() {
        return configRoot.get("glacier").get("vaultName").asText();
    }

    String getArchivePassword() {
        if (!configRoot.has("password"))
            return null;

        final String password = configRoot.get("password").asText();
        if (StringUtils.isNullOrEmpty(password))
            return null;

        return password;
    }

    void waitForJob(final String vaultName, final String jobId) throws Exception {
        while (true) {
            final DescribeJobResult describeJobResult = client.describeJob(new DescribeJobRequest(vaultName, jobId));
            final Boolean completed = describeJobResult.getCompleted();
            if (completed != null && completed)
                break;
            LOG.info("Job not completed. Waiting 15 minutes...");
            Thread.sleep(1000 * 60 * 15);
        }
    }

    Cipher createCipher() throws Exception {
        return Cipher.getInstance("AES/CBC/PKCS5Padding");
    }

    SecretKey createSecretKey(final byte[] salt) throws Exception {
        final String password = getArchivePassword();
        final SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        final KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
        final SecretKey tmp = factory.generateSecret(spec);
        final SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
        return secret;
    }

    void cipherizeFile(final File inFile, final File outFile, final Cipher cipher) throws Exception {
        try (final FileInputStream fis = new FileInputStream(inFile);
                final FileOutputStream fos = new FileOutputStream(outFile)) {
            final byte[] inbuf = new byte[1028];
            final byte[] outbuf = new byte[2056];
            int readCount, encCount;
            while ((readCount = fis.read(inbuf)) != -1) {
                encCount = cipher.update(inbuf, 0, readCount, outbuf);
                fos.write(outbuf, 0, encCount);
            }

            encCount = cipher.doFinal(inbuf, 0, 0, outbuf);
            fos.write(outbuf, 0, encCount);
        }
    }

    static class Archive implements Comparable<Archive> {
        String id;
        String filename;
        Date creation;
        long size;
        String hash;

        public Archive(final JsonNode json) throws ParseException {
            id = json.get("ArchiveId").asText();
            filename = json.get("ArchiveDescription").asText();
            creation = ISO8601Utils.parse(json.get("CreationDate").asText(), new ParsePosition(0));
            size = json.get("Size").asLong();
            hash = json.get("SHA256TreeHash").asText();
        }

        @Override
        public int compareTo(final Archive that) {
            return creation.compareTo(that.creation);
        }
    }
}
