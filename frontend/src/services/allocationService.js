import axiosInstance from '../api/axiosInstance';

const allocationService = {
  getAllAllocations: async () => {
    const response = await axiosInstance.get('/allocations');
    return response.data;
  },
  getAllocationById: async (id) => {
    const response = await axiosInstance.get(`/allocations/${id}`);
    return response.data;
  },
  createAllocation: async (data) => {
    const response = await axiosInstance.post('/allocations', data);
    return response.data;
  }
};

export default allocationService;
