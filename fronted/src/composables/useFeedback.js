import { reactive } from 'vue'

export function useFeedback() {
  const toast = reactive({ visible: false, message: '', type: 'warning', timer: 0 })
  const confirmState = reactive({ visible: false, title: '提示', message: '', resolve: null })

  function showToast(message, type = 'warning') {
    window.clearTimeout(toast.timer)
    toast.message = message
    toast.type = type
    toast.visible = true
    toast.timer = window.setTimeout(() => {
      toast.visible = false
    }, 2400)
  }

  function confirmDialog(message, title = '提示') {
    return new Promise((resolve) => {
      confirmState.title = title
      confirmState.message = message
      confirmState.resolve = resolve
      confirmState.visible = true
    })
  }

  function closeConfirm(result) {
    const resolve = confirmState.resolve
    confirmState.visible = false
    confirmState.resolve = null
    if (resolve) resolve(result)
  }

  return {
    toast,
    confirmState,
    showToast,
    confirmDialog,
    closeConfirm
  }
}
