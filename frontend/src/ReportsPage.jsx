import { useCallback, useEffect, useState } from 'react';

const isoDate = (date) => `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
const monthStart = () => { const date = new Date(); date.setDate(1); return isoDate(date); };
const today = () => isoDate(new Date());
const money = (value) => new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN' }).format(value || 0);
const channelLabel = { WHATSAPP: 'WhatsApp', FACEBOOK: 'Facebook', TIENDA: 'Tienda', OTRO: 'Otro' };

export default function ReportsPage({ auth }) {
  const [from, setFrom] = useState(monthStart);
  const [to, setTo] = useState(today);
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const loadReport = useCallback(async () => {
    setLoading(true); setError('');
    try {
      const params = new URLSearchParams({ desde: from, hasta: to });
      const response = await fetch(`/api/reportes/resumen?${params}`, { headers: { Authorization: auth } });
      if (!response.ok) {
        let message = 'No se pudo generar el reporte.';
        try { const body = await response.json(); message = body.detail || body.message || message; } catch { /* respuesta sin cuerpo JSON */ }
        throw new Error(message);
      }
      setReport(await response.json());
    } catch (exception) { setError(exception.message || 'No se pudo conectar con el servicio de reportes.'); }
    finally { setLoading(false); }
  }, [auth, from, to]);

  useEffect(() => { loadReport(); }, [loadReport]);

  const maxDailySales = Math.max(1, ...(report?.ventasDiarias || []).map((item) => Number(item.ventas)));
  const maxChannelSales = Math.max(1, ...(report?.ventasPorCanal || []).map((item) => Number(item.ventas)));

  return <section className="feature-page reports-page">
    <header className="feature-heading"><div><p className="eyebrow">ANALÍTICA · SPRINT 4</p><h1>Reportes de gestión</h1><p className="muted">Ventas despachadas, canales, productos y alertas de inventario.</p></div></header>
    <section className="client-panel report-filter"><div className="report-filter-fields"><label>Desde<input type="date" value={from} max={to} onChange={(event) => setFrom(event.target.value)} /></label><label>Hasta<input type="date" value={to} min={from} onChange={(event) => setTo(event.target.value)} /></label><button className="primary-button" onClick={loadReport} disabled={loading}>{loading ? 'Actualizando…' : 'Actualizar reporte'}</button></div><small>Las ventas se calculan con pedidos despachados dentro del periodo seleccionado.</small></section>
    {error && <p className="alert error" role="alert">{error}</p>}
    {report && <>
      <div className="summary-grid report-kpis">
        <article className="summary-card"><span>Ventas netas</span><strong>{money(report.ventas)}</strong><small>Pedidos despachados en el periodo</small></article>
        <article className="summary-card"><span>Pedidos registrados</span><strong>{report.pedidos}</strong><small>{report.despachados} despachados · {report.pendientes} pendientes</small></article>
        <article className="summary-card"><span>Ticket promedio</span><strong>{money(report.ticketPromedio)}</strong><small>Por pedido despachado</small></article>
        <article className="summary-card"><span>Alertas de stock</span><strong className={report.productosConStockBajo.length ? 'warning-number' : ''}>{report.productosConStockBajo.length}</strong><small>Productos en o bajo el mínimo</small></article>
      </div>

      <div className="reports-grid">
        <section className="client-panel chart-panel"><div className="panel-heading"><div><p className="eyebrow">TENDENCIA</p><h2>Ventas por día</h2></div></div>
          {report.ventasDiarias.length ? <div className="daily-chart" role="img" aria-label="Gráfico de ventas por día">
            {report.ventasDiarias.map((item) => <div className="daily-column" key={item.fecha} title={`${item.fecha}: ${money(item.ventas)}`}><span className="daily-value">{money(item.ventas)}</span><div className="daily-track"><i style={{ height: `${Math.max(5, Number(item.ventas) / maxDailySales * 100)}%` }} /></div><small>{new Date(`${item.fecha}T12:00:00`).toLocaleDateString('es-PE', { day: '2-digit', month: 'short' })}</small></div>)}
          </div> : <div className="empty-state compact-empty">No hay ventas despachadas en este periodo.</div>}
        </section>

        <section className="client-panel channel-panel"><div className="panel-heading"><div><p className="eyebrow">ORIGEN</p><h2>Ventas por canal</h2></div></div>
          {report.ventasPorCanal.length ? <div className="channel-list">{report.ventasPorCanal.map((item) => <div className="channel-row" key={item.canal}><div className="channel-label"><b>{channelLabel[item.canal] || item.canal}</b><small>{item.pedidos} pedidos</small></div><div className="channel-bar"><i style={{ width: `${Math.max(2, Number(item.ventas) / maxChannelSales * 100)}%` }} /></div><strong>{money(item.ventas)}</strong></div>)}</div> : <div className="empty-state compact-empty">Sin ventas despachadas por canal.</div>}
        </section>
      </div>

      <div className="reports-grid lower-reports">
        <section className="client-panel"><div className="panel-heading"><div><p className="eyebrow">DEMANDA</p><h2>Productos más vendidos</h2></div></div>
          <div className="table-wrap"><table><thead><tr><th>PRODUCTO</th><th>UNIDADES</th><th>VENTAS</th></tr></thead><tbody>{report.productosMasVendidos.map((product) => <tr key={product.productoId}><td><div className="product-cell"><b>{product.nombre}</b><small>{product.sku}</small></div></td><td>{product.unidades}</td><td>{money(product.ventas)}</td></tr>)}</tbody></table>{!report.productosMasVendidos.length && <div className="empty-state compact-empty">Sin productos vendidos en el periodo.</div>}</div>
        </section>
        <section className="client-panel"><div className="panel-heading"><div><p className="eyebrow">REPOSICIÓN</p><h2>Stock bajo</h2></div><span className="badge inactive">{report.productosConStockBajo.length} alertas</span></div>
          <div className="table-wrap"><table><thead><tr><th>PRODUCTO</th><th>DISPONIBLE</th><th>MÍNIMO</th></tr></thead><tbody>{report.productosConStockBajo.map((product) => <tr key={product.productoId}><td><div className="product-cell"><b>{product.nombre}</b><small>{product.sku}</small></div></td><td><span className="low-stock">{product.disponible}</span></td><td>{product.minimo}</td></tr>)}</tbody></table>{!report.productosConStockBajo.length && <div className="empty-state compact-empty">Sin alertas de reposición.</div>}</div>
        </section>
      </div>
    </>}
    <footer className="panel-footer report-footnote">Pedidos cancelados, en preparación y devueltos no se cuentan como ventas netas despachadas.</footer>
  </section>;
}
