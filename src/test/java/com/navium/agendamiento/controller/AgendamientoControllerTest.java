package com.navium.agendamiento.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.navium.agendamiento.dto.AgendamientoResponseDTO;
import com.navium.agendamiento.model.Agendamiento;
import com.navium.agendamiento.model.EstadoAgendamiento;
import com.navium.agendamiento.model.TipoOperacion;
import com.navium.agendamiento.service.AgendamientoService;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
})
@AutoConfigureMockMvc
class AgendamientoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AgendamientoService agendamientoService;

    // ── Helper ResponseDTO 
    private AgendamientoResponseDTO mockResponseDTO(Long id, String patente) {
        AgendamientoResponseDTO dto = new AgendamientoResponseDTO();
        dto.setId(id);
        dto.setPatenteCamion(patente);
        dto.setRutChofer("12345678-9");
        dto.setCorreoUsuario("chofer@puerto.cl");
        dto.setIdUsuario(1L);
        dto.setTipoOperacion("INGRESO_CARGA");
        dto.setHoraInicio(LocalDateTime.of(2026, 8, 11, 10, 0));
        dto.setBloqueFin(LocalDateTime.of(2026, 8, 11, 11, 0));
        dto.setEstadoAgendamiento("CREADO");
        return dto;
    }

    // ── Helper Agendamiento entidad
    private Agendamiento mockEntidad(Long id, String patente) {
        Agendamiento a = new Agendamiento();
        a.setId(id);
        a.setPatenteCamion(patente);
        a.setRutChofer("12345678-9");
        a.setCorreoUsuario("chofer@puerto.cl");
        a.setUsuarioId(1L);
        a.setTipoOperacion(TipoOperacion.INGRESO_CARGA);
        a.setBloqueInicio(LocalDateTime.of(2026, 8, 11, 10, 0));
        a.setBloqueFin(LocalDateTime.of(2026, 8, 11, 11, 0));
        a.setEstadoAgendamiento(EstadoAgendamiento.CREADO);
        return a;
    }

    // 1. CREAR AGENDAMIENTO
    @Test
    void debeCrearAgendamiento() throws Exception {
        AgendamientoResponseDTO mock = mockResponseDTO(1L, "ABCD-1212");
        when(agendamientoService.crearAgendamiento(any())).thenReturn(mock);

        String json = """
        {
            "patenteCamion": "ABCD-1212",
            "rutChofer": "12345678-9",
            "correoUsuario": "chofer@puerto.cl",
            "idUsuario": 1,
            "tipoOperacion": "INGRESO_CARGA",
            "horaInicio": "2026-08-11T10:00:00"
        }
        """;

        mockMvc.perform(post("/api/agendamientos")
                .with(user("user").authorities(new SimpleGrantedAuthority("ROL_SUCURSAL")))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.patenteCamion").value("ABCD-1212"));
    }

    // 2. ACTUALIZAR ESTADO — requiere ROL_OPERADOR o ROL_CENTRO_MANDO
    @Test
    void debeActualizarEstado() throws Exception {
        Agendamiento mock = mockEntidad(1L, "ABCD-1212");
        mock.setEstadoAgendamiento(EstadoAgendamiento.FINALIZADO);
        when(agendamientoService.actualizarEstado(eq(1L), eq("FINALIZADO"))).thenReturn(mock);

        mockMvc.perform(put("/api/agendamientos/1/estado")
                .with(user("user").authorities(new SimpleGrantedAuthority("ROL_OPERADOR")))
                .with(csrf())
                .param("estado", "FINALIZADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoAgendamiento").value("FINALIZADO"));
    }

    // 3. OBTENER TODOS
    @Test
    void debeObtenerTodos() throws Exception {
        Agendamiento a1 = mockEntidad(1L, "ABCD-1212");
        Agendamiento a2 = mockEntidad(2L, "XYZZ-9999");
        when(agendamientoService.obtenerTodos()).thenReturn(List.of(a1, a2));

        mockMvc.perform(get("/api/agendamientos")
                .with(user("user").authorities(new SimpleGrantedAuthority("ROL_CENTRO_MANDO"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].patenteCamion").value("ABCD-1212"))
                .andExpect(jsonPath("$[1].patenteCamion").value("XYZZ-9999"));
    }

    // 4. OBTENER POR ID
    @Test
    void debeObtenerPorId() throws Exception {
        Agendamiento mock = mockEntidad(1L, "ABCD-1212");
        when(agendamientoService.obtenerPorId(1L)).thenReturn(mock);

        mockMvc.perform(get("/api/agendamientos/1")
                .with(user("user").authorities(new SimpleGrantedAuthority("ROL_OPERADOR"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    // 5. BUSCAR POR ESTADO
    @Test
    void debeBuscarPorEstado() throws Exception {
        Agendamiento mock = mockEntidad(1L, "ABCD-1212");
        mock.setEstadoAgendamiento(EstadoAgendamiento.CREADO);
        when(agendamientoService.buscarPorEstado(EstadoAgendamiento.CREADO)).thenReturn(List.of(mock));

        mockMvc.perform(get("/api/agendamientos/estado/CREADO")
                .with(user("user").authorities(new SimpleGrantedAuthority("ROL_OPERADOR"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].estadoAgendamiento").value("CREADO"));
    }

    // 6. ACTUALIZAR TRANSPORTE — requiere ROL_OPERADOR o ROL_CENTRO_MANDO
    @Test
    void debeActualizarTransporte() throws Exception {
        Agendamiento mock = mockEntidad(1L, "ZZZZ-9999");
        when(agendamientoService.actualizarTransporte(eq(1L), eq("ZZZZ-9999"), eq("98765432-1")))
                .thenReturn(mock);

        mockMvc.perform(put("/api/agendamientos/1/transporte")
                .with(user("user").authorities(new SimpleGrantedAuthority("ROL_OPERADOR")))
                .with(csrf())
                .param("patente", "ZZZZ-9999")
                .param("rut", "98765432-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patenteCamion").value("ZZZZ-9999"));
    }

    // 7. CANCELAR AGENDAMIENTO
    @Test
    void debeCancelarAgendamiento() throws Exception {
        Agendamiento mock = mockEntidad(1L, "ABCD-1212");
        mock.setEstadoAgendamiento(EstadoAgendamiento.CANCELADO);
        when(agendamientoService.cancelarAgendamiento(1L)).thenReturn(mock);

        mockMvc.perform(put("/api/agendamientos/1/cancelar")
                .with(user("user").authorities(new SimpleGrantedAuthority("ROL_CENTRO_MANDO")))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoAgendamiento").value("CANCELADO"));
    }

    // 8. BUSCAR POR PATENTE
    @Test
    void debeBuscarPorPatente() throws Exception {
        Agendamiento mock = mockEntidad(1L, "ABCD-1212");
        when(agendamientoService.buscarPorPatente("ABCD-1212")).thenReturn(List.of(mock));

        mockMvc.perform(get("/api/agendamientos/patente/ABCD-1212")
                .with(user("user").authorities(new SimpleGrantedAuthority("ROL_SUCURSAL"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].patenteCamion").value("ABCD-1212"));
    }

    // 9. BUSCAR POR RANGO DE FECHAS
    @Test
    void debeBuscarPorRangoFechas() throws Exception {
        Agendamiento mock = mockEntidad(1L, "ABCD-1212");
        when(agendamientoService.buscarPorRangoFechas(any(), any())).thenReturn(List.of(mock));

        mockMvc.perform(get("/api/agendamientos/fechas")
                .with(user("user").authorities(new SimpleGrantedAuthority("ROL_CENTRO_MANDO")))
                .param("inicio", "2026-05-01T00:00:00")
                .param("fin", "2026-05-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // 10. CONSULTAR AGENDAMIENTO
    @Test
    void debeConsultarAgendamiento() throws Exception {
        Agendamiento mock = mockEntidad(1L, "ABCD-1212");
        when(agendamientoService.consultarAgendamiento(eq("ABCD-1212"), any(), any())).thenReturn(mock);

        mockMvc.perform(get("/api/agendamientos/consulta")
                .with(user("user").authorities(new SimpleGrantedAuthority("ROL_OPERADOR")))
                .param("patente", "ABCD-1212"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patenteCamion").value("ABCD-1212"));
    }

    // 11. 204 NO CONTENT
    @Test
    void debeRetornar204CuandoConsultaNoEncuentraResultado() throws Exception {
        when(agendamientoService.consultarAgendamiento(eq("XXXX-0000"), any(), any())).thenReturn(null);

        mockMvc.perform(get("/api/agendamientos/consulta")
                .with(user("user").authorities(new SimpleGrantedAuthority("ROL_OPERADOR")))
                .param("patente", "XXXX-0000"))
                .andExpect(status().isNoContent());
    }

    // 12. 403 FORBIDDEN — ROL_SUCURSAL no puede actualizar estado
    @Test
    void debeRetornar403AlActualizarEstadoSinRolOperador() throws Exception {
        mockMvc.perform(put("/api/agendamientos/1/estado")
                .with(user("user").authorities(new SimpleGrantedAuthority("ROL_SUCURSAL")))
                .with(csrf())
                .param("estado", "FINALIZADO"))
                .andExpect(status().isForbidden());
    }

    // 13. 401 UNAUTHORIZED
   @Test
    void debeRetornar403CuandoNoSeEnviaToken() throws Exception {
        mockMvc.perform(get("/api/agendamientos"))
            .andExpect(status().isForbidden());
}
}