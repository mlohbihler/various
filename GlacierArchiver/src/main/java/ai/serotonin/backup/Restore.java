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

import com.amazonaws.services.glacier.model.GetJobOutputRequest;
import com.amazonaws.services.glacier.model.GetJobOutputResult;
import com.amazonaws.services.glacier.model.InitiateJobRequest;
import com.amazonaws.services.glacier.model.InitiateJobResult;
import com.amazonaws.services.glacier.model.JobParameters;

public class Restore extends Base {
    public static void main(String[] args) throws Exception {
        new Restore(args).execute();
    }

    public Restore(String[] args) throws Exception {
        super(args);
    }

    @Override
    void executeImpl() throws Exception {
        // Get an inventory from Glacier
        List<Archive> archives = getInventory();
        if (archives == null) {
            log.warn("No inventory from which to restore");
            return;
        }
        if (archives.isEmpty()) {
            log.warn("No files in inventory to restore");
            return;
        }

        // Retrieve the latest file
        Archive latest = archives.get(archives.size() - 1);
        File file = retrieveArchive(latest.id, latest.filename);

        if (getArchivePassword() != null)
            // Decrypt it
            file = decryptFile(file);

        sendEmail("Restore completed", "Restored file " + file.getName());
    }

    private File retrieveArchive(String id, String filename) throws Exception {
        String vaultName = getVaultName();

        InitiateJobRequest initJobRequest = new InitiateJobRequest() //
                .withVaultName(vaultName) //
                .withJobParameters(new JobParameters() //
                        .withType("archive-retrieval") //
                        .withArchiveId(id) //
        //.withSNSTopic("*** provide SNS topic ARN ****") //
        //.withDescription("archive retrieval")
        );

        log.info("Initiating archive retrieval job...");
        InitiateJobResult initJobResult = client.initiateJob(initJobRequest);
        String jobId = initJobResult.getJobId();

        // Wait for the job to complete.
        waitForJob(vaultName, jobId);

        // Get the output of the retrieval job.
        log.info("Archive retrieval job completed. Getting output...");
        GetJobOutputRequest jobOutputRequest = new GetJobOutputRequest().withVaultName(vaultName).withJobId(jobId);
        GetJobOutputResult jobOutputResult = client.getJobOutput(jobOutputRequest);

        File file = new File(filename);
        FileOutputStream fos = new FileOutputStream(file);
        IOUtils.copy(jobOutputResult.getBody(), fos);
        fos.close();

        return file;
    }

    File decryptFile(File encryptedFile) throws Exception {
        String filename = encryptedFile.getName();

        int pos = filename.indexOf('_');
        String saltStr = filename.substring(0, pos);
        byte[] salt = Hex.decodeHex(saltStr.toCharArray());
        filename = filename.substring(pos + 1);

        pos = filename.indexOf('_');
        String ivStr = filename.substring(0, pos);
        byte[] iv = Hex.decodeHex(ivStr.toCharArray());
        filename = filename.substring(pos + 1);

        File file = new File(filename);

        SecretKey secret = createSecretKey(salt);

        Cipher cipher = createCipher();
        cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));

        cipherizeFile(encryptedFile, file, cipher);

        log.info("Decrypted archive to " + filename);

        return file;
    }
}
