package main;
import db.UserManager;
import services.AuctionManager;
import db.PasswordUtil;
import validators.CSRValidator;
import validators.KeyValidator;
import validators.USBValidator;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        AuctionManager auctionManager = new AuctionManager(); // Instanciar AuctionManager

        while (true) {
            System.out.println("Seleccione una opción:");
            System.out.println("1. Ver subastas");
            System.out.println("2. Eliminar subasta");
            System.out.println("4. Eliminar usuario");
            System.out.println("5. Salir");

            System.out.print("Opción: ");
            int opcion = scanner.nextInt();
            scanner.nextLine();  // Consumir el salto de línea residual

            // Opciones disponibles sin login
            if (opcion == 1) {
                // Opción para ver subastas
                System.out.println("Subastas disponibles:");
                var auctions = auctionManager.getAllAuctions();

                if (auctions.isEmpty()) {
                    System.out.println("No hay subastas disponibles.");
                } else {
                    for (var auction : auctions) {
                        System.out.println(auction);
                    }
                }
            } else if (opcion == 2) {
                // Opción para eliminar una subasta
                System.out.print("Ingresa el ID de la subasta que deseas eliminar: ");
                int auctionId = scanner.nextInt();

                boolean deletionSuccess = auctionManager.deleteAuction(auctionId);
                if (deletionSuccess) {
                    System.out.println("Subasta eliminada exitosamente.");
                } else {
                    System.out.println("No se pudo eliminar la subasta. Asegúrate de que el ID sea correcto.");
                }
            } else if (opcion == 4) {
                // Opción para eliminar un usuario
                System.out.print("Ingresa el nombre del usuario que deseas eliminar: ");
                String usernameToDelete = scanner.nextLine();

                boolean deletionSuccess = UserManager.deleteUser(usernameToDelete);
                if (deletionSuccess) {
                    System.out.println("Usuario eliminado exitosamente.");
                } else {
                    System.out.println("No se pudo eliminar el usuario. Asegúrate de que el usuario exista.");
                }
            } else if (opcion == 5) {
                // Opción para salir
                System.out.println("Saliendo...");
                break; // Salir del bucle
            } else {
                System.out.println("Opción no válida.");
            }
        }

        scanner.close();
    }
}
