/*
 * Copyright (c) 2015, Matthew Lohbihler
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package ai.serotonin.backup;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amazonaws.services.glacier.model.GetJobOutputRequest;
import com.amazonaws.services.glacier.model.GetJobOutputResult;
import com.amazonaws.services.glacier.model.InitiateJobRequest;
import com.amazonaws.services.glacier.model.InitiateJobResult;
import com.amazonaws.services.glacier.model.JobParameters;

public class Restore extends Base {
    private static final Log LOG = LogFactory.getLog(Backup.class);

    public static void main(final String[] args) throws Exception {
        new Restore(args).execute();
    }

    public Restore(final String[] args) throws Exception {
        super(args);
    }

    @Override
    void executeImpl() throws Exception {
        // Get an inventory from Glacier
        final List<Archive> archives = getInventory();
        if (archives == null) {
            LOG.warn("No inventory from which to restore");
            return;
        }
        if (archives.isEmpty()) {
            LOG.warn("No files in inventory to restore");
            return;
        }

        LOG.info("Successfully retrieved inventory");
        archives.forEach(e -> LOG.info("   " + e));

        // Retrieve the latest file
        final Archive latest = archives.get(archives.size() - 1);
        File file = retrieveArchive(latest.id, latest.filename);

        if (getArchivePassword() != null)
            // Decrypt it
            file = decryptFile(file);

        LOG.info("Restore completed of file " + file.getName());
        //sendEmail("Restore completed", "Restored file " + file.getName());
    }

    private File retrieveArchive(final String id, final String filename) throws Exception {
        final String vaultName = getVaultName();

        final InitiateJobRequest initJobRequest = new InitiateJobRequest() //
                .withVaultName(vaultName) //
                .withJobParameters(new JobParameters() //
                        .withType("archive-retrieval") //
                        .withArchiveId(id) //
                //.withSNSTopic("*** provide SNS topic ARN ****") //
                //.withDescription("archive retrieval")
                );

        LOG.info("Initiating archive retrieval job...");
        final InitiateJobResult initJobResult = glacierClient.initiateJob(initJobRequest);
        final String jobId = initJobResult.getJobId();

        // Wait for the job to complete.
        waitForJob(vaultName, jobId);

        // Get the output of the retrieval job.
        LOG.info("Archive retrieval job completed. Getting output...");
        final GetJobOutputRequest jobOutputRequest = new GetJobOutputRequest().withVaultName(vaultName)
                .withJobId(jobId);
        final GetJobOutputResult jobOutputResult = glacierClient.getJobOutput(jobOutputRequest);

        final File file = new File(filename);
        try (final FileOutputStream fos = new FileOutputStream(file)) {
            IOUtils.copy(jobOutputResult.getBody(), fos);
        }

        return file;
    }

    File decryptFile(final File encryptedFile) throws Exception {
        String filename = encryptedFile.getName();

        int pos = filename.indexOf('_');
        final String saltStr = filename.substring(0, pos);
        final byte[] salt = Hex.decodeHex(saltStr.toCharArray());
        filename = filename.substring(pos + 1);

        pos = filename.indexOf('_');
        final String ivStr = filename.substring(0, pos);
        final byte[] iv = Hex.decodeHex(ivStr.toCharArray());
        filename = filename.substring(pos + 1);

        final File file = new File(filename);

        final SecretKey secret = createSecretKey(salt);

        final Cipher cipher = createCipher();
        cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));

        cipherizeFile(encryptedFile, file, cipher);

        LOG.info("Decrypted archive to " + filename);

        return file;
    }
}
