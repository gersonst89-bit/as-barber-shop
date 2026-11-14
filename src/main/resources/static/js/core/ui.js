const ui = {
    soles(monto) {
        const num = parseFloat(monto) || 0;
        return `S/ ${num.toFixed(2)}`;
    },

    fecha(fechaStr) {
        if (!fechaStr) return '-';
        try {
            const f = new Date(fechaStr + 'T00:00:00');
            return f.toLocaleDateString('es-PE', {
                day: '2-digit',
                month: 'short',
                year: 'numeric'
            });
        } catch (e) {
            return fechaStr;
        }
    },

    hora(horaStr) {
        if (!horaStr) return '-';
        const partes = horaStr.split(':');
        if (partes.length < 2) return horaStr;
        let h = parseInt(partes[0], 10);
        const m = partes[1];
        const ampm = h >= 12 ? 'pm' : 'am';
        h = h % 12;
        if (h === 0) h = 12;
        return `${h}:${m} ${ampm}`;
    },

    toast(mensaje, tipo = 'info') {
        const id = 'toast-container-global';
        let contenedor = document.getElementById(id);
        if (!contenedor) {
            contenedor = document.createElement('div');
            contenedor.id = id;
            contenedor.className = 'fixed top-5 right-5 z-[9999] flex flex-col gap-2 pointer-events-none';
            document.body.appendChild(contenedor);
        }

        const colores = {
            exito: 'bg-emerald-950/90 border-emerald-500/50 text-emerald-300',
            error: 'bg-rose-950/90 border-rose-500/50 text-rose-300',
            info: 'bg-[#141210]/95 border-cyan-500/50 text-cyan-300',
            advertencia: 'bg-amber-950/90 border-amber-500/50 text-amber-300'
        };

        const toast = document.createElement('div');
        toast.className = `px-4 py-2.5 rounded-xl border shadow-2xl backdrop-blur-md text-xs font-semibold flex items-center gap-2 transform translate-y-2 opacity-0 transition-all duration-300 pointer-events-auto ${colores[tipo] || colores.info}`;
        toast.innerHTML = `<span>${mensaje}</span>`;

        contenedor.appendChild(toast);

        requestAnimationFrame(() => {
            toast.classList.remove('translate-y-2', 'opacity-0');
        });

        setTimeout(() => {
            toast.classList.add('opacity-0', '-translate-y-2');
            setTimeout(() => toast.remove(), 300);
        }, 3200);
    }
};
