import axios from 'axios';
import { ElMessage } from 'element-plus';
import router from '../router';

export const BASE_API = 'http://localhost:8080';
const request = axios.create({
    baseURL: BASE_API, // 直接写死
    timeout: 10000
});

// 请求拦截器添加Token
request.interceptors.request.use(
    config => {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        return config;
    },
    error => {
        return Promise.reject(error);
    }
);

// 响应拦截器处理401/403错误
request.interceptors.response.use(
    response => response,
    error => {
        if (error.response && (error.response.status === 401 || error.response.status === 403)) {
            ElMessage.error('登录已过期，请重新登录');
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            router.push('/login');
        }
        return Promise.reject(error);
    }
);
// 在 request.js 中添加调试方法
const debugToken = async () => {
    const token = localStorage.getItem('token');
    if (!token) {
        console.error('No token found in localStorage');
        return;
    }

    try {
        const response = await request.post('/user/validate-token', {}, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        console.log('Token验证结果:', response.data);
        return response.data;
    } catch (error) {
        console.error('Token验证失败:', error.response?.data);
        return null;
    }
};

// 在登录成功后调用
export { debugToken };
export default request;
