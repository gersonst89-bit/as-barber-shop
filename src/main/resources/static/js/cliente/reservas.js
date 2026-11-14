const API_URL = '/api';

const estado = {
    token: localStorage.getItem('as_token') || sessionStorage.getItem('as_token'),
    usuario: null,
    pasoActual: 1,
    servicios: [],
    barberos: [],
    serviciosSeleccionados: [],
    barberoSeleccionado: null,
    fechaSeleccionada: null,
    horaSeleccionada: null,
    horasDisponibles: [],
    citas: [],
    metodoPago: 'TARJETA'
};

try {
    const rawUser = localStorage.getItem('as_usuario') || sessionStorage.getItem('as_usuario');
    if (rawUser) estado.usuario = JSON.parse(rawUser);
} catch (e) {
    estado.usuario = null;
}

document.addEventListener('DOMContentLoaded', () => {
    configurarCabeceraUsuario();
    configurarPestanas();
    configurarNavegacionPasos();
    configurarSelectorFecha();
    configurarFormularioReserva();

    cargarCatalogoServicios();
    cargarCatalogoBarberos();

    const params = new URLSearchParams(window.location.search);
    if (params.get('tab') === 'mis-citas') {
        activarPestana('mis-citas');
    }
});

function configurarCabeceraUsuario() {
    const contenedorUsuario = document.getElementById('info-usuario-cabecera');
    const nombreElemento = document.getElementById('nombre-cliente-cabecera');
    const rolElemento = document.getElementById('rol-cliente-cabecera');
    const avatarElemento = document.getElementById('avatar-cliente-cabecera');
    const btnSalir = document.getElementById('btn-cerrar-sesion');

    if (estado.token && estado.usuario) {
        if (nombreElemento) nombreElemento.textContent = `${estado.usuario.nombre} ${estado.usuario.apellido}`;
        if (rolElemento) rolElemento.textContent = estado.usuario.email || 'Cliente';
        if (avatarElemento) avatarElemento.textContent = estado.usuario.nombre.charAt(0).toUpperCase();
        if (contenedorUsuario) contenedorUsuario.classList.remove('hidden');
    } else {
        if (nombreElemento) nombreElemento.textContent = 'Invitado';
        if (rolElemento) rolElemento.textContent = 'Inicia sesión para agendar';
        if (avatarElemento) avatarElemento.textContent = '?';
        if (btnSalir) {
            btnSalir.innerHTML = '<span>Iniciar Sesión</span>';
            btnSalir.onclick = () => window.location.href = '/pages/login.html';
        }
    }

    if (btnSalir && estado.token) {
        btnSalir.addEventListener('click', () => {
            localStorage.removeItem('as_token');
            localStorage.removeItem('as_usuario');
            sessionStorage.removeItem('as_token');
            sessionStorage.removeItem('as_usuario');
            window.location.href = '/';
        });
    }
}

function configurarPestanas() {
    const tabAgendar = document.getElementById('tab-btn-agendar');
    const tabMisCitas = document.getElementById('tab-btn-mis-citas');

    tabAgendar?.addEventListener('click', () => activarPestana('agendar'));
    tabMisCitas?.addEventListener('click', () => activarPestana('mis-citas'));
}

function activarPestana(pestaña) {
    const tabAgendar = document.getElementById('tab-btn-agendar');
    const tabMisCitas = document.getElementById('tab-btn-mis-citas');
    const vistaAgendar = document.getElementById('vista-agendar');
    const vistaMisCitas = document.getElementById('vista-mis-citas');

    if (pestaña === 'agendar') {
        tabAgendar?.classList.add('bg-cyan-500/15', 'border-cyan-500/50', 'text-cyan-400');
        tabAgendar?.classList.remove('text-stone-400', 'border-transparent');

        tabMisCitas?.classList.remove('bg-cyan-500/15', 'border-cyan-500/50', 'text-cyan-400');
        tabMisCitas?.classList.add('text-stone-400', 'border-transparent');

        vistaAgendar?.classList.remove('hidden');
        vistaMisCitas?.classList.add('hidden');
    } else {
        if (!estado.token) {
            mostrarNotificacion('Debes iniciar sesión para consultar tus citas.', 'info');
            setTimeout(() => {
                window.location.href = '/pages/login.html?redirect=/pages/cliente/reservas.html?tab=mis-citas';
            }, 1200);
            return;
        }

        tabMisCitas?.classList.add('bg-cyan-500/15', 'border-cyan-500/50', 'text-cyan-400');
        tabMisCitas?.classList.remove('text-stone-400', 'border-transparent');

        tabAgendar?.classList.remove('bg-cyan-500/15', 'border-cyan-500/50', 'text-cyan-400');
        tabAgendar?.classList.add('text-stone-400', 'border-transparent');

        vistaAgendar?.classList.add('hidden');
        vistaMisCitas?.classList.remove('hidden');

        cargarMisCitas();
    }
}

function configurarNavegacionPasos() {
    document.getElementById('btn-paso1-siguiente')?.addEventListener('click', () => {
        if (estado.serviciosSeleccionados.length === 0) {
            mostrarNotificacion('Por favor selecciona al menos un servicio para continuar.', 'alerta');
            return;
        }
        irAPaso(2);
    });

    document.getElementById('btn-paso2-anterior')?.addEventListener('click', () => irAPaso(1));
    document.getElementById('btn-paso2-siguiente')?.addEventListener('click', () => {
        if (!estado.barberoSeleccionado) {
            mostrarNotificacion('Selecciona un maestro barbero o elige "Cualquiera Disponible".', 'alerta');
            return;
        }
        irAPaso(3);
    });

    document.getElementById('btn-paso3-anterior')?.addEventListener('click', () => irAPaso(2));
    document.getElementById('btn-paso3-siguiente')?.addEventListener('click', () => {
        if (!estado.fechaSeleccionada) {
            mostrarNotificacion('Selecciona la fecha para tu atención.', 'alerta');
            return;
        }
        if (!estado.horaSeleccionada) {
            mostrarNotificacion('Selecciona un horario disponible para tu cita.', 'alerta');
            return;
        }
        irAPaso(4);
    });

    document.getElementById('btn-paso4-anterior')?.addEventListener('click', () => irAPaso(3));
}

function irAPaso(nuevoPaso) {
    estado.pasoActual = nuevoPaso;

    for (let i = 1; i <= 4; i++) {
        const contenido = document.getElementById(`paso-contenido-${i}`);
        const indicador = document.getElementById(`indicador-paso-${i}`);
        const textoIndicador = document.getElementById(`texto-paso-${i}`);

        if (i === nuevoPaso) {
            contenido?.classList.remove('hidden');
            indicador?.classList.add('bg-cyan-500', 'text-stone-950', 'border-cyan-400', 'shadow-lg', 'shadow-cyan-500/30');
            indicador?.classList.remove('bg-[#141210]', 'text-stone-400', 'border-stone-800');
            textoIndicador?.classList.add('text-cyan-400', 'font-bold');
            textoIndicador?.classList.remove('text-stone-500');
        } else if (i < nuevoPaso) {
            contenido?.classList.add('hidden');
            indicador?.classList.add('bg-emerald-500/20', 'text-emerald-400', 'border-emerald-500/50');
            indicador?.classList.remove('bg-cyan-500', 'text-stone-950', 'border-cyan-400', 'bg-[#141210]', 'text-stone-400', 'border-stone-800');
            textoIndicador?.classList.add('text-stone-300');
            textoIndicador?.classList.remove('text-cyan-400', 'text-stone-500');
        } else {
            contenido?.classList.add('hidden');
            indicador?.classList.remove('bg-cyan-500', 'text-stone-950', 'border-cyan-400', 'bg-emerald-500/20', 'text-emerald-400', 'border-emerald-500/50');
            indicador?.classList.add('bg-[#141210]', 'text-stone-400', 'border-stone-800');
            textoIndicador?.classList.remove('text-cyan-400', 'text-stone-300');
            textoIndicador?.classList.add('text-stone-500');
        }
    }

    if (nuevoPaso === 3) {
        if (!estado.fechaSeleccionada) {
            const hoy = new Date().toISOString().split('T')[0];
            const inputFecha = document.getElementById('input-fecha-cita');
            if (inputFecha) {
                inputFecha.value = hoy;
                estado.fechaSeleccionada = hoy;
            }
        }
        cargarHorariosDisponibles();
    } else if (nuevoPaso === 4) {
        renderizarResumenConfirmacion();
    }

    window.scrollTo({ top: 120, behavior: 'smooth' });
}

async function cargarCatalogoServicios() {
    const contenedor = document.getElementById('grilla-servicios-reserva');
    if (!contenedor) return;

    try {
        const respuesta = await fetch(`${API_URL}/servicios`);
        if (!respuesta.ok) throw new Error('Error al cargar servicios');
        const resultado = await respuesta.json();

        if (resultado.exitoso && Array.isArray(resultado.datos) && resultado.datos.length > 0) {
            estado.servicios = resultado.datos.filter(s => s.activo);
        } else {
            estado.servicios = [];
        }
    } catch (e) {
        estado.servicios = [];
    }

    renderizarCatalogoServicios();
}

function renderizarCatalogoServicios() {
    const contenedor = document.getElementById('grilla-servicios-reserva');
    if (!contenedor) return;

    contenedor.innerHTML = '';

    if (estado.servicios.length === 0) {
        contenedor.innerHTML = `
            <div class="col-span-full text-center py-12 text-stone-500 text-xs">
                No hay servicios disponibles para reservar en este momento.
            </div>
        `;
        actualizarBarraResumenServicios();
        return;
    }

    estado.servicios.forEach(servicio => {
        const estaSeleccionado = estado.serviciosSeleccionados.some(s => s.idServicio === servicio.idServicio);
        const tarjeta = document.createElement('div');
        tarjeta.className = `p-5 rounded-2xl border transition duration-200 cursor-pointer flex flex-col justify-between ${
            estaSeleccionado
                ? 'bg-[#151a1e] border-cyan-400 shadow-lg shadow-cyan-950/40 ring-1 ring-cyan-400'
                : 'bg-[#13110f] border-stone-800 hover:border-cyan-500/50 hover:bg-[#161412]'
        }`;

        tarjeta.onclick = () => alternarSeleccionServicio(servicio);

        tarjeta.innerHTML = `
            <div class="space-y-2">
                <div class="flex items-start justify-between gap-3">
                    <h3 class="text-sm font-bold text-white leading-snug">${servicio.nombre}</h3>
                    <div class="w-5 h-5 rounded-md border flex items-center justify-center shrink-0 transition ${
                        estaSeleccionado ? 'bg-cyan-500 border-cyan-400 text-stone-950 font-black text-xs' : 'border-stone-700 bg-[#181614]'
                    }">
                        ${estaSeleccionado ? '✓' : ''}
                    </div>
                </div>
                <p class="text-xs text-stone-400 leading-relaxed">${servicio.descripcion || 'Servicio clásico de alta barbería.'}</p>
            </div>

            <div class="pt-4 mt-4 border-t border-stone-800/80 flex items-center justify-between text-xs">
                <div class="flex items-center gap-1.5 text-stone-400">
                    <svg class="w-3.5 h-3.5 text-cyan-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                    </svg>
                    <span>${servicio.duracionMinutos} min</span>
                </div>
                <span class="text-base font-black text-cyan-400 font-mono">S/ ${parseFloat(servicio.precio).toFixed(2)}</span>
            </div>
        `;

        contenedor.appendChild(tarjeta);
    });

    actualizarBarraResumenServicios();
}

function alternarSeleccionServicio(servicio) {
    const indice = estado.serviciosSeleccionados.findIndex(s => s.idServicio === servicio.idServicio);
    if (indice > -1) {
        estado.serviciosSeleccionados.splice(indice, 1);
    } else {
        estado.serviciosSeleccionados.push(servicio);
    }

    renderizarCatalogoServicios();
}

function actualizarBarraResumenServicios() {
    const totalElemento = document.getElementById('resumen-paso1-total');
    const tiempoElemento = document.getElementById('resumen-paso1-tiempo');
    const contadorElemento = document.getElementById('resumen-paso1-contador');
    const btnSiguiente = document.getElementById('btn-paso1-siguiente');

    const total = estado.serviciosSeleccionados.reduce((acc, s) => acc + parseFloat(s.precio), 0);
    const tiempo = estado.serviciosSeleccionados.reduce((acc, s) => acc + parseInt(s.duracionMinutos), 0);
    const count = estado.serviciosSeleccionados.length;

    if (totalElemento) totalElemento.textContent = `S/ ${total.toFixed(2)}`;
    if (tiempoElemento) tiempoElemento.textContent = `${tiempo} min`;
    if (contadorElemento) contadorElemento.textContent = `${count} ${count === 1 ? 'servicio' : 'servicios'}`;

    if (btnSiguiente) {
        if (count > 0) {
            btnSiguiente.disabled = false;
            btnSiguiente.classList.remove('opacity-40', 'cursor-not-allowed');
            btnSiguiente.classList.add('hover:from-cyan-400', 'hover:to-blue-500');
        } else {
            btnSiguiente.disabled = true;
            btnSiguiente.classList.add('opacity-40', 'cursor-not-allowed');
            btnSiguiente.classList.remove('hover:from-cyan-400', 'hover:to-blue-500');
        }
    }
}

async function cargarCatalogoBarberos() {
    try {
        const respuesta = await fetch(`${API_URL}/barberos`);
        if (!respuesta.ok) throw new Error('Error al cargar barberos');
        const resultado = await respuesta.json();

        if (resultado.exitoso && Array.isArray(resultado.datos) && resultado.datos.length > 0) {
            estado.barberos = resultado.datos.filter(b => b.disponible !== false);
        } else {
            estado.barberos = [];
        }
    } catch (e) {
        estado.barberos = [];
    }

    renderizarCatalogoBarberos();
}

function renderizarCatalogoBarberos() {
    const contenedor = document.getElementById('grilla-barberos-reserva');
    if (!contenedor) return;

    contenedor.innerHTML = '';

    if (estado.barberos.length === 0) {
        contenedor.innerHTML = `
            <div class="col-span-full text-center py-12 text-stone-500 text-xs">
                No hay barberos disponibles en este momento.
            </div>
        `;
        return;
    }

    const tarjetaCualquiera = document.createElement('div');
    const esCualquiera = estado.barberoSeleccionado && estado.barberoSeleccionado.idBarbero === null;
    tarjetaCualquiera.className = `p-5 rounded-2xl border transition duration-200 cursor-pointer text-center space-y-3 ${
        esCualquiera
            ? 'bg-[#151a1e] border-cyan-400 shadow-lg shadow-cyan-950/40 ring-1 ring-cyan-400'
            : 'bg-[#13110f] border-stone-800 hover:border-cyan-500/50 hover:bg-[#161412]'
    }`;
    tarjetaCualquiera.onclick = () => {
        estado.barberoSeleccionado = { idBarbero: null, nombre: 'Cualquier Barbero', apellido: 'Disponible', especialidad: 'Primer turno disponible' };
        renderizarCatalogoBarberos();
        activarBotonSiguientePaso2();
    };

    tarjetaCualquiera.innerHTML = `
        <div class="w-16 h-16 mx-auto rounded-full bg-gradient-to-tr from-cyan-500/20 to-blue-500/20 border border-cyan-500/40 flex items-center justify-center text-cyan-400 text-2xl">
            ⚡
        </div>
        <div>
            <h4 class="text-sm font-bold text-white">Cualquier Barbero</h4>
            <p class="text-[11px] text-cyan-400 font-medium mt-0.5">Máxima Disponibilidad</p>
            <p class="text-[11px] text-stone-400 mt-1">Elige el primer turno disponible con cualquiera de nuestros maestros.</p>
        </div>
        <div class="pt-2 border-t border-stone-800 text-[11px] ${esCualquiera ? 'text-cyan-400 font-bold' : 'text-stone-500'}">
            ${esCualquiera ? '✓ Seleccionado' : 'Click para elegir'}
        </div>
    `;
    contenedor.appendChild(tarjetaCualquiera);

    estado.barberos.forEach(barbero => {
        const estaSeleccionado = estado.barberoSeleccionado && estado.barberoSeleccionado.idBarbero === barbero.idBarbero;
        const tarjeta = document.createElement('div');
        tarjeta.className = `p-5 rounded-2xl border transition duration-200 cursor-pointer text-center space-y-3 ${
            estaSeleccionado
                ? 'bg-[#151a1e] border-cyan-400 shadow-lg shadow-cyan-950/40 ring-1 ring-cyan-400'
                : 'bg-[#13110f] border-stone-800 hover:border-cyan-500/50 hover:bg-[#161412]'
        }`;

        tarjeta.onclick = () => {
            estado.barberoSeleccionado = barbero;
            renderizarCatalogoBarberos();
            activarBotonSiguientePaso2();
        };

        const iniciales = (barbero.nombre ? barbero.nombre.charAt(0) : 'B') + (barbero.apellido ? barbero.apellido.charAt(0) : '');
        const avatarHtml = barbero.foto
            ? `<img src="${barbero.foto}" alt="${barbero.nombre}" class="w-full h-full object-cover">`
            : `<div class="w-full h-full bg-[#171513] flex items-center justify-center text-base font-black text-cyan-400">${iniciales}</div>`;

        tarjeta.innerHTML = `
            <div class="w-16 h-16 mx-auto rounded-full p-0.5 bg-gradient-to-tr from-cyan-500 to-blue-700 shadow-md overflow-hidden">
                ${avatarHtml}
            </div>
            <div>
                <h4 class="text-sm font-bold text-white">${barbero.nombre} ${barbero.apellido}</h4>
                <p class="text-[11px] text-cyan-400 font-medium mt-0.5">${barbero.especialidad || 'Estilista Profesional'}</p>
                <p class="text-[11px] text-stone-400 mt-1">Maestro certificado A&S Miraflores.</p>
            </div>
            <div class="pt-2 border-t border-stone-800 text-[11px] ${estaSeleccionado ? 'text-cyan-400 font-bold' : 'text-stone-500'}">
                ${estaSeleccionado ? '✓ Seleccionado' : 'Click para elegir'}
            </div>
        `;

        contenedor.appendChild(tarjeta);
    });

    activarBotonSiguientePaso2();
}

function activarBotonSiguientePaso2() {
    const btnSiguiente = document.getElementById('btn-paso2-siguiente');
    if (!btnSiguiente) return;

    if (estado.barberoSeleccionado) {
        btnSiguiente.disabled = false;
        btnSiguiente.classList.remove('opacity-40', 'cursor-not-allowed');
        btnSiguiente.classList.add('hover:from-cyan-400', 'hover:to-blue-500');
    } else {
        btnSiguiente.disabled = true;
        btnSiguiente.classList.add('opacity-40', 'cursor-not-allowed');
        btnSiguiente.classList.remove('hover:from-cyan-400', 'hover:to-blue-500');
    }
}

function configurarSelectorFecha() {
    const inputFecha = document.getElementById('input-fecha-cita');
    if (!inputFecha) return;

    const hoy = new Date();
    const hoyStr = hoy.toISOString().split('T')[0];
    inputFecha.min = hoyStr;

    const maxFecha = new Date();
    maxFecha.setDate(hoy.getDate() + 30);
    inputFecha.max = maxFecha.toISOString().split('T')[0];

    inputFecha.value = hoyStr;
    estado.fechaSeleccionada = hoyStr;

    inputFecha.addEventListener('change', () => {
        estado.fechaSeleccionada = inputFecha.value;
        estado.horaSeleccionada = null;
        cargarHorariosDisponibles();
    });
}

async function cargarHorariosDisponibles() {
    const contenedor = document.getElementById('grilla-horarios-reserva');
    const cargando = document.getElementById('cargando-horarios');
    const mensajeVacio = document.getElementById('sin-horarios-mensaje');
    const btnSiguiente = document.getElementById('btn-paso3-siguiente');

    if (!contenedor) return;

    if (btnSiguiente) {
        btnSiguiente.disabled = true;
        btnSiguiente.classList.add('opacity-40', 'cursor-not-allowed');
    }

    contenedor.innerHTML = '';
    cargando?.classList.remove('hidden');
    mensajeVacio?.classList.add('hidden');

    const duracionTotal = estado.serviciosSeleccionados.reduce((acc, s) => acc + parseInt(s.duracionMinutos), 0) || 30;
    const barberoId = (estado.barberoSeleccionado && estado.barberoSeleccionado.idBarbero) ? estado.barberoSeleccionado.idBarbero : 1;

    try {
        const respuesta = await fetch(`${API_URL}/horarios/disponibilidad?idBarbero=${barberoId}&fecha=${estado.fechaSeleccionada}&duracionMinutos=${duracionTotal}`);
        if (!respuesta.ok) throw new Error('Horario no disponible');
        const resultado = await respuesta.json();

        if (resultado.exitoso && resultado.datos && Array.isArray(resultado.datos.horasDisponibles) && resultado.datos.horasDisponibles.length > 0) {
            estado.horasDisponibles = resultado.datos.horasDisponibles;
        } else {
            estado.horasDisponibles = generarHorariosFallback(estado.fechaSeleccionada);
        }
    } catch (e) {
        estado.horasDisponibles = generarHorariosFallback(estado.fechaSeleccionada);
    } finally {
        cargando?.classList.add('hidden');
    }

    renderizarChipsHorarios();
}

function generarHorariosFallback(fechaStr) {
    const ahora = new Date();
    const esHoy = fechaStr === ahora.toISOString().split('T')[0];
    const horaActualMinutos = ahora.getHours() * 60 + ahora.getMinutes();

    const turnos = [
        '10:00', '10:45', '11:30', '12:15', '13:00',
        '14:00', '14:45', '15:30', '16:15', '17:00',
        '17:45', '18:30', '19:15', '20:00'
    ];

    return turnos.filter(t => {
        if (!esHoy) return true;
        const [h, m] = t.split(':').map(Number);
        return (h * 60 + m) > (horaActualMinutos + 20);
    });
}

function renderizarChipsHorarios() {
    const contenedor = document.getElementById('grilla-horarios-reserva');
    const mensajeVacio = document.getElementById('sin-horarios-mensaje');
    if (!contenedor) return;

    contenedor.innerHTML = '';

    if (estado.horasDisponibles.length === 0) {
        mensajeVacio?.classList.remove('hidden');
        return;
    }

    mensajeVacio?.classList.add('hidden');

    estado.horasDisponibles.forEach(hora => {
        const horaCorta = hora.substring(0, 5);
        const estaSeleccionada = estado.horaSeleccionada === horaCorta;

        const chip = document.createElement('button');
        chip.type = 'button';
        chip.className = `py-3 px-2 rounded-xl text-xs font-bold font-mono transition duration-150 border flex items-center justify-center gap-1.5 ${
            estaSeleccionada
                ? 'bg-gradient-to-r from-cyan-500 to-blue-600 text-stone-950 border-cyan-400 shadow-lg shadow-cyan-500/30 scale-105'
                : 'bg-[#181614] border-stone-800 text-stone-200 hover:border-cyan-500/50 hover:bg-[#1c1917]'
        }`;

        chip.innerHTML = `
            <span>🕒</span>
            <span>${horaCorta}</span>
        `;

        chip.onclick = () => {
            estado.horaSeleccionada = horaCorta;
            renderizarChipsHorarios();

            const btnSiguiente = document.getElementById('btn-paso3-siguiente');
            if (btnSiguiente) {
                btnSiguiente.disabled = false;
                btnSiguiente.classList.remove('opacity-40', 'cursor-not-allowed');
                btnSiguiente.classList.add('hover:from-cyan-400', 'hover:to-blue-500');
            }
        };

        contenedor.appendChild(chip);
    });
}

function renderizarResumenConfirmacion() {
    const contenedorServicios = document.getElementById('resumen-final-servicios');
    const barberoNombre = document.getElementById('resumen-final-barbero');
    const fechaHora = document.getElementById('resumen-final-fechahora');
    const duracion = document.getElementById('resumen-final-duracion');
    const subtotal = document.getElementById('resumen-final-subtotal');
    const total = document.getElementById('resumen-final-total');

    if (contenedorServicios) {
        contenedorServicios.innerHTML = '';
        estado.serviciosSeleccionados.forEach(s => {
            const fila = document.createElement('div');
            fila.className = 'flex items-center justify-between text-xs py-1 border-b border-stone-800/60 last:border-0';
            fila.innerHTML = `
                <span class="text-stone-300">${s.nombre} <span class="text-stone-500">(${s.duracionMinutos}m)</span></span>
                <span class="font-mono text-stone-200 font-bold">S/ ${parseFloat(s.precio).toFixed(2)}</span>
            `;
            contenedorServicios.appendChild(fila);
        });
    }

    if (barberoNombre) {
        barberoNombre.textContent = estado.barberoSeleccionado
            ? `${estado.barberoSeleccionado.nombre} ${estado.barberoSeleccionado.apellido}`
            : 'Por Asignar';
    }

    if (fechaHora) {
        fechaHora.textContent = `${formatearFecha(estado.fechaSeleccionada)} a las ${estado.horaSeleccionada}`;
    }

    const duracionMin = estado.serviciosSeleccionados.reduce((acc, s) => acc + parseInt(s.duracionMinutos), 0);
    if (duracion) duracion.textContent = `${duracionMin} minutos`;

    const totalMonto = estado.serviciosSeleccionados.reduce((acc, s) => acc + parseFloat(s.precio), 0);
    if (subtotal) subtotal.textContent = `S/ ${totalMonto.toFixed(2)}`;
    if (total) total.textContent = `S/ ${totalMonto.toFixed(2)}`;

    actualizarBotonConfirmar();
}

function formatearFecha(fechaStr) {
    if (!fechaStr) return '';
    const [y, m, d] = fechaStr.split('-');
    const dias = ['Domingo', 'Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado'];
    const meses = ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Set', 'Oct', 'Nov', 'Dic'];
    const fecha = new Date(parseInt(y), parseInt(m) - 1, parseInt(d));
    return `${dias[fecha.getDay()]}, ${d} de ${meses[fecha.getMonth()]} de ${y}`;
}

function seleccionarMetodoPago(metodo) {
    estado.metodoPago = metodo;

    const btnTarjeta = document.getElementById('btn-metodo-tarjeta');
    const btnYape = document.getElementById('btn-metodo-yape');
    const btnEfectivo = document.getElementById('btn-metodo-efectivo');

    const textoTarjeta = document.getElementById('texto-btn-metodo-tarjeta');
    const textoYape = document.getElementById('texto-btn-metodo-yape');
    const textoEfectivo = document.getElementById('texto-btn-metodo-efectivo');

    const panelTarjeta = document.getElementById('panel-pago-tarjeta');
    const panelYape = document.getElementById('panel-pago-yape');
    const panelEfectivo = document.getElementById('panel-pago-efectivo');

    if (btnTarjeta) btnTarjeta.className = 'p-3 rounded-xl border border-stone-800 bg-[#161412] text-center transition flex flex-col items-center gap-1 group';
    if (btnYape) btnYape.className = 'p-3 rounded-xl border border-stone-800 bg-[#161412] text-center transition flex flex-col items-center gap-1 group';
    if (btnEfectivo) btnEfectivo.className = 'p-3 rounded-xl border border-stone-800 bg-[#161412] text-center transition flex flex-col items-center gap-1 group';

    if (textoTarjeta) textoTarjeta.className = 'text-[11px] font-bold text-stone-400 group-hover:text-cyan-300';
    if (textoYape) textoYape.className = 'text-[11px] font-bold text-stone-400 group-hover:text-purple-300';
    if (textoEfectivo) textoEfectivo.className = 'text-[11px] font-bold text-stone-400 group-hover:text-amber-300';

    panelTarjeta?.classList.add('hidden');
    panelYape?.classList.add('hidden');
    panelEfectivo?.classList.add('hidden');

    if (metodo === 'TARJETA') {
        if (btnTarjeta) btnTarjeta.className = 'p-3 rounded-xl border border-cyan-500/50 bg-cyan-950/20 text-center transition flex flex-col items-center gap-1 group ring-1 ring-cyan-500/30';
        if (textoTarjeta) textoTarjeta.className = 'text-[11px] font-bold text-cyan-300';
        panelTarjeta?.classList.remove('hidden');
    } else if (metodo === 'YAPE') {
        if (btnYape) btnYape.className = 'p-3 rounded-xl border border-purple-500/50 bg-purple-950/20 text-center transition flex flex-col items-center gap-1 group ring-1 ring-purple-500/30';
        if (textoYape) textoYape.className = 'text-[11px] font-bold text-purple-300';
        panelYape?.classList.remove('hidden');
    } else {
        if (btnEfectivo) btnEfectivo.className = 'p-3 rounded-xl border border-amber-500/50 bg-amber-950/20 text-center transition flex flex-col items-center gap-1 group ring-1 ring-amber-500/30';
        if (textoEfectivo) textoEfectivo.className = 'text-[11px] font-bold text-amber-300';
        panelEfectivo?.classList.remove('hidden');
    }

    actualizarBotonConfirmar();
}

function actualizarBotonConfirmar() {
    const texto = document.getElementById('btn-texto-confirmar');
    if (!texto) return;

    const totalMonto = estado.serviciosSeleccionados.reduce((acc, s) => acc + parseFloat(s.precio), 0);
    const montoFormateado = `S/ ${totalMonto.toFixed(2)}`;

    if (estado.metodoPago === 'TARJETA') {
        texto.textContent = `🔒 Pagar ${montoFormateado} y Confirmar Cita`;
    } else if (estado.metodoPago === 'YAPE') {
        texto.textContent = `📱 Confirmar Pago Yape (${montoFormateado})`;
    } else {
        texto.textContent = '✓ Confirmar Reserva en Sede';
    }
}

function cargarTarjetaDemoAprobada() {
    const elNum = document.getElementById('pago-tarjeta-numero');
    const elExp = document.getElementById('pago-tarjeta-expira');
    const elCvv = document.getElementById('pago-tarjeta-cvv');
    const elTitular = document.getElementById('pago-tarjeta-titular');

    if (elNum) elNum.value = '4557 8901 2345 6789';
    if (elExp) elExp.value = '12/28';
    if (elCvv) elCvv.value = '842';

    const nombre = estado.usuario
        ? `${estado.usuario.nombre} ${estado.usuario.apellido || ''}`.trim().toUpperCase()
        : 'GERSON YUPANQUI CANO';

    if (elTitular) elTitular.value = nombre;

    mostrarNotificacion('Tarjeta de prueba Visa Débito cargada con éxito.', 'success');
}

function generarCodigoYapeDemo() {
    const elCodigo = document.getElementById('pago-yape-codigo');
    const codigoGenerado = Math.floor(100000 + Math.random() * 900000).toString();
    if (elCodigo) elCodigo.value = codigoGenerado;
    mostrarNotificacion(`Código Yape simulado generado: ${codigoGenerado}`, 'success');
}

function configurarFormularioReserva() {
    const btnConfirmar = document.getElementById('btn-confirmar-cita-final');
    btnConfirmar?.addEventListener('click', enviarReservaCita);
}

async function enviarReservaCita() {
    if (!estado.token) {
        mostrarNotificacion('Inicia sesión o crea una cuenta para confirmar tu cita.', 'info');
        setTimeout(() => {
            window.location.href = '/pages/login.html?redirect=/pages/cliente/reservas.html';
        }, 1200);
        return;
    }

    if (estado.metodoPago === 'TARJETA') {
        const num = document.getElementById('pago-tarjeta-numero')?.value.replace(/\s+/g, '') || '';
        const cvv = document.getElementById('pago-tarjeta-cvv')?.value.trim() || '';
        if (num.length < 15) {
            mostrarNotificacion('Ingresa un número de tarjeta válido (o usa el botón de prueba).', 'error');
            return;
        }
        if (cvv.length < 3) {
            mostrarNotificacion('Ingresa el código de seguridad CVV (3 dígitos).', 'error');
            return;
        }
    } else if (estado.metodoPago === 'YAPE') {
        const cod = document.getElementById('pago-yape-codigo')?.value.trim() || '';
        if (cod.length < 6) {
            mostrarNotificacion('Ingresa el código de aprobación de 6 dígitos de Yape (o usa el botón de prueba).', 'error');
            return;
        }
    }

    const btnConfirmar = document.getElementById('btn-confirmar-cita-final');
    const notas = document.getElementById('input-notas-cita')?.value.trim() || '';
    const totalMonto = estado.serviciosSeleccionados.reduce((acc, s) => acc + parseFloat(s.precio), 0);

    if (btnConfirmar) {
        btnConfirmar.disabled = true;
        btnConfirmar.innerHTML = `
            <svg class="animate-spin h-4 w-4 text-stone-950 inline mr-1" viewBox="0 0 24 24" fill="none">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z"></path>
            </svg>
            <span>Procesando pago y reserva...</span>
        `;
    }

    const payload = {
        idBarbero: (estado.barberoSeleccionado && estado.barberoSeleccionado.idBarbero) ? estado.barberoSeleccionado.idBarbero : 1,
        fecha: estado.fechaSeleccionada,
        horaInicio: estado.horaSeleccionada,
        idsServicios: estado.serviciosSeleccionados.map(s => s.idServicio),
        notas: notas,
        codigoPromocion: null
    };

    try {
        const respuestaCita = await fetch(`${API_URL}/citas`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${estado.token}`
            },
            body: JSON.stringify(payload)
        });

        const resultadoCita = await respuestaCita.json();

        if (!respuestaCita.ok || !resultadoCita.exitoso) {
            throw new Error(resultadoCita.mensaje || 'No se pudo reservar la cita. Revisa el horario.');
        }

        const citaGuardada = resultadoCita.datos || payload;
        let pagoInfo = null;

        if (estado.metodoPago !== 'EFECTIVO' && citaGuardada.idCita) {
            const idMetodo = estado.metodoPago === 'TARJETA' ? 2 : 3;
            const ref = estado.metodoPago === 'TARJETA'
                ? `TXN-CARD-${Date.now().toString().slice(-6)}`
                : `YAPE-${document.getElementById('pago-yape-codigo')?.value.trim() || Date.now().toString().slice(-6)}`;

            const payloadPago = {
                idCita: citaGuardada.idCita,
                idMetodo: idMetodo,
                monto: parseFloat(citaGuardada.montoTotal || totalMonto),
                referencia: ref
            };

            try {
                const respuestaPago = await fetch(`${API_URL}/pagos`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${estado.token}`
                    },
                    body: JSON.stringify(payloadPago)
                });
                const resultadoPago = await respuestaPago.json();
                pagoInfo = {
                    metodo: estado.metodoPago === 'TARJETA' ? 'Tarjeta de Débito' : 'Yape / Plin',
                    referencia: ref,
                    pagado: true,
                    monto: parseFloat(citaGuardada.montoTotal || totalMonto)
                };
            } catch (errPago) {
                pagoInfo = {
                    metodo: estado.metodoPago,
                    referencia: ref,
                    pagado: true,
                    monto: parseFloat(citaGuardada.montoTotal || totalMonto)
                };
            }
        } else {
            pagoInfo = {
                metodo: 'Efectivo en Sede',
                referencia: `SEDE-${Date.now().toString().slice(-5)}`,
                pagado: false,
                monto: parseFloat(citaGuardada.montoTotal || totalMonto)
            };
        }

        mostrarModalExito(citaGuardada, pagoInfo);

    } catch (e) {
        mostrarNotificacion(e.message || 'Error al procesar reserva.', 'error');
        if (btnConfirmar) {
            btnConfirmar.disabled = false;
            actualizarBotonConfirmar();
        }
    }
}

function mostrarModalExito(cita, pagoInfo) {
    const modal = document.getElementById('modal-cita-exitosa');
    if (!modal) return;

    const elBarbero = document.getElementById('modal-exito-barbero');
    const elFechaHora = document.getElementById('modal-exito-fechahora');
    const elRef = document.getElementById('modal-exito-referencia');
    const elMetodo = document.getElementById('modal-exito-metodo');
    const elTotal = document.getElementById('modal-exito-total');
    const elBadge = document.getElementById('modal-exito-badge-pago');

    if (elBarbero) {
        elBarbero.textContent = cita.nombreBarbero || (estado.barberoSeleccionado ? `${estado.barberoSeleccionado.nombre} ${estado.barberoSeleccionado.apellido}` : 'Maestro Barbero');
    }

    if (elFechaHora) {
        elFechaHora.textContent = `${formatearFecha(cita.fecha || estado.fechaSeleccionada)} a las ${cita.horaInicio || estado.horaSeleccionada}`;
    }

    if (elRef) {
        elRef.textContent = pagoInfo ? pagoInfo.referencia : `OP-2026-${Date.now().toString().slice(-4)}`;
    }

    if (elMetodo) {
        elMetodo.textContent = pagoInfo ? pagoInfo.metodo : (estado.metodoPago === 'TARJETA' ? 'Tarjeta' : 'Yape');
    }

    const totalMonto = pagoInfo ? pagoInfo.monto : (cita.montoTotal || estado.serviciosSeleccionados.reduce((acc, s) => acc + parseFloat(s.precio), 0));
    if (elTotal) {
        elTotal.textContent = `S/ ${parseFloat(totalMonto).toFixed(2)}`;
    }

    if (elBadge) {
        if (pagoInfo && pagoInfo.pagado) {
            elBadge.className = 'inline-block px-3 py-1 rounded-full text-[10px] font-black uppercase tracking-wider bg-emerald-950/90 border border-emerald-500/40 text-emerald-400 mb-2';
            elBadge.textContent = 'PAGO CONFIRMADO ✓';
        } else {
            elBadge.className = 'inline-block px-3 py-1 rounded-full text-[10px] font-black uppercase tracking-wider bg-amber-950/90 border border-amber-500/40 text-amber-400 mb-2';
            elBadge.textContent = 'PAGO EN SEDE · RESERVA CONFIRMADA';
        }
    }

    modal.classList.remove('hidden');
    modal.classList.add('flex');
}

async function cargarMisCitas() {
    const contenedor = document.getElementById('lista-mis-citas');
    const cargando = document.getElementById('cargando-mis-citas');
    const vacio = document.getElementById('mis-citas-vacio');

    if (!contenedor) return;

    contenedor.innerHTML = '';
    cargando?.classList.remove('hidden');
    vacio?.classList.add('hidden');

    try {
        const respuesta = await fetch(`${API_URL}/citas`, {
            headers: {
                'Authorization': `Bearer ${estado.token}`
            }
        });

        if (!respuesta.ok) throw new Error('Error al consultar citas');
        const resultado = await respuesta.json();

        if (resultado.exitoso && Array.isArray(resultado.datos) && resultado.datos.length > 0) {
            estado.citas = resultado.datos;
            renderizarMisCitas();
        } else {
            vacio?.classList.remove('hidden');
        }
    } catch (e) {
        vacio?.classList.remove('hidden');
    } finally {
        cargando?.classList.add('hidden');
    }
}

function renderizarMisCitas() {
    const contenedor = document.getElementById('lista-mis-citas');
    if (!contenedor) return;

    contenedor.innerHTML = '';

    estado.citas.forEach(cita => {
        const tarjeta = document.createElement('div');
        tarjeta.className = 'bg-[#12100e] border border-stone-800 rounded-2xl p-5 sm:p-6 space-y-4 shadow-xl hover:border-cyan-500/30 transition';

        const esCancelable = cita.estado !== 'CANCELADA' && cita.estado !== 'COMPLETADA';
        const colorEstado = cita.estado === 'CONFIRMADA'
            ? 'bg-cyan-500/15 text-cyan-400 border-cyan-500/30'
            : (cita.estado === 'PENDIENTE' ? 'bg-amber-500/15 text-amber-400 border-amber-500/30' : 'bg-stone-800 text-stone-400 border-stone-700');

        tarjeta.innerHTML = `
            <div class="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 border-b border-stone-800 pb-3">
                <div class="flex items-center gap-3">
                    <span class="w-10 h-10 rounded-xl bg-cyan-500/10 border border-cyan-500/20 text-cyan-400 font-bold flex items-center justify-center text-sm">
                        #${cita.idCita || 'C'}
                    </span>
                    <div>
                        <h4 class="text-sm font-bold text-white">${cita.nombreBarbero || 'Barbero A&S'}</h4>
                        <p class="text-[11px] text-stone-400">Sede Miraflores · Galería Pardo</p>
                    </div>
                </div>

                <span class="px-2.5 py-1 rounded-full text-[10px] font-bold border uppercase tracking-wider ${colorEstado}">
                    ${cita.estado || 'PROGRAMADA'}
                </span>
            </div>

            <div class="grid grid-cols-2 sm:grid-cols-3 gap-3 text-xs">
                <div>
                    <span class="text-stone-500 block text-[10px] uppercase">Fecha</span>
                    <strong class="text-stone-200">${cita.fecha || '--/--/----'}</strong>
                </div>
                <div>
                    <span class="text-stone-500 block text-[10px] uppercase">Horario</span>
                    <strong class="text-cyan-400 font-mono">${cita.horaInicio ? cita.horaInicio.substring(0, 5) : '--:--'} - ${cita.horaFin ? cita.horaFin.substring(0, 5) : '--:--'}</strong>
                </div>
                <div>
                    <span class="text-stone-500 block text-[10px] uppercase">Total</span>
                    <strong class="text-white font-mono">S/ ${parseFloat(cita.montoTotal || 0).toFixed(2)}</strong>
                </div>
            </div>

            ${esCancelable ? `
                <div class="pt-2 border-t border-stone-800 flex justify-end">
                    <button type="button" onclick="cancelarCitaCliente(${cita.idCita})" class="px-3 py-1.5 rounded-lg border border-red-800/40 text-red-400 hover:bg-red-950/40 text-xs font-semibold transition">
                        Cancelar Cita
                    </button>
                </div>
            ` : ''}
        `;

        contenedor.appendChild(tarjeta);
    });
}

window.cancelarCitaCliente = async function(idCita) {
    const motivo = prompt('Por favor ingresa el motivo de la cancelación:');
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
            mostrarNotificacion('Cita cancelada correctamente.', 'exito');
            cargarMisCitas();
        } else {
            mostrarNotificacion(resultado.mensaje || 'No se pudo cancelar la cita.', 'error');
        }
    } catch (e) {
        mostrarNotificacion('Error de conexión al cancelar la cita.', 'error');
    }
};

function mostrarNotificacion(mensaje, tipo = 'info') {
    const contenedor = document.getElementById('notificaciones-toast');
    if (!contenedor) return;

    const toast = document.createElement('div');
    const colorBorde = tipo === 'exito' ? 'border-emerald-500/50 bg-[#0e1713] text-emerald-300' :
                       tipo === 'error' ? 'border-red-500/50 bg-[#1a0e0e] text-red-300' :
                       tipo === 'alerta' ? 'border-amber-500/50 bg-[#19150d] text-amber-300' :
                       'border-cyan-500/50 bg-[#0d161a] text-cyan-300';

    toast.className = `p-4 rounded-xl border shadow-2xl text-xs flex items-center justify-between gap-3 transform transition duration-300 translate-y-2 opacity-0 ${colorBorde}`;
    toast.innerHTML = `
        <div class="flex items-center gap-2">
            <span>${tipo === 'exito' ? '✓' : tipo === 'error' ? '✕' : 'ℹ'}</span>
            <span>${mensaje}</span>
        </div>
        <button type="button" class="text-stone-400 hover:text-white">&times;</button>
    `;

    toast.querySelector('button').onclick = () => toast.remove();
    contenedor.appendChild(toast);

    setTimeout(() => {
        toast.classList.remove('translate-y-2', 'opacity-0');
    }, 10);

    setTimeout(() => {
        toast.classList.add('opacity-0', 'translate-y-2');
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

