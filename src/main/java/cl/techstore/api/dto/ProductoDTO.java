package cl.techstore.api.dto;

import lombok.Data;

@Data
public class ProductoDTO {
    private String nombre;
    private String descripcion;
    private Double precio;
    private Integer stock;
    // Recibimos el ID de la categoría, no el objeto completo
    private Long categoriaId; 
    private Boolean activo;
}