import axios from "axios";

const api = axios.create({
    baseURL: "http://localhost:8080/api",
    headers: { "Content-Type": "application/json" },
});

api.interceptors.request.use((config) => {
    const token = sessionStorage.getItem("cabToken");
    if (token) config.headers.Authorization = `Bearer ${token}`;
    return config;
});

export function getErrorMessage(error) {
    const data = error?.response?.data;
    if (typeof data === "string") return data;
    return data?.message || data?.error || error?.message || "Something went wrong. Please try again.";
}

export default api;
