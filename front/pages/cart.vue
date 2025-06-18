<template>
  <div class="container">
    <h1>Carrito de Compras</h1>
    <div v-if="cartItems.length === 0" class="empty-cart">
      <p>Tu carrito está vacío.</p>
    </div>
    <div v-else class="cart-items">
      <div v-for="item in cartItems" :key="item.idMedicine" class="cart-item">
        <h3>{{ item.name }}</h3>
        <p>Precio: ${{ item.price }}</p>
        <p>Cantidad: {{ item.quantity }}</p>
        <button @click="removeFromCart(item.idMedicine)">Eliminar</button>
      </div>
      <div class="cart-total">
        <h3>Total: ${{ total }}</h3>
        <button @click="checkout">Proceder al Checkout</button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      cartItems: []
    };
  },
  computed: {
    total() {
      return this.cartItems.reduce((sum, item) => sum + item.price * item.quantity, 0);
    }
  },
  methods: {
    removeFromCart(id) {
      this.cartItems = this.cartItems.filter(item => item.idMedicine !== id);
    },
    checkout() {
      // Lógica para proceder al checkout
      alert('Procediendo al checkout...');
    }
  }
};
</script>

<style scoped>
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
.empty-cart {
  text-align: center;
  margin-top: 20px;
}
.cart-items {
  margin-top: 20px;
}
.cart-item {
  border: 1px solid #ccc;
  padding: 10px;
  margin-bottom: 10px;
  border-radius: 5px;
}
.cart-total {
  margin-top: 20px;
  text-align: right;
}
</style> 