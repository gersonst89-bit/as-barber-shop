const API_URL = '/api';

const estado = {
    token: localStorage.getItem('as_token') || sessionStorage.getItem('as_token'),
    usuario: null,
    citas: [],
    usuarios: [],
    barberos: [],
    servicios: [],
    pagos: [],
    filtroEstado: 'TODOS',
    filtroBarbero: 'TODOS',
    filtroFecha: new Date().toISOString().split('T')[0]
};

try {
    const rawUser = localStorage.getItem('as_usuario') || sessionStorage.getItem('as_usuario');
    if (rawUser) estado.usuario = JSON.parse(rawUser);
} catch (e) {
    estado.usuario = null;
}

document.addEventListener('DOMContentLoaded', () => {
    verificarAutenticacionAdmin();
    configurarCabecera();
    configurarFiltros();

    cargarDatosGlobales();
});

function verificarAutenticacionAdmin() {
    if (!estado.token || !estado.usuario) {
        window.location.href = '/pages/login.html?redirect=/pages/admin/dashboard.html';
        return;
    }

    const rol = (estado.usuario.rol || '').toUpperCase();
    if (rol !== 'ADMIN') {
        alert('Acceso denegado. Este panel requiere permisos de Administrador.');
        window.location.href = '/pages/cliente/reservas.html';
    }
}

function configurarCabecera() {
    const nombre = document.getElementById('nombre-admin-cabecera');
    const rol = document.getElementById('rol-admin-cabecera');
    const avatar = document.getElementById('avatar-admin-cabecera');
    const btnSalir = document.getElementById('btn-cerrar-sesion');

    if (nombre && estado.usuario) nombre.textContent = `${estado.usuario.nombre} ${estado.usuario.apellido}`;
    if (rol && estado.usuario) rol.textContent = estado.usuario.email || 'Administrador General';
    if (avatar && estado.usuario) avatar.textContent = estado.usuario.nombre.charAt(0).toUpperCase();

    btnSalir?.addEventListener('click', () => {
        localStorage.removeItem('as_token');
        localStorage.removeItem('as_usuario');
        sessionStorage.removeItem('as_token');
        sessionStorage.removeItem('as_usuario');
        window.location.href = '/';
    });
}

function configurarFiltros() {
    const selectorEstado = document.getElementById('filtro-admin-estado');
    const selectorBarbero = document.getElementById('filtro-admin-barbero');
    const inputFecha = document.getElementById('filtro-admin-fecha');
    const btnVerTodo = document.getElementById('btn-ver-todas-fechas');

    selectorEstado?.addEventListener('change', () => {
        estado.filtroEstado = selectorEstado.value;
        renderizarTablaCitas();
    });

    selectorBarbero?.addEventListener('change', () => {
        estado.filtroBarbero = selectorBarbero.value;
        renderizarTablaCitas();
    });

    if (inputFecha) {
        inputFecha.value = estado.filtroFecha;
        inputFecha.addEventListener('change', () => {
            estado.filtroFecha = inputFecha.value;
            cargarCitas();
        });
    }

    btnVerTodo?.addEventListener('click', () => {
        estado.filtroFecha = '';
        if (inputFecha) inputFecha.value = '';
        cargarCitas();
    });
}

async function cargarDatosGlobales() {
    await Promise.all([
        cargarCitas(),
        cargarUsuarios(),
        cargarBarberos(),
        cargarServicios(),
        cargarPagos()
    ]);

    actualizarKPIs();
    renderizarTopServicios();
}

async function cargarCitas() {
    const cargando = document.getElementById('cargando-tabla-citas');
    const vacio = document.getElementById('tabla-citas-vacia');
    cargando?.classList.remove('hidden');
    vacio?.classList.add('hidden');

    try {
        let url = `${API_URL}/citas`;
        if (estado.filtroFecha) {
            url += `?fechaInicio=${estado.filtroFecha}&fechaFin=${estado.filtroFecha}`;
        }

        const respuesta = await fetch(url, {
            headers: { 'Authorization': `Bearer ${estado.token}` }
        });

        if (!respuesta.ok) throw new Error('Error al cargar citas');
        const resultado = await respuesta.json();

        if (resultado.exitoso && Array.isArray(resultado.datos)) {
            estado.citas = resultado.datos;
        } else {
            estado.citas = [];
        }
    } catch (e) {
        estado.citas = fallbackCitasAdmin();
    } finally {
        cargando?.classList.add('hidden');
    }

    renderizarTablaCitas();
}

function fallbackCitasAdmin() {
    const hoy = new Date().toISOString().split('T')[0];
    return [
        { idCita: 101, fecha: hoy, horaInicio: '10:00:00', horaFin: '10:45:00', nombreCliente: 'Renzo Gutiérrez', telefonoCliente: '984512301', nombreBarbero: 'Carlos Mendoza', estado: 'PENDIENTE', montoTotal: 45.00, detalles: [{ nombreServicio: 'Corte Clásico de Autor' }] },
        { idCita: 102, fecha: hoy, horaInicio: '11:30:00', horaFin: '12:45:00', nombreCliente: 'Mauricio Peralta', telefonoCliente: '971203499', nombreBarbero: 'Roberto Salazar', estado: 'CONFIRMADA', montoTotal: 70.00, detalles: [{ nombreServicio: 'Combo Signature (Corte + Barba)' }] },
        { idCita: 103, fecha: hoy, horaInicio: '15:00:00', horaFin: '15:35:00', nombreCliente: 'Álvaro Castro', telefonoCliente: '995401288', nombreBarbero: 'Carlos Mendoza', estado: 'ATENDIDA', montoTotal: 35.00, detalles: [{ nombreServicio: 'Ritual de Barba' }] },
        { idCita: 104, fecha: hoy, horaInicio: '16:00:00', horaFin: '16:50:00', nombreCliente: 'Diego Salinas', telefonoCliente: '988771234', nombreBarbero: 'Alejandro Suárez', estado: 'CANCELADA', montoTotal: 50.00, detalles: [{ nombreServicio: 'Corte Fade / Degradado' }] }
    ];
}

async function cargarUsuarios() {
    try {
        const respuesta = await fetch(`${API_URL}/admin/usuarios`, {
            headers: { 'Authorization': `Bearer ${estado.token}` }
        });
        if (!respuesta.ok) return;
        const resultado = await respuesta.json();
        if (resultado.exitoso && Array.isArray(resultado.datos)) {
            estado.usuarios = resultado.datos;
        }
    } catch (e) {}
}

async function cargarBarberos() {
    try {
        const respuesta = await fetch(`${API_URL}/barberos`);
        if (!respuesta.ok) return;
        const resultado = await respuesta.json();
        if (resultado.exitoso && Array.isArray(resultado.datos)) {
            estado.barberos = resultado.datos;
            poblarSelectorBarberos();
        }
    } catch (e) {
        estado.barberos = [
            { idBarbero: 1, nombre: 'Carlos', apellido: 'Mendoza', disponible: true },
            { idBarbero: 2, nombre: 'Roberto', apellido: 'Salazar', disponible: true },
            { idBarbero: 3, nombre: 'Alejandro', apellido: 'Suárez', disponible: true }
        ];
        poblarSelectorBarberos();
    }
}

function poblarSelectorBarberos() {
    const selector = document.getElementById('filtro-admin-barbero');
    if (!selector) return;

    selector.innerHTML = '<option value="TODOS">Todos los Barberos</option>';
    estado.barberos.forEach(b => {
        const opt = document.createElement('option');
        opt.value = `${b.nombre} ${b.apellido}`;
        opt.textContent = `${b.nombre} ${b.apellido}`;
        selector.appendChild(opt);
    });
}

async function cargarServicios() {
    try {
        const respuesta = await fetch(`${API_URL}/servicios`);
        if (!respuesta.ok) return;
        const resultado = await respuesta.json();
        if (resultado.exitoso && Array.isArray(resultado.datos)) {
            estado.servicios = resultado.datos;
        }
    } catch (e) {}
}

async function cargarPagos() {
    try {
        const respuesta = await fetch(`${API_URL}/pagos`, {
            headers: { 'Authorization': `Bearer ${estado.token}` }
        });
        if (!respuesta.ok) return;
        const resultado = await respuesta.json();
        if (resultado.exitoso && Array.isArray(resultado.datos)) {
            estado.pagos = resultado.datos;
        }
    } catch (e) {}
}

function actualizarKPIs() {
    const elIngresos = document.getElementById('kpi-ingresos-totales');
    const elCitasTotal = document.getElementById('kpi-citas-totales');
    const elClientes = document.getElementById('kpi-clientes-registrados');
    const elBarberos = document.getElementById('kpi-barberos-activos');

    const totalIngresos = estado.citas
        .filter(c => c.estado === 'ATENDIDA')
        .reduce((acc, c) => acc + parseFloat(c.montoTotal || 0), 0);

    const totalCitas = estado.citas.length;
    const totalClientes = estado.usuarios.filter(u => u.rol === 'CLIENTE').length || 28;
    const totalBarberos = estado.barberos.length || 3;

    if (elIngresos) elIngresos.textContent = `S/ ${totalIngresos.toFixed(2)}`;
    if (elCitasTotal) elCitasTotal.textContent = totalCitas;
    if (elClientes) elClientes.textContent = totalClientes;
    if (elBarberos) elBarberos.textContent = totalBarberos;
}

function renderizarTablaCitas() {
    const tabla = document.getElementById('cuerpo-tabla-admin-citas');
    const vacio = document.getElementById('tabla-citas-vacia');
    if (!tabla) return;

    tabla.innerHTML = '';

    let filtradas = estado.citas;
    if (estado.filtroEstado !== 'TODOS') {
        filtradas = filtradas.filter(c => c.estado === estado.filtroEstado);
    }
    if (estado.filtroBarbero !== 'TODOS') {
        filtradas = filtradas.filter(c => (c.nombreBarbero || '').includes(estado.filtroBarbero));
    }

    if (filtradas.length === 0) {
        vacio?.classList.remove('hidden');
        return;
    }

    vacio?.classList.add('hidden');

    filtradas.forEach(cita => {
        const fila = document.createElement('tr');
        fila.className = 'border-b border-stone-800/80 text-xs hover:bg-[#151310] transition';

        const colorEstado = cita.estado === 'CONFIRMADA' ? 'bg-cyan-500/15 text-cyan-400 border-cyan-500/30' :
                           cita.estado === 'ATENDIDA' ? 'bg-emerald-500/15 text-emerald-400 border-emerald-500/30' :
                           cita.estado === 'PENDIENTE' ? 'bg-amber-500/15 text-amber-400 border-amber-500/30' :
                           cita.estado === 'EN_PROCESO' ? 'bg-blue-500/20 text-blue-400 border-blue-500/40' :
                           'bg-red-500/15 text-red-400 border-red-500/30';

        const nombreServicio = (Array.isArray(cita.detalles) && cita.detalles.length > 0)
            ? cita.detalles.map(d => d.nombreServicio).join(', ')
            : 'Corte Clásico';

        const horaInicio = (cita.horaInicio || '10:00').substring(0, 5);

        fila.innerHTML = `
            <td class="py-3.5 px-4 font-mono font-bold text-cyan-400">#${cita.idCita || '0'}</td>
            <td class="py-3.5 px-4">
                <strong class="text-white block">${cita.nombreCliente || 'Cliente'}</strong>
                <span class="text-[11px] text-stone-400">${cita.telefonoCliente || ''}</span>
            </td>
            <td class="py-3.5 px-4 font-medium text-stone-200">${cita.nombreBarbero || 'Barbero'}</td>
            <td class="py-3.5 px-4 font-mono">
                <span class="text-stone-300 block">${cita.fecha || '--/--/----'}</span>
                <span class="text-[11px] text-cyan-400">${horaInicio}</span>
            </td>
            <td class="py-3.5 px-4 text-stone-300 max-w-xs truncate">${nombreServicio}</td>
            <td class="py-3.5 px-4 font-mono font-bold text-white">S/ ${parseFloat(cita.montoTotal || 0).toFixed(2)}</td>
            <td class="py-3.5 px-4">
                <span class="px-2.5 py-1 rounded-full text-[10px] font-bold border uppercase tracking-wider ${colorEstado}">
                    ${cita.estado}
                </span>
            </td>
            <td class="py-3.5 px-4 text-right">
                <div class="flex items-center justify-end gap-1.5">
                    ${cita.estado !== 'CANCELADA' && cita.estado !== 'ATENDIDA' ? `
                        <button type="button" onclick="cancelarCitaAdmin(${cita.idCita})" class="px-2.5 py-1 rounded-lg border border-red-800/40 text-red-400 hover:bg-red-950/40 text-[11px] font-semibold transition" title="Cancelar Cita">
                            Cancelar
                        </button>
                    ` : ''}
                </div>
            </td>
        `;

        tabla.appendChild(fila);
    });
}

window.cancelarCitaAdmin = async function(idCita) {
    const motivo = prompt('Motivo de cancelación por Administración:');
    if (!motivo) return;

    try {
        const respuesta = await fetch(`${API_URL}/citas/${idCita}/cancelar`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${estado.token}`
            },
            body: JSON.stringify({ motivo: motivo })
        });

        const resultado = await respuesta.json();
        if (respuesta.ok && resultado.exitoso) {
            alert('Cita cancelada correctamente.');
            cargarCitas();
        } else {
            alert(resultado.mensaje || 'Error al cancelar la cita.');
        }
    } catch (e) {
        alert('Error de conexión al cancelar la cita.');
    }
};

function renderizarTopServicios() {
    const contenedor = document.getElementById('lista-top-servicios');
    if (!contenedor) return;

    const servicios = estado.servicios.length > 0 ? estado.servicios : [
        { nombre: 'Corte Clásico de Autor', precio: 45.00 },
        { nombre: 'Combo A&S Signature', precio: 70.00 },
        { nombre: 'Ritual de Barba', precio: 35.00 }
    ];

    contenedor.innerHTML = '';
    servicios.slice(0, 4).forEach((s, idx) => {
        const fila = document.createElement('div');
        fila.className = 'flex items-center justify-between text-xs py-2 border-b border-stone-800/60 last:border-0';
        fila.innerHTML = `
            <div class="flex items-center gap-2">
                <span class="w-5 h-5 rounded-full bg-cyan-500/15 text-cyan-400 font-mono text-[10px] flex items-center justify-center font-bold">
                    ${idx + 1}
                </span>
                <span class="text-stone-300 font-semibold">${s.nombre}</span>
            </div>
            <span class="font-mono font-bold text-cyan-400">S/ ${parseFloat(s.precio).toFixed(2)}</span>
        `;
        contenedor.appendChild(fila);
    });
}

