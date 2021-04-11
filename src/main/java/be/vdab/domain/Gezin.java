package be.vdab.domain;

import java.util.ArrayList;
import java.util.List;

public class Gezin {
    private final String papa;
    private final String mama;
    private final List<String> kinderen = new ArrayList<>();

    public Gezin(String papa, String mama) {
        this.papa = papa;
        this.mama = mama;
    }

    public void addKind(String kind) {
        kinderen.add(kind);
    }

    public String getPapa() {
        return papa;
    }

    public String getMama() {
        return mama;
    }

    public List<String> getKinderen() {
        return kinderen;
    }
}
