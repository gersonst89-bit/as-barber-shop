# A&S Barber Shop — Sistema Web Integral de Gestión y Reservas

Sistema web comercial y de gestión operativa para **A&S Barber Shop**, barbería tradicional fundada en 1991 ubicada en **Av. José Pardo 620, Galería Pardo, Sótano Tienda 18, Miraflores, Lima - Perú**.

---

## 🛠️ Stack Tecnológico

- **Backend:** Java 17+, Spring Boot 3.x, Spring Data JPA, Spring Security 6 (Autenticación JWT Stateless).
- **Base de Datos:** MySQL 8.0 (14 tablas normalizadas con integridad referencial y auditoría).
- **Frontend:** HTML5 Semántico, Tailwind CSS (Paleta Dark Luxury `#0a0908` con acentos en Cyan Eléctrico `#06b6d4`), JavaScript Vanilla (ES6+ modular y asíncrono con `fetch`).
- **Integraciones:** Google Maps interactivo oficial, Botón directo de WhatsApp con saludo automático, Libro de Reclamaciones Virtual conforme a la normativa de INDECOPI (Perú) y distintivos de medios de pago locales (Yape, Plin, Tarjetas, Efectivo).

---

## 📋 Mapeo de las 24 Historias de Usuario (Backlog Oficial)

| HU | Módulo | Rol | Descripción Funcional | Vista / Endpoint Principal |
| :--- | :--- | :--- | :--- | :--- |
| **HU01** | Base de Datos | Sistema | Diseño e implementación de base de datos relacional normalizada en MySQL (14 tablas). | `src/main/resources/schema.sql` |
| **HU02** | UI/UX Prototipos | Todos | Prototipos y diseño de interfaces con estética Dark Luxury e identidad de marca real. | `/` (`index.html`), `/pages/**` |
| **HU03** | Autenticación | Todos | Inicio y cierre de sesión seguro mediante tokens JWT y redirección por rol. | `/pages/login.html` · `POST /api/auth/login` |
| **HU04** | Usuarios | Cliente | Registro de nuevos clientes con validación de correo único y contraseña cifrada. | `/pages/registro.html` · `POST /api/auth/registro` |
| **HU05** | Servicios | Admin | Registro de nuevos servicios con nombre, precio, duración en minutos y estado. | `/pages/admin/servicios.html` · `POST /api/admin/servicios` |
| **HU06** | Usuarios | Admin | Modificación y edición de datos de usuarios (nombre, teléfono, rol). | `/pages/admin/usuarios.html` · `PUT /api/admin/usuarios/{id}` |
| **HU07** | Catálogo | Público | Visualización pública del tarifario y catálogo de servicios con precio y duración. | `/` (`index.html`) · `GET /api/servicios` |
| **HU08** | Horarios | Cliente | Consulta de horarios y turnos disponibles por barbero y fecha seleccionada. | `/pages/cliente/reservas.html` · `GET /api/horarios/disponibilidad` |
| **HU09** | Citas | Cliente | Wizard de reserva de citas en 4 pasos con confirmación inmediata. | `/pages/cliente/reservas.html` · `POST /api/citas` |
| **HU10** | Citas | Todos | Consulta de citas agendadas según rol (Mis Citas, Agenda de Barbero y Monitor Admin). | `/pages/cliente/reservas.html`, `/pages/barbero/agenda.html`, `/pages/admin/dashboard.html` |
| **HU11** | Usuarios | Admin | Activación y desactivación lógica de cuentas de usuario. | `/pages/admin/usuarios.html` · `PATCH /api/admin/usuarios/{id}/estado` |
| **HU12** | Servicios | Admin | Modificación de precios, descripciones y tiempos de atención de servicios. | `/pages/admin/servicios.html` · `PUT /api/admin/servicios/{id}` |
| **HU13** | Servicios | Admin | Activación y desactivación de servicios del catálogo público. | `/pages/admin/servicios.html` · `PATCH /api/admin/servicios/{id}/estado` |
| **HU14** | Citas | Cliente/Admin | Reprogramación de citas con cambio de fecha, horario o barbero. | `/pages/cliente/reservas.html` · `PUT /api/citas/{id}/reprogramar` |
| **HU15** | Citas | Barbero | Actualización de estados operativos de la cita (`EN_PROCESO`, `ATENDIDA`). | `/pages/barbero/agenda.html` · `PATCH /api/citas/{id}/estado` |
| **HU16** | Citas | Todos | Cancelación de citas con registro obligatorio de motivo de anulación. | En todos los paneles · `PATCH /api/citas/{id}/cancelar` |
| **HU17** | Pagos | Barbero/Admin | Registro de cobro de cita (Efectivo, Yape, Plin, Tarjeta) con desglose. | Modal de Cobro en `agenda.html` · `POST /api/pagos` |
| **HU18** | Citas | Admin | Consulta de historial y trazabilidad de cambios de estados de citas. | `historial_citas` · `GET /api/citas/{id}/historial` |
| **HU19** | Promociones | Admin | Registro de promociones y cupones de descuento con vigencia. | `/pages/admin/promociones.html` · `POST /api/admin/promociones` |
| **HU20** | Promociones | Cliente/Admin | Consulta y aplicación de cupones de descuento en el paso 4 de la reserva. | `/pages/admin/promociones.html` · `GET /api/promociones/activas` |
| **HU21** | Promociones | Admin | Modificación de porcentaje, fechas y condiciones de promociones. | `/pages/admin/promociones.html` · `PUT /api/admin/promociones/{id}` |
| **HU22** | Promociones | Admin | Activación y desactivación de promociones en tiempo real. | `/pages/admin/promociones.html` · `PATCH /api/admin/promociones/{id}/estado` |
| **HU23** | Pagos | Admin | Consulta y auditoría de historial de transacciones y pagos registrados. | `/pages/admin/dashboard.html` · `GET /api/pagos` |
| **HU24** | Comisiones | Barbero/Admin | Cálculo y liquidación de comisiones de barberos (`monto_base × porcentaje / 100`). | `/pages/barbero/agenda.html` (Tab Comisiones) · `GET /api/comisiones` |

---

## 👥 Credenciales de Acceso para Pruebas

| Rol | Correo Electrónico | Contraseña | Panel de Destino |
| :--- | :--- | :--- | :--- |
| **Administrador** | `admin@asbarbershop.pe` | `Admin123!` | `/pages/admin/dashboard.html` |
| **Maestro Barbero 1** | `carlos.barbero@asbarbershop.pe` | `Barbero123!` | `/pages/barbero/agenda.html` |
| **Maestro Barbero 2** | `roberto.barbero@asbarbershop.pe` | `Barbero123!` | `/pages/barbero/agenda.html` |
| **Cliente Frecuente** | `cliente@gmail.com` | `Cliente123!` | `/pages/cliente/reservas.html` |

---

## 🚀 Requisitos y Puesta en Marcha

### Prerrequisitos:
- **Java JDK 17** o superior instalado.
- **MySQL Server 8.0** activo en `localhost:3306`.
- **Apache Maven 3.8+** (o usar el wrapper `./mvnw` incluido).

### Pasos de Instalación:

1. **Crear la base de datos en MySQL:**
   ```sql
   CREATE DATABASE barberia_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. **Verificar credenciales de conexión en `src/main/resources/application.properties`:**
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/barberia_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
   spring.datasource.username=root
   spring.datasource.password=root
   ```

3. **Compilar y ejecutar el proyecto:**
   ```powershell
   mvn spring-boot:run
   ```

4. **Acceder a la aplicación:**
   - **Página Principal:** [http://localhost:8080](http://localhost:8080)
   - **Inicio de Sesión:** [http://localhost:8080/pages/login.html](http://localhost:8080/pages/login.html)
   - **Registro de Clientes:** [http://localhost:8080/pages/registro.html](http://localhost:8080/pages/registro.html)
   - **Libro de Reclamaciones:** [http://localhost:8080/pages/libro-reclamaciones.html](http://localhost:8080/pages/libro-reclamaciones.html)

---

## 📦 Estructura del Proyecto

```
as-barber-shop/
├── pom.xml                                      # Dependencias y configuración Maven
├── as-barber-shop-postman-collection.json       # Colección de pruebas de API REST
├── README.md                                    # Documentación general del sistema
└── src/
    └── main/
        ├── java/com/asbarbershop/
        │   ├── AsBarberShopApplication.java     # Clase principal Spring Boot
        │   ├── config/                          # Configuración de Seguridad JWT y CORS
        │   ├── controller/                      # Controladores REST (/api/**)
        │   ├── dto/                             # Data Transfer Objects (Request/Response)
        │   ├── model/                           # Entidades JPA de base de datos
        │   ├── repository/                      # Repositorios Spring Data JPA
        │   ├── security/                        # Filtro JWT, UserDetailsService, JwtUtils
        │   └── service/                         # Lógica de negocio transaccional
        └── resources/
            ├── application.properties           # Parámetros de entorno y base de datos
            ├── schema.sql                       # DDL: Estructura de las 14 tablas
            ├── data.sql                         # DML: Datos iniciales (servicios, usuarios, barberos)
            └── static/                          # Frontend Web
                ├── index.html                   # Landing page pública y catálogo
                ├── css/                         # Estilos personalizados Dark Luxury
                ├── js/                          # Lógica JavaScript por rol (admin, barbero, cliente)
                ├── img/                         # Fotografías reales, logos y assets de la sede
                └── pages/                       # Vistas HTML por rol
                    ├── login.html               # Inicio de sesión
                    ├── registro.html            # Registro de clientes
                    ├── libro-reclamaciones.html # Libro virtual INDECOPI
                    ├── admin/                   # Dashboard, Usuarios, Servicios, Promociones
                    ├── barbero/                 # Agenda, atención de citas y comisiones
                    └── cliente/                 # Wizard de reservas y Mis Citas
```

---

## 📍 Información de la Sede Oficial

- **Establecimiento:** A&S Barber Shop Miraflores (Fundada en 1991)
- **Dirección:** Av. José Pardo 620, Galería Pardo, Sótano Tienda 18, Miraflores, Lima, Perú.
- **Horario:** Lunes a Sábado de 10:00 am a 9:00 pm · Domingos de 10:00 am a 7:00 pm.
- **RUC:** 10458923011
- **Ficha en Google Maps:** [Barbershop A&S Miraflores](https://www.google.com/maps/place/Barbershop+A%26S/@-12.1188865,-77.0354794,17z/data=!3m1!4b1!4m6!3m5!1s0x9105c83d36c18abb:0xcae1c71719e24b5b!8m2!3d-12.1188865!4d-77.0354794!16s%2Fg%2F11c1w0_zq6)
