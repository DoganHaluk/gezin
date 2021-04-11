package be.vdab;

import be.vdab.domain.Gezin;
import be.vdab.dto.PersoonMetPapaEnMama;
import be.vdab.exceptions.PersoonNietGevondenException;
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
        for (String kind; !"STOP".equals((kind = scanner.nextLine())); ) {
            gezin.addKind(kind);
        }
        var repository1 = new PersoonRepository();
        try {
            repository1.creerEenGezin(gezin);
        } catch (SQLException ex) {
            ex.printStackTrace(System.err);
        }

        System.out.print("PersoonId: ");
        var id = scanner.nextInt();
        var repository2 = new PersoonRepository();
        try {
            repository2.vindEenPersoonDoorId(id).ifPresentOrElse(persoonMetPapaEnMama -> toon(persoonMetPapaEnMama), () -> System.out.println("Niet gevonden"));
        } catch (SQLException ex) {
            ex.printStackTrace(System.err);
        }

        System.out.print("PersoonId die overlijdt: ");
        var persoonId = scanner.nextInt();
        var repository3 = new PersoonRepository();
        try {
            repository3.eenPersoonOverlijdt(persoonId);
        } catch (PersoonNietGevondenException ex){
            System.out.println("Niet gevonden.");
        } catch (SQLException ex){
            ex.printStackTrace(System.err);
        }
    }

    private static void toon(PersoonMetPapaEnMama persoonMetPapaEnMama) {
        System.out.println(persoonMetPapaEnMama.getVoornaam());
        persoonMetPapaEnMama.getVoornaamPapa().ifPresent(voornaamPapa -> System.out.println("papa: " + voornaamPapa));
        persoonMetPapaEnMama.getVoornaamMama().ifPresent(voornaamMama -> System.out.println("mama: " + voornaamMama));
    }
}
