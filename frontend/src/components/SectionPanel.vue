<template>
  <section class="section-panel">
    <header class="section-title">
      <div>
        <span class="title-line"></span>
        <strong>{{ title }}</strong>
        <small v-if="subtitle">{{ subtitle }}</small>
      </div>
      <div class="section-title-actions">
        <button v-if="actionText" class="section-action" type="button" @click="$emit('action')">{{ actionText }}</button>
        <button v-if="collapsible" class="section-toggle" type="button" @click="collapsed = !collapsed">
          <ChevronUp v-if="!collapsed" :size="15" />
          <ChevronDown v-else :size="15" />
        </button>
      </div>
    </header>
    <div v-show="!collapsed" class="section-body">
      <slot />
    </div>
  </section>
</template>

<script setup>
import { ref } from 'vue'
import { ChevronDown, ChevronUp } from 'lucide-vue-next'

const props = defineProps({
  title: { type: String, required: true },
  subtitle: { type: String, default: '' },
  actionText: { type: String, default: '' },
  collapsible: { type: Boolean, default: true },
  initiallyCollapsed: { type: Boolean, default: false }
})

defineEmits(['action'])

const collapsed = ref(props.initiallyCollapsed)
</script>

<style scoped>
.section-toggle {
  background: none;
  border: none;
  padding: 0;
  cursor: pointer;
  color: #666;
  display: flex;
  align-items: center;
}

.section-toggle:hover {
  color: #333;
}
</style>
