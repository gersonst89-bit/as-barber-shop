const RUTA_API = '/api';

const token = localStorage.getItem('as_token') || sessionStorage.getItem('as_token');
const usuarioGuardado = localStorage.getItem('as_usuario') || sessionStorage.getItem('as_usuario');

let listaUsuariosGlobal = [];
let idUsuarioEnEdicion = null;

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

async function cargarUsuarios() {
    try {
        const respuesta = await fetch(`${RUTA_API}/admin/usuarios`, {
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
            throw new Error(resultado.mensaje || 'Error al obtener usuarios');
        }

        listaUsuariosGlobal = resultado.datos || [];
        actualizarMetricas(listaUsuariosGlobal);
        aplicarFiltros();

    } catch (error) {
        mostrarAviso(error.message);
    }
}

function actualizarMetricas(usuarios) {
    const total = usuarios.length;
    const barberos = usuarios.filter(u => u.rol === 'BARBERO' && u.activo).length;
    const clientes = usuarios.filter(u => u.rol === 'CLIENTE').length;

    const elTotal = document.getElementById('metrica-total-usuarios');
    const elBarberos = document.getElementById('metrica-barberos-activos');
    const elClientes = document.getElementById('metrica-clientes-registrados');

    if (elTotal) elTotal.textContent = total;
    if (elBarberos) elBarberos.textContent = barberos;
    if (elClientes) elClientes.textContent = clientes;
}

function aplicarFiltros() {
    const termino = (document.getElementById('campo-busqueda')?.value || '').toLowerCase().trim();
    const filtroRol = document.getElementById('filtro-rol')?.value || 'TODOS';
    const filtroEstado = document.getElementById('filtro-estado')?.value || 'TODOS';

    const filtrados = listaUsuariosGlobal.filter(usuario => {
        const nombreCompleto = `${usuario.nombre} ${usuario.apellido}`.toLowerCase();
        const coincideTermino = !termino || 
            nombreCompleto.includes(termino) || 
            (usuario.email && usuario.email.toLowerCase().includes(termino)) ||
            (usuario.telefono && usuario.telefono.includes(termino));

        const coincideRol = filtroRol === 'TODOS' || usuario.rol === filtroRol;

        let coincideEstado = true;
        if (filtroEstado === 'ACTIVOS') coincideEstado = usuario.activo === true;
        if (filtroEstado === 'INACTIVOS') coincideEstado = usuario.activo === false;

        return coincideTermino && coincideRol && coincideEstado;
    });

    renderizarTabla(filtrados);
}

function renderizarTabla(usuarios) {
    const cuerpo = document.getElementById('cuerpo-tabla-usuarios');
    if (!cuerpo) return;

    cuerpo.innerHTML = '';

    if (usuarios.length === 0) {
        cuerpo.innerHTML = `
            <tr>
                <td colspan="6" class="text-center py-12 text-stone-400 text-sm">
                    No se encontraron usuarios que coincidan con la búsqueda o filtros.
                </td>
            </tr>
        `;
        return;
    }

    usuarios.forEach(usuario => {
        const fila = document.createElement('tr');
        fila.className = 'border-b border-stone-800/60 hover:bg-stone-900/40 transition duration-150';

        let colorRol = 'bg-amber-500/15 text-amber-400 border-amber-500/30';
        if (usuario.rol === 'BARBERO') colorRol = 'bg-blue-500/15 text-blue-400 border-blue-500/30';
        if (usuario.rol === 'CLIENTE') colorRol = 'bg-emerald-500/15 text-emerald-400 border-emerald-500/30';

        let detalleRol = '<span class="text-stone-500 text-xs">—</span>';
        if (usuario.rol === 'BARBERO') {
            detalleRol = `
                <div class="text-xs">
                    <span class="text-stone-200 font-medium">${usuario.especialidad || 'General'}</span>
                    <span class="block text-[11px] text-amber-400">Comisión: ${usuario.porcentajeComision || 0}%</span>
                </div>
            `;
        } else if (usuario.rol === 'CLIENTE' && usuario.preferencias) {
            detalleRol = `<span class="text-xs text-stone-400 italic">${usuario.preferencias}</span>`;
        }

        const badgeEstado = usuario.activo
            ? `<span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[11px] font-semibold bg-emerald-950/80 text-emerald-400 border border-emerald-600/40">
                 <span class="w-1.5 h-1.5 rounded-full bg-emerald-400"></span> Activo
               </span>`
            : `<span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[11px] font-semibold bg-stone-900 text-stone-400 border border-stone-700/50">
                 <span class="w-1.5 h-1.5 rounded-full bg-stone-500"></span> Inactivo
               </span>`;

        fila.innerHTML = `
            <td class="py-3.5 px-4">
                <div class="flex items-center gap-3">
                    <div class="w-9 h-9 rounded-full bg-gradient-to-br from-amber-600 to-amber-900 flex items-center justify-center text-xs font-bold text-black shadow-md shrink-0">
                        ${usuario.nombre ? usuario.nombre.charAt(0).toUpperCase() : 'U'}
                    </div>
                    <div>
                        <p class="text-sm font-semibold text-stone-100">${usuario.nombre} ${usuario.apellido}</p>
                        <p class="text-xs text-stone-400">${usuario.email}</p>
                    </div>
                </div>
            </td>
            <td class="py-3.5 px-4 text-xs text-stone-300 font-mono">
                ${usuario.telefono || 'Sin teléfono'}
            </td>
            <td class="py-3.5 px-4">
                <span class="inline-block px-2.5 py-0.5 rounded-md text-[11px] font-bold border tracking-wider ${colorRol}">
                    ${usuario.rol}
                </span>
            </td>
            <td class="py-3.5 px-4">
                ${detalleRol}
            </td>
            <td class="py-3.5 px-4">
                ${badgeEstado}
            </td>
            <td class="py-3.5 px-4 text-right">
                <div class="flex items-center justify-end gap-1.5">
                    <button 
                        type="button" 
                        onclick="abrirModalEditarUsuario(${usuario.idUsuario})"
                        title="Editar usuario"
                        class="p-1.5 rounded-lg border border-stone-700/60 text-stone-300 hover:text-amber-400 hover:bg-stone-800 text-xs transition duration-150">
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path>
                        </svg>
                    </button>
                    <button 
                        type="button" 
                        onclick="alternarEstadoUsuario(${usuario.idUsuario}, ${!usuario.activo})"
                        title="${usuario.activo ? 'Desactivar usuario' : 'Activar usuario'}"
                        class="p-1.5 rounded-lg border text-xs transition duration-150 ${usuario.activo ? 'border-amber-600/40 text-amber-400 hover:bg-amber-950/40' : 'border-emerald-600/40 text-emerald-400 hover:bg-emerald-950/40'}">
                        ${usuario.activo 
                            ? '<svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728A9 9 0 015.636 5.636m12.728 12.728L5.636 5.636"></path></svg>'
                            : '<svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>'
                        }
                    </button>
                    <button 
                        type="button" 
                        onclick="eliminarUsuario(${usuario.idUsuario}, '${usuario.nombre} ${usuario.apellido}')"
                        title="Eliminar usuario"
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

function abrirModalCrearUsuario() {
    idUsuarioEnEdicion = null;
    const modal = document.getElementById('modal-usuario');
    const titulo = document.getElementById('titulo-modal-usuario');
    const formulario = document.getElementById('formulario-crear-usuario');
    const campoCorreo = document.getElementById('crear-correo');
    const bloqueClave = document.getElementById('bloque-clave');
    const campoClave = document.getElementById('crear-clave');
    const botonGuardar = document.getElementById('btn-guardar-usuario');

    if (formulario) formulario.reset();
    if (titulo) titulo.textContent = 'Registrar Nuevo Usuario';
    if (campoCorreo) campoCorreo.disabled = false;
    if (bloqueClave) bloqueClave.classList.remove('hidden');
    if (campoClave) campoClave.required = true;
    if (botonGuardar) botonGuardar.textContent = 'Guardar Usuario';

    actualizarCamposPorRol();

    if (modal) {
        modal.classList.remove('hidden');
        modal.classList.add('flex');
    }
}

function abrirModalEditarUsuario(idUsuario) {
    const usuario = listaUsuariosGlobal.find(u => u.idUsuario === idUsuario);
    if (!usuario) return;

    idUsuarioEnEdicion = idUsuario;

    const modal = document.getElementById('modal-usuario');
    const titulo = document.getElementById('titulo-modal-usuario');
    const campoNombre = document.getElementById('crear-nombre');
    const campoApellido = document.getElementById('crear-apellido');
    const campoCorreo = document.getElementById('crear-correo');
    const campoTelefono = document.getElementById('crear-telefono');
    const campoRol = document.getElementById('crear-rol');
    const bloqueClave = document.getElementById('bloque-clave');
    const campoClave = document.getElementById('crear-clave');
    const botonGuardar = document.getElementById('btn-guardar-usuario');

    if (titulo) titulo.textContent = 'Editar Usuario';
    if (campoNombre) campoNombre.value = usuario.nombre || '';
    if (campoApellido) campoApellido.value = usuario.apellido || '';
    if (campoCorreo) {
        campoCorreo.value = usuario.email || '';
        campoCorreo.disabled = true;
    }
    if (campoTelefono) campoTelefono.value = usuario.telefono || '';
    if (campoRol) campoRol.value = usuario.rol || 'BARBERO';

    if (bloqueClave) bloqueClave.classList.add('hidden');
    if (campoClave) campoClave.required = false;
    if (botonGuardar) botonGuardar.textContent = 'Actualizar Usuario';

    actualizarCamposPorRol();

    if (usuario.rol === 'BARBERO') {
        const campoEsp = document.getElementById('crear-especialidad');
        const campoCom = document.getElementById('crear-comision');
        if (campoEsp) campoEsp.value = usuario.especialidad || '';
        if (campoCom) campoCom.value = usuario.porcentajeComision || 0;
    } else if (usuario.rol === 'CLIENTE') {
        const campoPref = document.getElementById('crear-preferencias');
        if (campoPref) campoPref.value = usuario.preferencias || '';
    }

    if (modal) {
        modal.classList.remove('hidden');
        modal.classList.add('flex');
    }
}

function cerrarModalCrearUsuario() {
    const modal = document.getElementById('modal-usuario');
    const campoCorreo = document.getElementById('crear-correo');
    const bloqueClave = document.getElementById('bloque-clave');
    const campoClave = document.getElementById('crear-clave');

    if (modal) {
        modal.classList.add('hidden');
        modal.classList.remove('flex');
    }
    if (campoCorreo) campoCorreo.disabled = false;
    if (bloqueClave) bloqueClave.classList.remove('hidden');
    if (campoClave) campoClave.required = true;

    idUsuarioEnEdicion = null;
}

function actualizarCamposPorRol() {
    const rolSeleccionado = document.getElementById('crear-rol')?.value || 'BARBERO';
    const bloqueBarbero = document.getElementById('bloque-campos-barbero');
    const bloqueCliente = document.getElementById('bloque-campos-cliente');

    if (bloqueBarbero) {
        if (rolSeleccionado === 'BARBERO') {
            bloqueBarbero.classList.remove('hidden');
        } else {
            bloqueBarbero.classList.add('hidden');
        }
    }

    if (bloqueCliente) {
        if (rolSeleccionado === 'CLIENTE') {
            bloqueCliente.classList.remove('hidden');
        } else {
            bloqueCliente.classList.add('hidden');
        }
    }
}

async function guardarNuevoUsuario(evento) {
    evento.preventDefault();
    const botonGuardar = document.getElementById('btn-guardar-usuario');
    const textoOriginal = botonGuardar.innerHTML;

    const nombre = document.getElementById('crear-nombre').value.trim();
    const apellido = document.getElementById('crear-apellido').value.trim();
    const email = document.getElementById('crear-correo').value.trim();
    const telefono = document.getElementById('crear-telefono').value.trim();
    const rol = document.getElementById('crear-rol').value;

    if (!nombre || !apellido || !rol) {
        mostrarAviso('Por favor completa los nombres, apellidos y rol.');
        return;
    }

    if (!idUsuarioEnEdicion) {
        const password = document.getElementById('crear-clave').value;
        if (!email || !password) {
            mostrarAviso('Por favor completa el correo y contraseña.');
            return;
        }
        if (password.length < 6) {
            mostrarAviso('La contraseña provisional debe tener al menos 6 caracteres.');
            return;
        }
    }

    const cuerpoPeticion = {
        nombre,
        apellido,
        telefono,
        rol
    };

    if (rol === 'BARBERO') {
        const especialidad = document.getElementById('crear-especialidad').value.trim();
        const comision = document.getElementById('crear-comision').value;
        cuerpoPeticion.especialidad = especialidad || 'Estilista General';
        cuerpoPeticion.porcentajeComision = comision ? parseFloat(comision) : 0;
    } else if (rol === 'CLIENTE') {
        const preferencias = document.getElementById('crear-preferencias').value.trim();
        cuerpoPeticion.preferencias = preferencias || '';
    }

    const url = idUsuarioEnEdicion 
        ? `${RUTA_API}/admin/usuarios/${idUsuarioEnEdicion}` 
        : `${RUTA_API}/admin/usuarios`;
    const metodo = idUsuarioEnEdicion ? 'PUT' : 'POST';

    if (!idUsuarioEnEdicion) {
        cuerpoPeticion.email = email;
        cuerpoPeticion.password = document.getElementById('crear-clave').value;
    }

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
            throw new Error(respuestaJson.mensaje || 'Error al guardar el usuario.');
        }

        const mensajeAccion = idUsuarioEnEdicion ? 'actualizado' : 'creado';
        mostrarAviso(`Usuario ${nombre} ${apellido} ${mensajeAccion} exitosamente.`, 'success');
        cerrarModalCrearUsuario();
        await cargarUsuarios();

    } catch (error) {
        mostrarAviso(error.message);
    } finally {
        botonGuardar.disabled = false;
        botonGuardar.innerHTML = textoOriginal;
    }
}

async function alternarEstadoUsuario(idUsuario, nuevoEstado) {
    try {
        const respuesta = await fetch(`${RUTA_API}/admin/usuarios/${idUsuario}/estado?activo=${nuevoEstado}`, {
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
        await cargarUsuarios();

    } catch (error) {
        mostrarAviso(error.message);
    }
}

async function eliminarUsuario(idUsuario, nombreCompleto) {
    const confirmado = confirm(`¿Estás seguro de que deseas eliminar a ${nombreCompleto}? Esta acción no se puede deshacer.`);
    if (!confirmado) return;

    try {
        const respuesta = await fetch(`${RUTA_API}/admin/usuarios/${idUsuario}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        const respuestaJson = await respuesta.json();

        if (!respuesta.ok || !respuestaJson.exitoso) {
            throw new Error(respuestaJson.mensaje || 'Error al eliminar usuario.');
        }

        mostrarAviso('Usuario eliminado correctamente.', 'success');
        await cargarUsuarios();

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

    cargarUsuarios();

    const campoBusqueda = document.getElementById('campo-busqueda');
    if (campoBusqueda) {
        campoBusqueda.addEventListener('input', aplicarFiltros);
    }

    const filtroRol = document.getElementById('filtro-rol');
    if (filtroRol) {
        filtroRol.addEventListener('change', aplicarFiltros);
    }

    const filtroEstado = document.getElementById('filtro-estado');
    if (filtroEstado) {
        filtroEstado.addEventListener('change', aplicarFiltros);
    }

    const selectRolModal = document.getElementById('crear-rol');
    if (selectRolModal) {
        selectRolModal.addEventListener('change', actualizarCamposPorRol);
    }

    const formularioCrear = document.getElementById('formulario-crear-usuario');
    if (formularioCrear) {
        formularioCrear.addEventListener('submit', guardarNuevoUsuario);
    }

    const botonCerrarSesion = document.getElementById('btn-cerrar-sesion');
    if (botonCerrarSesion) {
        botonCerrarSesion.addEventListener('click', cerrarSesion);
    }
});

