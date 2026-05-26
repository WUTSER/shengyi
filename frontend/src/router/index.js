import { createRouter, createWebHashHistory } from 'vue-router'
import AppLayout from '@/components/AppLayout.vue'
import ListView from '@/components/ListView.vue'
import DetailView from '@/components/DetailView.vue'

const routes = [
  {
    path: '/',
    redirect: { name: 'reimbursement-list' }
  },
  {
    path: '/reimbursements',
    component: AppLayout,
    children: [
      {
        path: '',
        name: 'reimbursement-list',
        component: ListView
      },
      {
        path: 'new',
        name: 'reimbursement-create',
        component: DetailView
      },
      {
        path: ':id',
        name: 'reimbursement-detail',
        component: DetailView,
        props: { mode: 'view' }
      },
      {
        path: ':id/edit',
        name: 'reimbursement-edit',
        component: DetailView,
        props: { mode: 'edit' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

export default router
