package cl.techstore.api.controller;

import cl.techstore.api.dto.ProductoDTO;
import cl.techstore.api.model.Producto;
import cl.techstore.api.service.ProductoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private SqsClient sqsClient;

    @Autowired
    private ObjectMapper objectMapper;

    // Leemos la URL de la cola desde las propiedades. Si no se define, usa el valor por defecto de AWS real.
    @Value("${aws.sqs.queue-url:https://sqs.us-east-1.amazonaws.com/123456789012/techstore-audit-queue}")
    private String queueUrl;

    // 1. Listar todos los productos (GET /api/productos) -> 200 OK
    @GetMapping
    public ResponseEntity<List<Producto>> listar() {
        return ResponseEntity.ok(productoService.listarTodos());
    }

    // 2. Crear un producto nuevo (POST /api/productos) -> 201 Created
    @PostMapping
    public ResponseEntity<Producto> crear(@RequestBody ProductoDTO dto) {
        Producto nuevoProducto = productoService.crear(dto);
        
        // Disparamos la auditoría de forma asíncrona para no bloquear la respuesta HTTP
        enviarAuditoriaAsincrona("CREAR", nuevoProducto);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoProducto);
    }

    // 3. Modificar un producto existente (PUT /api/productos/{id}) -> 200 OK
    @PutMapping("/{id}")
    public ResponseEntity<Producto> modificar(@PathVariable Long id, @RequestBody ProductoDTO dto) {
        Producto productoModificado = productoService.modificar(id, dto);
        
        enviarAuditoriaAsincrona("MODIFICAR", productoModificado);
        
        return ResponseEntity.ok(productoModificado);
    }

    // 4. Eliminar un producto (DELETE /api/productos/{id}) -> 204 No Content
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        // Obtenemos el producto antes de invocar el borrado lógico para capturar su nombre
        Producto productoAEliminar = productoService.listarTodos().stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElse(null);

        productoService.eliminar(id);
        
        if (productoAEliminar != null) {
            enviarAuditoriaAsincrona("ELIMINAR", productoAEliminar);
        }
        
        return ResponseEntity.noContent().build();
    }

    /**
     * Método auxiliar encargado de procesar y despachar el evento JSON hacia SQS en un hilo secundario.
     */
    private void enviarAuditoriaAsincrona(String accion, Producto producto) {
        // 1. Obtener el usuario autenticado desde el contexto de Spring Security (JWT)
        String usuarioAutenticado = "anonimo@techstore.cl";
        if (SecurityContextHolder.getContext().getAuthentication() != null &&
            SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
            usuarioAutenticado = SecurityContextHolder.getContext().getAuthentication().getName();
        }

        final String usuarioLogueado = usuarioAutenticado;

        // 2. Ejecutar la llamada de red a AWS de forma asíncrona
        CompletableFuture.runAsync(() -> {
            try {
                // Construcción de la estructura JSON obligatoria exigida por la pauta
                Map<String, Object> eventoAudit = new HashMap<>();
                eventoAudit.put("accion", accion);
                eventoAudit.put("productoId", producto.getId());
                eventoAudit.put("nombre", producto.getNombre());
                eventoAudit.put("usuario", usuarioLogueado);
                eventoAudit.put("fecha", Instant.now().toString()); // Formato ISO 8601 exigido

                // Serializar el mapa a un String formato JSON
                String mensajeJson = objectMapper.writeValueAsString(eventoAudit);

                // Construcción de la petición para enviar el mensaje a SQS
                SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .messageBody(mensajeJson)
                        .build();

                // Envío efectivo del mensaje a la cola
                sqsClient.sendMessage(sendMsgRequest);
                System.out.println("[SQS Producer] Mensaje enviado con éxito: " + mensajeJson);

            } catch (Exception e) {
                // Manejo de excepciones en segundo plano para no interrumpir el flujo del usuario principal
                System.err.println("[SQS Error] Error enviando auditoría de inventario: " + e.getMessage());
            }
        });
    }
}