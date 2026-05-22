import { computed, ref, watch } from 'vue'
import { formatPercent, parsePercent, roundMoney, roundRatio } from '../utils/formatters'

let splitRowSeed = 0

export function useExpenseSplit(headerForm, totals, savedSplits, isReadonly, notify, confirmAction) {
  const splitRows = ref([])
  const totalAllowanceAmount = computed(() => Number(totals.value.totalAllowanceAmount || 0))
  const otherSplitRatioTotal = computed(() =>
    splitRows.value.slice(1).reduce((sum, row) => sum + Number(row.ratio || 0), 0)
  )
  const firstSplitRatio = computed(() => Math.max(0, roundRatio(1 - otherSplitRatioTotal.value)))
  const splitRatioTotal = computed(() => firstSplitRatio.value + otherSplitRatioTotal.value)
  const splitAmountTotal = computed(() =>
    splitRows.value.reduce((sum, row, index) => sum + splitAmount(row, index), 0)
  )

  watch(
    () => headerForm.reimCompanyId,
    (companyId) => {
      if (splitRows.value[0] && !splitRows.value[0].companyId) {
        splitRows.value[0].companyId = companyId || ''
      }
    },
    { immediate: true }
  )

  watch(
    savedSplits,
    (items) => {
      if (items?.length) {
        splitRows.value = items.map((item, index) => ({
          id: item.splitId || `split-${++splitRowSeed}`,
          companyId: item.reimCompanyId || '',
          projectId: item.projectId || '',
          ratio: Number(item.ratio || 0),
          ratioText: `${formatPercent(item.ratio || 0)} %`,
          persisted: true,
          sequenceNo: item.sequenceNo || index + 1
        }))
        return
      }
      splitRows.value = [createSplitRow(headerForm.reimCompanyId, true)]
    },
    { immediate: true, deep: true }
  )

  function createSplitRow(companyId = '', isFirst = false) {
    return {
      id: `split-${++splitRowSeed}`,
      companyId: isFirst ? companyId || '' : '',
      projectId: '',
      ratio: isFirst ? 1 : 0,
      ratioText: isFirst ? '100.00 %' : '0.00 %'
    }
  }

  function addSplitRow() {
    if (isReadonly.value) return
    splitRows.value.push(createSplitRow())
  }

  async function deleteSplitRow(index) {
    if (isReadonly.value) return
    if (splitRows.value.length <= 1) {
      notify('至少保留一条分摊信息', 'warning')
      return
    }
    if (!(await confirmAction('确定删除?'))) return
    splitRows.value.splice(index, 1)
  }

  function averageSplitRows() {
    if (isReadonly.value || splitRows.value.length === 0) return
    const ratio = roundRatio(1 / splitRows.value.length)
    splitRows.value.forEach((row, index) => {
      if (index > 0) {
        row.ratio = ratio
        row.ratioText = `${formatPercent(ratio)} %`
      }
    })
  }

  function changeSplitRatio(index, value) {
    if (index === 0 || isReadonly.value) return
    splitRows.value[index].ratioText = value
    const ratio = parsePercent(value)
    if (sumOtherSplitRatios(index, ratio) > 1) {
      splitRows.value[index].ratio = 0
      splitRows.value[index].ratioText = ''
      notify('分摊比例合计不能超过100%', 'warning')
      return
    }
    splitRows.value[index].ratio = ratio
  }

  function normalizeSplitRatio(index) {
    if (index === 0 || isReadonly.value) return
    const row = splitRows.value[index]
    row.ratio = parsePercent(row.ratioText)
    row.ratioText = `${formatPercent(row.ratio)} %`
  }

  function sumOtherSplitRatios(activeIndex, activeRatio) {
    return splitRows.value.slice(1).reduce((sum, row, index) => {
      const actualIndex = index + 1
      return sum + (actualIndex === activeIndex ? activeRatio : Number(row.ratio || 0))
    }, 0)
  }

  function splitAmount(row, index) {
    if (index === 0) {
      const usedAmount = splitRows.value
        .slice(1)
        .reduce((sum, item) => sum + roundMoney(totalAllowanceAmount.value * Number(item.ratio || 0)), 0)
      return Math.max(0, roundMoney(totalAllowanceAmount.value - usedAmount))
    }
    return roundMoney(totalAllowanceAmount.value * Number(row.ratio || 0))
  }

  function validateSplitRows() {
    if (splitRows.value.length === 0) throw new Error('至少保留一条分摊信息')
    if (splitRows.value.some((row) => !row.companyId)) throw new Error('费用归属不能为空')
    if (Math.abs(splitRatioTotal.value - 1) > 0.0001) throw new Error('分摊比例合计必须为100%')
    if (Math.abs(splitAmountTotal.value - totalAllowanceAmount.value) > 0.01) {
      throw new Error('分摊金额合计必须等于费用合计中的补助总金额')
    }
  }

  function toSaveSplits() {
    return splitRows.value.map((row, index) => ({
      reimCompanyId: row.companyId,
      projectId: row.projectId || null,
      ratio: index === 0 ? firstSplitRatio.value : Number(row.ratio || 0)
    }))
  }

  return {
    splitRows,
    firstSplitRatio,
    splitRatioTotal,
    splitAmountTotal,
    addSplitRow,
    deleteSplitRow,
    averageSplitRows,
    changeSplitRatio,
    normalizeSplitRatio,
    splitAmount,
    validateSplitRows,
    toSaveSplits
  }
}
