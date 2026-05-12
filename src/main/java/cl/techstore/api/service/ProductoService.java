package cl.techstore.api.service;

import cl.techstore.api.dto.ProductoDTO;
import cl.techstore.api.model.Categoria;
import cl.techstore.api.model.Producto;
import cl.techstore.api.repository.CategoriaRepository;
import cl.techstore.api.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    // 1. LISTAR (Solo los activos)
    public List<Producto> listarTodos() {
        return productoRepository.findByActivoTrue();
    }

    // 2. CREAR
    public Producto crear(ProductoDTO dto) {
        Producto producto = new Producto();
        return mapearYGuardar(producto, dto);
    }

    // 3. MODIFICAR
    public Producto modificar(Long id, ProductoDTO dto) {
        Producto productoExistente = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        return mapearYGuardar(productoExistente, dto);
    }

    // 4. ELIMINAR (Borrado Lógico)
    public void eliminar(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        
        producto.setActivo(false); // Cambia el estado en lugar de borrar la fila
        productoRepository.save(producto);
    }

    // Método auxiliar para no repetir código entre Crear y Modificar
    private Producto mapearYGuardar(Producto producto, ProductoDTO dto) {
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecio(dto.getPrecio());
        producto.setStock(dto.getStock());
        
        if (dto.getActivo() != null) {
            producto.setActivo(dto.getActivo());
        } else if (producto.getActivo() == null) {
            producto.setActivo(true);
        }

        // Buscar la categoría por ID y asignarla
        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        producto.setCategoria(categoria);

        return productoRepository.save(producto);
    }
}