const RUTA_API = '/api';

function mostrarMensaje(mensaje, tipo = 'error') {
    const contenedor = document.getElementById('contenedor-notificaciones') || document.getElementById('toast-container');
    if (!contenedor) {
        alert(mensaje);
        return;
    }

    const notificacion = document.createElement('div');
    const estiloFondo = tipo === 'success' 
        ? 'bg-emerald-900/90 border-emerald-500/50 text-emerald-100' 
        : 'bg-red-950/90 border-red-500/50 text-red-100';

    const icono = tipo === 'success' 
        ? '<svg class="w-5 h-5 text-emerald-400 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>'
        : '<svg class="w-5 h-5 text-red-400 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path></svg>';

    notificacion.className = `flex items-center gap-3 px-4 py-3 rounded-xl border backdrop-blur-md shadow-2xl text-sm transition-all duration-300 transform translate-y-2 opacity-0 ${estiloFondo}`;
    notificacion.innerHTML = `${icono}<span>${mensaje}</span>`;

    contenedor.appendChild(notificacion);

    requestAnimationFrame(() => {
        notificacion.classList.remove('translate-y-2', 'opacity-0');
    });

    setTimeout(() => {
        notificacion.classList.add('opacity-0', '-translate-y-2');
        setTimeout(() => notificacion.remove(), 300);
    }, 4000);
}

function activarVisibilidadClave() {
    const botones = document.querySelectorAll('[data-ver-clave], [data-toggle-password]');
    botones.forEach(boton => {
        boton.addEventListener('click', () => {
            const idDestino = boton.getAttribute('data-ver-clave') || boton.getAttribute('data-toggle-password');
            const campoTexto = document.getElementById(idDestino);
            if (!campoTexto) return;

            const esContrasena = campoTexto.type === 'password';
            campoTexto.type = esContrasena ? 'text' : 'password';

            boton.innerHTML = esContrasena 
                ? `<svg class="w-5 h-5 text-amber-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                     <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l18 18"></path>
                   </svg>`
                : `<svg class="w-5 h-5 text-stone-400 group-hover:text-stone-200" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                     <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
                     <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path>
                   </svg>`;
        });
    });
}

async function iniciarSesion(evento) {
    evento.preventDefault();
    const botonEnviar = document.getElementById('btn-ingresar') || document.getElementById('btn-login');
    const textoOriginal = botonEnviar ? botonEnviar.innerHTML : '';

    const elEmail = document.getElementById('login-correo') || document.getElementById('login-email');
    const elClave = document.getElementById('login-clave') || document.getElementById('login-password');
    const elRecordar = document.getElementById('login-recordarme') || document.getElementById('login-remember');

    const email = elEmail ? elEmail.value.trim() : '';
    const password = elClave ? elClave.value : '';
    const recordar = elRecordar ? elRecordar.checked : false;

    if (!email || !password) {
        mostrarMensaje('Por favor ingresa tu correo y contraseña.');
        return;
    }

    try {
        if (botonEnviar) {
            botonEnviar.disabled = true;
            botonEnviar.innerHTML = `
                <svg class="animate-spin h-5 w-5 text-stone-900" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z"></path>
                </svg>
                <span>Verificando...</span>
            `;
        }

        const respuesta = await fetch(`${RUTA_API}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });

        const respuestaJson = await respuesta.json();

        if (!respuesta.ok || !respuestaJson.exitoso) {
            throw new Error(respuestaJson.mensaje || 'Correo o contraseña incorrectos.');
        }

        const datosUsuario = respuestaJson.datos;
        const jsonUsuario = JSON.stringify({
            id: datosUsuario.idUsuario,
            nombre: datosUsuario.nombre,
            apellido: datosUsuario.apellido,
            email: datosUsuario.email,
            rol: datosUsuario.rol,
            idPerfil: datosUsuario.idPerfil
        });

        localStorage.setItem('as_token', datosUsuario.token);
        localStorage.setItem('as_usuario', jsonUsuario);
        sessionStorage.setItem('as_token', datosUsuario.token);
        sessionStorage.setItem('as_usuario', jsonUsuario);

        mostrarMensaje(`¡Bienvenido, ${datosUsuario.nombre}! Redirigiendo...`, 'success');

        setTimeout(() => {
            const params = new URLSearchParams(window.location.search);
            const redirectUrl = params.get('redirect');

            if (datosUsuario.rol === 'ADMIN') {
                window.location.href = '/pages/admin/dashboard.html';
            } else if (datosUsuario.rol === 'BARBERO') {
                window.location.href = '/pages/barbero/agenda.html';
            } else if (redirectUrl) {
                window.location.href = redirectUrl;
            } else {
                window.location.href = '/';
            }
        }, 1000);

    } catch (error) {
        mostrarMensaje(error.message);
    } finally {
        if (botonEnviar) {
            botonEnviar.disabled = false;
            botonEnviar.innerHTML = textoOriginal;
        }
    }
}

async function registrarCliente(evento) {
    evento.preventDefault();
    const botonEnviar = document.getElementById('btn-registro') || document.getElementById('btn-register');
    const textoOriginal = botonEnviar ? botonEnviar.innerHTML : '';

    const elNombre = document.getElementById('reg-nombre');
    const elApellido = document.getElementById('reg-apellido');
    const elTelefono = document.getElementById('reg-telefono');
    const elEmail = document.getElementById('reg-correo') || document.getElementById('reg-email');
    const elClave = document.getElementById('reg-clave') || document.getElementById('reg-password');
    const elConfirmar = document.getElementById('reg-confirmar-clave') || document.getElementById('reg-confirm-password');
    const elTerminos = document.getElementById('reg-terminos');

    let nombre = elNombre ? elNombre.value.trim() : '';
    let apellido = elApellido ? elApellido.value.trim() : '';
    const telefono = elTelefono ? elTelefono.value.trim() : '';
    const email = elEmail ? elEmail.value.trim() : '';
    const password = elClave ? elClave.value : '';
    const confirmarPassword = elConfirmar ? elConfirmar.value : '';
    const terminos = elTerminos ? elTerminos.checked : false;

    if (!elApellido && nombre.includes(' ')) {
        const partes = nombre.split(' ');
        nombre = partes[0];
        apellido = partes.slice(1).join(' ') || 'Cliente';
    }

    if (!nombre || !email || !password || !confirmarPassword) {
        mostrarMensaje('Por favor completa todos los campos obligatorios.');
        return;
    }

    if (!terminos) {
        mostrarMensaje('Debes aceptar los términos y condiciones.');
        return;
    }

    if (password.length < 6) {
        mostrarMensaje('La contraseña debe tener al menos 6 caracteres.');
        return;
    }

    if (password !== confirmarPassword) {
        mostrarMensaje('Las contraseñas no coinciden.');
        return;
    }

    if (!apellido) {
        apellido = 'Cliente';
    }

    try {
        if (botonEnviar) {
            botonEnviar.disabled = true;
            botonEnviar.innerHTML = `
                <svg class="animate-spin h-5 w-5 text-stone-900" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z"></path>
                </svg>
                <span>Creando cuenta...</span>
            `;
        }

        const respuesta = await fetch(`${RUTA_API}/auth/registro`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                nombre,
                apellido,
                telefono,
                email,
                password
            })
        });

        const respuestaJson = await respuesta.json();

        if (!respuesta.ok || !respuestaJson.exitoso) {
            throw new Error(respuestaJson.mensaje || 'Error al registrar cliente');
        }

        mostrarMensaje('¡Cuenta creada exitosamente! Redirigiendo al inicio de sesión...', 'success');

        setTimeout(() => {
            window.location.href = '/pages/login.html';
        }, 1200);

    } catch (error) {
        mostrarMensaje(error.message);
    } finally {
        if (botonEnviar) {
            botonEnviar.disabled = false;
            botonEnviar.innerHTML = textoOriginal;
        }
    }
}

document.addEventListener('DOMContentLoaded', () => {
    activarVisibilidadClave();

    const formularioLogin = document.getElementById('formulario-login') || document.getElementById('form-login');
    if (formularioLogin) {
        formularioLogin.addEventListener('submit', iniciarSesion);
    }

    const formularioRegistro = document.getElementById('formulario-registro') || document.getElementById('form-register');
    if (formularioRegistro) {
        formularioRegistro.addEventListener('submit', registrarCliente);
    }
});
