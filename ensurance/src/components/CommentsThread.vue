<script setup lang="ts">
import CommentItem from './CommentItem.vue'

type ReviewNode = any
const props = defineProps<{ items: ReviewNode[]; flightId: number; currentUserId: number | null }>()
const emit = defineEmits<{ (e: 'refresh'): void }>()
</script>

<template>
  <div class="space-y-3">
    <CommentItem v-for="n in items" :key="n.idReview" :node="n" :flight-id="flightId" :current-user-id="currentUserId" @refresh="emit('refresh')" />
  </div>
</template>


