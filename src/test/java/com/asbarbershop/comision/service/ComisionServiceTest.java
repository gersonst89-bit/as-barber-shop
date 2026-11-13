package com.asbarbershop.comision.service;

import com.asbarbershop.barbero.entity.Barbero;
import com.asbarbershop.cita.entity.Cita;
import com.asbarbershop.comision.entity.Comision;
import com.asbarbershop.comision.repository.ComisionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ComisionServiceTest {

    static class ComisionRepositoryStub implements ComisionRepository {
        private Comision comision;

        @Override
        public Optional<Comision> findByCitaIdCita(Integer idCita) {
            return Optional.ofNullable(comision);
        }

        @Override
        public boolean existsByCitaIdCita(Integer idCita) {
            return comision != null;
        }

        @Override
        public List<Comision> findByBarberoIdBarberoOrderByCreadoEnDesc(Integer idBarbero) {
            return (comision != null) ? List.of(comision) : List.of();
        }

        @Override
        public List<Comision> filtrarComisiones(Integer idBarbero, String estado, LocalDateTime f1, LocalDateTime f2) {
            return (comision != null) ? List.of(comision) : List.of();
        }

        @Override public void flush() {}
        @Override public <S extends Comision> S saveAndFlush(S entity) { return entity; }
        @Override public <S extends Comision> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
        @Override public void deleteAllInBatch(Iterable<Comision> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Integer> integers) {}
        @Override public void deleteAllInBatch() {}
        @Override public Comision getOne(Integer integer) { return comision; }
        @Override public Comision getById(Integer integer) { return comision; }
        @Override public Comision getReferenceById(Integer integer) { return comision; }
        @Override public <S extends Comision> Optional<S> findOne(org.springframework.data.domain.Example<S> example) { return Optional.empty(); }
        @Override public <S extends Comision> List<S> findAll(org.springframework.data.domain.Example<S> example) { return List.of(); }
        @Override public <S extends Comision> List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) { return List.of(); }
        @Override public <S extends Comision> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Pageable pageable) { return null; }
        @Override public <S extends Comision> long count(org.springframework.data.domain.Example<S> example) { return 0; }
        @Override public <S extends Comision> boolean exists(org.springframework.data.domain.Example<S> example) { return false; }
        @Override public <S extends Comision, R> R findBy(org.springframework.data.domain.Example<S> example, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public <S extends Comision> S save(S entity) { this.comision = entity; return entity; }
        @Override public <S extends Comision> List<S> saveAll(Iterable<S> entities) { return null; }
        @Override public Optional<Comision> findById(Integer integer) { return Optional.ofNullable(comision); }
        @Override public boolean existsById(Integer integer) { return comision != null; }
        @Override public List<Comision> findAll() { return (comision != null) ? List.of(comision) : List.of(); }
        @Override public List<Comision> findAllById(Iterable<Integer> integers) { return findAll(); }
        @Override public long count() { return comision != null ? 1 : 0; }
        @Override public void deleteById(Integer integer) { comision = null; }
        @Override public void delete(Comision entity) { comision = null; }
        @Override public void deleteAllById(Iterable<? extends Integer> integers) { comision = null; }
        @Override public void deleteAll(Iterable<? extends Comision> entities) { comision = null; }
        @Override public void deleteAll() { comision = null; }
        @Override public List<Comision> findAll(org.springframework.data.domain.Sort sort) { return findAll(); }
        @Override public org.springframework.data.domain.Page<Comision> findAll(org.springframework.data.domain.Pageable pageable) { return null; }
    }

    @Test
    @DisplayName("Debe generar la comisión con la fórmula montoBase * porcentaje / 100")
    void testGenerarComisionParaCita() {
        ComisionRepositoryStub repo = new ComisionRepositoryStub();
        ComisionService service = new ComisionService(repo, null);

        Barbero barbero = new Barbero();
        barbero.setIdBarbero(5);
        barbero.setPorcentajeComision(new BigDecimal("40.00"));

        Cita cita = new Cita();
        cita.setIdCita(10);
        cita.setBarbero(barbero);
        cita.setMontoTotal(new BigDecimal("150.00"));

        Optional<Comision> comisionOpt = service.generarComisionParaCita(cita);

        assertTrue(comisionOpt.isPresent());
        Comision comision = comisionOpt.get();
        assertEquals(new BigDecimal("150.00"), comision.getMontoBase());
        assertEquals(new BigDecimal("40.00"), comision.getPorcentaje());
        assertEquals(new BigDecimal("60.00"), comision.getMontoComision());
        assertEquals("PENDIENTE", comision.getEstado());
    }
}
