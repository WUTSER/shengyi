<script setup>
import { RouterView } from 'vue-router'
import { onMounted } from 'vue'
import { useMasterData } from '../stores/masterData'
import { useFeedback } from '../composables/useFeedback'
import AppToast from './common/AppToast.vue'
import ConfirmDialog from './common/ConfirmDialog.vue'

const { init } = useMasterData()
const { toast, confirmState, closeConfirm } = useFeedback()

onMounted(async () => {
  await init()
})
</script>

<template>
  <main class="app-shell">
    <RouterView />
    <AppToast :toast="toast" />
    <ConfirmDialog :state="confirmState" @close="closeConfirm" />
  </main>
</template>
