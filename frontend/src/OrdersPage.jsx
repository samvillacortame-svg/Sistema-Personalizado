import { useCallback, useEffect, useMemo, useState } from 'react';

const CHANNELS = [
  ['WHATSAPP', 'WhatsApp'], ['FACEBOOK', 'Facebook'], ['TIENDA', 'Tienda'], ['OTRO', 'Otro'],
];
const money = (value) => new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN' }).format(value || 0);
const dateTime = (value) => new Date(/[zZ]|[+-]\d{2}:\d{2}$/.test(value) ? value : `${value}Z`).toLocaleString('es-PE');
const statusName = { CONFIRMADO: 'Confirmado', PREPARACION: 'En preparación', DESPACHADO: 'Despachado', CANCELADO: 'Cancelado', DEVUELTO: 'Devuelto', RECIBIDO: 'Recibido', PENDIENTE_STOCK: 'Pendiente de stock' };
const errorText = async (response) => {
  try { const body = await response.json(); return body.detail || body.message || 'Ocurrió un error.'; }
  catch { return response.status === 403 ? 'Esta cuenta no tiene permiso para esta operación.' : 'Ocurrió un error.'; }
};

export default function OrdersPage({ auth, onSignOut }) {
  const [clients, setClients] = useState([]);
  const [products, setProducts] = useState([]);
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [open, setOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const [clientId, setClientId] = useState('');
  const [channel, setChannel] = useState('WHATSAPP');
  const [items, setItems] = useState([{ productoId: '', cantidad: 1 }]);

  const refresh = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const [clientResponse, productResponse, orderResponse] = await Promise.all([
        fetch('/api/ventas/clientes', { headers: { Authorization: auth } }),
        fetch('/api/ventas/productos', { headers: { Authorization: auth } }),
        fetch('/api/ventas/pedidos', { headers: { Authorization: auth } }),
      ]);
      const responses = [clientResponse, productResponse, orderResponse];
      const failed = responses.find((response) => !response.ok);
      if (failed) throw new Error(await errorText(failed));
      const [clientData, productData, orderData] = await Promise.all(responses.map((response) => response.json()));
      setClients(clientData);
      setProducts(productData);
      setOrders(orderData);
    } catch (exception) {
      setError(exception.message || 'No se pudieron cargar los pedidos.');
    } finally { setLoading(false); }
  }, [auth]);

  useEffect(() => { refresh(); }, [refresh]);

  const total = useMemo(() => items.reduce((sum, item) => {
    const product = products.find((candidate) => String(candidate.id) === item.productoId);
    return sum + (product?.precio || 0) * (Number(item.cantidad) || 0);
  }, 0), [items, products]);
  const productsWithStock = products.filter((product) => product.stockDisponible > 0).length;

  function updateItem(index, key, value) {
    setItems((current) => current.map((item, position) => position === index ? { ...item, [key]: value } : item));
  }

  async function createOrder(event) {
    event.preventDefault();
    setSaving(true); setError(''); setNotice('');
    try {
      const response = await fetch('/api/ventas/pedidos', {
        method: 'POST',
        headers: { Authorization: auth, 'Content-Type': 'application/json' },
        body: JSON.stringify({ clienteId: Number(clientId), canal, items: items.map((item) => ({ productoId: Number(item.productoId), cantidad: Number(item.cantidad) })) }),
      });
      if (!response.ok) throw new Error(await errorText(response));
      setOpen(false); setItems([{ productoId: '', cantidad: 1 }]); setClientId('');
      setNotice('Pedido registrado y stock reservado.');
      await refresh();
    } catch (exception) { setError(exception.message || 'No se pudo registrar el pedido.'); }
    finally { setSaving(false); }
  }

  async function cancelOrder(order) {
    if (!window.confirm(`¿Cancelar el pedido #${order.id} y liberar su stock reservado?`)) return;
    setError(''); setNotice('');
    try {
      const response = await fetch(`/api/ventas/pedidos/${order.id}/cancelar`, { method: 'POST', headers: { Authorization: auth } });
      if (!response.ok) throw new Error(await errorText(response));
      setNotice(`Pedido #${order.id} cancelado; se liberó la reserva.`);
      await refresh();
    } catch (exception) { setError(exception.message || 'No se pudo cancelar el pedido.'); }
  }

  return <section className="feature-page">
    <header className="feature-heading"><div><p className="eyebrow">VENTAS · SPRINT 3</p><h1>Pedidos</h1><p className="muted">Registra pedidos de clientes y consulta su seguimiento.</p></div><button className="primary-button" onClick={() => { setError(''); setOpen(true); }}>＋ Nuevo pedido</button></header>
    <div className="summary-grid order-summary">
      <article className="summary-card"><span>Pedidos registrados</span><strong>{orders.length}</strong><small>Historial comercial</small></article>
      <article className="summary-card"><span>En preparación</span><strong>{orders.filter((order) => order.estado === 'PREPARACION').length}</strong><small>Esperando al almacén</small></article>
      <article className="summary-card"><span>Productos disponibles</span><strong>{productsWithStock}</strong><small>Con stock para registrar pedidos</small></article>
    </div>
    {notice && <p className="alert success">{notice}</p>}{error && <p className="alert error">{error}</p>}
    <div className="orders-list">
      {loading && <div className="empty-state">Cargando pedidos…</div>}
      {!loading && !error && orders.length === 0 && <div className="empty-state"><b>Aún no hay pedidos</b><span>Al registrar uno, sus productos se reservarán del inventario.</span></div>}
      {orders.map((order) => <article className="order-card" key={order.id}>
        <div className="order-top"><div><span className="order-number">PEDIDO #{String(order.id).padStart(4, '0')}</span><h3>{order.clienteNombre}</h3><small>{dateTime(order.creadoEn)} · {CHANNELS.find(([value]) => value === order.canal)?.[1] || order.canal}</small></div><span className={`badge order-status status-${order.estado.toLowerCase()}`}>{statusName[order.estado] || order.estado}</span></div>
        <div className="order-lines">{order.items.map((line, index) => <div className="order-line" key={`${line.productoId}-${index}`}><span>{line.cantidad} × {line.productoNombre}</span><span>{money(line.subtotal)}</span></div>)}</div>
        <div className="order-bottom"><span>Total <b>{money(order.total)}</b></span>{order.estado === 'CONFIRMADO' && <button className="text-action" onClick={() => cancelOrder(order)}>Cancelar pedido</button>}</div>
      </article>)}
    </div>

    {open && <div className="modal-backdrop" onMouseDown={(event) => { if (event.target === event.currentTarget) setOpen(false); }}><section className="form-modal wide-modal" role="dialog" aria-modal="true" aria-labelledby="order-form-title">
      <div className="modal-heading"><div><p className="eyebrow">REGISTRO DE VENTA</p><h2 id="order-form-title">Nuevo pedido</h2></div><button className="close-button" onClick={() => setOpen(false)} aria-label="Cerrar">×</button></div>
      <form className="client-form" onSubmit={createOrder}>
        <label>Cliente<select required value={clientId} onChange={(event) => setClientId(event.target.value)}><option value="">Selecciona un cliente activo</option>{clients.filter((client) => client.activo).map((client) => <option key={client.id} value={client.id}>{client.nombre}{client.documento ? ` · ${client.documento}` : ''}</option>)}</select></label>
        <label>Canal de venta<select value={channel} onChange={(event) => setChannel(event.target.value)}>{CHANNELS.map(([value, label]) => <option value={value} key={value}>{label}</option>)}</select></label>
        <div className="order-items-title"><b>Productos</b><button type="button" className="text-action" onClick={() => setItems([...items, { productoId: '', cantidad: 1 }])}>＋ Agregar producto</button></div>
        {productsWithStock === 0 && <div className="empty-state compact-empty" role="status"><b>{products.length === 0 ? 'No hay productos activos en el catálogo.' : 'Los productos del catálogo no tienen stock disponible.'}</b><span>{products.length === 0 ? 'Ingresa como almacén y registra un producto con su stock inicial.' : 'Ingresa como almacén y registra una entrada de stock para habilitar los pedidos.'} Después vuelve a ventas.</span>{onSignOut && <button type="button" className="text-action" onClick={() => { setOpen(false); onSignOut(); }}>Cambiar a cuenta de almacén</button>}</div>}
        {items.map((item, index) => <div className="order-item-row" key={index}>
          <label>Producto<select required value={item.productoId} onChange={(event) => updateItem(index, 'productoId', event.target.value)}><option value="">Selecciona un producto</option>{products.map((product) => <option key={product.id} value={product.id} disabled={product.stockDisponible < 1}>{product.sku} · {product.nombre} · {money(product.precio)} · disp. {product.stockDisponible}</option>)}</select></label>
          <label>Cantidad<input type="number" min="1" step="1" required value={item.cantidad} onChange={(event) => updateItem(index, 'cantidad', event.target.value)} /></label>
          {items.length > 1 && <button type="button" className="remove-line" onClick={() => setItems(items.filter((_, position) => position !== index))} aria-label="Quitar producto">×</button>}
        </div>)}
        <div className="order-total"><span>Total estimado</span><b>{money(total)}</b><small>El stock se valida y reserva al confirmar.</small></div>
        {error && <p className="alert error">{error}</p>}
        <div className="modal-actions"><button type="button" className="quiet-button" onClick={() => setOpen(false)}>Cancelar</button><button className="primary-button" disabled={saving || !clients.some((client) => client.activo) || productsWithStock === 0}>{saving ? 'Registrando…' : 'Registrar y reservar stock'}</button></div>
      </form>
    </section></div>}
  </section>;
}
