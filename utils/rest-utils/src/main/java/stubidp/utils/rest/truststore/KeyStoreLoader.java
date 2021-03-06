package stubidp.utils.rest.truststore;

import javax.inject.Inject;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class KeyStoreLoader {

    @Inject
    public KeyStoreLoader() { }

    public KeyStore load(String uri, String password) {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            char[] charPassword = password.toCharArray();
            try (InputStream inputStream = new FileInputStream(uri)) {
                keyStore.load(inputStream, charPassword);
            }
            return keyStore;
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            throw new RuntimeException(e);
        }
    }
}
