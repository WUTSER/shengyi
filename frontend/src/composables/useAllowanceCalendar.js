import { computed, ref } from 'vue'
import { getAllowanceCalendar, saveAllowanceCalendar as saveAllowanceCalendarApi } from '../api/travelReimbursements'

export function useAllowanceCalendar(headerForm, trips, isReadonly, emit) {
  const allowanceModal = ref({ visible: false })
  const selectedAllowance = ref(null)
  const calendarRows = ref([])
  const calendarLoading = ref(false)
  const calendarSaving = ref(false)
  const calendarError = ref('')

  const allowanceTrip = computed(() => trips.value.find((trip) => trip.tripId === selectedAllowance.value?.tripId))
  const allowanceRouteStart = computed(() => allowanceTrip.value?.departureCityName || routePart(selectedAllowance.value?.route, 0))
  const allowanceRouteEnd = computed(() => allowanceTrip.value?.arrivalCityName || routePart(selectedAllowance.value?.route, 1))
  const calendarStandardAmount = computed(() => sumCalendarItems('standardAmount'))
  const calendarAllowanceAmount = computed(() => sumCalendarItems('amount'))
  const isAllCalendarChecked = computed(() =>
    calendarRows.value.length > 0 &&
    calendarRows.value.every((row) => row.meal.checked && row.traffic.checked && row.communication.checked)
  )

  async function openAllowanceModal(allowance) {
    selectedAllowance.value = allowance
    allowanceModal.value.visible = true
    calendarLoading.value = true
    calendarSaving.value = false
    calendarError.value = ''
    calendarRows.value = []
    try {
      const rows = await getAllowanceCalendar(headerForm.reimbursementId, allowance.allowanceId)
      calendarRows.value = rows.map(cloneCalendarRow)
    } catch (error) {
      calendarError.value = error.message
    } finally {
      calendarLoading.value = false
    }
  }

  function closeAllowanceModal() {
    allowanceModal.value.visible = false
    selectedAllowance.value = null
    calendarRows.value = []
    calendarError.value = ''
  }

  async function saveAllowanceCalendar() {
    const validation = validateCalendarRows()
    if (validation) {
      calendarError.value = validation
      return
    }

    calendarSaving.value = true
    calendarError.value = ''
    try {
      await saveAllowanceCalendarApi(headerForm.reimbursementId, selectedAllowance.value.allowanceId, {
        items: calendarRows.value.map((row) => ({
          calendarId: row.calendarId,
          meal: toSaveItem(row.meal),
          traffic: toSaveItem(row.traffic),
          communication: toSaveItem(row.communication)
        }))
      })
      closeAllowanceModal()
      emit('changed', headerForm.reimbursementId)
    } catch (error) {
      calendarError.value = error.message
    } finally {
      calendarSaving.value = false
    }
  }

  function cloneCalendarRow(row) {
    return {
      ...row,
      meal: cloneAllowanceItem(row.meal),
      traffic: cloneAllowanceItem(row.traffic),
      communication: cloneAllowanceItem(row.communication)
    }
  }

  function cloneAllowanceItem(item) {
    return {
      checked: Boolean(item?.checked),
      standardAmount: Number(item?.standardAmount || 0),
      amount: Number(item?.amount || 0)
    }
  }

  function toSaveItem(item) {
    return {
      checked: item.checked,
      amount: item.checked ? Number(item.amount || 0) : 0
    }
  }

  function routePart(route, index) {
    if (!route) return '-'
    return route.split('-')[index]?.trim() || '-'
  }

  function eachAllowanceItem(callback) {
    calendarRows.value.forEach((row) => {
      callback(row.meal, row, 'meal')
      callback(row.traffic, row, 'traffic')
      callback(row.communication, row, 'communication')
    })
  }

  function sumCalendarItems(field) {
    let total = 0
    eachAllowanceItem((item) => {
      if (item.checked) {
        total += Number(item[field] || 0)
      }
    })
    return total
  }

  function isRowChecked(row) {
    return row.meal.checked && row.traffic.checked && row.communication.checked
  }

  function isColumnChecked(key) {
    return calendarRows.value.length > 0 && calendarRows.value.every((row) => row[key].checked)
  }

  function toggleAllCalendar(checked) {
    if (isReadonly.value) return
    eachAllowanceItem((item) => setItemChecked(item, checked))
  }

  function toggleRow(row, checked) {
    if (isReadonly.value) return
    setItemChecked(row.meal, checked)
    setItemChecked(row.traffic, checked)
    setItemChecked(row.communication, checked)
  }

  function toggleColumn(key, checked) {
    if (isReadonly.value) return
    calendarRows.value.forEach((row) => setItemChecked(row[key], checked))
  }

  function setItemChecked(item, checked) {
    item.checked = checked
    normalizeItem(item)
  }

  function normalizeItem(item) {
    if (!item.checked) {
      item.amount = 0
      return
    }
    if (!Number(item.amount)) {
      item.amount = Number(item.standardAmount || 0)
    }
    clampItem(item)
  }

  function clampItem(item) {
    if (!item.checked) {
      item.amount = 0
      return
    }
    const standard = Number(item.standardAmount || 0)
    const amount = Number(item.amount || 0)
    item.amount = Math.min(Math.max(amount, 0), standard)
  }

  function validateCalendarRows() {
    let message = ''
    eachAllowanceItem((item) => {
      if (message) return
      const amount = Number(item.amount || 0)
      const standard = Number(item.standardAmount || 0)
      if (!item.checked && amount !== 0) {
        message = '未选中的补助金额必须为0'
      } else if (item.checked && amount <= 0) {
        message = '选中的补助金额必须大于0'
      } else if (item.checked && amount > standard) {
        message = '补助金额不能大于标准金额'
      }
    })
    return message
  }

  return {
    allowanceModal,
    selectedAllowance,
    calendarRows,
    calendarLoading,
    calendarSaving,
    calendarError,
    allowanceTrip,
    allowanceRouteStart,
    allowanceRouteEnd,
    calendarStandardAmount,
    calendarAllowanceAmount,
    isAllCalendarChecked,
    openAllowanceModal,
    closeAllowanceModal,
    saveAllowanceCalendar,
    isRowChecked,
    isColumnChecked,
    toggleAllCalendar,
    toggleRow,
    toggleColumn,
    normalizeItem,
    clampItem
  }
}
