-- Base de datos para el sistema de barbería A&S Barber Shop
CREATE DATABASE IF NOT EXISTS barberia_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE barberia_db;

-- 1. Tabla de roles
CREATE TABLE IF NOT EXISTS roles (
    id_rol INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(150)
) ENGINE=InnoDB;

-- 2. Tabla de usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    id_rol INT NOT NULL,
    nombre VARCHAR(50) NOT NULL,
    apellido VARCHAR(50) NOT NULL,
    telefono VARCHAR(20) UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_usuarios_roles
        FOREIGN KEY (id_rol)
        REFERENCES roles(id_rol)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT
) ENGINE=InnoDB;

-- 3. Tabla de clientes
CREATE TABLE IF NOT EXISTS clientes (
    id_cliente INT PRIMARY KEY,
    preferencias VARCHAR(255),
    fecha_registro DATE NOT NULL,

    CONSTRAINT fk_clientes_usuarios
        FOREIGN KEY (id_cliente)
        REFERENCES usuarios(id_usuario)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT
) ENGINE=InnoDB;

-- 4. Tabla de barberos
CREATE TABLE IF NOT EXISTS barberos (
    id_barbero INT PRIMARY KEY,
    especialidad VARCHAR(100),
    porcentaje_comision DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    disponible BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_barberos_usuarios
        FOREIGN KEY (id_barbero)
        REFERENCES usuarios(id_usuario)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,

    CONSTRAINT chk_barberos_porcentaje_comision
        CHECK (porcentaje_comision BETWEEN 0 AND 100)
) ENGINE=InnoDB;

-- 5. Tabla de servicios
CREATE TABLE IF NOT EXISTS servicios (
    id_servicio INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    precio DECIMAL(10,2) NOT NULL,
    duracion_minutos INT NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT chk_servicios_precio
        CHECK (precio >= 0),

    CONSTRAINT chk_servicios_duracion
        CHECK (duracion_minutos > 0)
) ENGINE=InnoDB;

-- 6. Tabla intermedia barbero_servicio
CREATE TABLE IF NOT EXISTS barbero_servicio (
    id_barbero INT NOT NULL,
    id_servicio INT NOT NULL,

    PRIMARY KEY (id_barbero, id_servicio),

    CONSTRAINT fk_barbero_servicio_barberos
        FOREIGN KEY (id_barbero)
        REFERENCES barberos(id_barbero)
        ON DELETE CASCADE
        ON UPDATE RESTRICT,

    CONSTRAINT fk_barbero_servicio_servicios
        FOREIGN KEY (id_servicio)
        REFERENCES servicios(id_servicio)
        ON DELETE CASCADE
        ON UPDATE RESTRICT
) ENGINE=InnoDB;

-- 7. Tabla de horarios de barberos
CREATE TABLE IF NOT EXISTS horarios_barbero (
    id_horario INT AUTO_INCREMENT PRIMARY KEY,
    id_barbero INT NOT NULL,
    dia_semana TINYINT NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_horarios_barberos
        FOREIGN KEY (id_barbero)
        REFERENCES barberos(id_barbero)
        ON DELETE CASCADE
        ON UPDATE RESTRICT,

    CONSTRAINT chk_horarios_dia_semana
        CHECK (dia_semana BETWEEN 1 AND 7),

    CONSTRAINT chk_horarios_rango
        CHECK (hora_fin > hora_inicio)
) ENGINE=InnoDB;

-- 8. Tabla de promociones
CREATE TABLE IF NOT EXISTS promociones (
    id_promocion INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(30) NOT NULL UNIQUE,
    descripcion VARCHAR(255),
    tipo_descuento VARCHAR(20) NOT NULL,
    valor_descuento DECIMAL(10,2) NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT chk_promociones_tipo
        CHECK (
            tipo_descuento IN ('PORCENTAJE', 'MONTO_FIJO')
        ),

    CONSTRAINT chk_promociones_valor
        CHECK (
            valor_descuento > 0
            AND (
                (tipo_descuento = 'PORCENTAJE'
                 AND valor_descuento <= 100)
                OR tipo_descuento = 'MONTO_FIJO'
            )
        ),

    CONSTRAINT chk_promociones_fechas
        CHECK (fecha_fin >= fecha_inicio)
) ENGINE=InnoDB;

-- 9. Tabla de estados de cita
CREATE TABLE IF NOT EXISTS estados_cita (
    id_estado INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(30) NOT NULL UNIQUE,
    color_hex VARCHAR(7) NOT NULL DEFAULT '#6c757d'
) ENGINE=InnoDB;

-- 10. Tabla de citas
CREATE TABLE IF NOT EXISTS citas (
    id_cita INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente INT NOT NULL,
    id_barbero INT NOT NULL,
    id_estado INT NOT NULL,
    id_promocion INT NULL,
    creado_por INT NOT NULL,
    fecha DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    notas VARCHAR(255),
    motivo_cancelacion VARCHAR(255),
    monto_subtotal DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    monto_descuento DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    monto_total DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_citas_clientes
        FOREIGN KEY (id_cliente)
        REFERENCES clientes(id_cliente)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,

    CONSTRAINT fk_citas_barberos
        FOREIGN KEY (id_barbero)
        REFERENCES barberos(id_barbero)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,

    CONSTRAINT fk_citas_estados
        FOREIGN KEY (id_estado)
        REFERENCES estados_cita(id_estado)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,

    CONSTRAINT fk_citas_promociones
        FOREIGN KEY (id_promocion)
        REFERENCES promociones(id_promocion)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,

    CONSTRAINT fk_citas_usuarios_creador
        FOREIGN KEY (creado_por)
        REFERENCES usuarios(id_usuario)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,

    CONSTRAINT chk_citas_horario
        CHECK (hora_fin > hora_inicio),

    CONSTRAINT chk_citas_montos
        CHECK (
            monto_subtotal >= 0
            AND monto_descuento >= 0
            AND monto_total >= 0
            AND monto_descuento <= monto_subtotal
        )
) ENGINE=InnoDB;

-- 11. Tabla de detalle de cita
CREATE TABLE IF NOT EXISTS detalle_cita (
    id_detalle INT AUTO_INCREMENT PRIMARY KEY,
    id_cita INT NOT NULL,
    id_servicio INT NOT NULL,
    nombre_servicio VARCHAR(100) NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    cantidad INT NOT NULL DEFAULT 1,
    duracion_minutos INT NOT NULL,

    CONSTRAINT fk_detalle_citas
        FOREIGN KEY (id_cita)
        REFERENCES citas(id_cita)
        ON DELETE CASCADE
        ON UPDATE RESTRICT,

    CONSTRAINT fk_detalle_servicios
        FOREIGN KEY (id_servicio)
        REFERENCES servicios(id_servicio)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,

    CONSTRAINT chk_detalle_precio
        CHECK (precio_unitario >= 0),

    CONSTRAINT chk_detalle_cantidad
        CHECK (cantidad > 0),

    CONSTRAINT chk_detalle_duracion
        CHECK (duracion_minutos > 0)
) ENGINE=InnoDB;

-- 12. Tabla de historial de citas
CREATE TABLE IF NOT EXISTS historial_citas (
    id_historial INT AUTO_INCREMENT PRIMARY KEY,
    id_cita INT NOT NULL,
    id_estado_anterior INT NULL,
    id_estado_nuevo INT NOT NULL,
    fecha_anterior DATE,
    hora_inicio_anterior TIME,
    hora_fin_anterior TIME,
    fecha_nueva DATE,
    hora_inicio_nueva TIME,
    hora_fin_nueva TIME,
    id_barbero_anterior INT,
    id_barbero_nuevo INT,
    id_usuario_cambio INT NOT NULL,
    observacion VARCHAR(255),
    fecha_cambio TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_historial_citas
        FOREIGN KEY (id_cita)
        REFERENCES citas(id_cita)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,

    CONSTRAINT fk_historial_estado_anterior
        FOREIGN KEY (id_estado_anterior)
        REFERENCES estados_cita(id_estado)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,

    CONSTRAINT fk_historial_estado_nuevo
        FOREIGN KEY (id_estado_nuevo)
        REFERENCES estados_cita(id_estado)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,

    CONSTRAINT fk_historial_barbero_anterior
        FOREIGN KEY (id_barbero_anterior)
        REFERENCES barberos(id_barbero)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,

    CONSTRAINT fk_historial_barbero_nuevo
        FOREIGN KEY (id_barbero_nuevo)
        REFERENCES barberos(id_barbero)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,

    CONSTRAINT fk_historial_usuarios
        FOREIGN KEY (id_usuario_cambio)
        REFERENCES usuarios(id_usuario)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT
) ENGINE=InnoDB;

-- 13. Tabla de métodos de pago
CREATE TABLE IF NOT EXISTS metodos_pago (
    id_metodo INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(40) NOT NULL UNIQUE,
    activo BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

-- 14. Tabla de pagos
CREATE TABLE IF NOT EXISTS pagos (
    id_pago INT AUTO_INCREMENT PRIMARY KEY,
    id_cita INT NOT NULL,
    id_metodo INT NOT NULL,
    monto DECIMAL(10,2) NOT NULL,
    referencia VARCHAR(80),
    estado_pago VARCHAR(20) NOT NULL DEFAULT 'CONFIRMADO',
    registrado_por INT NOT NULL,
    fecha_pago TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_pagos_citas
        FOREIGN KEY (id_cita)
        REFERENCES citas(id_cita)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,

    CONSTRAINT fk_pagos_metodos
        FOREIGN KEY (id_metodo)
        REFERENCES metodos_pago(id_metodo)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,

    CONSTRAINT fk_pagos_usuarios
        FOREIGN KEY (registrado_por)
        REFERENCES usuarios(id_usuario)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,

    CONSTRAINT chk_pagos_monto
        CHECK (monto > 0)
) ENGINE=InnoDB;

-- 15. Tabla de comisiones
CREATE TABLE IF NOT EXISTS comisiones (
    id_comision INT AUTO_INCREMENT PRIMARY KEY,
    id_cita INT NOT NULL UNIQUE,
    id_barbero INT NOT NULL,
    monto_base DECIMAL(10,2) NOT NULL,
    porcentaje DECIMAL(5,2) NOT NULL,
    monto_comision DECIMAL(10,2) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_comisiones_citas
        FOREIGN KEY (id_cita)
        REFERENCES citas(id_cita)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,

    CONSTRAINT fk_comisiones_barberos
        FOREIGN KEY (id_barbero)
        REFERENCES barberos(id_barbero)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,

    CONSTRAINT chk_comisiones_porcentaje
        CHECK (porcentaje BETWEEN 0 AND 100),

    CONSTRAINT chk_comisiones_montos
        CHECK (
            monto_base >= 0
            AND monto_comision >= 0
        )
) ENGINE=InnoDB;

-- Índices
CREATE INDEX IF NOT EXISTS idx_citas_barbero_fecha
    ON citas(id_barbero, fecha, hora_inicio);

CREATE INDEX IF NOT EXISTS idx_citas_cliente_fecha
    ON citas(id_cliente, fecha);

CREATE INDEX IF NOT EXISTS idx_citas_estado_fecha
    ON citas(id_estado, fecha);

CREATE INDEX IF NOT EXISTS idx_pagos_cita_fecha
    ON pagos(id_cita, fecha_pago);

-- Datos iniciales
INSERT INTO roles (nombre, descripcion) VALUES
('ADMIN', 'Acceso total al sistema y administracion general'),
('BARBERO', 'Gestion de agenda, horarios y atencion de citas'),
('CLIENTE', 'Registro, gestion de perfil y reserva de citas')
ON DUPLICATE KEY UPDATE nombre=nombre;

INSERT INTO estados_cita (nombre, color_hex) VALUES
('PENDIENTE', '#ffc107'),
('CONFIRMADA', '#17a2b8'),
('EN_PROCESO', '#007bff'),
('ATENDIDA', '#28a745'),
('CANCELADA', '#dc3545'),
('AUSENTE', '#6f42c1')
ON DUPLICATE KEY UPDATE nombre=nombre;

INSERT INTO metodos_pago (nombre) VALUES
('EFECTIVO'),
('TARJETA'),
('YAPE'),
('PLIN'),
('TRANSFERENCIA')
ON DUPLICATE KEY UPDATE nombre=nombre;

