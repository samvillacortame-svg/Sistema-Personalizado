package pe.sie.gestion.pedidos;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import pe.sie.gestion.clientes.Cliente;

@Entity
@Table(name = "pedidos")
public class Pedido {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private CanalPedido canal;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private EstadoPedido estado = EstadoPedido.RECIBIDO;
    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;
    @Column(name = "despachado_en")
    private LocalDateTime despachadoEn;
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PedidoDetalle> detalles = new ArrayList<>();

    protected Pedido() { }

    public Pedido(Cliente cliente, CanalPedido canal) {
        this.cliente = cliente;
        this.canal = canal;
        this.estado = EstadoPedido.CONFIRMADO;
    }

    public void agregarDetalle(PedidoDetalle detalle) {
        detalles.add(detalle);
        detalle.asociarPedido(this);
    }

    public void cambiarEstado(EstadoPedido estado) { this.estado = estado; }
    public void marcarDespachado(LocalDateTime fecha) {
        this.estado = EstadoPedido.DESPACHADO;
        this.despachadoEn = fecha;
    }
    public Long getId() { return id; }
    public Cliente getCliente() { return cliente; }
    public CanalPedido getCanal() { return canal; }
    public EstadoPedido getEstado() { return estado; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public LocalDateTime getDespachadoEn() { return despachadoEn; }
    public List<PedidoDetalle> getDetalles() { return detalles; }
}
