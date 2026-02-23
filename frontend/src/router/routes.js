const getBaseRoute = () => {
  const base = sessionStorage.getItem('basePath') || '/'
  return base
}

export default {
  base: getBaseRoute(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue'),
      meta: { title: '登录', public: true }
    },
    {
      path: '/register',
      name: 'ReaderRegister',
      component: () => import('@/views/ReaderRegister.vue'),
      meta: { title: '读者注册', public: true }
    },
    {
      path: '/',
      component: () => import('@/layout/Layout.vue'),
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('@/views/Dashboard.vue'),
          meta: { title: '首页', icon: 'Odometer' }
        },
        {
          path: 'book',
          name: 'Book',
          component: () => import('@/views/book/BookList.vue'),
          meta: { title: '图书管理', icon: 'Reading' }
        },
        {
          path: 'book/add',
          name: 'BookAdd',
          component: () => import('@/views/book/BookForm.vue'),
          meta: { title: '添加图书', parent: '图书管理' }
        },
        {
          path: 'book/edit/:id',
          name: 'BookEdit',
          component: () => import('@/views/book/BookForm.vue'),
          meta: { title: '编辑图书', parent: '图书管理' }
        },
        {
          path: 'category',
          name: 'Category',
          component: () => import('@/views/category/CategoryList.vue'),
          meta: { title: '图书分类', icon: 'Grid' }
        },
        {
          path: 'reader',
          name: 'Reader',
          component: () => import('@/views/reader/ReaderList.vue'),
          meta: { title: '读者管理', icon: 'User' }
        },
        {
          path: 'reader/add',
          name: 'ReaderAdd',
          component: () => import('@/views/reader/ReaderForm.vue'),
          meta: { title: '注册读者', parent: '读者管理' }
        },
        {
          path: 'reader/edit/:id',
          name: 'ReaderEdit',
          component: () => import('@/views/reader/ReaderForm.vue'),
          meta: { title: '编辑读者', parent: '读者管理' }
        },
        {
          path: 'borrow',
          name: 'Borrow',
          component: () => import('@/views/borrow/BorrowList.vue'),
          meta: { title: '借阅管理', icon: 'Tickets' }
        },
        {
          path: 'borrow/borrow',
          name: 'BorrowBook',
          component: () => import('@/views/borrow/BorrowForm.vue'),
          meta: { title: '图书借阅', parent: '借阅管理' }
        },
        {
          path: 'borrow/return/:id',
          name: 'ReturnBook',
          component: () => import('@/views/borrow/ReturnForm.vue'),
          meta: { title: '图书归还', parent: '借阅管理' }
        },
        {
          path: 'borrow/rule',
          name: 'BorrowRule',
          component: () => import('@/views/borrow/BorrowRule.vue'),
          meta: { title: '借阅规则管理', parent: '借阅管理' }
        },
        {
          path: 'statistics',
          name: 'Statistics',
          component: () => import('@/views/statistics/Statistics.vue'),
          meta: { title: '数据统计', icon: 'DataAnalysis' }
        },
        {
          path: 'admin',
          name: 'Admin',
          component: () => import('@/views/admin/AdminList.vue'),
          meta: { title: '管理员管理', icon: 'UserFilled' }
        },
        {
          path: 'profile',
          name: 'Profile',
          component: () => import('@/views/Profile.vue'),
          meta: { title: '个人中心', icon: 'User' }
        }
      ]
    },
    {
      path: '/reader',
      component: () => import('@/layout/ReaderLayout.vue'),
      redirect: '/reader/dashboard',
      meta: { title: '读者中心' },
      children: [
        {
          path: 'dashboard',
          name: 'ReaderDashboard',
          component: () => import('@/views/reader/ReaderDashboard.vue'),
          meta: { title: '读者首页', icon: 'User' }
        },
        {
          path: 'books',
          name: 'ReaderBooks',
          component: () => import('@/views/reader/ReaderBooks.vue'),
          meta: { title: '图书搜索', icon: 'Reading' }
        },
        {
          path: 'borrow-records',
          name: 'ReaderBorrowRecords',
          component: () => import('@/views/reader/ReaderBorrowRecords.vue'),
          meta: { title: '借阅记录', icon: 'Document' }
        },
        {
          path: 'profile',
          name: 'ReaderProfile',
          component: () => import('@/views/reader/ReaderProfile.vue'),
          meta: { title: '个人设置', icon: 'Setting' }
        }
      ]
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/dashboard'
    }
  ]
}
