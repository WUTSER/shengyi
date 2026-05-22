<template>
  <div v-if="state.visible" class="confirm-mask">
    <section class="confirm-dialog">
      <header class="confirm-header">
        <h2>{{ state.title }}</h2>
        <button type="button" title="关闭" @click="$emit('close', false)">×</button>
      </header>
      <div class="confirm-body">
        <span class="confirm-icon">!</span>
        <p>{{ state.message }}</p>
      </div>
      <footer class="confirm-footer">
        <button class="btn ghost" type="button" @click="$emit('close', false)">取消</button>
        <button class="btn primary" type="button" @click="$emit('close', true)">确定</button>
      </footer>
    </section>
  </div>
</template>

<script setup>
defineProps({
  state: { type: Object, required: true }
})

defineEmits(['close'])
</script>
