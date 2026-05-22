<template>
  <section class="section-panel">
    <header class="section-title">
      <div>
        <span class="title-line"></span>
        <strong>{{ title }}</strong>
        <small v-if="subtitle">{{ subtitle }}</small>
      </div>
      <button v-if="actionText" class="section-action" type="button" @click="$emit('action')">{{ actionText }}</button>
      <ChevronUp v-else :size="15" />
    </header>
    <div class="section-body">
      <slot />
    </div>
  </section>
</template>

<script setup>
import { ChevronUp } from 'lucide-vue-next'

defineProps({
  title: { type: String, required: true },
  subtitle: { type: String, default: '' },
  actionText: { type: String, default: '' }
})

defineEmits(['action'])
</script>
