package uz.paykap.service;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import uz.paykap.domain.enumeration.HashAlgoritm;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class CryptoService {

    private static final Logger log = LoggerFactory.getLogger(CryptoService.class);

    public static String calculateMD5Hash(String data) {
        log.debug("Calculate HashCode for : {}", data);
        return data != null ? DigestUtils.md5Hex(data) : null;
    }

    public static String encode(HashAlgoritm hashAlgoritm, String data) throws NoSuchAlgorithmException {
        log.debug("Calculate HashCode for : {}", data);
        HttpHeaders headers = new HttpHeaders();


        switch (hashAlgoritm) {
            case MD5: {
                return data != null ? DigestUtils.md5Hex(data) : null;
            }
            case SHA1: {
                MessageDigest digest = MessageDigest.getInstance("SHA-1");
                byte[] encodedhash = digest.digest((data).getBytes(StandardCharsets.UTF_8));
                return bytesToHex(encodedhash);

            }
            default: {
                MessageDigest digest = MessageDigest.getInstance("SHA-1");
                byte[] encodedhash = digest.digest((data).getBytes(StandardCharsets.UTF_8));
                return bytesToHex(encodedhash);
            }
        }

    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte aHash : hash) {
            String hex = Integer.toHexString(0xff & aHash);
            if (hex.length() == 1) stringBuilder.append('0');
            stringBuilder.append(hex);
        }
        return stringBuilder.toString();
    }
}
