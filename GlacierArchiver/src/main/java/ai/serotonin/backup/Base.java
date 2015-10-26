/* 
 * Copyright (c) 2015, Matthew Lohbihler
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package ai.serotonin.backup;

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

import org.apache.commons.io.IOUtils;
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
import com.serotonin.web.mail.EmailSender;

abstract public class Base {
    static final Log log = LogFactory.getLog(Backup.class);

    final String suffix;
    ObjectNode configRoot;
    EmailSender emailSender;
    AWSCredentials credentials;
    AmazonGlacierClient client;

    Base(String[] args) throws Exception {
        if (args.length > 0)
            suffix = args[0];
        else
            suffix = "";

        // Initialize
        loadConfig();
        createSmtpClient();
        createGlacierClient();
    }

    void execute() throws Exception {
        File processLock = new File(".lock" + suffix);
        if (processLock.exists()) {
            sendEmail("Backup/restore failure!", "Another process is already running. Aborting new run.");
            return;
        }

        FileOutputStream fis = null;
        PrintStream out = null;
        try {
            lock(processLock);

            // Redirect output to file
            fis = new FileOutputStream("log" + suffix + ".txt", true);
            out = new PrintStream(fis);
            System.setOut(out);
            System.setErr(out);

            executeImpl();
        }
        catch (Exception e) {
            log.error("An error occurred", e);

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            sendEmail("Backup/restore failure!", sw.toString());
        }
        finally {
            unlock(processLock);
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(fis);
        }
    }

    abstract void executeImpl() throws Exception;

    private void loadConfig() throws Exception {
        String name = "/config" + suffix + ".json";
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream in = Backup.class.getResourceAsStream(name);
            configRoot = mapper.readValue(in, ObjectNode.class);
            in.close();
        }
        catch (Exception e) {
            throw new Exception("Failed to load " + name, e);
        }
    }

    private void createSmtpClient() {
        JsonNode smtp = configRoot.get("smtp");
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
        String accessKey = configRoot.get("glacier").get("accessKey").asText();
        String secretKey = configRoot.get("glacier").get("secretKey").asText();
        String endpoint = configRoot.get("glacier").get("endpoint").asText();
        credentials = new BasicAWSCredentials(accessKey, secretKey);
        client = new AmazonGlacierClient(credentials) //
                .withEndpoint(endpoint);
    }

    void sendEmail(String subject, String content) {
        String prefix = configRoot.get("prefix").asText();

        emailSender.send( //
                configRoot.get("smtp").get("from").asText(), //
                configRoot.get("smtp").get("to").asText(), //
                subject + " (" + prefix + ")", //
                content, //
                (String) null);
        log.info("Sent job completion email");
    }

    private void lock(File lockFile) throws IOException {
        FileWriter fw = new FileWriter(lockFile);
        fw.write("Running...");
        fw.close();
    }

    private void unlock(File lockFile) {
        lockFile.delete();
    }

    List<Archive> getInventory() throws Exception {
        String vaultName = getVaultName();

        InitiateJobRequest initJobRequest = new InitiateJobRequest() //
                .withVaultName(vaultName) //
                .withJobParameters(new JobParameters() //
                        .withType("inventory-retrieval") //
        //.withSNSTopic("*** provide SNS topic ARN ****") //
        );

        String jobId;
        try {
            log.info("Initiating inventory job...");
            InitiateJobResult initJobResult = client.initiateJob(initJobRequest);
            jobId = initJobResult.getJobId();
        }
        catch (ResourceNotFoundException e) {
            // Ignore. No inventory is available.
            log.warn("Inventory not available: " + e.getErrorMessage());
            return null;
        }

        // Wait for the inventory job to complete.
        waitForJob(vaultName, jobId);

        // Get the output of the inventory job.
        log.info("Inventory job completed. Getting output...");
        GetJobOutputRequest jobOutputRequest = new GetJobOutputRequest().withVaultName(vaultName).withJobId(jobId);
        GetJobOutputResult jobOutputResult = client.getJobOutput(jobOutputRequest);

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.readValue(jobOutputResult.getBody(), ObjectNode.class);

        List<Archive> archives = new ArrayList<>();
        for (JsonNode archiveNode : rootNode.get("ArchiveList"))
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

        String password = configRoot.get("password").asText();
        if (StringUtils.isNullOrEmpty(password))
            return null;

        return password;
    }

    void waitForJob(String vaultName, String jobId) throws Exception {
        while (true) {
            DescribeJobResult describeJobResult = client.describeJob(new DescribeJobRequest(vaultName, jobId));
            Boolean completed = describeJobResult.getCompleted();
            if (completed != null && completed)
                break;
            log.info("Job not completed. Waiting 15 minutes...");
            Thread.sleep(1000 * 60 * 15);
        }
    }

    Cipher createCipher() throws Exception {
        return Cipher.getInstance("AES/CBC/PKCS5Padding");
    }

    SecretKey createSecretKey(byte[] salt) throws Exception {
        String password = getArchivePassword();
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
        return secret;
    }

    void cipherizeFile(File inFile, File outFile, Cipher cipher) throws Exception {
        FileInputStream fis = new FileInputStream(inFile);
        FileOutputStream fos = new FileOutputStream(outFile);

        try {
            byte[] inbuf = new byte[1028];
            byte[] outbuf = new byte[2056];
            int readCount, encCount;
            while ((readCount = fis.read(inbuf)) != -1) {
                encCount = cipher.update(inbuf, 0, readCount, outbuf);
                fos.write(outbuf, 0, encCount);
            }

            encCount = cipher.doFinal(inbuf, 0, 0, outbuf);
            fos.write(outbuf, 0, encCount);
        }
        finally {
            IOUtils.closeQuietly(fis);
            IOUtils.closeQuietly(fos);
        }
    }

    static class Archive implements Comparable<Archive> {
        String id;
        String filename;
        Date creation;
        long size;
        String hash;

        public Archive(JsonNode json) throws ParseException {
            id = json.get("ArchiveId").asText();
            filename = json.get("ArchiveDescription").asText();
            creation = ISO8601Utils.parse(json.get("CreationDate").asText(), new ParsePosition(0));
            size = json.get("Size").asLong();
            hash = json.get("SHA256TreeHash").asText();
        }

        @Override
        public int compareTo(Archive that) {
            return creation.compareTo(that.creation);
        }
    }
}
