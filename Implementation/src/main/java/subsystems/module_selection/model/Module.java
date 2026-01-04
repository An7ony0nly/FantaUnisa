package subsystems.module_selection.model;

import java.util.ArrayList;
import java.util.List;

public class Module {
    private String id; // Es. "3-4-3"
    private int difensori;
    private int centrocampisti;
    private int attaccanti;

    public Module(String id, int difensori, int centrocampisti, int attaccanti) {
        this.id = id;
        this.difensori = difensori;
        this.centrocampisti = centrocampisti;
        this.attaccanti = attaccanti;
    }

    public String getId() { return id; }
    public int getDifensori() { return difensori; }
    public int getCentrocampisti() { return centrocampisti; }
    public int getAttaccanti() { return attaccanti; }

    /**
     * Restituisce la lista dei moduli ammessi nel Fantacalcio.
     */
    public static List<Module> getValidModules() {
        List<Module> modules = new ArrayList<>();
        modules.add(new Module("3-4-3", 3, 4, 3));
        modules.add(new Module("3-5-2", 3, 5, 2));
        modules.add(new Module("4-3-3", 4, 3, 3));
        modules.add(new Module("4-4-2", 4, 4, 2));
        modules.add(new Module("4-5-1", 4, 5, 1));
        modules.add(new Module("5-3-2", 5, 3, 2));
        modules.add(new Module("5-4-1", 5, 4, 1));
        return modules;
    }

    /**
     * Trova un modulo specifico dato il suo ID (es. "4-4-2").
     */
    public static Module findById(String id) {
        for (Module m : getValidModules()) {
            if (m.getId().equals(id)) return m;
        }
        return null;
    }
}