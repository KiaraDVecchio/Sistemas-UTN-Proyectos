/*package ar.utn.ba.ddsi.models.repositories.impl;

import ar.utn.ba.ddsi.models.entities.Hecho;
import ar.utn.ba.ddsi.models.repositories.IHechoRepository;
import java.util.ArrayList;
import java.util.List;

public class HechoRepository implements IHechoRepository {

  private List<Hecho> hechos = new ArrayList<>();

  @Override
  public Hecho save(Hecho hecho){
    hechos.add(hecho);
    return hecho;
  }

  @Override
  public List<Hecho> findAll(){
    return hechos;
  }

  @Override
  public Hecho findById(Long hechoId){
    return hechos.stream().filter(h -> h.getId().equals(hechoId)).findFirst().orElse(null);
  }

  @Override
  public void deleteById(Long id){
    hechos.removeIf(h -> h.getId().equals(id));
  }

}
*/