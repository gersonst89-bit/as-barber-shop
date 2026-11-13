package com.asbarbershop.promocion.service;

import com.asbarbershop.promocion.dto.ValidarPromocionResponse;
import com.asbarbershop.promocion.entity.Promocion;
import com.asbarbershop.promocion.repository.PromocionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PromocionServiceTest {

    // Stub simple en memoria sin requerir agentes dinámicos ni named pipes de Mockito
    static class PromocionRepositoryStub implements PromocionRepository {
        private Promocion promo;

        void setPromocion(Promocion p) {
            this.promo = p;
        }

        @Override
        public Optional<Promocion> findByCodigo(String codigo) {
            if (promo != null && promo.getCodigo().equalsIgnoreCase(codigo)) {
                return Optional.of(promo);
            }
            return Optional.empty();
        }

        @Override
        public boolean existsByCodigo(String codigo) {
            return promo != null && promo.getCodigo().equalsIgnoreCase(codigo);
        }

        @Override
        public List<Promocion> findVigentes(LocalDate fecha) {
            return (promo != null && promo.getActivo()) ? List.of(promo) : List.of();
        }

        @Override public void flush() {}
        @Override public <S extends Promocion> S saveAndFlush(S entity) { return entity; }
        @Override public <S extends Promocion> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
        @Override public void deleteAllInBatch(Iterable<Promocion> entities) {}
        @Override public void deleteAllByIdInBatch(Iterable<Integer> integers) {}
        @Override public void deleteAllInBatch() {}
        @Override public Promocion getOne(Integer integer) { return promo; }
        @Override public Promocion getById(Integer integer) { return promo; }
        @Override public Promocion getReferenceById(Integer integer) { return promo; }
        @Override public <S extends Promocion> Optional<S> findOne(org.springframework.data.domain.Example<S> example) { return Optional.empty(); }
        @Override public <S extends Promocion> List<S> findAll(org.springframework.data.domain.Example<S> example) { return List.of(); }
        @Override public <S extends Promocion> List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) { return List.of(); }
        @Override public <S extends Promocion> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Pageable pageable) { return null; }
        @Override public <S extends Promocion> long count(org.springframework.data.domain.Example<S> example) { return 0; }
        @Override public <S extends Promocion> boolean exists(org.springframework.data.domain.Example<S> example) { return false; }
        @Override public <S extends Promocion, R> R findBy(org.springframework.data.domain.Example<S> example, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { return null; }
        @Override public <S extends Promocion> S save(S entity) { this.promo = entity; return entity; }
        @Override public <S extends Promocion> List<S> saveAll(Iterable<S> entities) { return null; }
        @Override public Optional<Promocion> findById(Integer integer) { return Optional.ofNullable(promo); }
        @Override public boolean existsById(Integer integer) { return promo != null; }
        @Override public List<Promocion> findAll() { return (promo != null) ? List.of(promo) : List.of(); }
        @Override public List<Promocion> findAllById(Iterable<Integer> integers) { return findAll(); }
        @Override public long count() { return promo != null ? 1 : 0; }
        @Override public void deleteById(Integer integer) { promo = null; }
        @Override public void delete(Promocion entity) { promo = null; }
        @Override public void deleteAllById(Iterable<? extends Integer> integers) { promo = null; }
        @Override public void deleteAll(Iterable<? extends Promocion> entities) { promo = null; }
        @Override public void deleteAll() { promo = null; }
        @Override public List<Promocion> findAll(org.springframework.data.domain.Sort sort) { return findAll(); }
        @Override public org.springframework.data.domain.Page<Promocion> findAll(org.springframework.data.domain.Pageable pageable) { return null; }
    }

    @Test
    @DisplayName("Debe calcular correctamente el descuento porcentual del 20%")
    void testValidarPromocionPorcentaje() {
        PromocionRepositoryStub repo = new PromocionRepositoryStub();
        Promocion promo = new Promocion();
        promo.setIdPromocion(1);
        promo.setCodigo("DESC20");
        promo.setTipoDescuento("PORCENTAJE");
        promo.setValorDescuento(new BigDecimal("20.00"));
        promo.setFechaInicio(LocalDate.now().minusDays(2));
        promo.setFechaFin(LocalDate.now().plusDays(5));
        promo.setActivo(true);
        repo.setPromocion(promo);

        PromocionService service = new PromocionService(repo);

        BigDecimal subtotal = new BigDecimal("100.00");
        ValidarPromocionResponse response = service.validarPromocion("DESC20", subtotal);

        assertTrue(response.isValida());
        assertEquals(new BigDecimal("20.00"), response.getMontoDescuentoCalculado());
        assertEquals(new BigDecimal("80.00"), response.getMontoFinal());
    }

    @Test
    @DisplayName("Debe calcular correctamente el descuento de monto fijo")
    void testValidarPromocionMontoFijo() {
        PromocionRepositoryStub repo = new PromocionRepositoryStub();
        Promocion promo = new Promocion();
        promo.setIdPromocion(2);
        promo.setCodigo("PROMO15");
        promo.setTipoDescuento("MONTO_FIJO");
        promo.setValorDescuento(new BigDecimal("15.00"));
        promo.setFechaInicio(LocalDate.now().minusDays(1));
        promo.setFechaFin(LocalDate.now().plusDays(10));
        promo.setActivo(true);
        repo.setPromocion(promo);

        PromocionService service = new PromocionService(repo);

        BigDecimal subtotal = new BigDecimal("50.00");
        ValidarPromocionResponse response = service.validarPromocion("PROMO15", subtotal);

        assertTrue(response.isValida());
        assertEquals(new BigDecimal("15.00"), response.getMontoDescuentoCalculado());
        assertEquals(new BigDecimal("35.00"), response.getMontoFinal());
    }

    @Test
    @DisplayName("Debe rechazar un cupón inactivo")
    void testValidarPromocionInactiva() {
        PromocionRepositoryStub repo = new PromocionRepositoryStub();
        Promocion promo = new Promocion();
        promo.setCodigo("DESC20");
        promo.setTipoDescuento("PORCENTAJE");
        promo.setValorDescuento(new BigDecimal("20.00"));
        promo.setFechaInicio(LocalDate.now().minusDays(2));
        promo.setFechaFin(LocalDate.now().plusDays(5));
        promo.setActivo(false); // INACTIVO
        repo.setPromocion(promo);

        PromocionService service = new PromocionService(repo);

        ValidarPromocionResponse response = service.validarPromocion("DESC20", new BigDecimal("100.00"));

        assertFalse(response.isValida());
        assertTrue(response.getMensaje().contains("inactivo"));
    }
}
