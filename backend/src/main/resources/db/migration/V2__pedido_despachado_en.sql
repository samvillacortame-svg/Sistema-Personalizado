SET @columna_despachado_existe = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'pedidos'
      AND column_name = 'despachado_en'
);

SET @sql_migracion = IF(
    @columna_despachado_existe = 0,
    'ALTER TABLE pedidos ADD COLUMN despachado_en DATETIME NULL',
    'SELECT 1'
);

PREPARE migracion_despacho FROM @sql_migracion;
EXECUTE migracion_despacho;
DEALLOCATE PREPARE migracion_despacho;

UPDATE pedidos
SET despachado_en = creado_en
WHERE estado IN ('DESPACHADO', 'DEVUELTO')
  AND despachado_en IS NULL;
