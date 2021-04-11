package be.vdab;

import be.vdab.domain.Gezin;
import be.vdab.repositories.PersoonRepository;

import java.sql.SQLException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        var scanner = new Scanner(System.in);
        System.out.print("Papa:");
        var papa = scanner.nextLine();
        System.out.print("Mama:");
        var mama = scanner.nextLine();
        var gezin = new Gezin(papa, mama);
        System.out.println("Kinderen (typ STOP om te stoppen):");
        for (String kind; ! "STOP".equals((kind = scanner.nextLine()));) {
            gezin.addKind(kind);
        }
        var repository = new PersoonRepository();
        try {
            repository.creerEenGezin(gezin);
        } catch (SQLException ex) {
            ex.printStackTrace(System.err);
        }
    }
}
