import { useEffect, useState } from 'react';

const modules = [
  ['01', 'Clientes', 'Ficha e historial comercial'],
  ['02', 'Pedidos', 'Registro y seguimiento multicanal'],
  ['03', 'Inventario', 'Existencias y movimientos'],
  ['04', 'Reportes', 'Información para decisiones'],
];

export default function App() {
  const [apiStatus, setApiStatus] = useState('Comprobando conexión');

  useEffect(() => {
    fetch('/api/estado')
      .then((response) => {
        if (!response.ok) throw new Error('La API no respondió correctamente');
        return response.json();
      })
      .then((data) => setApiStatus('API ' + data.api + ' · MySQL ' + data.baseDeDatos))
      .catch(() => setApiStatus('API sin conexión'));
  }, []);

  return (
    <main className="layout">
      <aside>
        <div className="brand"><b>D</b><span>Dermogestión<small>Sistema comercial</small></span></div>
        <p className="label">ESPACIO DE TRABAJO</p>
        {modules.map(([number, name]) => <button key={name}><span>{number}</span>{name}</button>)}
        <div className="note">Prototipo académico<p>Datos de ejemplo únicamente. No ingresar información real de clientes.</p></div>
      </aside>
      <section>
        <header>
          <div><small>SISTEMA PERSONALIZADO INTEGRADO</small><h1>Panel de gestión</h1><p>Clientes, pedidos e inventario en un solo flujo.</p></div>
          <span className="pill">{apiStatus}</span>
        </header>
        <article className="hero">
          <small>SPRINT 1 · BASE TÉCNICA</small>
          <h2>Información conectada para la operación comercial</h2>
          <p>Base inicial de React, Spring Boot y MySQL. El equipo construirá los módulos de forma incremental.</p>
        </article>
        <div className="row"><h2>Alcance del sistema</h2><span className="status">Requisitos por validar</span></div>
        <div className="cards">
          {modules.map(([number, name, description]) =>
            <article className="card" key={name}><small>{number}</small><h3>{name}</h3><p>{description}</p></article>)}
        </div>
        <article className="flow"><small>FLUJO PROPUESTO</small><h2>Del pedido al inventario</h2><p>Registrar pedido → verificar y reservar stock → despachar y actualizar</p></article>
        <footer>Versión inicial · Sprint 1</footer>
      </section>
    </main>
  );
}
