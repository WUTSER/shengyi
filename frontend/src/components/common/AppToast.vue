<template>
  <transition name="toast-fade">
    <div v-if="toast.visible" class="app-toast" :class="toast.type">
      <span class="toast-icon">!</span>
      <span>{{ toast.message }}</span>
    </div>
  </transition>
</template>

<script setup>
defineProps({
  toast: { type: Object, required: true }
})
</script>
