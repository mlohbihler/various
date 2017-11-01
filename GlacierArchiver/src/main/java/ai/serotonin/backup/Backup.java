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
    private static final Log LOG = LogFactory.getLog(Backup.class);

    public static void main(final String[] args) throws Exception {
        new Backup(args).execute();
    }

    public Backup(final String[] args) throws Exception {
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
        LOG.info("Backup process completed");
    }

    private File generateBackupFilename() {
        final String prefix = configRoot.get("prefix").asText();
        final String ts = new SimpleDateFormat("yyyyMMdd.HHmmss").format(new Date());
        return new File("./" + prefix + "-" + ts + ".zip");
    }

    private void createBackup(final File filename) throws Exception {
        LOG.info("Creating backup file " + filename);

        try (final FileOutputStream out = new FileOutputStream(filename);
                final ZipOutputStream zip = new ZipOutputStream(out)) {
            final JsonNode files = configRoot.get("files");
            for (final JsonNode filesNode : files) {
                final String prefix = filesNode.get("prefix").asText();
                final JsonNode paths = filesNode.get("paths");
                for (final JsonNode path : paths)
                    addFile(zip, prefix, path.asText());
            }
        }

        LOG.info("Created backup file " + filename + ", " + FileUtils.byteCountToDisplaySize(filename.length()));
    }

    private void addFile(final ZipOutputStream zip, final String prefix, final String path) throws IOException {
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

    private static void addZipEntry(final ZipOutputStream zip, final String prefix, final String path)
            throws IOException {
        final ZipEntry e = new ZipEntry(path);
        zip.putNextEntry(e);

        try (FileInputStream in = new FileInputStream(new File(prefix, path))) {
            final long len = IOUtils.copy(in, zip);
            if (LOG.isDebugEnabled())
                LOG.debug("Wrote " + path + ", " + len + " bytes ");
        }

        zip.closeEntry();
    }

    private File encryptFile(final File file) throws Exception {
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

        LOG.info("Encrypted backup file to " + encryptedFile.getPath() + ", "
                + FileUtils.byteCountToDisplaySize(encryptedFile.length()));
        return encryptedFile;
    }

    private void copyToGlacier(final File file) throws Exception {
        final String vaultName = getVaultName();
        final ArchiveTransferManager atm = new ArchiveTransferManager(client, credentials);
        final UploadResult result = atm.upload(vaultName, file.getName(), file);
        LOG.info("Upload archive ID: " + result.getArchiveId());
    }

    private void deleteOldBackups() throws Exception {
        final int maxFiles = configRoot.get("maxFiles").asInt();
        if (maxFiles <= 0)
            return;

        final String vaultName = getVaultName();
        final List<Archive> archives = getInventory();

        if (archives == null)
            LOG.warn("Could not delete old backups");
        else {
            Collections.sort(archives);
            LOG.info("Found " + archives.size() + " archives in inventory");

            while (archives.size() > maxFiles) {
                final Archive archive = archives.remove(0);
                LOG.info("Purging archive named " + archive.filename);
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
            LOG.info("Keeping archive(s) named " + sb.toString());
        }
    }
}
