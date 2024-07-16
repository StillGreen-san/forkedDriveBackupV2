package ratismal.drivebackup;

import org.apache.commons.io.FilenameUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Nullable;
import ratismal.drivebackup.UploadThread.UploadLogger;
import ratismal.drivebackup.config.ConfigParser;
import ratismal.drivebackup.config.ConfigParser.Config;

import org.jetbrains.annotations.NotNull;
import ratismal.drivebackup.plugin.DriveBackup;
import ratismal.drivebackup.uploaders.Uploader;
import ratismal.drivebackup.uploaders.dropbox.DropboxUploader;
import ratismal.drivebackup.uploaders.ftp.FTPUploader;
import ratismal.drivebackup.uploaders.googledrive.GoogleDriveUploader;
import ratismal.drivebackup.uploaders.onedrive.OneDriveUploader;
import ratismal.drivebackup.uploaders.s3.S3Uploader;
import ratismal.drivebackup.uploaders.webdav.NextcloudUploader;
import ratismal.drivebackup.uploaders.webdav.WebDAVUploader;
import ratismal.drivebackup.util.LocalDateTimeFormatter;
import ratismal.drivebackup.util.MessageUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static ratismal.drivebackup.config.Localization.intl;

public class TestThread implements Runnable {
    private UploadLogger logger;
    private String[] args;
    private CommandSender initiator;

    /**
     * Creates an instance of the {@code TestThread} object
     * @param initiator the player who initiated the test
     * @param args any arguments that followed the command that initiated the test
     */
    public TestThread(CommandSender initiator, String[] args) {
        logger = new UploadLogger() {
            @Override
            public void log(String input, String... placeholders) {
                MessageUtil.Builder()
                    .mmText(input, placeholders)
                    .to(initiator)
                    .send();
            }

            @Override
            public void initiatorError(String input, String... placeholders) {
                MessageUtil.Builder()
                    .mmText(input, placeholders)
                    .to(initiator)
                    .toConsole(false)
                    .send();
            }
        };

        this.args = args;
        this.initiator = initiator;
    }

    /**
     * Starts a test of a backup method
     */
    @Override
    public void run() {

        /*
          Arguments:
          0) The test command
          1) The backup method to test
          2) The name of the test file to upload during test
          3) The size (in bytes) of the file.

          drivebackup test onedrive f:testfile.txt s:1000 p:"some path/to" p: "other path" p:more path
         */

        if (args.length < 2) {
            logger.initiatorError(intl("test-method-not-specified"));

            return;
        }

        String testFileName = "testfile.txt";
        int testFileSize = 1000;
        String method = args[1];

        if (args.length > 2) {
            String altFolder = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
            fullTestRun(method, testFileSize, altFolder);
            DriveBackup.reloadLocalConfig();
        } else {
            testUploadMethod(testFileName, testFileSize, method);
        }
    }

    /**
     * Tests a specific upload method
     * @param testFileName name of the test file to upload during the test
     * @param testFileSize the size (in bytes) of the file
     * @param method name of the upload method to test
     */
    private void testUploadMethod(String testFileName, int testFileSize, @NotNull String method) {
        Config config = ConfigParser.getConfig();
        Uploader uploadMethod = getUploader(method, config);
        if (uploadMethod == null) {
            return;
        }

        logger.log(intl("test-method-begin"), "upload-method", uploadMethod.getName());

	File testFile;
	try {
	    testFile = getTestFile(testFileName, testFileSize, config);
	} catch (Exception e) {
            logger.log(intl("test-file-creation-failed"));
            MessageUtil.sendConsoleException(e);
            return;
	}

	uploadMethod.test(testFile);

        if (uploadMethod.isErrorWhileUploading()) {
            logger.log(intl("test-method-failed"), "upload-method", uploadMethod.getName());
        } else {
            logger.log(intl("test-method-successful"), "upload-method", uploadMethod.getName());
        }
        
        testFile.delete();
        uploadMethod.close();
    }

    @NotNull
    private String concatPath(@NotNull String lhs, @NotNull String rhs) {
        if (rhs.isEmpty()) {
            return lhs;
        }
        if (lhs.isEmpty()) {
            return rhs;
        }
        if(lhs.endsWith("/")) {
            lhs = lhs.substring(0, lhs.length() - 1);
        }
        if(rhs.startsWith("/")) {
            rhs = rhs.substring(1);
        }
        return lhs + '/' + rhs;
    }

    private void fullTestRun(@NotNull String method, int testFileSize, @NotNull String altFolder) {
        Config config = ConfigParser.getConfig();

        Uploader uploadMethod = getUploader(method, config);
        if (uploadMethod == null) {
            return;
        }
        String timestamp = LocalDateTimeFormatter.ofPattern("YYYYMMddHHmmss").format(ZonedDateTime.now());
        String testFileName = "DriveBackupV2TestFile" + timestamp + ".txt";

        logger.log("begin full test run");

        File testFile;
        try {
            testFile = getTestFile(testFileName, testFileSize, config);
        } catch (Exception e) {
            MessageUtil.sendConsoleException(e);
            return;
        }

        FileConfiguration fileConfiguration = DriveBackup.getInstance().getConfig();
        ConfigParser configParser = new ConfigParser(fileConfiguration);

        String testFolderName = "DriveBackupV2TestFolder" + timestamp;
        List<String> baseFolders = Arrays.asList("", "/", "./", altFolder);
        List<String> subFolders = Arrays.asList("", testFolderName + "/sub", testFolderName + "/sub/", testFolderName);
        List<CommandSender> senders = Arrays.asList(initiator);

        for (String baseFolder : baseFolders) {
            for (String subFolder : subFolders) {
                String testFolderPath = concatPath(baseFolder, subFolder);
                fileConfiguration.set("remote-save-directory", testFolderPath);
                configParser.reload(fileConfiguration, senders);

                uploadMethod.test(testFile);

                if (uploadMethod.isErrorWhileUploading()) {
                    logger.log("FAIL: " + concatPath(testFolderPath, testFile.getName()));
                    return;
                }
                logger.log("PASS: " + concatPath(testFolderPath, testFile.getName()));
                try {
                    TimeUnit.MILLISECONDS.sleep(2222);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
	        }
        }

        logger.log("end of full test run");
    }

    private @NotNull File getTestFile(String testFileName, int testFileSize, Config config) throws IOException {
        File testFile = Paths.get(config.backupStorage.localDirectory, testFileName).toFile();
        testFile.getParentFile().mkdirs();

        try (FileOutputStream fos = new FileOutputStream(testFile)) {
            Random byteGenerator = new Random();
            byte[] randomBytes = new byte[1024];
            int remainingSize = testFileSize;

            for (; remainingSize > 0; remainingSize -= randomBytes.length) {
                byteGenerator.nextBytes(randomBytes);
                fos.write(randomBytes, 0, Math.min(randomBytes.length, remainingSize));
                fos.flush();
            }
        }
        return testFile;
    }

    private @Nullable Uploader getUploader(@NotNull String method, Config config) {
        switch (method) {
            case "googledrive":
                if (config.backupMethods.googleDrive.enabled) {
                    return new GoogleDriveUploader(logger);
                }
		sendMethodDisabled(logger, GoogleDriveUploader.UPLOADER_NAME);
                return null;
            case "onedrive":
                if (config.backupMethods.oneDrive.enabled) {
                    return new OneDriveUploader(logger);
                }
		sendMethodDisabled(logger, OneDriveUploader.UPLOADER_NAME);
		return null;
            case "dropbox":
                if (config.backupMethods.dropbox.enabled) {
                    return new DropboxUploader(logger);
                }
		sendMethodDisabled(logger, DropboxUploader.UPLOADER_NAME);
		return null;
            case "webdav":
                if (config.backupMethods.webdav.enabled) {
                    return new WebDAVUploader(logger, config.backupMethods.webdav);
                }
		sendMethodDisabled(logger, WebDAVUploader.UPLOADER_NAME);
		return null;
            case "nextcloud":
                if (config.backupMethods.nextcloud.enabled) {
                    return new NextcloudUploader(logger, config.backupMethods.nextcloud);
                }
		sendMethodDisabled(logger, NextcloudUploader.UPLOADER_NAME);
		return null;
            case "s3":
                if (config.backupMethods.s3.enabled) {
                    return new S3Uploader(logger, config.backupMethods.s3);
                }
		sendMethodDisabled(logger, S3Uploader.UPLOADER_NAME);
		return null;
            case "ftp":
                if (config.backupMethods.ftp.enabled) {
                    return new FTPUploader(logger, config.backupMethods.ftp);
                }
		sendMethodDisabled(logger, FTPUploader.UPLOADER_NAME);
		return null;
            default:
                logger.initiatorError(intl("test-method-invalid"), "specified-method", method);
                return null;
        }
    }

    private void sendMethodDisabled(@NotNull UploadLogger logger, String methodName) {
        logger.log(intl("test-method-not-enabled"), "upload-method", methodName);
    }
}
