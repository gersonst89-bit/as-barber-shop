const RUTA_API = '/api';

const token = localStorage.getItem('as_token') || sessionStorage.getItem('as_token');
const usuarioGuardado = localStorage.getItem('as_usuario') || sessionStorage.getItem('as_usuario');

let listaServiciosGlobal = [];
let idServicioEnEdicion = null;

function verificarSesionAdmin() {
    if (!token || !usuarioGuardado) {
        window.location.href = '/pages/login.html';
        return false;
    }

    try {
        const usuario = JSON.parse(usuarioGuardado);
        if (usuario.rol !== 'ADMIN') {
            window.location.href = '/pages/login.html';
            return false;
        }

        const elementoNombreAdmin = document.getElementById('nombre-admin-cabecera');
        if (elementoNombreAdmin) {
            elementoNombreAdmin.textContent = `${usuario.nombre} ${usuario.apellido}`;
        }
        return true;
    } catch (e) {
        window.location.href = '/pages/login.html';
        return false;
    }
}

function mostrarAviso(mensaje, tipo = 'error') {
    const contenedor = document.getElementById('contenedor-avisos');
    if (!contenedor) return;

    const aviso = document.createElement('div');
    const fondo = tipo === 'success'
        ? 'bg-emerald-950/95 border-emerald-500/60 text-emerald-100'
        : 'bg-red-950/95 border-red-500/60 text-red-100';

    const icono = tipo === 'success'
        ? '<svg class="w-5 h-5 text-emerald-400 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>'
        : '<svg class="w-5 h-5 text-red-400 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path></svg>';

    aviso.className = `flex items-center gap-3 px-4 py-3 rounded-xl border backdrop-blur-md shadow-2xl text-sm transition-all duration-300 transform translate-y-2 opacity-0 ${fondo}`;
    aviso.innerHTML = `${icono}<span>${mensaje}</span>`;

    contenedor.appendChild(aviso);

    requestAnimationFrame(() => {
        aviso.classList.remove('translate-y-2', 'opacity-0');
    });

    setTimeout(() => {
        aviso.classList.add('opacity-0', '-translate-y-2');
        setTimeout(() => aviso.remove(), 300);
    }, 4000);
}

async function cargarServicios() {
    try {
        const respuesta = await fetch(`${RUTA_API}/admin/servicios`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (respuesta.status === 401 || respuesta.status === 403) {
            cerrarSesion();
            return;
        }

        const resultado = await respuesta.json();

        if (!respuesta.ok || !resultado.exitoso) {
            throw new Error(resultado.mensaje || 'Error al obtener servicios');
        }

        listaServiciosGlobal = resultado.datos || [];
        actualizarMetricas(listaServiciosGlobal);
        aplicarFiltros();

    } catch (error) {
        mostrarAviso(error.message);
    }
}

function actualizarMetricas(servicios) {
    const total = servicios.length;
    const activos = servicios.filter(s => s.activo).length;

    let promedio = 0;
    if (servicios.length > 0) {
        const suma = servicios.reduce((acc, s) => acc + (parseFloat(s.precio) || 0), 0);
        promedio = (suma / servicios.length).toFixed(2);
    }

    const elTotal = document.getElementById('metrica-total-servicios');
    const elActivos = document.getElementById('metrica-servicios-activos');
    const elPromedio = document.getElementById('metrica-precio-promedio');

    if (elTotal) elTotal.textContent = total;
    if (elActivos) elActivos.textContent = activos;
    if (elPromedio) elPromedio.textContent = `S/ ${promedio}`;
}

function aplicarFiltros() {
    const termino = (document.getElementById('campo-busqueda')?.value || '').toLowerCase().trim();
    const filtroEstado = document.getElementById('filtro-estado')?.value || 'TODOS';

    const filtrados = listaServiciosGlobal.filter(servicio => {
        const coincideTermino = !termino || 
            servicio.nombre.toLowerCase().includes(termino) || 
            (servicio.descripcion && servicio.descripcion.toLowerCase().includes(termino));

        let coincideEstado = true;
        if (filtroEstado === 'ACTIVOS') coincideEstado = servicio.activo === true;
        if (filtroEstado === 'INACTIVOS') coincideEstado = servicio.activo === false;

        return coincideTermino && coincideEstado;
    });

    renderizarTabla(filtrados);
}

function renderizarTabla(servicios) {
    const cuerpo = document.getElementById('cuerpo-tabla-servicios');
    if (!cuerpo) return;

    cuerpo.innerHTML = '';

    if (servicios.length === 0) {
        cuerpo.innerHTML = `
            <tr>
                <td colspan="5" class="text-center py-12 text-stone-400 text-sm">
                    No hay servicios que coincidan con la búsqueda.
                </td>
            </tr>
        `;
        return;
    }

    servicios.forEach(servicio => {
        const fila = document.createElement('tr');
        fila.className = 'border-b border-stone-800/60 hover:bg-stone-900/40 transition duration-150';

        const badgeEstado = servicio.activo
            ? `<span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[11px] font-semibold bg-emerald-950/80 text-emerald-400 border border-emerald-600/40">
                 <span class="w-1.5 h-1.5 rounded-full bg-emerald-400"></span> Activo
               </span>`
            : `<span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[11px] font-semibold bg-stone-900 text-stone-400 border border-stone-700/50">
                 <span class="w-1.5 h-1.5 rounded-full bg-stone-500"></span> Inactivo
               </span>`;

        fila.innerHTML = `
            <td class="py-3.5 px-4">
                <div class="flex items-center gap-3">
                    <div class="w-9 h-9 rounded-xl bg-amber-500/10 border border-amber-500/30 flex items-center justify-center text-amber-400 shrink-0 font-bold">
                        ✂️
                    </div>
                    <div>
                        <p class="text-sm font-semibold text-stone-100">${servicio.nombre}</p>
                        <p class="text-xs text-stone-400 line-clamp-1">${servicio.descripcion || 'Sin descripción'}</p>
                    </div>
                </div>
            </td>
            <td class="py-3.5 px-4">
                <span class="inline-flex items-center gap-1 text-xs text-stone-300 font-medium">
                    <svg class="w-3.5 h-3.5 text-amber-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                    </svg>
                    ${servicio.duracionMinutos} min
                </span>
            </td>
            <td class="py-3.5 px-4">
                <span class="text-sm font-bold text-amber-400 font-mono">
                    S/ ${parseFloat(servicio.precio).toFixed(2)}
                </span>
            </td>
            <td class="py-3.5 px-4">
                ${badgeEstado}
            </td>
            <td class="py-3.5 px-4 text-right">
                <div class="flex items-center justify-end gap-1.5">
                    <button 
                        type="button" 
                        onclick="abrirModalEditarServicio(${servicio.idServicio})"
                        title="Editar servicio"
                        class="p-1.5 rounded-lg border border-stone-700/60 text-stone-300 hover:text-amber-400 hover:bg-stone-800 text-xs transition duration-150">
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path>
                        </svg>
                    </button>
                    <button 
                        type="button" 
                        onclick="alternarEstadoServicio(${servicio.idServicio}, ${!servicio.activo})"
                        title="${servicio.activo ? 'Desactivar servicio' : 'Activar servicio'}"
                        class="p-1.5 rounded-lg border text-xs transition duration-150 ${servicio.activo ? 'border-amber-600/40 text-amber-400 hover:bg-amber-950/40' : 'border-emerald-600/40 text-emerald-400 hover:bg-emerald-950/40'}">
                        ${servicio.activo 
                            ? '<svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728A9 9 0 015.636 5.636m12.728 12.728L5.636 5.636"></path></svg>'
                            : '<svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>'
                        }
                    </button>
                    <button 
                        type="button" 
                        onclick="eliminarServicio(${servicio.idServicio}, '${servicio.nombre}')"
                        title="Eliminar servicio"
                        class="p-1.5 rounded-lg border border-red-700/40 text-red-400 hover:bg-red-950/40 text-xs transition duration-150">
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
                        </svg>
                    </button>
                </div>
            </td>
        `;

        cuerpo.appendChild(fila);
    });
}

function abrirModalCrearServicio() {
    idServicioEnEdicion = null;
    const modal = document.getElementById('modal-servicio');
    const titulo = document.getElementById('titulo-modal-servicio');
    const formulario = document.getElementById('formulario-servicio');

    if (formulario) formulario.reset();
    if (titulo) titulo.textContent = 'Registrar Nuevo Servicio';

    if (modal) {
        modal.classList.remove('hidden');
        modal.classList.add('flex');
    }
}

function abrirModalEditarServicio(idServicio) {
    const servicio = listaServiciosGlobal.find(s => s.idServicio === idServicio);
    if (!servicio) return;

    idServicioEnEdicion = idServicio;

    const modal = document.getElementById('modal-servicio');
    const titulo = document.getElementById('titulo-modal-servicio');

    document.getElementById('servicio-nombre').value = servicio.nombre;
    document.getElementById('servicio-precio').value = servicio.precio;
    document.getElementById('servicio-duracion').value = servicio.duracionMinutos;
    document.getElementById('servicio-descripcion').value = servicio.descripcion || '';

    if (titulo) titulo.textContent = 'Editar Servicio';

    if (modal) {
        modal.classList.remove('hidden');
        modal.classList.add('flex');
    }
}

function cerrarModalServicio() {
    const modal = document.getElementById('modal-servicio');
    if (modal) {
        modal.classList.add('hidden');
        modal.classList.remove('flex');
    }
    idServicioEnEdicion = null;
}

async function guardarServicio(evento) {
    evento.preventDefault();
    const botonGuardar = document.getElementById('btn-guardar-servicio');
    const textoOriginal = botonGuardar.innerHTML;

    const nombre = document.getElementById('servicio-nombre').value.trim();
    const precio = parseFloat(document.getElementById('servicio-precio').value);
    const duracionMinutos = parseInt(document.getElementById('servicio-duracion').value, 10);
    const descripcion = document.getElementById('servicio-descripcion').value.trim();

    if (!nombre || isNaN(precio) || isNaN(duracionMinutos)) {
        mostrarAviso('Por favor completa todos los campos requeridos.');
        return;
    }

    if (precio < 0) {
        mostrarAviso('El precio no puede ser negativo.');
        return;
    }

    if (duracionMinutos <= 0) {
        mostrarAviso('La duración debe ser de al menos 1 minuto.');
        return;
    }

    const cuerpoPeticion = {
        nombre,
        precio,
        duracionMinutos,
        descripcion,
        activo: true
    };

    const url = idServicioEnEdicion 
        ? `${RUTA_API}/admin/servicios/${idServicioEnEdicion}` 
        : `${RUTA_API}/admin/servicios`;
    const metodo = idServicioEnEdicion ? 'PUT' : 'POST';

    try {
        botonGuardar.disabled = true;
        botonGuardar.innerHTML = '<span>Guardando...</span>';

        const respuesta = await fetch(url, {
            method: metodo,
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(cuerpoPeticion)
        });

        const respuestaJson = await respuesta.json();

        if (!respuesta.ok || !respuestaJson.exitoso) {
            throw new Error(respuestaJson.mensaje || 'Error al guardar el servicio.');
        }

        const accionMsg = idServicioEnEdicion ? 'actualizado' : 'creado';
        mostrarAviso(`Servicio "${nombre}" ${accionMsg} exitosamente.`, 'success');
        cerrarModalServicio();
        await cargarServicios();

    } catch (error) {
        mostrarAviso(error.message);
    } finally {
        botonGuardar.disabled = false;
        botonGuardar.innerHTML = textoOriginal;
    }
}

async function alternarEstadoServicio(idServicio, nuevoEstado) {
    try {
        const respuesta = await fetch(`${RUTA_API}/admin/servicios/${idServicio}/estado?activo=${nuevoEstado}`, {
            method: 'PATCH',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        const respuestaJson = await respuesta.json();

        if (!respuesta.ok || !respuestaJson.exitoso) {
            throw new Error(respuestaJson.mensaje || 'Error al cambiar estado.');
        }

        mostrarAviso(respuestaJson.mensaje || 'Estado actualizado.', 'success');
        await cargarServicios();

    } catch (error) {
        mostrarAviso(error.message);
    }
}

async function eliminarServicio(idServicio, nombreServicio) {
    const confirmado = confirm(`¿Estás seguro de que deseas eliminar el servicio "${nombreServicio}"? Esta acción no se puede deshacer.`);
    if (!confirmado) return;

    try {
        const respuesta = await fetch(`${RUTA_API}/admin/servicios/${idServicio}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        const respuestaJson = await respuesta.json();

        if (!respuesta.ok || !respuestaJson.exitoso) {
            throw new Error(respuestaJson.mensaje || 'Error al eliminar el servicio.');
        }

        mostrarAviso('Servicio eliminado exitosamente.', 'success');
        await cargarServicios();

    } catch (error) {
        mostrarAviso(error.message);
    }
}

function cerrarSesion() {
    localStorage.removeItem('as_token');
    localStorage.removeItem('as_usuario');
    sessionStorage.removeItem('as_token');
    sessionStorage.removeItem('as_usuario');
    window.location.href = '/';
}

document.addEventListener('DOMContentLoaded', () => {
    if (!verificarSesionAdmin()) return;

    cargarServicios();

    const campoBusqueda = document.getElementById('campo-busqueda');
    if (campoBusqueda) {
        campoBusqueda.addEventListener('input', aplicarFiltros);
    }

    const filtroEstado = document.getElementById('filtro-estado');
    if (filtroEstado) {
        filtroEstado.addEventListener('change', aplicarFiltros);
    }

    const formulario = document.getElementById('formulario-servicio');
    if (formulario) {
        formulario.addEventListener('submit', guardarServicio);
    }

    const botonCerrarSesion = document.getElementById('btn-cerrar-sesion');
    if (botonCerrarSesion) {
        botonCerrarSesion.addEventListener('click', cerrarSesion);
    }
});

