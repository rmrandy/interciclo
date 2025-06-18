<template>
  <div class="container">
    <h1>Checkout</h1>
    <div v-if="cartItems.length === 0" class="empty-cart">
      <p>Tu carrito está vacío.</p>
    </div>
    <div v-else>
      <div class="checkout-form">
        <h2>Datos de Envío</h2>
        <input v-model="shippingAddress" placeholder="Dirección de envío" />
        <h2>Datos de Pago</h2>
        <input v-model="cardNumber" placeholder="Número de tarjeta" />
        <input v-model="cardName" placeholder="Nombre en la tarjeta" />
        <input v-model="cardExpiry" placeholder="Fecha de expiración" />
        <input v-model="cardCvv" placeholder="CVV" />
      </div>
      <div class="order-summary">
        <h2>Resumen de la Compra</h2>
        <div v-for="item in cartItems" :key="item.idMedicine" class="order-item">
          <h3>{{ item.name }}</h3>
          <p>Precio: ${{ item.price }}</p>
          <p>Cantidad: {{ item.quantity }}</p>
        </div>
        <div class="order-total">
          <h3>Total: ${{ total }}</h3>
        </div>
        <button @click="placeOrder">Finalizar Compra</button>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      cartItems: [],
      shippingAddress: '',
      cardNumber: '',
      cardName: '',
      cardExpiry: '',
      cardCvv: ''
    };
  },
  computed: {
    total() {
      return this.cartItems.reduce((sum, item) => sum + item.price * item.quantity, 0);
    }
  },
  methods: {
    async placeOrder() {
      try {
        const order = {
          shippingAddress: this.shippingAddress,
          total: this.total,
          items: this.cartItems.map(item => ({
            productId: item.idMedicine,
            quantity: item.quantity
          }))
        };
        const response = await axios.post('http://localhost:8080/api2/orders', order);
        if (response.status === 201) {
          alert('Compra finalizada con éxito!');
          this.cartItems = [];
          this.shippingAddress = '';
          this.cardNumber = '';
          this.cardName = '';
          this.cardExpiry = '';
          this.cardCvv = '';
        } else {
          alert('Error al finalizar la compra.');
        }
      } catch (error) {
        alert('Error al finalizar la compra: ' + error.message);
      }
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
.checkout-form {
  margin-top: 20px;
}
.order-summary {
  margin-top: 20px;
}
.order-item {
  border: 1px solid #ccc;
  padding: 10px;
  margin-bottom: 10px;
  border-radius: 5px;
}
.order-total {
  margin-top: 20px;
  text-align: right;
}
</style> 