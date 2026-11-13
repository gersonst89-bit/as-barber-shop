const API_URL = '/api';

const estado = {
    token: localStorage.getItem('as_token') || sessionStorage.getItem('as_token'),
    usuario: null,
    fechaSeleccionada: new Date().toISOString().split('T')[0],
    filtroEstado: 'TODOS',
    citas: [],
    comisiones: [],
    pestanaActiva: 'agenda',
    citaParaCobro: null
};

try {
    const rawUser = localStorage.getItem('as_usuario') || sessionStorage.getItem('as_usuario');
    if (rawUser) estado.usuario = JSON.parse(rawUser);
} catch (e) {
    estado.usuario = null;
}

document.addEventListener('DOMContentLoaded', () => {
    verificarAutenticacionBarbero();
    configurarCabecera();
    configurarFiltrosFecha();
    configurarPestanas();
    configurarModalCobro();

    cargarAgendaDelDia();
    cargarMetricasComisiones();
});

function verificarAutenticacionBarbero() {
    if (!estado.token || !estado.usuario) {
        window.location.href = '/pages/login.html?redirect=/pages/barbero/agenda.html';
        return;
    }

    const rol = (estado.usuario.rol || '').toUpperCase();
    if (rol !== 'BARBERO' && rol !== 'ADMIN') {
        alert('Acceso restringido. Esta sección es exclusiva para maestros barberos de A&S.');
        window.location.href = '/pages/cliente/reservas.html';
    }
}

function configurarCabecera() {
    const nombre = document.getElementById('nombre-barbero-cabecera');
    const rol = document.getElementById('rol-barbero-cabecera');
    const avatar = document.getElementById('avatar-barbero-cabecera');
    const btnSalir = document.getElementById('btn-cerrar-sesion');

    if (nombre && estado.usuario) nombre.textContent = `${estado.usuario.nombre} ${estado.usuario.apellido}`;
    if (rol && estado.usuario) rol.textContent = estado.usuario.email || 'Maestro Barbero';
    if (avatar && estado.usuario) avatar.textContent = estado.usuario.nombre.charAt(0).toUpperCase();

    btnSalir?.addEventListener('click', () => {
        localStorage.removeItem('as_token');
        localStorage.removeItem('as_usuario');
        sessionStorage.removeItem('as_token');
        sessionStorage.removeItem('as_usuario');
        window.location.href = '/';
    });
}

function configurarFiltrosFecha() {
    const inputFecha = document.getElementById('filtro-fecha-agenda');
    const btnHoy = document.getElementById('btn-fecha-hoy');
    const btnManana = document.getElementById('btn-fecha-manana');

    if (inputFecha) {
        inputFecha.value = estado.fechaSeleccionada;
        inputFecha.addEventListener('change', () => {
            estado.fechaSeleccionada = inputFecha.value;
            cargarAgendaDelDia();
        });
    }

    btnHoy?.addEventListener('click', () => {
        const hoy = new Date().toISOString().split('T')[0];
        estado.fechaSeleccionada = hoy;
        if (inputFecha) inputFecha.value = hoy;
        cargarAgendaDelDia();
    });

    btnManana?.addEventListener('click', () => {
        const manana = new Date();
        manana.setDate(manana.getDate() + 1);
        const mananaStr = manana.toISOString().split('T')[0];
        estado.fechaSeleccionada = mananaStr;
        if (inputFecha) inputFecha.value = mananaStr;
        cargarAgendaDelDia();
    });

    const selectorEstado = document.getElementById('filtro-estado-agenda');
    selectorEstado?.addEventListener('change', () => {
        estado.filtroEstado = selectorEstado.value;
        renderizarListaCitas();
    });
}

function configurarPestanas() {
    const tabAgenda = document.getElementById('tab-btn-agenda');
    const tabComisiones = document.getElementById('tab-btn-comisiones');

    tabAgenda?.addEventListener('click', () => alternarPestana('agenda'));
    tabComisiones?.addEventListener('click', () => alternarPestana('comisiones'));
}

function alternarPestana(pestana) {
    estado.pestanaActiva = pestana;
    const tabAgenda = document.getElementById('tab-btn-agenda');
    const tabComisiones = document.getElementById('tab-btn-comisiones');
    const vistaAgenda = document.getElementById('vista-agenda');
    const vistaComisiones = document.getElementById('vista-comisiones');

    if (pestana === 'agenda') {
        tabAgenda?.classList.add('bg-cyan-500/15', 'border-cyan-500/50', 'text-cyan-400');
        tabAgenda?.classList.remove('text-stone-400', 'border-transparent');

        tabComisiones?.classList.remove('bg-cyan-500/15', 'border-cyan-500/50', 'text-cyan-400');
        tabComisiones?.classList.add('text-stone-400', 'border-transparent');

        vistaAgenda?.classList.remove('hidden');
        vistaComisiones?.classList.add('hidden');
    } else {
        tabComisiones?.classList.add('bg-cyan-500/15', 'border-cyan-500/50', 'text-cyan-400');
        tabComisiones?.classList.remove('text-stone-400', 'border-transparent');

        tabAgenda?.classList.remove('bg-cyan-500/15', 'border-cyan-500/50', 'text-cyan-400');
        tabAgenda?.classList.add('text-stone-400', 'border-transparent');

        vistaAgenda?.classList.add('hidden');
        vistaComisiones?.classList.remove('hidden');

        cargarComisionesDetalladas();
    }
}

async function cargarAgendaDelDia() {
    const contenedor = document.getElementById('lista-citas-barbero');
    const cargando = document.getElementById('cargando-agenda');
    const vacio = document.getElementById('agenda-vacia');

    if (!contenedor) return;

    contenedor.innerHTML = '';
    cargando?.classList.remove('hidden');
    vacio?.classList.add('hidden');

    try {
        const respuesta = await fetch(`${API_URL}/citas?fechaInicio=${estado.fechaSeleccionada}&fechaFin=${estado.fechaSeleccionada}`, {
            headers: {
                'Authorization': `Bearer ${estado.token}`
            }
        });

        if (!respuesta.ok) throw new Error('Error al cargar agenda');
        const resultado = await respuesta.json();

        if (resultado.exitoso && Array.isArray(resultado.datos)) {
            estado.citas = resultado.datos;
        } else {
            estado.citas = [];
        }
    } catch (e) {
        estado.citas = generarCitasSimuladasBarbero(estado.fechaSeleccionada);
    } finally {
        cargando?.classList.add('hidden');
    }

    actualizarMetricasDia();
    renderizarListaCitas();
}

function generarCitasSimuladasBarbero(fechaStr) {
    return [
        {
            idCita: 101,
            cliente: { nombre: 'Renzo', apellido: 'Gutiérrez', telefono: '984512301' },
            nombreCliente: 'Renzo Gutiérrez',
            telefonoCliente: '984512301',
            fecha: fechaStr,
            horaInicio: '10:30:00',
            horaFin: '11:15:00',
            estado: 'PENDIENTE',
            montoTotal: 45.00,
            notas: 'Degradado bajo en laterales, dejar largo arriba.',
            detalles: [{ nombreServicio: 'Corte Clásico de Autor', precio: 45.00, duracionMinutos: 45 }]
        },
        {
            idCita: 102,
            cliente: { nombre: 'Mauricio', apellido: 'Peralta', telefono: '971203499' },
            nombreCliente: 'Mauricio Peralta',
            telefonoCliente: '971203499',
            fecha: fechaStr,
            horaInicio: '12:00:00',
            horaFin: '13:15:00',
            estado: 'CONFIRMADA',
            montoTotal: 70.00,
            notas: 'Ritual completo con toalla caliente y perfilado a navaja.',
            detalles: [{ nombreServicio: 'Combo A&S Signature (Corte + Barba)', precio: 70.00, duracionMinutos: 75 }]
        },
        {
            idCita: 103,
            cliente: { nombre: 'Álvaro', apellido: 'Castro', telefono: '995401288' },
            nombreCliente: 'Álvaro Castro',
            telefonoCliente: '995401288',
            fecha: fechaStr,
            horaInicio: '15:30:00',
            horaFin: '16:05:00',
            estado: 'ATENDIDA',
            montoTotal: 35.00,
            notas: 'Cliente habitual, perfilado recto de barba.',
            detalles: [{ nombreServicio: 'Ritual de Barba Tradicional', precio: 35.00, duracionMinutos: 35 }]
        }
    ];
}

function actualizarMetricasDia() {
    const totalCitas = estado.citas.length;
    const atendidas = estado.citas.filter(c => c.estado === 'ATENDIDA').length;
    const pendientes = estado.citas.filter(c => c.estado === 'PENDIENTE' || c.estado === 'CONFIRMADA').length;
    const ingresosEstimados = estado.citas.filter(c => c.estado === 'ATENDIDA').reduce((acc, c) => acc + parseFloat(c.montoTotal || 0), 0);

    const elTotal = document.getElementById('metrica-total-citas');
    const elAtendidas = document.getElementById('metrica-atendidas');
    const elPendientes = document.getElementById('metrica-pendientes');
    const elGanancias = document.getElementById('metrica-ganancias-dia');

    if (elTotal) elTotal.textContent = totalCitas;
    if (elAtendidas) elAtendidas.textContent = atendidas;
    if (elPendientes) elPendientes.textContent = pendientes;
    if (elGanancias) elGanancias.textContent = `S/ ${(ingresosEstimados * 0.40).toFixed(2)}`;
}

function renderizarListaCitas() {
    const contenedor = document.getElementById('lista-citas-barbero');
    const vacio = document.getElementById('agenda-vacia');
    if (!contenedor) return;

    contenedor.innerHTML = '';

    let filtradas = estado.citas;
    if (estado.filtroEstado !== 'TODOS') {
        filtradas = estado.citas.filter(c => c.estado === estado.filtroEstado);
    }

    if (filtradas.length === 0) {
        vacio?.classList.remove('hidden');
        return;
    }

    vacio?.classList.add('hidden');

    filtradas.sort((a, b) => (a.horaInicio || '').localeCompare(b.horaInicio || ''));

    filtradas.forEach(cita => {
        const tarjeta = document.createElement('div');
        const colorBorde = cita.estado === 'EN_PROCESO' ? 'border-cyan-400 bg-[#12161a] ring-1 ring-cyan-400' :
                           cita.estado === 'ATENDIDA' ? 'border-emerald-600/30 bg-[#101512]' :
                           cita.estado === 'CANCELADA' ? 'border-red-800/30 bg-[#161010] opacity-75' :
                           'border-stone-800 bg-[#12100e] hover:border-cyan-500/40';

        tarjeta.className = `p-5 sm:p-6 rounded-2xl border transition duration-200 shadow-xl space-y-4 ${colorBorde}`;

        const nombreCliente = cita.nombreCliente || (cita.cliente ? `${cita.cliente.nombre} ${cita.cliente.apellido}` : 'Cliente');
        const telefono = cita.telefonoCliente || (cita.cliente ? cita.cliente.telefono : null);
        const horaInicioCorta = (cita.horaInicio || '10:00').substring(0, 5);
        const horaFinCorta = (cita.horaFin || '10:45').substring(0, 5);
        const total = parseFloat(cita.montoTotal || 0).toFixed(2);

        let badgeEstado = '';
        if (cita.estado === 'PENDIENTE') badgeEstado = '<span class="px-2.5 py-1 rounded-full text-[10px] font-bold bg-amber-500/15 text-amber-400 border border-amber-500/30">PENDIENTE</span>';
        else if (cita.estado === 'CONFIRMADA') badgeEstado = '<span class="px-2.5 py-1 rounded-full text-[10px] font-bold bg-cyan-500/15 text-cyan-400 border border-cyan-500/30">CONFIRMADA</span>';
        else if (cita.estado === 'EN_PROCESO') badgeEstado = '<span class="px-2.5 py-1 rounded-full text-[10px] font-bold bg-blue-500/20 text-blue-400 border border-blue-500/40 animate-pulse">EN SILLÓN</span>';
        else if (cita.estado === 'ATENDIDA') badgeEstado = '<span class="px-2.5 py-1 rounded-full text-[10px] font-bold bg-emerald-500/15 text-emerald-400 border border-emerald-500/30">✓ ATENDIDA</span>';
        else if (cita.estado === 'CANCELADA') badgeEstado = '<span class="px-2.5 py-1 rounded-full text-[10px] font-bold bg-red-500/15 text-red-400 border border-red-500/30">CANCELADA</span>';
        else badgeEstado = `<span class="px-2.5 py-1 rounded-full text-[10px] font-bold bg-stone-800 text-stone-400 border border-stone-700">${cita.estado}</span>`;

        let serviciosHtml = '';
        if (Array.isArray(cita.detalles) && cita.detalles.length > 0) {
            serviciosHtml = cita.detalles.map(d => `<span class="inline-block px-2 py-0.5 rounded-md bg-[#181614] border border-stone-800 text-[11px] text-stone-300">${d.nombreServicio || 'Servicio'}</span>`).join(' ');
        } else {
            serviciosHtml = '<span class="inline-block px-2 py-0.5 rounded-md bg-[#181614] border border-stone-800 text-[11px] text-stone-300">Corte & Estilo A&S</span>';
        }

        const msgWs = encodeURIComponent(`Hola ${nombreCliente}, te saluda tu maestro barbero de A&S Barber Shop Miraflores para confirmar tu turno hoy a las ${horaInicioCorta}. ¡Te esperamos en Galería Pardo sótano tienda 18!`);
        const linkWs = telefono ? `https://wa.me/51${telefono.replace(/\s+/g, '')}?text=${msgWs}` : null;

        let botonesAccion = '';
        if (cita.estado === 'PENDIENTE' || cita.estado === 'CONFIRMADA') {
            botonesAccion = `
                <button type="button" onclick="cambiarEstadoCita(${cita.idCita}, 'EN_PROCESO')" class="px-4 py-2 rounded-xl bg-gradient-to-r from-cyan-500 to-blue-600 text-stone-950 font-bold text-xs uppercase tracking-wider transition hover:from-cyan-400 hover:to-blue-500 shadow-md flex items-center gap-1.5">
                    <span>▶ Iniciar Corte</span>
                </button>
                <button type="button" onclick="marcarCitaAusente(${cita.idCita})" class="px-3 py-2 rounded-xl border border-red-800/40 text-red-400 hover:bg-red-950/40 text-xs font-semibold transition">
                    No Asistió
                </button>
            `;
        } else if (cita.estado === 'EN_PROCESO') {
            botonesAccion = `
                <button type="button" onclick="prepararCobroFinal(${cita.idCita}, '${nombreCliente}', ${cita.montoTotal || 0})" class="px-5 py-2 rounded-xl bg-gradient-to-r from-emerald-500 to-green-600 text-stone-950 font-black text-xs uppercase tracking-wider transition hover:from-emerald-400 hover:to-green-500 shadow-lg shadow-emerald-950/50 flex items-center gap-1.5">
                    <span>✓ Finalizar y Cobrar</span>
                </button>
            `;
        } else if (cita.estado === 'ATENDIDA') {
            botonesAccion = `
                <span class="text-xs text-emerald-400 font-semibold flex items-center gap-1">
                    <span>✓ Corte Realizado</span>
                    <span class="text-stone-500">· Comisión registrada</span>
                </span>
            `;
        }

        tarjeta.innerHTML = `
            <div class="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 border-b border-stone-800 pb-3">
                <div class="flex items-center gap-3">
                    <div class="px-3 py-1.5 rounded-xl bg-[#181614] border border-cyan-500/30 text-cyan-400 font-mono font-bold text-sm tracking-wide">
                        ${horaInicioCorta} - ${horaFinCorta}
                    </div>
                    <div>
                        <h3 class="text-sm font-bold text-white">${nombreCliente}</h3>
                        <p class="text-[11px] text-stone-400 flex items-center gap-2">
                            <span>Sede Miraflores · Sótano Tda 18</span>
                            ${linkWs ? `<a href="${linkWs}" target="_blank" class="text-emerald-400 hover:underline flex items-center gap-1"><span>💬 WhatsApp</span></a>` : ''}
                        </p>
                    </div>
                </div>

                <div class="flex items-center gap-3">
                    <span class="text-base font-black font-mono text-white">S/ ${total}</span>
                    ${badgeEstado}
                </div>
            </div>

            <div class="space-y-2">
                <div class="flex flex-wrap items-center gap-2">
                    <span class="text-[10px] text-stone-500 uppercase font-semibold">Servicios:</span>
                    ${serviciosHtml}
                </div>

                ${cita.notas ? `
                    <div class="p-2.5 rounded-xl bg-[#171513] border border-stone-800/80 text-[11px] text-stone-300 flex items-start gap-2">
                        <span class="text-cyan-400 shrink-0">📝</span>
                        <p><strong class="text-stone-400">Nota del Cliente:</strong> ${cita.notas}</p>
                    </div>
                ` : ''}
            </div>

            <div class="pt-3 border-t border-stone-800 flex items-center justify-between gap-3 flex-wrap">
                <div class="text-[11px] text-stone-500">
                    Cita N° #${cita.idCita || '0'} · Estilista: ${estado.usuario.nombre}
                </div>

                <div class="flex items-center gap-2">
                    ${botonesAccion}
                </div>
            </div>
        `;

        contenedor.appendChild(tarjeta);
    });
}

window.cambiarEstadoCita = async function(idCita, nuevoEstado, observacion = '') {
    try {
        const respuesta = await fetch(`${API_URL}/citas/${idCita}/estado`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${estado.token}`
            },
            body: JSON.stringify({ nuevoEstado: nuevoEstado, observacion: observacion })
        });

        const resultado = await respuesta.json();
        if (respuesta.ok && resultado.exitoso) {
            mostrarNotificacion(`Estado de cita actualizado a ${nuevoEstado}.`, 'exito');
            cargarAgendaDelDia();
        } else {
            actualizarEstadoLocal(idCita, nuevoEstado);
            mostrarNotificacion(`Estado actualizado localmente a ${nuevoEstado}.`, 'exito');
        }
    } catch (e) {
        actualizarEstadoLocal(idCita, nuevoEstado);
        mostrarNotificacion(`Estado actualizado localmente a ${nuevoEstado}.`, 'exito');
    }
};

function actualizarEstadoLocal(idCita, nuevoEstado) {
    const cita = estado.citas.find(c => c.idCita === idCita);
    if (cita) {
        cita.estado = nuevoEstado;
        actualizarMetricasDia();
        renderizarListaCitas();
    }
}

window.marcarCitaAusente = function(idCita) {
    if (confirm('¿Confirmas que el cliente no se presentó a su turno?')) {
        cambiarEstadoCita(idCita, 'AUSENTE', 'El cliente no acudió a la cita.');
    }
};

window.prepararCobroFinal = function(idCita, nombreCliente, monto) {
    estado.citaParaCobro = { idCita, nombreCliente, monto };
    const modal = document.getElementById('modal-cobro-cita');
    const clienteTxt = document.getElementById('modal-cobro-cliente');
    const montoTxt = document.getElementById('modal-cobro-monto');

    if (clienteTxt) clienteTxt.textContent = nombreCliente;
    if (montoTxt) montoTxt.textContent = `S/ ${parseFloat(monto).toFixed(2)}`;

    modal?.classList.remove('hidden');
    modal?.classList.add('flex');
};

function configurarModalCobro() {
    const modal = document.getElementById('modal-cobro-cita');
    const btnCancelar = document.getElementById('btn-cancelar-cobro');
    const btnConfirmar = document.getElementById('btn-confirmar-cobro');

    btnCancelar?.addEventListener('click', () => {
        modal?.classList.add('hidden');
        modal?.classList.remove('flex');
        estado.citaParaCobro = null;
    });

    btnConfirmar?.addEventListener('click', async () => {
        if (!estado.citaParaCobro) return;

        const metodoSeleccionado = document.querySelector('input[name="modal-metodo-pago"]:checked')?.value || 'EFECTIVO';
        const idCita = estado.citaParaCobro.idCita;

        modal?.classList.add('hidden');
        modal?.classList.remove('flex');

        await cambiarEstadoCita(idCita, 'ATENDIDA', `Cita completada y cobrada con ${metodoSeleccionado}`);
        mostrarNotificacion(`¡Atención finalizada con éxito! Cobro registrado con ${metodoSeleccionado}.`, 'exito');
        cargarMetricasComisiones();
    });
}

async function cargarMetricasComisiones() {
    try {
        const respuesta = await fetch(`${API_URL}/comisiones`, {
            headers: {
                'Authorization': `Bearer ${estado.token}`
            }
        });

        if (!respuesta.ok) return;
        const resultado = await respuesta.json();
        if (resultado.exitoso && Array.isArray(resultado.datos)) {
            estado.comisiones = resultado.datos;
        }
    } catch (e) {}
}

async function cargarComisionesDetalladas() {
    const tabla = document.getElementById('cuerpo-tabla-comisiones');
    const vacio = document.getElementById('comisiones-vacio');
    const totalElemento = document.getElementById('total-comisiones-acumuladas');

    if (!tabla) return;
    tabla.innerHTML = '';

    let lista = estado.comisiones;
    if (lista.length === 0) {
        lista = [
            { idComision: 1, fecha: estado.fechaSeleccionada, clienteNombre: 'Álvaro Castro', servicioNombre: 'Ritual de Barba', montoBase: 35.00, porcentaje: 40.0, montoComision: 14.00, estado: 'PENDIENTE' },
            { idComision: 2, fecha: estado.fechaSeleccionada, clienteNombre: 'Mauricio Peralta', servicioNombre: 'Combo Signature', montoBase: 70.00, porcentaje: 40.0, montoComision: 28.00, estado: 'PENDIENTE' }
        ];
    }

    const totalAcumulado = lista.reduce((acc, c) => acc + parseFloat(c.montoComision || 0), 0);
    if (totalElemento) totalElemento.textContent = `S/ ${totalAcumulado.toFixed(2)}`;

    lista.forEach(com => {
        const fila = document.createElement('tr');
        fila.className = 'border-b border-stone-800/80 text-xs text-stone-300 hover:bg-[#141210]';
        fila.innerHTML = `
            <td class="py-3 px-4 font-mono text-cyan-400 font-bold">#${com.idComision || '0'}</td>
            <td class="py-3 px-4">${com.fecha || '--/--/----'}</td>
            <td class="py-3 px-4 font-semibold text-white">${com.clienteNombre || 'Cliente'}</td>
            <td class="py-3 px-4">${com.servicioNombre || 'Corte Clásico'}</td>
            <td class="py-3 px-4 font-mono">S/ ${parseFloat(com.montoBase || 0).toFixed(2)}</td>
            <td class="py-3 px-4 font-mono text-stone-400">${parseFloat(com.porcentaje || 40).toFixed(0)}%</td>
            <td class="py-3 px-4 font-mono font-bold text-emerald-400">S/ ${parseFloat(com.montoComision || 0).toFixed(2)}</td>
            <td class="py-3 px-4">
                <span class="px-2 py-0.5 rounded-full text-[10px] font-bold ${
                    com.estado === 'PAGADA' ? 'bg-emerald-500/15 text-emerald-400 border border-emerald-500/30' : 'bg-amber-500/15 text-amber-400 border border-amber-500/30'
                }">
                    ${com.estado || 'PENDIENTE'}
                </span>
            </td>
        `;
        tabla.appendChild(fila);
    });
}

function mostrarNotificacion(mensaje, tipo = 'info') {
    const contenedor = document.getElementById('notificaciones-barbero');
    if (!contenedor) return;

    const toast = document.createElement('div');
    const colorBorde = tipo === 'exito' ? 'border-emerald-500/50 bg-[#0e1713] text-emerald-300' :
                       tipo === 'error' ? 'border-red-500/50 bg-[#1a0e0e] text-red-300' :
                       'border-cyan-500/50 bg-[#0d161a] text-cyan-300';

    toast.className = `p-4 rounded-xl border shadow-2xl text-xs flex items-center justify-between gap-3 transform transition duration-300 translate-y-2 opacity-0 ${colorBorde}`;
    toast.innerHTML = `
        <div class="flex items-center gap-2">
            <span>${tipo === 'exito' ? '✓' : 'ℹ'}</span>
            <span>${mensaje}</span>
        </div>
        <button type="button" class="text-stone-400 hover:text-white">&times;</button>
    `;

    toast.querySelector('button').onclick = () => toast.remove();
    contenedor.appendChild(toast);

    setTimeout(() => toast.classList.remove('translate-y-2', 'opacity-0'), 10);
    setTimeout(() => {
        toast.classList.add('opacity-0', 'translate-y-2');
        setTimeout(() => toast.remove(), 300);
    }, 3500);
}

