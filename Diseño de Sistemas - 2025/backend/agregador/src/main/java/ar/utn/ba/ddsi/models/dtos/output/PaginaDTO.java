package ar.utn.ba.ddsi.models.dtos.output;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class PaginaDTO<T> {
    public List<T> content;
    public int number;
    public int size;
    public long totalElements;
    public int totalPages;
    public int numberOfElements;
    public boolean first;
    public boolean last;
}
