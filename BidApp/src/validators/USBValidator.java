package validators;

import java.io.File;
import java.util.Scanner;

public class USBValidator {
    public static String USB_PATH = "D:";

    public static boolean verifyUSBFiles() {

        File usbDirectory = new File(USB_PATH);
        if (!usbDirectory.exists() || !usbDirectory.isDirectory()) {
            System.out.println("USB is not connected or path is invalid.");
            return false;
        }

        File privateKeyFile = new File(getKeyPath(USB_PATH, "privateKey"));

        System.out.println("Type the name of your CSR file: ");
        File csrFile = new File(getCsrPath(USB_PATH, "CCSR"));

        if (!privateKeyFile.exists() || !csrFile.exists()) {
            System.out.println("Private key or CSR are not in the USB.");
            return false;
        }

        System.out.println("Files found successfully!");
        return true;
    }

    public static String getUsbPath(String path) {
        return path + ":\\";
    }

    public static String getKeyPath(String path, String keyFileName) {
        return path + "\\" + keyFileName + ".key";
    }

    public static String getCsrPath(String path, String csrFileName) {
        return path + "\\" + csrFileName + ".csr";
    }
}
