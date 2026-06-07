package com.navium.agendamiento.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.navium.agendamiento.model.Agendamiento;
import com.navium.agendamiento.model.EstadoAgendamiento;

@Repository
public interface AgendamientoRepository extends JpaRepository<Agendamiento, Long> {
    //Metodos personalizados

    //Verifica si un camión ya está agendando en el bloque inicio
    boolean existsByPatenteCamionAndBloqueInicio(String patenteCamion, LocalDateTime bloqueInicio);

    //Cuenta cuántos camiones totales hay en el bloque inicio
    long countByBloqueInicio(LocalDateTime bloqueInicio);

    //Cuenta cuantos registros coinciden con esas fechas en la base de datos
    int countByBloqueInicioAndBloqueFin(LocalDateTime bloqueInicio, LocalDateTime bloquefin);

    //Busca agendamientos por el rut del chofer
    List<Agendamiento> findByRutChofer(String rutChofer);
    
    //Busca un agendamiento por estado
    List<Agendamiento> findByEstado(EstadoAgendamiento estado);

    //Busca por Patente
    List<Agendamiento> findByPatenteCamion(String patenteCamion);

    //Busca en un rango de fechas
    List<Agendamiento> findByBloqueInicioBetween(LocalDateTime inicio, LocalDateTime fin);

    //Metodo personalizado
    Optional<Agendamiento> findFirstByPatenteCamionAndBloqueInicioLessThanEqualAndBloqueFinGreaterThanEqualAndEstadoNotInOrderByBloqueInicioDesc(
        String patente, LocalDateTime momentoConsulta1, LocalDateTime momentoConsulta2, List<EstadoAgendamiento> estadosNoPermitidos);

    Optional<Agendamiento> findFirstByPatenteCamionAndBloqueInicioAfterAndEstadoNotInOrderByBloqueInicioAsc(
        String patente, LocalDateTime momentoConsulta, List<EstadoAgendamiento> estadosNoPermitidos);
}
