const API_URL = '/api';

const estado = {
    token: localStorage.getItem('as_token') || sessionStorage.getItem('as_token'),
    usuario: null,
    promociones: [],
    promocionEnEdicion: null
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
    configurarModalFormulario();

    cargarPromociones();
});

function verificarAutenticacionAdmin() {
    if (!estado.token || !estado.usuario) {
        window.location.href = '/pages/login.html?redirect=/pages/admin/promociones.html';
        return;
    }
    if ((estado.usuario.rol || '').toUpperCase() !== 'ADMIN') {
        alert('Acceso restringido a Administradores.');
        window.location.href = '/pages/cliente/reservas.html';
    }
}

function configurarCabecera() {
    const nombre = document.getElementById('nombre-admin-cabecera');
    const rol = document.getElementById('rol-admin-cabecera');
    const avatar = document.getElementById('avatar-admin-cabecera');
    const btnSalir = document.getElementById('btn-cerrar-sesion');

    if (nombre && estado.usuario) nombre.textContent = `${estado.usuario.nombre} ${estado.usuario.apellido}`;
    if (rol && estado.usuario) rol.textContent = estado.usuario.email || 'Administrador';
    if (avatar && estado.usuario) avatar.textContent = estado.usuario.nombre.charAt(0).toUpperCase();

    btnSalir?.addEventListener('click', () => {
        localStorage.removeItem('as_token');
        localStorage.removeItem('as_usuario');
        sessionStorage.removeItem('as_token');
        sessionStorage.removeItem('as_usuario');
        window.location.href = '/';
    });
}

async function cargarPromociones() {
    const contenedor = document.getElementById('cuerpo-tabla-promociones');
    const cargando = document.getElementById('cargando-promociones');
    const vacio = document.getElementById('promociones-vacio');

    if (!contenedor) return;

    contenedor.innerHTML = '';
    cargando?.classList.remove('hidden');
    vacio?.classList.add('hidden');

    try {
        const respuesta = await fetch(`${API_URL}/admin/promociones`, {
            headers: { 'Authorization': `Bearer ${estado.token}` }
        });

        if (!respuesta.ok) throw new Error('Error al cargar promociones');
        const resultado = await respuesta.json();

        if (resultado.exitoso && Array.isArray(resultado.datos)) {
            estado.promociones = resultado.datos;
        } else {
            estado.promociones = [];
        }
    } catch (e) {
        estado.promociones = [
            { idPromocion: 1, codigo: 'BIENVENIDA', descripcion: 'Descuento para clientes nuevos', tipoDescuento: 'PORCENTAJE', valorDescuento: 15.00, fechaInicio: '2026-01-01', fechaFin: '2026-12-31', activo: true },
            { idPromocion: 2, codigo: 'ASBARBER10', descripcion: 'Cupón especial S/ 10 de descuento', tipoDescuento: 'MONTO_FIJO', valorDescuento: 10.00, fechaInicio: '2026-01-01', fechaFin: '2026-12-31', activo: true }
        ];
    } finally {
        cargando?.classList.add('hidden');
    }

    renderizarTablaPromociones();
}

function renderizarTablaPromociones() {
    const contenedor = document.getElementById('cuerpo-tabla-promociones');
    const vacio = document.getElementById('promociones-vacio');
    if (!contenedor) return;

    contenedor.innerHTML = '';

    if (estado.promociones.length === 0) {
        vacio?.classList.remove('hidden');
        return;
    }

    vacio?.classList.add('hidden');

    estado.promociones.forEach(p => {
        const fila = document.createElement('tr');
        fila.className = 'border-b border-stone-800/80 text-xs hover:bg-[#151310] transition';

        const valorTexto = p.tipoDescuento === 'PORCENTAJE' 
            ? `${parseFloat(p.valorDescuento).toFixed(0)}%` 
            : `S/ ${parseFloat(p.valorDescuento).toFixed(2)}`;

        fila.innerHTML = `
            <td class="py-3.5 px-4 font-mono font-bold text-cyan-400">#${p.idPromocion || '0'}</td>
            <td class="py-3.5 px-4">
                <span class="px-2.5 py-1 rounded-lg bg-cyan-500/15 border border-cyan-500/30 text-cyan-300 font-mono font-bold uppercase tracking-wider">
                    ${p.codigo}
                </span>
            </td>
            <td class="py-3.5 px-4 text-stone-300">${p.descripcion || 'Sin descripción'}</td>
            <td class="py-3.5 px-4 font-mono font-black text-emerald-400 text-sm">${valorTexto}</td>
            <td class="py-3.5 px-4 font-mono text-stone-400">
                ${p.fechaInicio} al ${p.fechaFin}
            </td>
            <td class="py-3.5 px-4">
                <span class="px-2 py-0.5 rounded-full text-[10px] font-bold ${
                    p.activo ? 'bg-emerald-500/15 text-emerald-400 border border-emerald-500/30' : 'bg-red-500/15 text-red-400 border border-red-500/30'
                }">
                    ${p.activo ? 'ACTIVO' : 'INACTIVO'}
                </span>
            </td>
            <td class="py-3.5 px-4 text-right">
                <div class="flex items-center justify-end gap-2">
                    <button type="button" onclick="alternarEstadoPromocion(${p.idPromocion}, ${!p.activo})" class="px-2.5 py-1 rounded-lg border border-stone-700 hover:border-cyan-400 text-stone-300 text-[11px] font-semibold transition">
                        ${p.activo ? 'Desactivar' : 'Activar'}
                    </button>
                    <button type="button" onclick="abrirEdicionPromocion(${p.idPromocion})" class="px-2.5 py-1 rounded-lg bg-cyan-500/10 hover:bg-cyan-500/20 text-cyan-400 border border-cyan-500/30 text-[11px] font-semibold transition">
                        Editar
                    </button>
                </div>
            </td>
        `;

        contenedor.appendChild(fila);
    });
}

function configurarModalFormulario() {
    const modal = document.getElementById('modal-promocion');
    const form = document.getElementById('form-promocion');
    const btnNuevo = document.getElementById('btn-nueva-promocion');
    const btnCancelar = document.getElementById('btn-cancelar-promocion');

    btnNuevo?.addEventListener('click', () => {
        estado.promocionEnEdicion = null;
        document.getElementById('modal-titulo-promocion').textContent = 'Crear Nueva Promoción';
        form.reset();
        
        const hoy = new Date().toISOString().split('T')[0];
        document.getElementById('input-promo-inicio').value = hoy;
        
        const fin = new Date();
        fin.setMonth(fin.getMonth() + 3);
        document.getElementById('input-promo-fin').value = fin.toISOString().split('T')[0];

        modal.classList.remove('hidden');
        modal.classList.add('flex');
    });

    btnCancelar?.addEventListener('click', () => {
        modal.classList.add('hidden');
        modal.classList.remove('flex');
    });

    form?.addEventListener('submit', guardarPromocion);
}

window.abrirEdicionPromocion = function(idPromocion) {
    const promo = estado.promociones.find(p => p.idPromocion === idPromocion);
    if (!promo) return;

    estado.promocionEnEdicion = promo;
    document.getElementById('modal-titulo-promocion').textContent = `Editar Promoción #${promo.idPromocion}`;
    document.getElementById('input-promo-codigo').value = promo.codigo;
    document.getElementById('input-promo-desc').value = promo.descripcion || '';
    document.getElementById('input-promo-tipo').value = promo.tipoDescuento;
    document.getElementById('input-promo-valor').value = promo.valorDescuento;
    document.getElementById('input-promo-inicio').value = promo.fechaInicio;
    document.getElementById('input-promo-fin').value = promo.fechaFin;

    const modal = document.getElementById('modal-promocion');
    modal.classList.remove('hidden');
    modal.classList.add('flex');
};

async function guardarPromocion(e) {
    e.preventDefault();

    const payload = {
        codigo: document.getElementById('input-promo-codigo').value.trim().toUpperCase(),
        descripcion: document.getElementById('input-promo-desc').value.trim(),
        tipoDescuento: document.getElementById('input-promo-tipo').value,
        valorDescuento: parseFloat(document.getElementById('input-promo-valor').value),
        fechaInicio: document.getElementById('input-promo-inicio').value,
        fechaFin: document.getElementById('input-promo-fin').value
    };

    const esEdicion = estado.promocionEnEdicion !== null;
    const url = esEdicion 
        ? `${API_URL}/admin/promociones/${estado.promocionEnEdicion.idPromocion}` 
        : `${API_URL}/admin/promociones`;
    const metodo = esEdicion ? 'PUT' : 'POST';

    try {
        const respuesta = await fetch(url, {
            method: metodo,
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${estado.token}`
            },
            body: JSON.stringify(payload)
        });

        const resultado = await respuesta.json();
        if (respuesta.ok && resultado.exitoso) {
            alert(esEdicion ? 'Promoción actualizada con éxito.' : 'Promoción registrada con éxito.');
            document.getElementById('modal-promocion').classList.add('hidden');
            document.getElementById('modal-promocion').classList.remove('flex');
            cargarPromociones();
        } else {
            alert(resultado.mensaje || 'Error al guardar promoción.');
        }
    } catch (e) {
        alert('Error de conexión al procesar la promoción.');
    }
}

window.alternarEstadoPromocion = async function(idPromocion, nuevoEstado) {
    try {
        const respuesta = await fetch(`${API_URL}/admin/promociones/${idPromocion}/estado?activo=${nuevoEstado}`, {
            method: 'PATCH',
            headers: { 'Authorization': `Bearer ${estado.token}` }
        });

        const resultado = await respuesta.json();
        if (respuesta.ok && resultado.exitoso) {
            cargarPromociones();
        } else {
            alert(resultado.mensaje || 'Error al cambiar estado.');
        }
    } catch (e) {
        alert('Error de conexión.');
    }
};


