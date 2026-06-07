package com.navium.agendamiento.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.navium.agendamiento.AgendamientoApplication;
import com.navium.agendamiento.dto.AgendamientoRequestDTO;
import com.navium.agendamiento.dto.AgendamientoResponseDTO;
import com.navium.agendamiento.model.Agendamiento;
import com.navium.agendamiento.model.EstadoAgendamiento;
import com.navium.agendamiento.model.TipoOperacion;
import com.navium.agendamiento.repository.AgendamientoRepository;

@ExtendWith(MockitoExtension.class)
class AgendamientoServiceTests {

    @Mock
    private AgendamientoRepository agendamientoRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private AgendamientoApplication agendamientoApplication;

    @InjectMocks
    private AgendamientoService agendamientoService;

    @Test
    void debeCrearAgendamientoYEnviarMensaje() {
        LocalDateTime inicio = LocalDateTime.now().plusDays(1);
        AgendamientoRequestDTO dtoMock = new AgendamientoRequestDTO();
        dtoMock.setIdUsuario(1L);
        dtoMock.setPatenteCamion("AB-CD-12");
        dtoMock.setRutChofer("12345678-9");
        dtoMock.setCorreoUsuario("chofer@puerto.cl");
        dtoMock.setHoraInicio(inicio.toString());
        dtoMock.setTipoOperacion(TipoOperacion.INGRESO_CARGA);

        Agendamiento agendamientoMock = new Agendamiento();
        agendamientoMock.setId(1L);
        agendamientoMock.setUsuarioId(1L);
        agendamientoMock.setPatenteCamion("AB-CD-12");
        agendamientoMock.setRutChofer("12345678-9");
        agendamientoMock.setCorreoUsuario("chofer@puerto.cl");
        agendamientoMock.setTipoOperacion(TipoOperacion.INGRESO_CARGA);
        agendamientoMock.setEstadoAgendamiento(EstadoAgendamiento.CREADO);
        agendamientoMock.setBloqueInicio(inicio);
        agendamientoMock.setBloqueFin(inicio.plusHours(1));

        when(agendamientoRepository.countByBloqueInicioAndBloqueFin(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(0);
        when(agendamientoRepository.existsByPatenteCamionAndBloqueInicio(anyString(), any(LocalDateTime.class))).thenReturn(false);
        when(agendamientoRepository.save(any(Agendamiento.class))).thenReturn(agendamientoMock);

        AgendamientoResponseDTO resultado = agendamientoService.crearAgendamiento(dtoMock);

        assertNotNull(resultado);
        assertEquals("AB-CD-12", resultado.getPatenteCamion());
        assertEquals("CREADO", resultado.getEstadoAgendamiento());

        verify(agendamientoRepository, times(1)).save(any(Agendamiento.class));
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void debeFallarCrearAgendamientoConFechaPasada() {
        AgendamientoRequestDTO dtoMock = new AgendamientoRequestDTO();
        dtoMock.setIdUsuario(1L);
        dtoMock.setPatenteCamion("AB-CD-12");
        dtoMock.setRutChofer("12345678-9");
        dtoMock.setCorreoUsuario("chofer@puerto.cl");
        dtoMock.setHoraInicio(LocalDateTime.now().minusDays(1).toString());
        dtoMock.setTipoOperacion(TipoOperacion.INGRESO_CARGA);

        assertThrows(IllegalArgumentException.class, () -> agendamientoService.crearAgendamiento(dtoMock));
    }

    @Test
    void debeActualizarEstadoYEnviarMensaje() {
        Agendamiento agendamientoMock = new Agendamiento();
        agendamientoMock.setId(1L);
        agendamientoMock.setPatenteCamion("AB-CD-12");
        agendamientoMock.setRutChofer("12345678-9");
        agendamientoMock.setCorreoUsuario("chofer@puerto.cl");
        agendamientoMock.setBloqueInicio(LocalDateTime.parse("2026-05-10T10:00:00"));
        agendamientoMock.setBloqueFin(LocalDateTime.parse("2026-05-10T11:00:00"));
        agendamientoMock.setEstadoAgendamiento(EstadoAgendamiento.CREADO);

        when(agendamientoRepository.findById(1L)).thenReturn(Optional.of(agendamientoMock));
        when(agendamientoRepository.save(any(Agendamiento.class))).thenReturn(agendamientoMock);

        Agendamiento resultado = agendamientoService.actualizarEstado(1L, "FINALIZADO");

        assertNotNull(resultado);
        assertEquals(EstadoAgendamiento.FINALIZADO, resultado.getEstadoAgendamiento());

        verify(agendamientoRepository, times(1)).findById(1L);
        verify(agendamientoRepository, times(1)).save(any(Agendamiento.class));
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void debeFallarActualizarEstadoSiNoExiste() {
        when(agendamientoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> agendamientoService.actualizarEstado(999L, "CREADO"));
    }

    @Test
    void debeObtenerTodosLosAgendamientos() {
        Agendamiento primerAgendamiento = new Agendamiento();
        primerAgendamiento.setId(1L);
        Agendamiento segundoAgendamiento = new Agendamiento();
        segundoAgendamiento.setId(2L);

        when(agendamientoRepository.findAll()).thenReturn(List.of(primerAgendamiento, segundoAgendamiento));

        List<Agendamiento> resultado = agendamientoService.obtenerTodos();

        assertEquals(2, resultado.size());
    }

    @Test
    void debeObtenerPorIdExistente() {
        Agendamiento agendamientoMock = new Agendamiento();
        agendamientoMock.setId(1L);
        when(agendamientoRepository.findById(1L)).thenReturn(Optional.of(agendamientoMock));

        Agendamiento resultado = agendamientoService.obtenerPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    void debeFallarObtenerPorIdInexistente() {
        when(agendamientoRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> agendamientoService.obtenerPorId(5L));
    }

    @Test
    void debeActualizarTransporte() {
        Agendamiento agendamientoMock = new Agendamiento();
        agendamientoMock.setId(1L);
        agendamientoMock.setPatenteCamion("AB-CD-12");
        agendamientoMock.setRutChofer("12345678-9");

        when(agendamientoRepository.findById(1L)).thenReturn(Optional.of(agendamientoMock));
        when(agendamientoRepository.save(any(Agendamiento.class))).thenReturn(agendamientoMock);

        Agendamiento resultado = agendamientoService.actualizarTransporte(1L, "ZZ-99-XX", "98765432-1");

        assertNotNull(resultado);
        assertEquals("ZZ-99-XX", resultado.getPatenteCamion());
        assertEquals("98765432-1", resultado.getRutChofer());
    }

    @Test
    void debeBuscarPorEstado() {
        Agendamiento agendamientoMock = new Agendamiento();
        agendamientoMock.setEstadoAgendamiento(EstadoAgendamiento.CREADO);
        when(agendamientoRepository.findByEstado(EstadoAgendamiento.CREADO)).thenReturn(List.of(agendamientoMock));

        List<Agendamiento> resultado = agendamientoService.buscarPorEstado(EstadoAgendamiento.CREADO);

        assertEquals(1, resultado.size());
        assertEquals(EstadoAgendamiento.CREADO, resultado.get(0).getEstadoAgendamiento());
    }

    @Test
    void debeCancelarAgendamiento() {
        LocalDateTime inicio = LocalDateTime.now().plusDays(1);
        Agendamiento agendamientoMock = new Agendamiento();
        agendamientoMock.setId(1L);
        agendamientoMock.setEstadoAgendamiento(EstadoAgendamiento.CREADO);
        agendamientoMock.setBloqueInicio(inicio);
        when(agendamientoRepository.findById(1L)).thenReturn(Optional.of(agendamientoMock));
        when(agendamientoRepository.save(any(Agendamiento.class))).thenReturn(agendamientoMock);

        Agendamiento resultado = agendamientoService.cancelarAgendamiento(1L);

        assertEquals(EstadoAgendamiento.CANCELADO, resultado.getEstadoAgendamiento());
    }

    @Test
    void debeFallarCancelarAgendamientoFinalizado() {
        Agendamiento agendamientoMock = new Agendamiento();
        agendamientoMock.setId(1L);
        agendamientoMock.setEstadoAgendamiento(EstadoAgendamiento.FINALIZADO);
        when(agendamientoRepository.findById(1L)).thenReturn(Optional.of(agendamientoMock));

        assertThrows(IllegalStateException.class, () -> agendamientoService.cancelarAgendamiento(1L));
    }

    @Test
    void debeBuscarPorPatente() {
        Agendamiento agendamientoMock = new Agendamiento();
        agendamientoMock.setPatenteCamion("AB-CD-12");
        when(agendamientoRepository.findByPatenteCamion("AB-CD-12")).thenReturn(List.of(agendamientoMock));

        List<Agendamiento> resultado = agendamientoService.buscarPorPatente("AB-CD-12");

        assertEquals(1, resultado.size());
        assertEquals("AB-CD-12", resultado.get(0).getPatenteCamion());
    }

    @Test
    void debeBuscarPorRangoFechas() {
        LocalDateTime inicio = LocalDateTime.parse("2026-05-10T10:00:00");
        LocalDateTime fin = LocalDateTime.parse("2026-05-10T11:00:00");
        Agendamiento agendamientoMock = new Agendamiento();
        when(agendamientoRepository.findByBloqueInicioBetween(inicio, fin)).thenReturn(List.of(agendamientoMock));

        List<Agendamiento> resultado = agendamientoService.buscarPorRangoFechas(inicio, fin);

        assertEquals(1, resultado.size());
    }

    @Test
    void debeConsultarAgendamientoPorId() {
        Agendamiento agendamientoMock = new Agendamiento();
        agendamientoMock.setId(1L);
        when(agendamientoRepository.findById(1L)).thenReturn(Optional.of(agendamientoMock));

        Agendamiento resultado = agendamientoService.consultarAgendamiento(null, 1L, null);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    void debeConsultarAgendamientoVigentePorPatente() {
        LocalDateTime ahora = LocalDateTime.parse("2026-05-10T10:00:00");
        Agendamiento agendamientoMock = new Agendamiento();
        agendamientoMock.setPatenteCamion("AB-CD-12");

        when(agendamientoRepository.findFirstByPatenteCamionAndBloqueInicioLessThanEqualAndBloqueFinGreaterThanEqualAndEstadoNotInOrderByBloqueInicioDesc(
                eq("AB-CD-12"), eq(ahora), eq(ahora), any(List.class)))
            .thenReturn(Optional.of(agendamientoMock));

        Agendamiento resultado = agendamientoService.consultarAgendamiento("AB-CD-12", null, ahora);

        assertNotNull(resultado);
        assertEquals("AB-CD-12", resultado.getPatenteCamion());
    }

    @Test
    void debeConsultarAgendamientoFuturoPorPatente() {
        LocalDateTime ahora = LocalDateTime.parse("2026-05-10T10:00:00");
        Agendamiento agendamientoMock = new Agendamiento();
        agendamientoMock.setPatenteCamion("AB-CD-12");

        when(agendamientoRepository.findFirstByPatenteCamionAndBloqueInicioLessThanEqualAndBloqueFinGreaterThanEqualAndEstadoNotInOrderByBloqueInicioDesc(
                anyString(), any(LocalDateTime.class), any(LocalDateTime.class), any(List.class)))
            .thenReturn(Optional.empty());
        when(agendamientoRepository.findFirstByPatenteCamionAndBloqueInicioAfterAndEstadoNotInOrderByBloqueInicioAsc(
                eq("AB-CD-12"), eq(ahora), any(List.class)))
            .thenReturn(Optional.of(agendamientoMock));

        Agendamiento resultado = agendamientoService.consultarAgendamiento("AB-CD-12", null, ahora);

        assertNotNull(resultado);
        assertEquals("AB-CD-12", resultado.getPatenteCamion());
    }

    @Test
    void debeFallarConsultarAgendamientoSinIdYPantente() {
        assertThrows(IllegalArgumentException.class, () -> agendamientoService.consultarAgendamiento("", null, null));
    }
}
