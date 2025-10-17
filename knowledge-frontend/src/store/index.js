import { createStore } from 'vuex'

export default createStore({
  state: {
    user: localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')) : null,
    loading: false,
    error: null
  },
  getters: {
    isAuthenticated(state) {
      return !!state.user
    }
  },
  mutations: {
    setUser(state, user) {
      state.user = user
      localStorage.setItem('user', JSON.stringify(user))
    },
    setLoading(state, loading) {
      state.loading = loading
    },
    setError(state, error) {
      state.error = error
    },
    clearError(state) {
      state.error = null
    }
  },
  actions: {
    // 登录 action
    async login({ commit }, { username, password }) {
      commit('setLoading', true)
      commit('clearError')

      try {
        // 模拟登录成功
        if (username && password) {
          const user = { id: 1, username, role: 'user' }
          commit('setUser', user)
          return true
        } else {
          commit('setError', '用户名和密码不能为空')
          return false
        }
      } catch (err) {
        commit('setError', '登录失败，请检查用户名和密码')
        return false
      } finally {
        commit('setLoading', false)
      }
    },

    // 注册 action - 移除未使用的参数
    async register({ commit }, { username, password }) {
      commit('setLoading', true)
      commit('clearError')

      try {
        // 模拟注册成功
        if (username && password) {
          return true
        } else {
          commit('setError', '注册信息不完整')
          return false
        }
      } catch (err) {
        commit('setError', '注册失败，请稍后再试')
        return false
      } finally {
        commit('setLoading', false)
      }
    },

    // 退出登录
    logout({ commit }) {
      commit('setUser', null)
      localStorage.removeItem('user')
    }
  },
  modules: {
  }
})
