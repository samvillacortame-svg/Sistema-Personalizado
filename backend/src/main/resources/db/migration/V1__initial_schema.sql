CREATE TABLE clientes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(160) NOT NULL,
    documento VARCHAR(24) NULL,
    telefono VARCHAR(32) NULL,
    correo VARCHAR(160) NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_cliente_documento (documento)
);

CREATE TABLE productos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sku VARCHAR(60) NOT NULL UNIQUE,
    nombre VARCHAR(180) NOT NULL,
    descripcion VARCHAR(500) NULL,
    precio DECIMAL(12, 2) NOT NULL,
    stock_minimo INT NOT NULL DEFAULT 0,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE pedidos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cliente_id BIGINT NOT NULL,
    canal ENUM('WHATSAPP', 'FACEBOOK', 'TIENDA', 'OTRO') NOT NULL,
    estado ENUM(
        'RECIBIDO', 'PENDIENTE_STOCK', 'CONFIRMADO', 'PREPARACION',
        'DESPACHADO', 'CANCELADO', 'DEVUELTO'
    ) NOT NULL DEFAULT 'RECIBIDO',
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pedido_cliente FOREIGN KEY (cliente_id) REFERENCES clientes (id)
);

CREATE TABLE pedido_detalles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    pedido_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(12, 2) NOT NULL,
    CONSTRAINT fk_detalle_pedido FOREIGN KEY (pedido_id) REFERENCES pedidos (id),
    CONSTRAINT fk_detalle_producto FOREIGN KEY (producto_id) REFERENCES productos (id)
);

CREATE TABLE movimientos_inventario (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    producto_id BIGINT NOT NULL,
    pedido_id BIGINT NULL,
    tipo ENUM('ENTRADA', 'RESERVA', 'SALIDA', 'AJUSTE', 'DEVOLUCION') NOT NULL,
    cantidad INT NOT NULL,
    motivo VARCHAR(240) NULL,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_movimiento_producto FOREIGN KEY (producto_id) REFERENCES productos (id),
    CONSTRAINT fk_movimiento_pedido FOREIGN KEY (pedido_id) REFERENCES pedidos (id)
);
