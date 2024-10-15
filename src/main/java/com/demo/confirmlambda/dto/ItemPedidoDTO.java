package com.demo.confirmlambda.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true) // Ignora campos desconocidos, como "precioTotal"
public class ItemPedidoDTO {
    private String productoId;      // ID del producto
    private String nombreProducto;  // Nombre del producto
    private int cantidad;           // Cantidad de productos
    private double precioUnitario;  // Precio unitario del producto

    // Constructor sin argumentos
    public ItemPedidoDTO() {
    }

    // Constructor con todos los argumentos
    public ItemPedidoDTO(String productoId, String nombreProducto, int cantidad, double precioUnitario) {
        this.productoId = productoId;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    // Getters y setters
    public String getProductoId() {
        return productoId;
    }

    public void setProductoId(String productoId) {
        this.productoId = productoId;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    // Método para calcular el precio total de los ítems
    public double getPrecioTotal() {
        return cantidad * precioUnitario;
    }
}
