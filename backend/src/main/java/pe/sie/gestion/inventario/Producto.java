package pe.sie.gestion.inventario;

import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "productos")
public class Producto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 60)
    private String sku;
    @Column(nullable = false, length = 180)
    private String nombre;
    @Column(length = 500)
    private String descripcion;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;
    @Column(name = "stock_minimo", nullable = false)
    private int stockMinimo;
    @Column(nullable = false)
    private boolean activo = true;

    protected Producto() { }

    public Producto(String sku, String nombre, String descripcion, BigDecimal precio, int stockMinimo) {
        this.sku = sku;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stockMinimo = stockMinimo;
    }

    public Long getId() { return id; }
    public String getSku() { return sku; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public BigDecimal getPrecio() { return precio; }
    public int getStockMinimo() { return stockMinimo; }
    public boolean isActivo() { return activo; }
}
