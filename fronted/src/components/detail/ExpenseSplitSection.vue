<template>
  <SectionPanel title="费用归属及分摊" :subtitle="`分摊金额：${formatMoney(totals.totalAllowanceAmount)}`">
    <table class="section-table split-table">
      <thead>
        <tr>
          <th>序号</th>
          <th>费用归属<span class="required">*</span></th>
          <th>项目</th>
          <th>
            分摊比例<span class="required">*</span>
            <button v-if="!isReadonly" class="split-average" type="button" @click="$emit('average')">均摊</button>
          </th>
          <th>分摊金额</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(row, index) in rows" :key="row.id">
          <td>{{ index + 1 }}</td>
          <td>
            <select v-model="row.companyId" :disabled="isReadonly">
              <option value="">请选择</option>
              <option v-for="item in options.companies" :key="item.reimCompanyId" :value="item.reimCompanyId">
                {{ item.reimCompanyName }}
              </option>
            </select>
          </td>
          <td>
            <select v-model="row.projectId" :disabled="isReadonly">
              <option value="">请选择</option>
              <option v-for="item in options.projects" :key="item.projectId" :value="item.projectId">
                {{ item.projectName }}
              </option>
            </select>
          </td>
          <td>
            <input
              class="ratio-input"
              :value="index === 0 ? `${formatPercent(firstRatio)} %` : row.ratioText"
              :readonly="index === 0"
              :disabled="isReadonly"
              @input="$emit('ratio-input', index, $event.target.value)"
              @blur="$emit('ratio-blur', index)"
            />
          </td>
          <td><input class="money-input" :value="formatMoney(splitAmount(row, index))" readonly /></td>
          <td>
            <button class="icon-link" type="button" :disabled="isReadonly" @click="$emit('delete', index)">
              <Trash2 :size="14" />
            </button>
          </td>
        </tr>
        <tr>
          <td colspan="6">
            <button class="add-row" type="button" :disabled="isReadonly" @click="$emit('add')">
              <CirclePlus :size="14" />添加一行
            </button>
          </td>
        </tr>
        <tr class="summary-row">
          <td colspan="3">合计</td>
          <td>{{ formatPercent(ratioTotal) }}%</td>
          <td>CNY {{ formatMoney(amountTotal) }}</td>
          <td></td>
        </tr>
      </tbody>
    </table>
  </SectionPanel>
</template>

<script setup>
import { CirclePlus, Trash2 } from 'lucide-vue-next'
import SectionPanel from '../SectionPanel.vue'
import { formatMoney, formatPercent } from '../../utils/formatters'

defineProps({
  totals: { type: Object, required: true },
  rows: { type: Array, required: true },
  options: { type: Object, required: true },
  isReadonly: { type: Boolean, required: true },
  firstRatio: { type: Number, required: true },
  ratioTotal: { type: Number, required: true },
  amountTotal: { type: Number, required: true },
  splitAmount: { type: Function, required: true }
})

defineEmits(['add', 'delete', 'average', 'ratio-input', 'ratio-blur'])
</script>
