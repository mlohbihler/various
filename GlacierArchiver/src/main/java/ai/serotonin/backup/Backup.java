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
        new Backup().execute();
    }

    public Backup() throws Exception {
        super();
    }

    @Override
    void executeImpl() throws Exception {
        File backupFile = generateBackupFilename();
        createBackup(backupFile);
        backupFile = encryptFile(backupFile);
        copyToGlacier(backupFile);
        sendEmail("Backup successful", "" + backupFile.length() + " bytes uploaded");
        backupFile.delete();
        deleteOldBackups();
        log.info("Backup process completed");
    }

    private File generateBackupFilename() {
        String prefix = configRoot.get("prefix").asText();
        String ts = new SimpleDateFormat("yyyyMMdd.HHmmss").format(new Date());
        return new File("./" + prefix + "-" + ts + ".zip");
    }

    private void createBackup(File filename) throws Exception {
        log.info("Creating backup file " + filename);
        FileOutputStream out = new FileOutputStream(filename);
        ZipOutputStream zip = new ZipOutputStream(out);
        try {
            JsonNode files = configRoot.get("files");
            for (JsonNode filesNode : files) {
                String prefix = filesNode.get("prefix").asText();
                JsonNode paths = filesNode.get("paths");
                for (JsonNode path : paths)
                    addFile(zip, prefix, path.asText());
            }
        }
        finally {
            IOUtils.closeQuietly(zip);
            IOUtils.closeQuietly(out);
        }
        log.info("Created backup file " + filename + ", " + filename.length() + " bytes");
    }

    private void addFile(ZipOutputStream zip, String prefix, String path) throws IOException {
        File file = new File(prefix, path);
        if (file.isFile())
            addZipEntry(zip, prefix, path);
        else {
            String[] subFiles = file.list();
            if (subFiles != null) {
                for (String subFile : subFiles)
                    addFile(zip, prefix, path + File.separator + subFile);
            }
        }
    }

    private void addZipEntry(ZipOutputStream zip, String prefix, String path) throws IOException {
        ZipEntry e = new ZipEntry(path);
        zip.putNextEntry(e);

        FileInputStream in = null;
        try {
            in = new FileInputStream(new File(prefix, path));
            long len = IOUtils.copy(in, zip);
            if (log.isDebugEnabled())
                log.debug("Wrote " + path + ", " + len + " bytes ");
        }
        finally {
            IOUtils.closeQuietly(in);
        }

        zip.closeEntry();
    }

    private File encryptFile(File file) throws Exception {
        SecureRandom random = new SecureRandom();
        byte[] salt = random.generateSeed(8);
        String saltStr = Hex.encodeHexString(salt);

        SecretKey secret = createSecretKey(salt);

        Cipher cipher = createCipher();
        cipher.init(Cipher.ENCRYPT_MODE, secret);
        AlgorithmParameters params = cipher.getParameters();
        byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
        String ivStr = Hex.encodeHexString(iv);

        File encryptedFile = new File(file.getParent(), saltStr + "_" + ivStr + "_" + file.getName());

        cipherizeFile(file, encryptedFile, cipher);

        file.delete();

        log.info("Encrypted backup file to " + encryptedFile.getPath() + ", " + encryptedFile.length() + " bytes");
        return encryptedFile;
    }

    private void copyToGlacier(File file) throws Exception {
        String vaultName = getVaultName();
        ArchiveTransferManager atm = new ArchiveTransferManager(client, credentials);
        UploadResult result = atm.upload(vaultName, file.getName(), file);
        log.info("Upload archive ID: " + result.getArchiveId());
    }

    private void deleteOldBackups() throws Exception {
        List<Archive> archives = getInventory();

        if (archives == null)
            log.warn("Could not delete old backups");
        else {
            Collections.sort(archives);
            log.info("Found " + archives.size() + " archives in inventory");

            String vaultName = getVaultName();
            int maxFiles = configRoot.get("maxFiles").asInt();

            for (int i = 0; i + maxFiles < archives.size(); i++) {
                Archive archive = archives.get(i);
                log.info("Purging archive named " + archive.filename);
                DeleteArchiveRequest request = new DeleteArchiveRequest() //
                        .withVaultName(vaultName) //
                        .withArchiveId(archive.id);
                client.deleteArchive(request);
            }
        }
    }
}
