package uz.paykap.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.io.FileUtils;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.config.Constants;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;

@UtilityClass
public class PathUtils {

    /*
     * Folder path to upload files in.
     * */
    public static String getUploadedFolderPath(String gatewayName, ApplicationProperties properties) {
        return properties.getReconciliation().getUploadedFolderPath() + File.separator + gatewayName + File.separator;
    }

    /*
     * File path (full = folder + file) to upload.
     * Should be used to upload new file to future reconciliation or to get path of file to reconcile
     * */
    public static String getFilePathToUpload(String gatewayName, Instant fromTime, ApplicationProperties properties) {
        return getUploadedFolderPath(gatewayName, properties) + gatewayName + "-" + Constants.RECONCILE_DATE_FORMATTER.format(fromTime);
    }

    /*
     * File path (full = folder + file + extension) to upload.
     * Should be used to upload new file to future reconciliation or to get path of file to reconcile
     * */
    public static String getFilePathToUpload(String gatewayName, Instant fromTime, ApplicationProperties properties, String extension) {
        return getUploadedFolderPath(gatewayName, properties) + gatewayName + "-" + Constants.RECONCILE_DATE_FORMATTER.format(fromTime) + "." + extension;
    }

    public static String getFilePathToUpload(String gatewayName, LocalDate date, ApplicationProperties properties, String extension) {
        return getUploadedFolderPath(gatewayName, properties) + gatewayName + "-" + Constants.RECONCILE_DATE_FORMATTER.format(date) + "." + extension;
    }

    /*
     * Folder path to save results in.
     * Should be used after reconciliation to store results for history or to send by email or ect.
     * */
    public static String getResultFolderPath(String gatewayName, ApplicationProperties properties) {
        return properties.getReconciliation().getPath() + File.separator + gatewayName + File.separator;
    }

    public static String getResultFilePath(String gatewayName, String login, Instant fromTime, ApplicationProperties properties, String extension) {
        return getUploadedFolderPath(gatewayName, properties) + gatewayName + "_" + login + "_" + "-" + Constants.RECONCILE_DATE_FORMATTER.format(fromTime) + "." + extension;
    }

    /*
    * Commonly used to clear folder after tests
    * */
    public static void clearUploadedFolder(String gatewayName, ApplicationProperties properties){
        try {
            FileUtils.deleteDirectory(new File(getUploadedFolderPath(gatewayName, properties)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
