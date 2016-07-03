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
import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amazonaws.services.glacier.model.DeleteArchiveRequest;
import com.amazonaws.services.glacier.transfer.ArchiveTransferManager;
import com.amazonaws.services.glacier.transfer.UploadResult;
import com.fasterxml.jackson.databind.JsonNode;

public class Backup extends Base {
    static final Log log = LogFactory.getLog(Backup.class);

    public static void main(String[] args) throws Exception {
        new Backup(args).execute();
    }

    public Backup(String[] args) throws Exception {
        super(args);
    }

    @Override
    void executeImpl() throws Exception {
        File backupFile = generateBackupFilename();
        createBackup(backupFile);
        if (getArchivePassword() != null)
            backupFile = encryptFile(backupFile);
        copyToGlacier(backupFile);
        //sendEmail("Backup successful", "" + backupFile.length() + " bytes uploaded");
        backupFile.delete();
        deleteOldBackups();
        log.info("Backup process completed");
    }

    private File generateBackupFilename() {
        final String prefix = configRoot.get("prefix").asText();
        final String ts = new SimpleDateFormat("yyyyMMdd.HHmmss").format(new Date());
        return new File("./" + prefix + "-" + ts + ".zip");
    }

    private void createBackup(File filename) throws Exception {
        log.info("Creating backup file " + filename);
        final FileOutputStream out = new FileOutputStream(filename);
        final ZipOutputStream zip = new ZipOutputStream(out);
        try {
            final JsonNode files = configRoot.get("files");
            for (final JsonNode filesNode : files) {
                final String prefix = filesNode.get("prefix").asText();
                final JsonNode paths = filesNode.get("paths");
                for (final JsonNode path : paths)
                    addFile(zip, prefix, path.asText());
            }
        }
        finally {
            IOUtils.closeQuietly(zip);
            IOUtils.closeQuietly(out);
        }
        log.info("Created backup file " + filename + ", " + FileUtils.byteCountToDisplaySize(filename.length()));
    }

    private void addFile(ZipOutputStream zip, String prefix, String path) throws IOException {
        final File file = new File(prefix, path);
        if (file.isFile())
            addZipEntry(zip, prefix, path);
        else {
            final String[] subFiles = file.list();
            if (subFiles != null) {
                for (final String subFile : subFiles)
                    addFile(zip, prefix, path + File.separator + subFile);
            }
        }
    }

    private void addZipEntry(ZipOutputStream zip, String prefix, String path) throws IOException {
        final ZipEntry e = new ZipEntry(path);
        zip.putNextEntry(e);

        FileInputStream in = null;
        try {
            in = new FileInputStream(new File(prefix, path));
            final long len = IOUtils.copy(in, zip);
            if (log.isDebugEnabled())
                log.debug("Wrote " + path + ", " + len + " bytes ");
        }
        finally {
            IOUtils.closeQuietly(in);
        }

        zip.closeEntry();
    }

    private File encryptFile(File file) throws Exception {
        final SecureRandom random = new SecureRandom();
        final byte[] salt = random.generateSeed(8);
        final String saltStr = Hex.encodeHexString(salt);

        final SecretKey secret = createSecretKey(salt);

        final Cipher cipher = createCipher();
        cipher.init(Cipher.ENCRYPT_MODE, secret);
        final AlgorithmParameters params = cipher.getParameters();
        final byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
        final String ivStr = Hex.encodeHexString(iv);

        final File encryptedFile = new File(file.getParent(), saltStr + "_" + ivStr + "_" + file.getName());

        cipherizeFile(file, encryptedFile, cipher);

        file.delete();

        log.info("Encrypted backup file to " + encryptedFile.getPath() + ", "
                + FileUtils.byteCountToDisplaySize(encryptedFile.length()));
        return encryptedFile;
    }

    private void copyToGlacier(File file) throws Exception {
        final String vaultName = getVaultName();
        final ArchiveTransferManager atm = new ArchiveTransferManager(client, credentials);
        final UploadResult result = atm.upload(vaultName, file.getName(), file);
        log.info("Upload archive ID: " + result.getArchiveId());
    }

    private void deleteOldBackups() throws Exception {
        final int maxFiles = configRoot.get("maxFiles").asInt();
        if (maxFiles <= 0)
            return;

        final String vaultName = getVaultName();
        final List<Archive> archives = getInventory();

        if (archives == null)
            log.warn("Could not delete old backups");
        else {
            Collections.sort(archives);
            log.info("Found " + archives.size() + " archives in inventory");

            while (archives.size() > maxFiles) {
                final Archive archive = archives.remove(0);
                log.info("Purging archive named " + archive.filename);
                final DeleteArchiveRequest request = new DeleteArchiveRequest() //
                        .withVaultName(vaultName) //
                        .withArchiveId(archive.id);
                client.deleteArchive(request);
            }

            final StringBuilder sb = new StringBuilder();
            for (final Archive archive : archives) {
                if (sb.length() > 0)
                    sb.append(", ");
                sb.append(archive.filename);
            }
            log.info("Keeping archive(s) named " + sb.toString());
        }
    }
}
