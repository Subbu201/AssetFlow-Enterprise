import axiosInstance from '../api/axiosInstance';

const assetService = {
  getAllAssets: async (params) => {
    // params can include keyword, assetTag, categoryId, status, departmentId, etc.
    const response = await axiosInstance.get('/assets', { params });
    return response.data;
  },
  getAssetById: async (id) => {
    const response = await axiosInstance.get(`/assets/${id}`);
    return response.data;
  },
  createAsset: async (data) => {
    const response = await axiosInstance.post('/assets', data);
    return response.data;
  },
  updateAsset: async (id, data) => {
    const response = await axiosInstance.put(`/assets/${id}`, data);
    return response.data;
  },
  updateStatus: async (id, statusData) => {
    const response = await axiosInstance.patch(`/assets/${id}/status`, statusData);
    return response.data;
  }
};

export default assetService;
