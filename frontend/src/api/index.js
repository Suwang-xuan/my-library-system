import request from './request'

export const bookApi = {
  getList: (params) => request.get('/api/book/list', { params }),
  getById: (id) => request.get(`/api/book/${id}`),
  add: (data) => request.post('/api/book/add', data),
  update: (data) => request.put('/api/book/update', data),
  delete: (id) => request.delete(`/api/book/delete/${id}`),
  batchDelete: (ids) => request.delete('/api/book/batch-delete', { data: ids }),
  getHotBooks: (limit) => request.get('/api/book/hot', { params: { limit } }),
  getLowStockBooks: (threshold) => request.get('/api/book/low-stock', { params: { threshold } }),
  getStats: () => request.get('/api/book/stats')
}

export const categoryApi = {
  getList: () => request.get('/api/category/list'),
  getById: (id) => request.get(`/api/category/${id}`),
  add: (data) => request.post('/api/category/add', data),
  update: (data) => request.put('/api/category/update', data),
  delete: (id) => request.delete(`/api/category/delete/${id}`)
}

export const readerApi = {
  getList: (params) => request.get('/api/reader/list', { params }),
  getById: (id) => request.get(`/api/reader/${id}`),
  add: (data) => request.post('/api/reader/add', data),
  register: (data) => request.post('/api/reader/register', data),
  update: (data) => request.put('/api/reader/update', data),
  updateStatus: (id, status) => request.put(`/api/reader/status/${id}`, null, { params: { status } }),
  delete: (id) => request.delete(`/api/reader/delete/${id}`),
  batchDelete: (ids) => request.delete('/api/reader/batch-delete', { data: ids }),
  getActiveReaders: () => request.get('/api/reader/active'),
  getStats: () => request.get('/api/reader/stats')
}

export const borrowApi = {
  getList: (params) => request.get('/api/borrow/list', { params }),
  getById: (id) => request.get(`/api/borrow/${id}`),
  borrow: (data) => request.post('/api/borrow/borrow', data),
  returnBook: (data) => request.post('/api/borrow/return', data),
  renew: (data) => request.post('/api/borrow/renew', data),
  getReaderHistory: (readerId) => request.get(`/api/borrow/reader/${readerId}/history`),
  getOverdueRecords: () => request.get('/api/borrow/overdue'),
  getDueRecords: (date) => request.get('/api/borrow/due', { params: { date } }),
  getStats: (startDate, endDate) => request.get('/api/borrow/stats', { params: { startDate, endDate } })
}

export const statisticsApi = {
  getDashboard: () => request.get('/api/statistics/dashboard'),
  getCategoryBorrow: () => request.get('/api/statistics/category-borrow'),
  getMonthlyTrend: (year) => request.get('/api/statistics/monthly-trend', { params: { year } }),
  getHotBooks: (limit) => request.get('/api/statistics/hot-books', { params: { limit } }),
  getLowStockBooks: (threshold) => request.get('/api/statistics/low-stock', { params: { threshold } }),
  getReaderBehavior: () => request.get('/api/statistics/reader-behavior'),
  getOverdueAnalysis: (startDate, endDate) => request.get('/api/statistics/overdue-analysis', { params: { startDate, endDate } }),
  getBorrowRate: () => request.get('/api/statistics/borrow-rate')
}

export const adminApi = {
  getList: () => request.get('/api/admin/list'),
  getById: (id) => request.get(`/api/admin/${id}`),
  add: (data) => request.post('/api/admin/add', data),
  update: (data) => request.put('/api/admin/update', data),
  updateStatus: (id, status) => request.put(`/api/admin/status/${id}`, null, { params: { status } }),
  delete: (id) => request.delete(`/api/admin/delete/${id}`),
  login: (data) => request.post('/api/admin/login', data),
  logout: () => request.post('/api/logout'),
  changePassword: (data) => request.put('/api/admin/password', data),
  uploadAvatar: (data) => request.post('/api/admin/upload-avatar', data, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }),
  getCurrentUser: () => request.get('/api/admin/current-user')
}

export const roleApi = {
  getList: () => request.get('/api/role/list'),
  getById: (id) => request.get(`/api/role/${id}`),
  add: (data) => request.post('/api/role/add', data),
  update: (data) => request.put('/api/role/update', data),
  delete: (id) => request.delete(`/api/role/delete/${id}`),
  getPermissions: (id) => request.get(`/api/role/${id}/permissions`),
  assignPermissions: (id, permIds) => request.post(`/api/role/${id}/permissions`, permIds)
}

export const permissionApi = {
  getList: () => request.get('/api/permission/list'),
  getById: (id) => request.get(`/api/permission/${id}`),
  add: (data) => request.post('/api/permission/add', data),
  update: (data) => request.put('/api/permission/update', data),
  delete: (id) => request.delete(`/api/permission/delete/${id}`)
}

export const borrowRuleApi = {
  getList: () => request.get('/api/borrow/rule/list'),
  getByReaderType: (readerType) => request.get('/api/borrow/rule/getByReaderType', { params: { readerType } }),
  add: (data) => request.post('/api/borrow/rule/add', data),
  update: (data) => request.post('/api/borrow/rule/update', data),
  delete: (id) => request.delete(`/api/borrow/rule/delete/${id}`)
}

export const authApi = {
  login: (data) => request.post('/api/login', data),
  logout: () => request.post('/api/logout'),
  getCurrentUser: () => request.get('/api/current-user'),
  checkLogin: () => request.get('/api/check-login')
}
