import axiosInstance from '../api/axiosInstance';

const dashboardService = {
  getDashboardData: async () => {
    const response = await axiosInstance.get('/dashboard');
    return response.data;
  }
};

export default dashboardService;
