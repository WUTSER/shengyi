<template>
  <SectionPanel title="费用合计">
    <div class="totals-grid">
      <span>补助总金额 <b>{{ formatMoney(totals.totalAllowanceAmount) }}</b></span>
      <span>餐费补助 <b>{{ formatMoney(totals.mealAllowanceAmount) }}</b></span>
      <span>交通补助 <b>{{ formatMoney(totals.trafficAllowanceAmount) }}</b></span>
      <span>通讯补助 <b>{{ formatMoney(totals.communicationAllowanceAmount) }}</b></span>
    </div>
  </SectionPanel>
</template>

<script setup>
import SectionPanel from '../SectionPanel.vue'
import { formatMoney } from '../../utils/formatters'

defineProps({
  totals: { type: Object, required: true }
})
</script>
