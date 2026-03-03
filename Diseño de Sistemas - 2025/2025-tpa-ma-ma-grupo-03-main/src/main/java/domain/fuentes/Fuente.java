package domain.fuentes;
import domain.Hecho;
import java.util.List;

public abstract class Fuente {
    public abstract void leerHechos();
    public abstract List<Hecho> getHechos();
}
