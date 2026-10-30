import { useCallback, useEffect, useState } from 'react';

const money = (value) => new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN' }).format(value || 0);
const dateTime = (value) => new Date(/[zZ]|[+-]\d{2}:\d{2}$/.test(value) ? value : `${value}Z`).toLocaleString('es-PE');
const stateLabel = { CONFIRMADO: 'Confirmado', PREPARACION: 'En preparación', DESPACHADO: 'Despachado', CANCELADO: 'Cancelado', DEVUELTO: 'Devuelto' };
async function message(response) {
  try { const body = await response.json(); return body.detail || body.message || 'Ocurrió un error.'; }
  catch { return response.status === 403 ? 'Esta cuenta no tiene el rol de almacén. Cierra sesión e ingresa como almacen_demo.' : 'Ocurrió un error.'; }
}

export default function InventoryPage({ auth, role, onSignOut }) {
  const canManageInventory = role === 'ALMACEN' || role === 'ADMIN';
  const [products, setProducts] = useState([]);
  const [moves, setMoves] = useState([]);
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [productOpen, setProductOpen] = useState(false);
  const [productSaving, setProductSaving] = useState(false);
  const [productForm, setProductForm] = useState({ sku: '', nombre: '', descripcion: '', precio: '', stockMinimo: 0, stockInicial: 0 });
  const [movementForm, setMovementForm] = useState({ productoId: '', tipo: 'ENTRADA', cantidad: '', motivo: '' });

  const refresh = useCallback(async () => {
    setLoading(true); setError('');
    try {
      const paths = canManageInventory
        ? ['/api/almacen/productos?incluirInactivos=true', '/api/almacen/movimientos', '/api/almacen/pedidos']
        : ['/api/ventas/productos'];
      const responses = await Promise.all(paths.map((path) => fetch(path, { headers: { Authorization: auth } })));
      const failed = responses.find((response) => !response.ok);
      if (failed) throw Object.assign(new Error(await message(failed)), { status: failed.status });
      const data = await Promise.all(responses.map((response) => response.json()));
      const [productsData, movesData = [], ordersData = []] = data;
      setProducts(productsData); setMoves(movesData); setOrders(ordersData);
      const firstActiveProduct = productsData.find((product) => product.activo);
      if (canManageInventory && !movementForm.productoId && firstActiveProduct) {
        setMovementForm((current) => ({ ...current, productoId: String(firstActiveProduct.id) }));
      }
    } catch (exception) {
      if (exception.status === 403) setError('Para administrar el inventario, cierra sesión e ingresa con almacen_demo / almacen_local_demo.');
      else setError(exception.message || 'No se pudo conectar con el almacén.');
    } finally { setLoading(false); }
  }, [auth, canManageInventory, movementForm.productoId]);

  useEffect(() => { refresh(); }, [refresh]);

  async function createProduct(event) {
    event.preventDefault(); setProductSaving(true); setError(''); setNotice('');
    try {
      const response = await fetch('/api/almacen/productos', {
        method: 'POST', headers: { Authorization: auth, 'Content-Type': 'application/json' },
        body: JSON.stringify({ ...productForm, precio: Number(productForm.precio), stockMinimo: Number(productForm.stockMinimo), stockInicial: Number(productForm.stockInicial) }),
      });
      if (!response.ok) throw new Error(await message(response));
      setProductOpen(false); setProductForm({ sku: '', nombre: '', descripcion: '', precio: '', stockMinimo: 0, stockInicial: 0 });
      setNotice('Producto creado y stock inicial registrado.'); await refresh();
    } catch (exception) { setError(exception.message || 'No se pudo crear el producto.'); }
    finally { setProductSaving(false); }
  }

  async function addMovement(event) {
    event.preventDefault(); setError(''); setNotice('');
    try {
      const response = await fetch(`/api/almacen/productos/${movementForm.productoId}/movimientos`, {
        method: 'POST', headers: { Authorization: auth, 'Content-Type': 'application/json' },
        body: JSON.stringify({ tipo: movementForm.tipo, cantidad: Number(movementForm.cantidad), motivo: movementForm.motivo || null }),
      });
      if (!response.ok) throw new Error(await message(response));
      setMovementForm((current) => ({ ...current, cantidad: '', motivo: '' }));
      setNotice(movementForm.tipo === 'ENTRADA' ? 'Entrada de stock registrada.' : 'Ajuste de inventario registrado.'); await refresh();
    } catch (exception) { setError(exception.message || 'No se pudo registrar el movimiento.'); }
  }

  async function orderAction(order, action, label) {
    setError(''); setNotice('');
    try {
      const response = await fetch(`/api/almacen/pedidos/${order.id}/${action}`, { method: 'POST', headers: { Authorization: auth } });
      if (!response.ok) throw new Error(await message(response));
      setNotice(`Pedido #${order.id}: ${label.toLowerCase()}.`); await refresh();
    } catch (exception) { setError(exception.message || 'No se pudo actualizar el pedido.'); }
  }

  const pendingOrders = orders.filter((order) => ['CONFIRMADO', 'PREPARACION'].includes(order.estado));

  return <section className="feature-page">
    <header className="feature-heading"><div><p className="eyebrow">{canManageInventory ? 'OPERACIONES · SPRINT 3' : 'VENTAS · CATÁLOGO'}</p><h1>{canManageInventory ? 'Inventario y despacho' : 'Productos disponibles'}</h1><p className="muted">{canManageInventory ? 'Controla existencias, movimientos y preparación de pedidos.' : 'Consulta los productos activos y el stock disponible para registrar pedidos.'}</p></div>{canManageInventory && <button className="primary-button" onClick={() => setProductOpen(true)}>＋ Nuevo producto</button>}</header>
    {notice && <p className="alert success">{notice}</p>}{error && <p className="alert error">{error}</p>}
    {error.includes('almacen_demo') ? <article className="role-hint"><b>Acceso de almacén requerido</b><p>Usa Cerrar sesión e inicia con <code>almacen_demo</code> y <code>almacen_local_demo</code>. Las ventas y almacén tienen permisos separados.</p></article> : <>
      <div className="summary-grid order-summary">
        <article className="summary-card"><span>Productos en catálogo</span><strong>{products.filter((p) => p.activo).length}</strong><small>SKU activos</small></article>
        <article className="summary-card"><span>Stock bajo</span><strong className={products.some((p) => p.stockBajo && p.activo) ? 'warning-number' : ''}>{products.filter((p) => p.stockBajo && p.activo).length}</strong><small>Igual o menor al mínimo configurado</small></article>
        {canManageInventory && <article className="summary-card"><span>Pedidos por preparar</span><strong>{pendingOrders.length}</strong><small>Confirmados o en preparación</small></article>}
      </div>

      <div className="inventory-grid" style={canManageInventory ? undefined : { gridTemplateColumns: '1fr' }}>
        <section className="client-panel inventory-products"><div className="panel-heading"><div><p className="eyebrow">CATÁLOGO</p><h2>{canManageInventory ? 'Productos y existencias' : 'Catálogo para pedidos'}</h2></div></div>
          <div className="table-wrap"><table><thead><tr><th>PRODUCTO</th><th>PRECIO</th>{canManageInventory && <><th>FÍSICO</th><th>RESERVADO</th></>}<th>DISPONIBLE</th></tr></thead><tbody>
            {products.map((product) => <tr key={product.id}><td><div className="product-cell"><b>{product.nombre}</b><small>{product.sku}{!product.activo ? ' · Inactivo' : ''}</small></div></td><td>{money(product.precio)}</td>{canManageInventory && <><td>{product.stockFisico}</td><td>{product.stockReservado}</td></>}<td><span className={`stock-value ${product.stockBajo ? 'low-stock' : ''}`}>{product.stockDisponible}</span></td></tr>)}
          </tbody></table>{!loading && products.length === 0 && <div className="empty-state"><b>Aún no hay productos</b><span>{canManageInventory ? 'Crea un producto e ingresa su stock inicial.' : 'Almacén debe registrar productos activos para que ventas pueda consultarlos y crear pedidos.'}</span>{!canManageInventory && onSignOut && <button type="button" className="text-action" onClick={onSignOut}>Cambiar a cuenta de almacén</button>}</div>}{loading && <div className="empty-state">Cargando inventario…</div>}</div>
        </section>
        {canManageInventory && <>
        <section className="client-panel movement-panel"><p className="eyebrow">MOVIMIENTO MANUAL</p><h2>Actualizar stock</h2><p className="muted">Las salidas de pedidos se registran automáticamente al despachar.</p>
          <form className="movement-form" onSubmit={addMovement}>
            <label>Producto<select required value={movementForm.productoId} onChange={(event) => setMovementForm({ ...movementForm, productoId: event.target.value })}><option value="">Selecciona</option>{products.filter((p) => p.activo).map((p) => <option value={p.id} key={p.id}>{p.sku} · {p.nombre}</option>)}</select></label>
            <label>Tipo<select value={movementForm.tipo} onChange={(event) => setMovementForm({ ...movementForm, tipo: event.target.value })}><option value="ENTRADA">Entrada</option><option value="AJUSTE">Ajuste</option></select></label>
            <label>{movementForm.tipo === 'AJUSTE' ? 'Unidades (+ o -)' : 'Unidades de entrada'}<input type="number" step="1" required value={movementForm.cantidad} onChange={(event) => setMovementForm({ ...movementForm, cantidad: event.target.value })} /></label>
            <label>Motivo<input maxLength="240" value={movementForm.motivo} onChange={(event) => setMovementForm({ ...movementForm, motivo: event.target.value })} placeholder="Compra, conteo físico…" /></label>
            <button className="primary-button" disabled={!products.some((p) => p.activo)}>Registrar movimiento</button>
          </form>
        </section>
        </>}
      </div>

      {canManageInventory && <>
      <section className="client-panel fulfillment-panel"><div className="panel-heading"><div><p className="eyebrow">PREPARACIÓN</p><h2>Pedidos de ventas</h2><p className="muted">El stock reservado queda protegido hasta despachar o cancelar el pedido.</p></div></div>
        <div className="fulfillment-list">{pendingOrders.map((order) => <article className="fulfillment-row" key={order.id}><div><b>Pedido #{order.id} · {order.clienteNombre}</b><small>{order.items.map((item) => `${item.cantidad} × ${item.productoNombre}`).join(' · ')}</small></div><span className={`badge order-status status-${order.estado.toLowerCase()}`}>{stateLabel[order.estado]}</span><div className="actions">{order.estado === 'CONFIRMADO' && <button onClick={() => orderAction(order, 'preparar', 'En preparación')}>Iniciar preparación</button>}{order.estado === 'PREPARACION' && <button onClick={() => orderAction(order, 'despachar', 'Despachado')}>Despachar</button>}</div></article>)}{!pendingOrders.length && <div className="empty-state compact-empty">No hay pedidos pendientes de preparación.</div>}</div>
      </section>

      <section className="client-panel movement-history"><div className="panel-heading"><div><p className="eyebrow">TRAZABILIDAD</p><h2>Movimientos recientes</h2></div></div><div className="table-wrap"><table><thead><tr><th>FECHA</th><th>PRODUCTO</th><th>TIPO</th><th>CANTIDAD</th><th>REFERENCIA</th></tr></thead><tbody>{moves.map((move) => <tr key={move.id}><td>{move.creadoEn ? dateTime(move.creadoEn) : '—'}</td><td>{move.productoNombre}</td><td>{move.tipo}</td><td>{move.cantidad > 0 ? '+' : ''}{move.cantidad}</td><td>{move.pedidoId ? `Pedido #${move.pedidoId}` : move.motivo || '—'}</td></tr>)}</tbody></table>{!moves.length && !loading && <div className="empty-state">Los movimientos aparecerán aquí.</div>}</div></section>
      </>}
    </>}

    {productOpen && <div className="modal-backdrop" onMouseDown={(event) => { if (event.target === event.currentTarget) setProductOpen(false); }}><section className="form-modal" role="dialog" aria-modal="true" aria-labelledby="product-title"><div className="modal-heading"><div><p className="eyebrow">CATÁLOGO</p><h2 id="product-title">Nuevo producto</h2></div><button className="close-button" onClick={() => setProductOpen(false)} aria-label="Cerrar">×</button></div>
      <form className="client-form" onSubmit={createProduct}>
        <label>SKU<input required maxLength="60" value={productForm.sku} onChange={(event) => setProductForm({ ...productForm, sku: event.target.value })} /></label>
        <label>Nombre<input required maxLength="180" value={productForm.nombre} onChange={(event) => setProductForm({ ...productForm, nombre: event.target.value })} /></label>
        <label>Descripción<input maxLength="500" value={productForm.descripcion} onChange={(event) => setProductForm({ ...productForm, descripcion: event.target.value })} /></label>
        <div className="two-fields"><label>Precio (S/)<input type="number" min="0.01" step="0.01" required value={productForm.precio} onChange={(event) => setProductForm({ ...productForm, precio: event.target.value })} /></label><label>Stock mínimo<input type="number" min="0" step="1" value={productForm.stockMinimo} onChange={(event) => setProductForm({ ...productForm, stockMinimo: event.target.value })} /></label></div>
        <label>Stock inicial<input type="number" min="0" step="1" value={productForm.stockInicial} onChange={(event) => setProductForm({ ...productForm, stockInicial: event.target.value })} /></label>
        {error && <p className="alert error">{error}</p>}<div className="modal-actions"><button type="button" className="quiet-button" onClick={() => setProductOpen(false)}>Cancelar</button><button className="primary-button" disabled={productSaving}>{productSaving ? 'Guardando…' : 'Crear producto'}</button></div>
      </form>
    </section></div>}
  </section>;
}
