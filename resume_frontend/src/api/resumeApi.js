import axios from 'axios';

const api = axios.create({
    baseURL: 'https://resume-production-e7b0.up.railway.app',
    headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    },
    withCredentials: true
});

export const generateResume = async (userDescription) => {
    try {
        const response = await api.post('/api/v1/resume/generate', {
            userDescription: userDescription
        });
        return response.data;
    } catch (error) {
        throw error;
    }
};