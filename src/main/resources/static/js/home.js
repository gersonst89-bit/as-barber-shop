function iniciarCabeceraSesion() {
    const token = api.getToken();
    const usuario = api.getUsuario();

    const contenedorAcciones = document.getElementById('contenedor-acciones-cabecera');
    const contenedorUsuario = document.getElementById('contenedor-usuario-activo');
    const nombreCabecera = document.getElementById('nombre-usuario-cabecera');
    const rolCabecera = document.getElementById('rol-usuario-cabecera');
    const avatarCabecera = document.getElementById('avatar-usuario-cabecera');
    const enlacePanel = document.getElementById('enlace-panel-usuario');
    const btnHero = document.getElementById('btn-reservar-hero');

    if (token && usuario) {
        if (contenedorAcciones) contenedorAcciones.classList.add('hidden');
        if (contenedorUsuario) {
            contenedorUsuario.classList.remove('hidden');
            contenedorUsuario.classList.add('flex');
        }

        if (nombreCabecera) nombreCabecera.textContent = `${usuario.nombre} ${usuario.apellido}`;
        if (rolCabecera) rolCabecera.textContent = usuario.rol;
        if (avatarCabecera && usuario.nombre) {
            avatarCabecera.textContent = usuario.nombre.charAt(0).toUpperCase();
        }

        const rutaPanel = usuario.rol === 'ADMIN' 
            ? '/pages/admin/dashboard.html'
            : (usuario.rol === 'BARBERO' ? '/pages/barbero/agenda.html' : '/pages/cliente/reservas.html');

        if (enlacePanel) enlacePanel.href = rutaPanel;
        if (btnHero) btnHero.href = rutaPanel;
    }

    document.getElementById('btn-cerrar-sesion-cabecera')?.addEventListener('click', () => {
        api.limpiarSesion();
        window.location.href = '/';
    });
}

async function cargarCatalogoServicios() {
    const grilla = document.getElementById('grilla-servicios');
    if (!grilla) return;

    try {
        const datos = await api.get('/api/servicios');
        if (!datos || !datos.exitoso || !datos.datos || datos.datos.length === 0) {
            grilla.innerHTML = `<div class="col-span-full text-center py-12 text-stone-500 text-xs">No hay servicios disponibles temporalmente.</div>`;
            return;
        }

        grilla.innerHTML = '';
        datos.datos.filter(s => s.activo !== false).forEach(servicio => {
            const tarjeta = document.createElement('div');
            tarjeta.className = 'bg-[#12100e] border border-stone-800 hover:border-cyan-500/50 rounded-2xl p-6 flex flex-col justify-between transition duration-200 shadow-xl group';
            tarjeta.innerHTML = `
                <div class="space-y-4">
                    <div class="flex items-center justify-between">
                        <span class="px-2.5 py-1 rounded-lg bg-cyan-500/10 border border-cyan-500/20 text-cyan-400 text-xs font-bold uppercase tracking-wider">
                            ${servicio.categoria || 'Servicio'}
                        </span>
                        <span class="text-xs text-stone-400 font-semibold flex items-center gap-1">
                            ⏱️ ${servicio.duracionMinutos} min
                        </span>
                    </div>
                    <div>
                        <h3 class="text-lg font-bold text-white group-hover:text-cyan-400 transition">${servicio.nombre}</h3>
                        <p class="text-xs text-stone-400 mt-2 leading-relaxed">${servicio.descripcion || 'Servicio clásico de alta barbería.'}</p>
                    </div>
                </div>
                <div class="pt-5 mt-5 border-t border-stone-800/80 flex items-center justify-between">
                    <div>
                        <span class="text-[10px] text-stone-400 uppercase tracking-wider block">Precio</span>
                        <span class="text-xl font-black text-cyan-400 font-mono">${ui.soles(servicio.precio)}</span>
                    </div>
                    <a href="/pages/cliente/reservas.html" class="px-4 py-2 rounded-xl bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 text-stone-950 font-black text-xs uppercase tracking-wider transition">
                        Reservar Turno
                    </a>
                </div>
            `;
            grilla.appendChild(tarjeta);
        });
    } catch (e) {
        grilla.innerHTML = `<div class="col-span-full text-center py-12 text-stone-500 text-xs">Error al sincronizar tarifas con la base de datos.</div>`;
    }
}

async function cargarBarberos() {
    const grilla = document.getElementById('grilla-barberos');
    if (!grilla) return;

    try {
        const datos = await api.get('/api/barberos');
        if (!datos || !datos.exitoso || !datos.datos || datos.datos.length === 0) {
            grilla.innerHTML = `<div class="col-span-full text-center py-12 text-stone-500 text-xs">No hay barberos asignados en este momento.</div>`;
            return;
        }

        grilla.innerHTML = '';
        datos.datos.filter(b => b.disponible !== false).forEach(barbero => {
            const iniciales = (barbero.nombre ? barbero.nombre.charAt(0) : 'B') + (barbero.apellido ? barbero.apellido.charAt(0) : '');
            const tarjeta = document.createElement('div');
            tarjeta.className = 'bg-[#12100e] border border-stone-800 hover:border-cyan-500/40 rounded-2xl p-6 text-center space-y-4 transition duration-200 shadow-xl';
            tarjeta.innerHTML = `
                <div class="w-20 h-20 mx-auto rounded-full bg-gradient-to-tr from-cyan-500 to-blue-700 p-0.5 shadow-xl shadow-cyan-950/40">
                    <div class="w-full h-full rounded-full bg-[#171513] flex items-center justify-center text-2xl font-black text-cyan-400">
                        ${iniciales}
                    </div>
                </div>
                <div>
                    <h3 class="text-base font-bold text-white">${barbero.nombre} ${barbero.apellido}</h3>
                    <p class="text-xs text-cyan-400 font-bold uppercase tracking-wider mt-0.5">${barbero.especialidad || 'Estilista Profesional'}</p>
                    <p class="text-xs text-stone-400 mt-2 leading-relaxed">Maestro barbero certificado de la sede Miraflores.</p>
                </div>
                <div class="pt-2 border-t border-stone-800">
                    <span class="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-[11px] font-semibold bg-emerald-950/80 text-emerald-400 border border-emerald-600/40">
                        <span class="w-1.5 h-1.5 rounded-full bg-emerald-400"></span> ${barbero.disponible ? 'Disponible hoy' : 'En atención'}
                    </span>
                </div>
            `;
            grilla.appendChild(tarjeta);
        });
    } catch (e) {
        grilla.innerHTML = `<div class="col-span-full text-center py-12 text-stone-500 text-xs">No se pudo cargar el equipo de barberos.</div>`;
    }
}

function cambiarVistaUbicacion(vista) {
    const mapa = document.getElementById('contenedor-mapa');
    const fachada = document.getElementById('contenedor-fachada');
    const btnMapa = document.getElementById('btn-vista-mapa');
    const btnFachada = document.getElementById('btn-vista-fachada');

    if (vista === 'mapa') {
        if (mapa) mapa.classList.remove('hidden');
        if (fachada) fachada.classList.add('hidden');
        if (btnMapa) {
            btnMapa.className = 'px-3.5 py-1.5 rounded-lg text-xs font-bold transition duration-200 bg-cyan-500/20 text-cyan-300 border border-cyan-500/40 flex items-center gap-1.5 shadow-sm';
        }
        if (btnFachada) {
            btnFachada.className = 'px-3.5 py-1.5 rounded-lg text-xs font-bold transition duration-200 text-stone-400 hover:text-stone-200 border border-transparent flex items-center gap-1.5';
        }
    } else {
        if (mapa) mapa.classList.add('hidden');
        if (fachada) fachada.classList.remove('hidden');
        if (btnFachada) {
            btnFachada.className = 'px-3.5 py-1.5 rounded-lg text-xs font-bold transition duration-200 bg-cyan-500/20 text-cyan-300 border border-cyan-500/40 flex items-center gap-1.5 shadow-sm';
        }
        if (btnMapa) {
            btnMapa.className = 'px-3.5 py-1.5 rounded-lg text-xs font-bold transition duration-200 text-stone-400 hover:text-stone-200 border border-transparent flex items-center gap-1.5';
        }
    }
}

document.addEventListener('DOMContentLoaded', () => {
    iniciarCabeceraSesion();
    cargarCatalogoServicios();
    cargarBarberos();
});
