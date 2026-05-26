import { reactive, ref } from 'vue'
import { loadMasterData } from '../api/masterData'

const options = reactive({
  companies: [],
  departments: [],
  employees: [],
  businessTypes: [],
  cities: [],
  projects: []
})

const loading = ref(false)
const loaded = ref(false)

export function useMasterData() {
  async function init() {
    if (loaded.value) return
    loading.value = true
    try {
      Object.assign(options, await loadMasterData())
      loaded.value = true
    } finally {
      loading.value = false
    }
  }

  return {
    options,
    loading,
    init
  }
}
