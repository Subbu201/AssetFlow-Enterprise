import axiosInstance from '../api/axiosInstance';

const categoryService = {
  getAllCategories: async () => {
    const response = await axiosInstance.get('/admin/categories');
    return response.data;
  },
  createCategory: async (data) => {
    const response = await axiosInstance.post('/admin/categories', data);
    return response.data;
  },
  updateCategory: async (id, data) => {
    const response = await axiosInstance.put(`/admin/categories/${id}`, data);
    return response.data;
  }
};

export default categoryService;
