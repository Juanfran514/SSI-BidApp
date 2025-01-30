
# USB KEY

1. Entender el Proceso
El objetivo es que los vendedores puedan autenticarse usando un USB que contiene un certificado firmado por ti. Para ello:
1.	El vendedor genera una solicitud de firma de certificado (CSR) en su USB.
2.	Tú recibes esa CSR, la firmas con tu Autoridad Certificadora (CA) y devuelves un certificado firmado.
3.	El vendedor guarda el certificado firmado en su USB.
4.	Cuando el vendedor conecta el USB, tu sistema verifica el certificado y, si es válido, le da acceso.
________________________________________
2. Herramientas que Necesitas
•	OpenSSL: Es una herramienta de línea de comandos que se usa para generar y firmar certificados. Viene instalada en la mayoría de los sistemas Linux y macOS. Si usas Windows, puedes descargarla desde aquí.
•	Un editor de texto (como Notepad, VSCode, o cualquier otro) para guardar los archivos de configuración.
________________________________________
3. Paso 1: Crear una Autoridad Certificadora (CA)
¿Qué es una CA?
Es como una "fábrica de certificados". Tú serás la CA y firmarás las solicitudes de los vendedores.
Código para crear la CA:
1.	Abre una terminal (en Linux/macOS) o el símbolo del sistema (en Windows).
2.	Ejecuta los siguientes comandos:
bash
Copy
# Generar una clave privada para la CA
openssl genrsa -out ca.key 2048

# Crear un certificado autofirmado para la CA (válido por 365 días)
openssl req -new -x509 -days 365 -key ca.key -out ca.crt
•	ca.key: Es la clave privada de tu CA. ¡No la compartas!
•	ca.crt: Es el certificado público de tu CA. Lo usarás para verificar los certificados firmados.
________________________________________
4. Paso 2: El Vendedor Genera una Solicitud de Firma (CSR)
¿Qué es una CSR?
Es una solicitud que el vendedor genera para que tú le firmes un certificado.
Código para generar la CSR:
El vendedor debe ejecutar estos comandos en su computadora:
bash
Copy
# Generar una clave privada para el vendedor
openssl genrsa -out vendedor.key 2048

# Generar una solicitud de firma (CSR)
openssl req -new -key vendedor.key -out vendedor.csr
•	vendedor.key: Es la clave privada del vendedor. ¡No la compartas!
•	vendedor.csr: Es la solicitud de firma que el vendedor te enviará.
________________________________________
5. Paso 3: Firmar la CSR y Crear un Certificado
¿Qué haces tú?
Recibes el archivo vendedor.csr, lo firmas con tu CA y devuelves un certificado firmado.
Código para firmar la CSR:
1.	Guarda el archivo vendedor.csr en tu computadora.
2.	Ejecuta este comando:
bash
Copy
# Firmar la CSR y generar un certificado válido por 365 días
openssl x509 -req -in vendedor.csr -CA ca.crt -CAkey ca.key -CAcreateserial -out vendedor.crt -days 365
•	vendedor.crt: Es el certificado firmado que debes devolver al vendedor.
________________________________________
6. Paso 4: El Vendedor Guarda el Certificado en el USB
El vendedor debe copiar los siguientes archivos en su USB:
1.	vendedor.crt (el certificado firmado que le enviaste).
2.	vendedor.key (su clave privada, que generó en el Paso 2).
________________________________________
7. Paso 5: Verificar el Certificado en tu Sistema
Cuando el vendedor conecta el USB, tu sistema debe verificar que el certificado es válido.
Código para verificar el certificado:
1.	Copia los archivos vendedor.crt y ca.crt a tu computadora.
2.	Ejecuta este comando:
bash
Copy
# Verificar que el certificado es válido
openssl verify -CAfile ca.crt vendedor.crt
•	Si el certificado es válido, verás un mensaje como: vendedor.crt: OK.
________________________________________
8. Paso 6: Automatizar la Verificación (Opcional)
Si quieres que tu sistema verifique automáticamente el certificado cuando se conecta un USB, puedes usar un script. Aquí te dejo un ejemplo en Python:
Código del script (verificar.py):
python
Copy
import os
import subprocess

# Ruta al certificado del vendedor y a la CA
usb_path = "/media/usb/vendedor.crt"  # Cambia esto según tu sistema
ca_path = "ca.crt"

# Verificar el certificado
result = subprocess.run(["openssl", "verify", "-CAfile", ca_path, usb_path], capture_output=True, text=True)

# Mostrar el resultado
if "OK" in result.stdout:
    print("Acceso concedido. Certificado válido.")
else:
    print("Acceso denegado. Certificado inválido.")
¿Cómo usar el script?
1.	Guarda el código en un archivo llamado verificar.py.
2.	Ejecuta el script cuando el USB esté conectado:
bash
Copy
python3 verificar.py
________________________________________
9. Resumen de Archivos y su Uso
Archivo	Descripción
ca.key	Clave privada de tu CA. ¡No la compartas!
ca.crt	Certificado público de tu CA. Lo usas para verificar certificados.
vendedor.key	Clave privada del vendedor. ¡No la compartas!
vendedor.csr	Solicitud de firma que el vendedor te envía.
vendedor.crt	Certificado firmado que devuelves al vendedor.
________________________________________
10. Próximos Pasos
1.	Prueba el proceso completo en tu computadora.
2.	Si todo funciona, puedes integrar la verificación en tu sistema (por ejemplo, usando el script de Python).
3.	Asegúrate de proteger los archivos de clave privada (ca.key y vendedor.key).







2ª parte
Crear la Clase CertificateAuthority
Esta agregado en el codigo

Integrar la CA con los Validadores
Ahora que tienes la clase CertificateAuthority, puedes integrarla con tus validadores existentes.
CSRValidator
Ya está validando si el archivo CSR es válido. No necesita cambios.
KeyValidator
Ya está validando si la clave privada es válida. No necesita cambios.
USBValidator
Aquí puedes agregar la verificación del certificado firmado por la CA.
Nuevo Método en USBValidator
Agrega este método para verificar el certificado firmado:
Esta agregado en el codigo



Gepeto crea este main como “ejemplo de uso”

public class Main {
    public static void main(String[] args) {
        // Ruta del USB
        String usbPath = "D:";

        // Validar el USB
        if (USBValidator.verifyUSBFiles()) {
            System.out.println("USB validado correctamente.");

            // Validar la clave privada
            String privateKeyPath = USBValidator.getKeyPath(usbPath, "privateKey");
            if (KeyValidator.validatePrivateKey(privateKeyPath, "password")) {
                System.out.println("Clave privada validada correctamente.");

                // Validar la CSR
                String csrPath = USBValidator.getCsrPath(usbPath, "CCSR");
                if (CSRValidator.validateCSR(csrPath)) {
                    System.out.println("CSR validada correctamente.");

                    // Firmar la CSR
                    CertificateAuthority ca = new CertificateAuthority();
                    String signedCertPath = usbPath + "\\signed_cert.crt";
                    if (ca.signCSR(csrPath, signedCertPath)) {
                        System.out.println("CSR firmada y certificado guardado en el USB.");

                        // Verificar el certificado firmado
                        if (ca.verifyCertificate(signedCertPath)) {
                            System.out.println("Certificado firmado verificado correctamente.");
                        }
                    }
                }
            }
        }
    }
}



