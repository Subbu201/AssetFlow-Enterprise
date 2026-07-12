import axiosInstance from '../api/axiosInstance';

const departmentService = {
  getAllDepartments: async () => {
    const response = await axiosInstance.get('/admin/departments');
    return response.data;
  },
  createDepartment: async (data) => {
    const response = await axiosInstance.post('/admin/departments', data);
    return response.data;
  },
  updateDepartment: async (id, data) => {
    const response = await axiosInstance.put(`/admin/departments/${id}`, data);
    return response.data;
  },
  updateStatus: async (id, status) => {
    const response = await axiosInstance.put(`/admin/departments/${id}/status`, { status });
    return response.data;
  }
};

export default departmentService;
