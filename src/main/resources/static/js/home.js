const token = localStorage.getItem('as_token') || sessionStorage.getItem('as_token');
const usuarioGuardado = localStorage.getItem('as_usuario') || sessionStorage.getItem('as_usuario');

const contenedorAcciones = document.getElementById('contenedor-acciones-cabecera');
const contenedorUsuario = document.getElementById('contenedor-usuario-activo');
const nombreCabecera = document.getElementById('nombre-usuario-cabecera');
const rolCabecera = document.getElementById('rol-usuario-cabecera');
const avatarCabecera = document.getElementById('avatar-usuario-cabecera');
const enlacePanel = document.getElementById('enlace-panel-usuario');
const btnHero = document.getElementById('btn-reservar-hero');

if (token && usuarioGuardado) {
    try {
        const usuario = JSON.parse(usuarioGuardado);
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

        if (usuario.rol === 'ADMIN') {
            if (enlacePanel) enlacePanel.href = '/pages/admin/dashboard.html';
            if (btnHero) btnHero.href = '/pages/admin/dashboard.html';
        } else if (usuario.rol === 'BARBERO') {
            if (enlacePanel) enlacePanel.href = '/pages/barbero/agenda.html';
            if (btnHero) btnHero.href = '/pages/barbero/agenda.html';
        } else {
            if (enlacePanel) enlacePanel.href = '/pages/cliente/reservas.html';
            if (btnHero) btnHero.href = '/pages/cliente/reservas.html';
        }
    } catch (e) {
        localStorage.removeItem('as_token');
        localStorage.removeItem('as_usuario');
    }
}

document.getElementById('btn-cerrar-sesion-cabecera')?.addEventListener('click', () => {
    localStorage.removeItem('as_token');
    localStorage.removeItem('as_usuario');
    sessionStorage.removeItem('as_token');
    sessionStorage.removeItem('as_usuario');
    window.location.href = '/';
});

async function cargarCatalogoServicios() {
    try {
        const respuesta = await fetch('/api/servicios');
        if (!respuesta.ok) return;
        const datos = await respuesta.json();
        if (!datos.exitoso || !datos.datos || datos.datos.length === 0) return;

        const grilla = document.getElementById('grilla-servicios');
        if (!grilla) return;

        grilla.innerHTML = '';
        datos.datos.forEach(servicio => {
            const tarjeta = document.createElement('div');
            tarjeta.className = 'bg-[#12100e] border border-stone-800 hover:border-cyan-500/50 rounded-2xl p-6 flex flex-col justify-between transition duration-200 shadow-xl group';
            tarjeta.innerHTML = `
                <div class="space-y-4">
                    <div class="flex items-center justify-between">
                        <span class="px-2.5 py-1 rounded-lg bg-cyan-500/10 border border-cyan-500/20 text-cyan-400 text-xs font-bold uppercase tracking-wider">
                            Servicio
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
                        <span class="text-xl font-black text-cyan-400 font-mono">S/ ${parseFloat(servicio.precio).toFixed(2)}</span>
                    </div>
                    <a href="/pages/cliente/reservas.html" class="px-4 py-2 rounded-xl bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 text-stone-950 font-black text-xs uppercase tracking-wider transition">
                        Reservar Turno
                    </a>
                </div>
            `;
            grilla.appendChild(tarjeta);
        });
    } catch (e) {}
}

async function cargarBarberos() {
    try {
        const respuesta = await fetch('/api/barberos');
        if (!respuesta.ok) return;
        const datos = await respuesta.json();
        if (!datos.exitoso || !datos.datos || datos.datos.length === 0) return;

        const grilla = document.getElementById('grilla-barberos');
        if (!grilla) return;

        grilla.innerHTML = '';
        datos.datos.forEach(barbero => {
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
    } catch (e) {}
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
    cargarCatalogoServicios();
    cargarBarberos();
});
