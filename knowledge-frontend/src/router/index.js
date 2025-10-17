// 首先确保已经安装了 request：npm install request
// import request from '../utils/request'
import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/LoginView.vue'
import Register from '../views/RegisterView.vue'
import Home from '../views/HomeView.vue'
import ProfileView from '../views/ProfileView.vue'
import NoteCreateView from "@/views/NoteCreateView.vue";
import TodoView from "@/views/TodoView.vue";

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', name: 'Login', component: Login },
  { path: '/register', name: 'Register', component: Register },
  {
    path: '/home',
    name: 'Home',
    component: Home,
    meta: { requiresAuth: true }, // 需要登录才能访问
    props: (route) => ({
      activeMenu: route.query.menu || 'home'
    })
  },
  {
    path: '/editor', // 关键：确保路径与HomeView中跳转的路径一致
    name: 'note-create',
    component: NoteCreateView,
    meta: {
      requiresAuth: true // 如果需要登录才能访问
    }
  },
  {
    path: '/editor/:id', // 关键：确保路径与HomeView中跳转的路径一致
    name: 'EditNote',
    component: NoteCreateView,
    meta: {
      title: ' 编辑笔记 '
    }
  },
  {
    path: '/profile',  // 个人中心路由
    name: 'profile',
    component: ProfileView,
    meta: {
      requiresAuth: true  // 需要登录才能访问
    }},
  {
    path: '/todo',
    name: 'Todo',
    component: TodoView,
    meta: { requiresAuth: true } // 如果需要登录验证
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
      try {
        if (to.matched.some(record => record.meta.requiresAuth)) {
          const token = localStorage.getItem('token');
          const userStr = localStorage.getItem('user');

          // 同时验证token和user信息
          if (!token || !userStr) {
            next('/login');
            return;
          }

          try {
            const user = JSON.parse(userStr);
            if (!user || !user.id) {
              throw new Error('用户信息不完整');
            }
            next();
          } catch (e) {
            localStorage.removeItem('user');
            localStorage.removeItem('token');  // 同时清除token
            next('/login');
          }
        } else {
          next();
        }
      } catch (error) {
        console.error('路由守卫错误:', error);
        next('/login');
      }
    });
console.log('已注册的路由:', router.getRoutes().map(route => route.path));
export default router