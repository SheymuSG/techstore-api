package cl.techstore.api.repository;

import cl.techstore.api.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    // Este método es clave: solo queremos listar los productos que no han sido "eliminados"
    List<Producto> findByActivoTrue();
}