import axiosInstance from '../api/axiosInstance';

const notificationService = {
  getMyNotifications: async () => {
    const response = await axiosInstance.get('/notifications/me');
    return response.data;
  },
  getUnreadCount: async () => {
    const response = await axiosInstance.get('/notifications/me/unread-count');
    return response.data;
  },
  markRead: async (id) => {
    const response = await axiosInstance.patch(`/notifications/${id}/read`);
    return response.data;
  },
  markAllRead: async () => {
    const response = await axiosInstance.patch('/notifications/read-all');
    return response.data;
  },
  deleteNotification: async (id) => {
    const response = await axiosInstance.delete(`/notifications/${id}`);
    return response.data;
  }
};

export default notificationService;
