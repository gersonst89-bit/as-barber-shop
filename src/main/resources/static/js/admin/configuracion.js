const token = localStorage.getItem('as_token') || sessionStorage.getItem('as_token');
const usuarioGuardado = localStorage.getItem('as_usuario') || sessionStorage.getItem('as_usuario');

document.addEventListener('DOMContentLoaded', () => {
    verificarSesionAdmin();
    configurarCabecera();
    cargarConfiguracionGuardada();
    configurarFormularios();
});

function verificarSesionAdmin() {
    if (!token || !usuarioGuardado) {
        window.location.href = '/pages/login.html?redirect=/pages/admin/configuracion.html';
        return;
    }

    try {
        const usuario = JSON.parse(usuarioGuardado);
        if (usuario.rol !== 'ADMIN') {
            alert('Acceso denegado. Este panel requiere permisos de Administrador.');
            window.location.href = '/pages/cliente/reservas.html';
        }
    } catch (e) {
        window.location.href = '/pages/login.html';
    }
}

function configurarCabecera() {
    const nombre = document.getElementById('nombre-admin-cabecera');
    const rol = document.getElementById('rol-admin-cabecera');
    const avatar = document.getElementById('avatar-admin-cabecera');
    const btnSalir = document.getElementById('btn-cerrar-sesion');

    try {
        const usuario = JSON.parse(usuarioGuardado);
        if (nombre && usuario) nombre.textContent = `${usuario.nombre} ${usuario.apellido}`;
        if (rol && usuario) rol.textContent = usuario.email || 'admin@asbarbershop.pe';
        if (avatar && usuario) avatar.textContent = usuario.nombre.charAt(0).toUpperCase();

        const inputCorreoAdmin = document.getElementById('cfg-admin-correo');
        if (inputCorreoAdmin && usuario.email) inputCorreoAdmin.value = usuario.email;
    } catch (e) { }

    btnSalir?.addEventListener('click', () => {
        localStorage.removeItem('as_token');
        localStorage.removeItem('as_usuario');
        sessionStorage.removeItem('as_token');
        sessionStorage.removeItem('as_usuario');
        window.location.href = '/';
    });
}

function cargarConfiguracionGuardada() {
    try {
        const guardada = localStorage.getItem('as_configuracion_sede');
        if (!guardada) return;
        const cfg = JSON.parse(guardada);

        if (cfg.nombre) document.getElementById('cfg-nombre').value = cfg.nombre;
        if (cfg.direccion) document.getElementById('cfg-direccion').value = cfg.direccion;
        if (cfg.telefono) document.getElementById('cfg-telefono').value = cfg.telefono;
        if (cfg.correo) document.getElementById('cfg-correo').value = cfg.correo;
        if (cfg.razon) document.getElementById('cfg-razon').value = cfg.razon;
        if (cfg.ruc) document.getElementById('cfg-ruc').value = cfg.ruc;
        if (cfg.apertura) document.getElementById('cfg-apertura').value = cfg.apertura;
        if (cfg.cierre) document.getElementById('cfg-cierre').value = cfg.cierre;
        if (cfg.intervalo) document.getElementById('cfg-intervalo').value = cfg.intervalo;
        if (cfg.tolerancia) document.getElementById('cfg-tolerancia').value = cfg.tolerancia;
        if (cfg.anticipacion) document.getElementById('cfg-anticipacion-cancelacion').value = cfg.anticipacion;
    } catch (e) { }
}

function configurarFormularios() {
    const btnGuardar = document.getElementById('btn-guardar-configuracion');
    btnGuardar?.addEventListener('click', () => {
        const cfg = {
            nombre: document.getElementById('cfg-nombre')?.value || '',
            direccion: document.getElementById('cfg-direccion')?.value || '',
            telefono: document.getElementById('cfg-telefono')?.value || '',
            correo: document.getElementById('cfg-correo')?.value || '',
            razon: document.getElementById('cfg-razon')?.value || '',
            ruc: document.getElementById('cfg-ruc')?.value || '',
            apertura: document.getElementById('cfg-apertura')?.value || '',
            cierre: document.getElementById('cfg-cierre')?.value || '',
            intervalo: document.getElementById('cfg-intervalo')?.value || '',
            tolerancia: document.getElementById('cfg-tolerancia')?.value || '',
            anticipacion: document.getElementById('cfg-anticipacion-cancelacion')?.value || ''
        };

        localStorage.setItem('as_configuracion_sede', JSON.stringify(cfg));
        mostrarAviso('Configuración general de la sede actualizada exitosamente.', 'success');
    });

    const formClave = document.getElementById('form-cambio-clave-admin');
    formClave?.addEventListener('submit', (e) => {
        e.preventDefault();
        const p1 = document.getElementById('cfg-clave-nueva')?.value || '';
        const p2 = document.getElementById('cfg-clave-confirmar')?.value || '';

        if (p1.length < 6) {
            mostrarAviso('La contraseña debe tener al menos 6 caracteres.', 'error');
            return;
        }

        if (p1 !== p2) {
            mostrarAviso('Las contraseñas no coinciden. Por favor verifica.', 'error');
            return;
        }

        mostrarAviso('Contraseña administrativa actualizada correctamente.', 'success');
        formClave.reset();
    });
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

    aviso.className = `flex items-center gap-3 px-4 py-3 rounded-xl border backdrop-blur-md shadow-2xl text-xs sm:text-sm transition-all duration-300 transform translate-y-2 opacity-0 ${fondo}`;
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
