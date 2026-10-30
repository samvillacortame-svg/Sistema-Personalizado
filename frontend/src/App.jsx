import { useCallback, useEffect, useMemo, useState } from 'react';

const EMPTY_FORM = { nombre: '', documento: '', telefono: '', correo: '' };
const AUTH_KEY = 'sie-session';

async function readError(response) {
  try {
    const body = await response.json();
    return body.detail || body.message || body.error || 'No se pudo completar la operación.';
  } catch {
    return 'No se pudo completar la operación.';
  }
}

export default function App() {
  const [auth, setAuth] = useState(() => sessionStorage.getItem(AUTH_KEY) || '');
  const [username, setUsername] = useState('ventas_demo');
  const [password, setPassword] = useState('');
  const [clients, setClients] = useState([]);
  const [search, setSearch] = useState('');
  const [includeInactive, setIncludeInactive] = useState(false);
  const [loading, setLoading] = useState(false);
  const [loginLoading, setLoginLoading] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [formOpen, setFormOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [saving, setSaving] = useState(false);
  const [apiStatus, setApiStatus] = useState('Comprobando conexión');

  useEffect(() => {
    fetch('/api/estado')
      .then((response) => response.json())
      .then((data) => setApiStatus(`API ${data.api} · MySQL ${data.baseDeDatos}`))
      .catch(() => setApiStatus('API sin conexión'));
  }, []);

  const loadClients = useCallback(async (token = auth, query = search, inactive = includeInactive) => {
    setLoading(true);
    setError('');
    try {
      const params = new URLSearchParams({ busqueda: query, incluirInactivos: String(inactive) });
      const response = await fetch(`/api/ventas/clientes?${params}`, {
        headers: { Authorization: token },
      });
      if (!response.ok) {
        const message = await readError(response);
        if (response.status === 401 || response.status === 403) throw Object.assign(new Error(message), { status: response.status });
        throw new Error(message);
      }
      setClients(await response.json());
    } catch (exception) {
      if (exception.status === 401 || exception.status === 403) {
        sessionStorage.removeItem(AUTH_KEY);
        setAuth('');
      }
      setError(exception.message || 'No se pudo conectar con el servidor.');
    } finally {
      setLoading(false);
    }
  }, [auth, search, includeInactive]);

  useEffect(() => {
    if (!auth) return;
    const timer = window.setTimeout(() => loadClients(), 180);
    return () => window.clearTimeout(timer);
  }, [auth, search, includeInactive, loadClients]);

  const activeCount = useMemo(() => clients.filter((client) => client.activo).length, [clients]);

  async function signIn(event) {
    event.preventDefault();
    setLoginLoading(true);
    setError('');
    const token = `Basic ${btoa(`${username.trim()}:${password}`)}`;
    try {
      const response = await fetch('/api/ventas/clientes', { headers: { Authorization: token } });
      if (!response.ok) throw new Error(response.status === 401 ? 'Usuario o contraseña incorrectos.' : await readError(response));
      sessionStorage.setItem(AUTH_KEY, token);
      setAuth(token);
      setPassword('');
    } catch (exception) {
      setError(exception.message || 'No se pudo conectar con el servidor.');
    } finally {
      setLoginLoading(false);
    }
  }

  function signOut() {
    sessionStorage.removeItem(AUTH_KEY);
    setAuth('');
    setClients([]);
    setNotice('');
    setError('');
  }

  function openNewForm() {
    setEditing(null);
    setForm(EMPTY_FORM);
    setError('');
    setFormOpen(true);
  }

  function openEditForm(client) {
    setEditing(client);
    setForm({
      nombre: client.nombre || '',
      documento: client.documento || '',
      telefono: client.telefono || '',
      correo: client.correo || '',
    });
    setError('');
    setFormOpen(true);
  }

  async function saveClient(event) {
    event.preventDefault();
    setSaving(true);
    setError('');
    setNotice('');
    const payload = Object.fromEntries(Object.entries(form).map(([key, value]) => [key, value.trim() || null]));
    try {
      const response = await fetch(editing ? `/api/ventas/clientes/${editing.id}` : '/api/ventas/clientes', {
        method: editing ? 'PUT' : 'POST',
        headers: { Authorization: auth, 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });
      if (!response.ok) throw new Error(await readError(response));
      setFormOpen(false);
      setNotice(editing ? 'Cambios guardados.' : 'Cliente registrado.');
      await loadClients();
    } catch (exception) {
      setError(exception.message || 'No se pudo guardar el cliente.');
    } finally {
      setSaving(false);
    }
  }

  async function toggleClient(client) {
    const nextState = !client.activo;
    const message = nextState ? '¿Reactivar este cliente?' : '¿Desactivar este cliente? Su historial se conservará.';
    if (!window.confirm(message)) return;
    setError('');
    setNotice('');
    try {
      const response = await fetch(`/api/ventas/clientes/${client.id}/estado`, {
        method: 'PATCH',
        headers: { Authorization: auth, 'Content-Type': 'application/json' },
        body: JSON.stringify({ activo: nextState }),
      });
      if (!response.ok) throw new Error(await readError(response));
      setNotice(nextState ? 'Cliente reactivado.' : 'Cliente desactivado.');
      await loadClients();
    } catch (exception) {
      setError(exception.message || 'No se pudo actualizar el estado.');
    }
  }

  if (!auth) {
    return (
      <main className="login-shell">
        <section className="login-card">
          <div className="brand-mark">D</div>
          <p className="eyebrow">SISTEMA PERSONALIZADO · SPRINT 2</p>
          <h1>Inicia sesión</h1>
          <p className="muted">Acceso al módulo de clientes para el equipo de ventas.</p>
          <form onSubmit={signIn} className="login-form">
            <label>Usuario<input autoComplete="username" value={username} onChange={(event) => setUsername(event.target.value)} required /></label>
            <label>Contraseña<input type="password" autoComplete="current-password" value={password} onChange={(event) => setPassword(event.target.value)} required /></label>
            {error && <p className="alert error" role="alert">{error}</p>}
            <button className="primary-button" disabled={loginLoading}>{loginLoading ? 'Verificando…' : 'Entrar'}</button>
          </form>
          <p className="login-hint">Usa las credenciales de ventas configuradas en el archivo local <code>.env</code>.</p>
          <span className="connection-note">{apiStatus}</span>
        </section>
      </main>
    );
  }

  return (
    <main className="app-shell">
      <aside className="sidebar">
        <div className="brand"><span className="brand-mark small-mark">D</span><span>Dermogestión<small>Sistema comercial</small></span></div>
        <p className="nav-label">ESPACIO DE TRABAJO</p>
        <div className="nav-item selected"><span>01</span>Clientes</div>
        <div className="nav-item disabled"><span>02</span>Pedidos <small>Próximo sprint</small></div>
        <div className="nav-item disabled"><span>03</span>Inventario</div>
        <div className="sidebar-note"><b>CRM de clientes</b><p>Registra y consulta la información comercial desde un solo lugar.</p></div>
      </aside>

      <section className="workspace">
        <header className="topbar">
          <div><p className="eyebrow">SISTEMA PERSONALIZADO INTEGRADO</p><h1>Gestión de clientes</h1><p className="muted">Administra los contactos y datos de tus clientes.</p></div>
          <div className="user-area"><span className="connection-pill"><i />{apiStatus}</span><button className="quiet-button" onClick={signOut}>Cerrar sesión</button></div>
        </header>

        <div className="summary-grid">
          <article className="summary-card"><span>Clientes visibles</span><strong>{clients.length}</strong><small>Según búsqueda y filtros actuales</small></article>
          <article className="summary-card"><span>Clientes activos</span><strong>{activeCount}</strong><small>Disponibles para nuevos pedidos</small></article>
          <article className="summary-card accent-card"><span>Base de datos</span><strong>Conectada</strong><small>Los cambios se guardan en MySQL</small></article>
        </div>

        <section className="client-panel">
          <div className="panel-heading"><div><p className="eyebrow">CRM</p><h2>Directorio de clientes</h2><p className="muted">Busca, registra y actualiza fichas comerciales.</p></div><button className="primary-button add-button" onClick={openNewForm}>＋ Nuevo cliente</button></div>
          <div className="toolbar">
            <label className="search-box"><span>⌕</span><input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Buscar por nombre, documento o teléfono" /></label>
            <label className="check-filter"><input type="checkbox" checked={includeInactive} onChange={(event) => setIncludeInactive(event.target.checked)} /> Incluir inactivos</label>
          </div>

          {notice && <p className="alert success" role="status">{notice}</p>}
          {error && <p className="alert error" role="alert">{error}</p>}

          <div className="table-wrap">
            <table>
              <thead><tr><th>CLIENTE</th><th>DOCUMENTO</th><th>TELÉFONO</th><th>CORREO</th><th>ESTADO</th><th>ACCIONES</th></tr></thead>
              <tbody>
                {clients.map((client) => (
                  <tr key={client.id}>
                    <td><div className="client-name"><span>{client.nombre.trim().charAt(0).toUpperCase()}</span><b>{client.nombre}</b></div></td>
                    <td>{client.documento || '—'}</td><td>{client.telefono || '—'}</td><td>{client.correo || '—'}</td>
                    <td><span className={`badge ${client.activo ? 'active' : 'inactive'}`}>{client.activo ? 'Activo' : 'Inactivo'}</span></td>
                    <td><div className="actions"><button onClick={() => openEditForm(client)}>Editar</button><button onClick={() => toggleClient(client)}>{client.activo ? 'Desactivar' : 'Reactivar'}</button></div></td>
                  </tr>
                ))}
              </tbody>
            </table>
            {loading && <div className="empty-state">Cargando clientes…</div>}
            {!loading && clients.length === 0 && <div className="empty-state"><b>{search ? 'No encontramos coincidencias' : 'Aún no hay clientes'}</b><span>{search ? 'Prueba con otro nombre, documento o teléfono.' : 'Registra el primer cliente para comenzar el directorio.'}</span></div>}
          </div>
          <footer className="panel-footer">Información protegida por rol · Los clientes desactivados conservan su historial.</footer>
        </section>
      </section>

      {formOpen && <div className="modal-backdrop" onMouseDown={(event) => { if (event.target === event.currentTarget) setFormOpen(false); }}>
        <section className="form-modal" role="dialog" aria-modal="true" aria-labelledby="form-title">
          <div className="modal-heading"><div><p className="eyebrow">FICHA COMERCIAL</p><h2 id="form-title">{editing ? 'Editar cliente' : 'Nuevo cliente'}</h2></div><button className="close-button" onClick={() => setFormOpen(false)} aria-label="Cerrar">×</button></div>
          <form onSubmit={saveClient} className="client-form">
            <label>Nombre completo <em>*</em><input autoFocus maxLength="160" value={form.nombre} onChange={(event) => setForm({ ...form, nombre: event.target.value })} required /></label>
            <label>Documento<input maxLength="24" value={form.documento} onChange={(event) => setForm({ ...form, documento: event.target.value })} placeholder="DNI, RUC u otro" /></label>
            <label>Teléfono<input maxLength="32" type="tel" value={form.telefono} onChange={(event) => setForm({ ...form, telefono: event.target.value })} /></label>
            <label>Correo electrónico<input maxLength="160" type="email" value={form.correo} onChange={(event) => setForm({ ...form, correo: event.target.value })} /></label>
            <p className="form-help">El documento es opcional, pero si se registra debe ser único.</p>
            {error && <p className="alert error" role="alert">{error}</p>}
            <div className="modal-actions"><button type="button" className="quiet-button" onClick={() => setFormOpen(false)}>Cancelar</button><button className="primary-button" disabled={saving}>{saving ? 'Guardando…' : editing ? 'Guardar cambios' : 'Registrar cliente'}</button></div>
          </form>
        </section>
      </div>}
    </main>
  );
}
