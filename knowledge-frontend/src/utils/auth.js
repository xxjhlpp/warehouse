// 在 utils 目录下创建 auth.js
export const checkAuth = () => {
    const token = localStorage.getItem('token');
    const user = localStorage.getItem('user');

    if (!token || !user) {
        return false;
    }

    try {
        const userObj = JSON.parse(user);
        return !!(token && userObj && userObj.id);
    } catch (e) {
        console.error('解析用户信息失败:', e);
        return false;
    }
};

export const getToken = () => {
    return localStorage.getItem('token');
};

export const getUserInfo = () => {
    try {
        const user = localStorage.getItem('user');
        return user ? JSON.parse(user) : null;
    } catch (e) {
        return null;
    }
};