 package validators;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;

import java.io.File;
import java.io.FileReader;
import java.security.Security;

public class CSRValidator {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static boolean validateCSR(String csrFilePath) {
        try {
            File csrFile = new File(csrFilePath);
            if (!csrFile.exists()) {
                System.out.println("CSR file doesn't exist.");
                return false;
            }

            FileReader reader = new FileReader(csrFile);
            PEMParser pemParser = new PEMParser(reader);
            Object object = pemParser.readObject();

            if (object instanceof PKCS10CertificationRequest) {
                System.out.println("CSR is valid.");
                pemParser.close();
                return true;
            } else {
                System.out.println("File doesn't contain a valid CSR.");
                pemParser.close();
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error verifying CSR: " + e.getMessage());
            e.printStackTrace(); 
            return false;
        }
    }

}
