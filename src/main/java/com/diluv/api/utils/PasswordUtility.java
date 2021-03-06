package com.diluv.api.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.crypto.generators.OpenBSDBCrypt;

import com.diluv.api.DiluvAPIServer;
import com.diluv.confluencia.database.record.CompromisedPasswordRecord;

import static com.diluv.api.Main.DATABASE;

public class PasswordUtility {

    public static String generateBCrypt (String password) {

        try {
            final byte[] salt = new byte[16];
            SecureRandom.getInstanceStrong().nextBytes(salt);
            return OpenBSDBCrypt.generate(password.toCharArray(), salt, Constants.BCRYPT_COST);
        }
        catch (NoSuchAlgorithmException e) {
            DiluvAPIServer.LOGGER.catching(e);
        }
        return null;
    }

    public static boolean isCompromised (String password) {

        String hash = DigestUtils.sha1Hex(password).toUpperCase();

        CompromisedPasswordRecord record = DATABASE.securityDAO.findOnePasswordByHash(hash);
        if (record == null || System.currentTimeMillis() - record.getLastUpdated() >= TimeUnit.DAYS.toMillis(30)) {
            String hash5 = hash.substring(0, 5);
            List<String> passwords = getPasswordCheck(hash5);
            if (passwords.isEmpty()) {
                return false;
            }

            Map<String, Long> compromisedMap = new HashMap<>();
            boolean compromised = false;
            for (String passwordOccurrences : passwords) {
                String[] s = passwordOccurrences.split(":");
                String p = hash5 + s[0];
                long o = Long.parseLong(s[1]);
                if (o > 0) {
                    compromisedMap.put(p, o);

                    if (hash.equalsIgnoreCase(p)) {
                        compromised = true;
                    }
                }
            }
            DATABASE.securityDAO.insertPassword(compromisedMap);

            return compromised;
        }
        return true;
    }

    public static List<String> getPasswordCheck (String hash) {

        HttpURLConnection connection = null;

        try {

            URL url = new URL("https://api.pwnedpasswords.com/range/" + hash);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Add-Padding", "true");
            connection.setRequestProperty("Accept-Encoding", "gzip");
            connection.setDoOutput(true);

            try (InputStream inputStream = getStreamWithGzip(connection)) {

                return IOUtils.readLines(inputStream, Charset.defaultCharset());
            }
        }

        catch (Exception e) {

            DiluvAPIServer.LOGGER.catching(e);
            return Collections.emptyList();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static InputStream getStreamWithGzip (HttpURLConnection connection) throws IOException {

        if ("gzip".equals(connection.getContentEncoding())) {

            return new GZIPInputStream(connection.getInputStream());
        }

        return connection.getInputStream();
    }
}