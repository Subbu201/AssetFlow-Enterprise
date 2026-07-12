import axiosInstance from '../api/axiosInstance';

const employeeService = {
  getAllEmployees: async () => {
    const response = await axiosInstance.get('/admin/employees');
    return response.data;
  },
  updateRole: async (id, role) => {
    const response = await axiosInstance.patch(`/admin/employees/${id}/role`, { role });
    return response.data;
  }
};

export default employeeService;
