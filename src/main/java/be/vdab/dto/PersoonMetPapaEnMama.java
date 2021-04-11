package be.vdab.dto;

import java.util.Optional;

public class PersoonMetPapaEnMama {
    private final String voornaam;
    private final String papaVoornaam;
    private final String mamaVoornaam;

    public PersoonMetPapaEnMama(String voornaam, String papaVoornaam, String mamaVoornaam) {
        this.voornaam = voornaam;
        this.papaVoornaam = papaVoornaam;
        this.mamaVoornaam = mamaVoornaam;
    }

    public String getVoornaam() {
        return voornaam;
    }

    public Optional<String> getVoornaamPapa() {
        return Optional.ofNullable(papaVoornaam);
    }

    public Optional<String> getVoornaamMama() {
        return Optional.ofNullable(mamaVoornaam);
    }
}
