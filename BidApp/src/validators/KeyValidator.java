package validators;
public class KeyValidator {

    public static boolean validatePrivateKey(String privateKeyPath, String password) {
        try {
            String command = String.format("C:\\Program Files\\OpenSSL-Win64\\bin\\openssl.exe pkcs8 -inform PEM -in %s -passin pass:%s",privateKeyPath,password);

            Process process = Runtime.getRuntime().exec(command);
            int exitCode = process.waitFor();

            if(exitCode==0) {
            	System.out.println("Valid Key");
            	return true;
            }else {
            	return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
