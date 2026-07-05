import axios, { AxiosError, type AxiosInstance } from "axios";
import type {
  ApiResponse,
  CreateTransactionRequest,
  Form90CRequest,
  Form90CResponse,
  InsightsResponse,
  LoginResponse,
  RegisterRequest,
  TransactionPageResponse,
  TransactionQuery,
  TransactionDTO,
  UploadResponse,
  VerifyOtpRequest,
} from "../types";

// CRA exposes env vars prefixed with REACT_APP_. Defaults to localhost:8080.
const API_BASE = process.env.REACT_APP_API_BASE_URL ?? "http://localhost:8080/api";

const client: AxiosInstance = axios.create({
  baseURL: API_BASE,
  headers: { "Content-Type": "application/json" },
});

// Attach JWT from localStorage on every request.
client.interceptors.request.use((config) => {
  const token = localStorage.getItem("tt_token");
  if (token) {
    config.headers = config.headers ?? {};
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

interface BackendError {
  messages?: string[];
  message?: string;
}

// Normalize the backend ErrorResponse (messages: string[]) into one string.
export function extractError(err: unknown): string {
  const axiosErr = err as AxiosError<BackendError>;
  const data = axiosErr?.response?.data;
  if (data?.messages && Array.isArray(data.messages) && data.messages.length) {
    return data.messages.join(" ");
  }
  if (data?.message) return data.message;
  if (axiosErr?.message) return axiosErr.message;
  return "Something went wrong. Please try again.";
}

export const api = {
  register(payload: RegisterRequest) {
    return client.post<ApiResponse>("/auth/register", payload);
  },

  verifyOtp(payload: VerifyOtpRequest) {
    return client.post<ApiResponse>("/auth/verify-otp", payload);
  },

  login(emailId: string, password: string) {
    return client.post<LoginResponse>("/auth/login", { emailId, password });
  },

  logout() {
    return client.get<ApiResponse>("/auth/logout");
  },

  getTransactions(params: TransactionQuery) {
    return client.get<TransactionPageResponse>("/transactions", { params });
  },

  createTransaction(payload: CreateTransactionRequest) {
    return client.post<TransactionDTO>("/transactions", payload);
  },

  getInsights(financialYear?: string) {
    return client.get<InsightsResponse>("/insights", {
      params: financialYear ? { financialYear } : {},
    });
  },

  downloadTransactions(format: string, financialYear?: string) {
    return client.get("/transactions/download", {
      params: { format, financialYear },
      responseType: "blob",
    });
  },

  saveForm90C(payload: Form90CRequest, status: string) {
    return client.post<Form90CResponse>("/forms/90c", payload, {
      params: { status },
    });
  },

  getMyForm90C() {
    return client.get<Form90CResponse>("/forms/90c/me");
  },

  uploadDocument(file: File) {
    const formData = new FormData();
    formData.append("file", file);
    return client.post<UploadResponse>("/uploads", formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });
  },

  submitForm(formId: number) {
    return client.post<ApiResponse>("/submissions", { formId });
  },
};
