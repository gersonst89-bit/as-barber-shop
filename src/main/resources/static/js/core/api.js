const api = {
    getToken() {
        return localStorage.getItem('as_token') || sessionStorage.getItem('as_token');
    },

    getUsuario() {
        const raw = localStorage.getItem('as_usuario') || sessionStorage.getItem('as_usuario');
        try {
            return raw ? JSON.parse(raw) : null;
        } catch (e) {
            return null;
        }
    },

    limpiarSesion() {
        localStorage.removeItem('as_token');
        localStorage.removeItem('as_usuario');
        sessionStorage.removeItem('as_token');
        sessionStorage.removeItem('as_usuario');
    },

    async request(url, opciones = {}) {
        const headers = {
            'Content-Type': 'application/json',
            ...(opciones.headers || {})
        };

        const token = this.getToken();
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        try {
            const respuesta = await fetch(url, { ...opciones, headers });

            if (respuesta.status === 401) {
                this.limpiarSesion();
                window.location.href = '/pages/login.html?sesion=expirada';
                return null;
            }

            const datos = await respuesta.json();
            return datos;
        } catch (error) {
            console.error(`[API Error] ${opciones.method || 'GET'} ${url}:`, error);
            throw error;
        }
    },

    get(url, headers = {}) {
        return this.request(url, { method: 'GET', headers });
    },

    post(url, body = {}, headers = {}) {
        return this.request(url, {
            method: 'POST',
            headers,
            body: JSON.stringify(body)
        });
    },

    put(url, body = {}, headers = {}) {
        return this.request(url, {
            method: 'PUT',
            headers,
            body: JSON.stringify(body)
        });
    },

    patch(url, body = {}, headers = {}) {
        return this.request(url, {
            method: 'PATCH',
            headers,
            body: JSON.stringify(body)
        });
    },

    delete(url, headers = {}) {
        return this.request(url, { method: 'DELETE', headers });
    }
};
