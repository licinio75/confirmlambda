package com.demo.confirmlambda.dto;

import java.util.List;

public class PedidoSQSMessageDTO {

    private String pedidoId;               // ID del pedido
    private String usuarioNombre;  // Nombre del usuario que realiza el pedido
    private String usuarioEmail;   // Email del usuario que realiza el pedido
    private List<ItemPedidoDTO> items;  // Lista de ítems pedidos
    private double precioTotal;    // Precio total del pedido

    // Constructor sin argumentos
    public PedidoSQSMessageDTO() {
    }

    // Constructor con todos los argumentos
    public PedidoSQSMessageDTO(String pedidoId, String usuarioNombre, String usuarioEmail, List<ItemPedidoDTO> items, double precioTotal) {
        this.pedidoId = pedidoId;
        this.usuarioNombre = usuarioNombre;
        this.usuarioEmail = usuarioEmail;
        this.items = items;
        this.precioTotal = precioTotal;
    }

    // Getters y setters
    public String getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(String pedidoId) {
        this.pedidoId = pedidoId;
    }

    public String getUsuarioNombre() {
        return usuarioNombre;
    }

    public void setUsuarioNombre(String usuarioNombre) {
        this.usuarioNombre = usuarioNombre;
    }

    public String getUsuarioEmail() {
        return usuarioEmail;
    }

    public void setUsuarioEmail(String usuarioEmail) {
        this.usuarioEmail = usuarioEmail;
    }

    public List<ItemPedidoDTO> getItems() {
        return items;
    }

    public void setItems(List<ItemPedidoDTO> items) {
        this.items = items;
    }

    public double getPrecioTotal() {
        return precioTotal;
    }

    public void setPrecioTotal(double precioTotal) {
        this.precioTotal = precioTotal;
    }
}
