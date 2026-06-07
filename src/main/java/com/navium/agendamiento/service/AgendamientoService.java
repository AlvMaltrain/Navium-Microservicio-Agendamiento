package com.navium.agendamiento.service;
 
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
 
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
 
import com.navium.agendamiento.AgendamientoApplication;
import com.navium.agendamiento.config.RabbitMQConfig;
import com.navium.agendamiento.dto.AgendamientoMensajeDTO;
import com.navium.agendamiento.dto.AgendamientoRequestDTO;
import com.navium.agendamiento.dto.AgendamientoResponseDTO;
import com.navium.agendamiento.exception.NotFoundException;
import com.navium.agendamiento.model.Agendamiento;
import com.navium.agendamiento.model.EstadoAgendamiento;
import com.navium.agendamiento.repository.AgendamientoRepository;
 
@Service
public class AgendamientoService {
 
    private final AgendamientoApplication agendamientoApplication;
 
    private static final int CAPACIDAD_MAXIMA_POR_BLOQUE = 50;
 
    private final AgendamientoRepository repositorio;
    private final RabbitTemplate rabbitTemplate;
 
    public AgendamientoService(AgendamientoRepository repositorio, RabbitTemplate rabbitTemplate, AgendamientoApplication agendamientoApplication) {
        this.repositorio = repositorio;
        this.rabbitTemplate = rabbitTemplate;
        this.agendamientoApplication = agendamientoApplication;
    }
    
    //---Metodo para crear un agendamiento---
    public AgendamientoResponseDTO crearAgendamiento(AgendamientoRequestDTO dto) {

    LocalDateTime inicio = LocalDateTime.parse(dto.getHoraInicio());
    LocalDateTime fin = inicio.plusHours(1);

    // Validador 0: No permitir reservas para fechas pasadas
    if (inicio.isBefore(LocalDateTime.now())) {
        throw new IllegalArgumentException("No se puede agendar un camión para una fecha pasada.");
    }

    // Validador 1: Lógica de tiempo
    if (inicio.isAfter(fin) || inicio.isEqual(fin)) {
        throw new IllegalArgumentException("La hora de inicio debe ser anterior a la hora de fin.");
    }

    // Validador 2: Control de congestión
    int camionesYaAgendados = repositorio.countByBloqueInicioAndBloqueFin(inicio, fin);
    if (camionesYaAgendados >= CAPACIDAD_MAXIMA_POR_BLOQUE) {
        throw new IllegalStateException("El puerto está lleno para este bloque horario. Por favor seleccione otro");
    }

    // Validador 3: El mismo camión no puede agendarse dos veces en el mismo bloque
    boolean yaExiste = repositorio.existsByPatenteCamionAndBloqueInicio(dto.getPatenteCamion(), inicio);
    if (yaExiste) {
        throw new IllegalStateException("Error: El camión con patente " + dto.getPatenteCamion() + " ya tiene una reserva para este bloque");
    }

    Agendamiento entidad = new Agendamiento();
    entidad.setUsuarioId(dto.getIdUsuario());
    entidad.setCorreoUsuario(dto.getCorreoUsuario());
    entidad.setPatenteCamion(dto.getPatenteCamion());
    entidad.setRutChofer(dto.getRutChofer());
    entidad.setTipoOperacion(dto.getTipoOperacion());
    entidad.setContenedorId(dto.getContenedorId());
    entidad.setBloqueInicio(inicio);
    entidad.setBloqueFin(fin);
    entidad.setEstadoAgendamiento(EstadoAgendamiento.CREADO);

    Agendamiento guardado = repositorio.save(entidad);

    // Envío a RabbitMQ
    AgendamientoMensajeDTO mensajeJson = new AgendamientoMensajeDTO(
        guardado.getCorreoUsuario(),
        guardado.getEstadoAgendamiento().toString(),
        guardado.getPatenteCamion(),
        guardado.getRutChofer(),
        guardado.getBloqueInicio().toString()
    );

    rabbitTemplate.convertAndSend(
        RabbitMQConfig.EXCHANGE_NOTIFICACIONES,
        RabbitMQConfig.ROUTING_KEY,
        mensajeJson
    );

    // Mapeo entidad → ResponseDTO
    AgendamientoResponseDTO response = new AgendamientoResponseDTO();
    response.setId(guardado.getId());
    response.setIdUsuario(guardado.getUsuarioId());
    response.setCorreoUsuario(guardado.getCorreoUsuario());
    response.setPatenteCamion(guardado.getPatenteCamion());
    response.setRutChofer(guardado.getRutChofer());
    response.setTipoOperacion(guardado.getTipoOperacion().toString());
    response.setContenedorId(guardado.getContenedorId());
    response.setHoraInicio(guardado.getBloqueInicio());
    response.setBloqueFin(guardado.getBloqueFin());
    response.setEstadoAgendamiento(guardado.getEstadoAgendamiento().toString());

    return response;
}
    //---ACTUALIZA EL ESTADO DE UN AGENDAMIENTO---
    public Agendamiento actualizarEstado(Long id, String nuevoEstado) {
        Agendamiento agendamiento = repositorio.findById(id)
            .orElseThrow(() -> new NotFoundException("Agendamiento no encontrado con ID: " + id));
 
        try {
            agendamiento.setEstadoAgendamiento(EstadoAgendamiento.valueOf(nuevoEstado));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Estado inválido: " + nuevoEstado + ". Los valores permitidos son: CREADO, EN_TRANSITO, EN_PUERTA, DENTRO_DEL_PUERTO, FINALIZADO, CANCELADO");
        }
 
        Agendamiento actualizado = repositorio.save(agendamiento);
 
        AgendamientoMensajeDTO mensajeJson = new AgendamientoMensajeDTO(
            actualizado.getCorreoUsuario(),
            actualizado.getEstadoAgendamiento().toString(),
            actualizado.getPatenteCamion(),
            actualizado.getRutChofer(),
            actualizado.getBloqueInicio().toString()
        );
 
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE_NOTIFICACIONES,
            RabbitMQConfig.ROUTING_KEY,
            mensajeJson
        );
 
        return actualizado;
    }

    //--- BUSCA AGENDAMIENTOS POR RUT DE CHOFER ---
    public List<Agendamiento> buscarPorRutChofer(String rutChofer) {
        return repositorio.findByRutChofer(rutChofer);
    }
    
    //---OBTIENE TODOS LOS AGENDAMIENTOS ---
    public List<Agendamiento> obtenerTodos() {
        return repositorio.findAll();
    }
    
    //---OBTIENE UN AGENDAMIENTO POR ID ---
    public Agendamiento obtenerPorId(Long id) {
        return repositorio.findById(id)
            .orElseThrow(() -> new NotFoundException("Agendamiento no encontrado con ID: " + id));
    }
    
    //--- ACTUALIZA UN CAMION ---
    public Agendamiento actualizarTransporte(Long id, String nuevaPatente, String nuevoRut) {
        Agendamiento agendamiento = obtenerPorId(id);
        agendamiento.setPatenteCamion(nuevaPatente);
        agendamiento.setRutChofer(nuevoRut);
        return repositorio.save(agendamiento);
    }
    
    //--- BUSCA UN AGENDAMIENTO POR ESTADO ---
    public List<Agendamiento> buscarPorEstado(EstadoAgendamiento estado) {
        return repositorio.findByEstado(estado);
    }
    
    //--- CAMBIA EL ESTADO DE UN AGENDAMIENTO A CANCELADO ---
    public Agendamiento cancelarAgendamiento(Long id) {
        Agendamiento agendamiento = obtenerPorId(id);
 
        if (agendamiento.getEstadoAgendamiento() == EstadoAgendamiento.FINALIZADO) {
            throw new IllegalStateException("No se puede cancelar un agendamiento que ya fue completado.");
        }
 
        agendamiento.setEstadoAgendamiento(EstadoAgendamiento.CANCELADO);
        Agendamiento cancelado = repositorio.save(agendamiento);
 
        // Notificar a RabbitMQ
        AgendamientoMensajeDTO mensajeJson = new AgendamientoMensajeDTO(
            cancelado.getCorreoUsuario(),
            cancelado.getEstadoAgendamiento().toString(),
            cancelado.getPatenteCamion(),
            cancelado.getRutChofer(),
            cancelado.getBloqueInicio().toString()
        );
 
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE_NOTIFICACIONES,
            RabbitMQConfig.ROUTING_KEY,
            mensajeJson
        );
 
        return cancelado;
    }
    
    //--- BUSCA AGENDAMIENTOS POR PATENTE ---
    public List<Agendamiento> buscarPorPatente(String patente) {
        return repositorio.findByPatenteCamion(patente);
    }
    
    //--- BUSCA AGENDAMIENTOS POR RANGO DE FECHAS ---
    public List<Agendamiento> buscarPorRangoFechas(LocalDateTime inicio, LocalDateTime fin) {
        return repositorio.findByBloqueInicioBetween(inicio, fin);
    }
    
    //-- BUSCA AGENDAMIENTO POR PATENTE O ID ---
    public Agendamiento consultarAgendamiento(String patente, Long id, LocalDateTime momento) {
 
        if (id != null) {
            return obtenerPorId(id);
        }
 
        if (patente == null || patente.isBlank()) {
            throw new IllegalArgumentException("Debe indicar id o patente");
        }
 
        LocalDateTime momentoConsulta = (momento != null) ? momento : LocalDateTime.now();
 
        List<EstadoAgendamiento> estadosNoPermitidos = List.of(EstadoAgendamiento.CANCELADO, EstadoAgendamiento.FINALIZADO);
 
        Optional<Agendamiento> vigente = repositorio
            .findFirstByPatenteCamionAndBloqueInicioLessThanEqualAndBloqueFinGreaterThanEqualAndEstadoNotInOrderByBloqueInicioDesc(
                patente,
                momentoConsulta,
                momentoConsulta,
                estadosNoPermitidos);
 
        if (vigente.isPresent()) {
            return vigente.get();
        }
 
        return repositorio
            .findFirstByPatenteCamionAndBloqueInicioAfterAndEstadoNotInOrderByBloqueInicioAsc(
                patente,
                momentoConsulta,
                estadosNoPermitidos)
            .orElse(null);
    }
}
