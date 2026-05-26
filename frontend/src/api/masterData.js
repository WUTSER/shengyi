import { request } from './http'

export async function loadMasterData() {
  const [companies, departments, employees, businessTypes, cities, projects] = await Promise.all([
    request('/master-data/reim-companies'),
    request('/master-data/reim-departments'),
    request('/master-data/employees'),
    request('/master-data/business-types'),
    request('/master-data/cities'),
    request('/master-data/projects')
  ])

  return { companies, departments, employees, businessTypes, cities, projects }
}
