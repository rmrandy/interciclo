<script setup lang="ts">
import { ref } from 'vue'
import { AirlineApiClient } from '../utils/airlineApi'

defineOptions({ name: 'CommentItem' })

const props = defineProps<{ node: any; flightId: number; currentUserId: number | null }>()
const emit = defineEmits<{ (e: 'refresh'): void }>()

const api = new AirlineApiClient()
const openReply = ref(false)
const text = ref('')
const rating = ref(5) // valor interno para backend; no se muestra en respuestas

const doReply = async () => {
  if (!props.currentUserId) { alert('Inicia sesión para responder'); return }
  await api.createFlightReview(props.flightId, {
    userId: props.currentUserId,
    rating: rating.value,
    comment: text.value,
    parentReviewId: props.node.idReview,
  })
  text.value = ''
  rating.value = 5
  openReply.value = false
  emit('refresh')
}
</script>

<template>
  <div class="border rounded-lg p-3">
    <div class="flex items-center justify-between mb-1">
      <div class="text-sm text-gray-700">
        <strong>{{ (node.user && (node.user.firstName || node.user.name)) ? ((node.user.firstName || node.user.name) + (node.user.lastName ? (' ' + node.user.lastName) : '')) : 'Anónimo' }}</strong>
        <span class="text-xs text-gray-400 ml-2">{{ node.createdAt || '' }}</span>
      </div>
      <div v-if="!node.parentReviewId" class="flex items-center gap-2 text-yellow-500">
        <span v-for="i in 5" :key="i">{{ i <= (node.rating || 0) ? '★' : '☆' }}</span>
      </div>
    </div>
    <div class="text-sm text-gray-800 whitespace-pre-wrap">{{ node.reviewText || node.comment || '' }}</div>
    <div class="mt-2 flex gap-3 text-sm">
      <button class="text-blue-600" @click="openReply = !openReply">{{ openReply ? 'Ocultar' : 'Responder' }}</button>
    </div>
    <div v-if="openReply" class="mt-2 pl-4">
      <textarea v-model="text" rows="2" class="airline-input w-full" placeholder="Tu respuesta…"></textarea>
      <div class="mt-2 flex gap-2">
        <button class="btn-primary" @click="doReply">Responder</button>
        <button class="btn-secondary" @click="openReply = false">Cancelar</button>
      </div>
    </div>
    <div class="mt-3 space-y-2 pl-4 border-l" v-if="node.children && node.children.length">
      <CommentItem v-for="c in node.children" :key="c.idReview" :node="c" :flight-id="flightId" :current-user-id="currentUserId" @refresh="emit('refresh')" />
    </div>
  </div>
</template>

