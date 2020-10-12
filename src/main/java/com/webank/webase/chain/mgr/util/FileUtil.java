package com.webank.webase.chain.mgr.util;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import org.apache.commons.io.FileUtils;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;

import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.exception.BaseException;

import lombok.extern.slf4j.Slf4j;

/**
 *
 */

@Slf4j
public class FileUtil {

    public static final Path DOWNLOAD_ROOT_DIR = Paths.get("download");

    static {
        // create parent dir
        if (Files.notExists(DOWNLOAD_ROOT_DIR)){
            try {
                Files.createDirectories(DOWNLOAD_ROOT_DIR);
            } catch (IOException e) {
                log.error("Create download dir error", e);
            }
        }
        log.info("Download root dir:[{}]",DOWNLOAD_ROOT_DIR.toAbsolutePath().toString());

        SSLContext sslContext = null;
        try {
            sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustStrategy() {
                //信任所有
                @Override
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            }).build();
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        } catch (Exception e) {
            log.error("Load loadTrustMaterial error.",e);
        }
    }

    /**
     * Download file.
     *
     * @param url
     * @param fileName
     * @param connectionTimeout
     */
    public static void download(String url, String fileName,
                                final int connectionTimeout, final int timeout) throws IOException {
        download(true, url, fileName, timeout );
    }

    /**
     * Download file if not exits.
     *
     * @param force             force to download when true.
     * @param url
     * @param fileName
     * @param timeout
     */
    public static void download(boolean force, String url, String fileName, final int timeout) throws IOException {
        Path filePath = DOWNLOAD_ROOT_DIR.resolve( fileName);
        if (force || Files.notExists(filePath)) {
            log.info("File:[{}] not exists, start to download from:[{}]....", filePath.toAbsolutePath().toString(), url);

            // Open connection to the URL
            URL downloadUrl = new URL(url);
            URLConnection connection = downloadUrl.openConnection();
            FileUtils.copyURLToFile(connection.getURL(),filePath.toFile(),timeout,timeout);
        } else {
            log.warn("Files:[{}] exists, skip download from:[{}].", filePath.toAbsolutePath().toString(), url);
        }

        // check file
        if (Files.notExists(filePath)) {
            log.error("Download file:[{}] from url:[{}] failed!!!", filePath.toAbsolutePath().toString(), url);
            throw new BaseException(ConstantCode.DOWNLOAD_FILE_ERROR.attach(url));
        }
    }

    /**
     *
     * @param fileName
     * @return
     */
    public static String getFilePath(String fileName){
        Path path = DOWNLOAD_ROOT_DIR.resolve(fileName);
        if (Files.notExists(path)){
            throw new BaseException(ConstantCode.FILE_NOT_EXISTS);
        }
        return path.toAbsolutePath().toString();
    }

    /**
     *
     * @param fileName
     * @return
     */
    public static boolean exists(String fileName){
        Path path = DOWNLOAD_ROOT_DIR.resolve(fileName);
        return Files.exists(path);
    }

    /**
     *
     * @param fileName
     * @return
     */
    public static boolean notExists(String fileName){
        Path path = DOWNLOAD_ROOT_DIR.resolve(fileName);
        return Files.notExists(path);
    }
}