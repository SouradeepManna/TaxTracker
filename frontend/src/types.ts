// Shared domain types mirroring the backend DTOs.

export interface AuthUser {
  name: string;
  email: string;
}

export interface LoginResponse {
  token: string;
  name: string;
  email: string;
  message: string;
}

export interface AddressDTO {
  addressLine1: string;
  addressLine2: string;
  area: string;
  city: string;
  state: string;
  pin: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  mobNumber: string;
  address: AddressDTO;
}

export interface TransactionDTO {
  id: number;
  date: string;
  amount: number;
  taxAmount: number;
  type: string;
  organizationName: string;
  financialYear: string;
}

export interface TransactionPageResponse {
  totalRecords: number;
  pageNumber: number;
  pageSize: number;
  transactions: TransactionDTO[];
}

export interface Form90CTransactionDTO {
  organizationName: string;
  amount: number;
  taxAmount: number;
  type: string;
}

export interface Form90CRequest {
  name: string;
  mobileNumber: string;
  financialYear: string;
  transactionHistory: Form90CTransactionDTO[];
}

export interface Form90CResponse {
  formId: number;
  email: string;
  name: string;
  mobileNumber: string;
  financialYear: string;
  status: string;
  documentName: string | null;
  transactionHistory: Form90CTransactionDTO[];
}

export interface UploadResponse {
  documentId: number;
  fileName: string;
  message: string;
}

export interface ApiResponse {
  message: string;
  success: boolean;
}

export interface TransactionQuery {
  pageNumber?: number;
  pageSize?: number;
  financialYear?: string;
  month?: string;
  organizationName?: string;
  type?: string;
  allMonths?: boolean;
}

export interface CreateTransactionRequest {
  date: string;
  amount: number;
  taxAmount: number;
  type: string;
  organizationName: string;
  financialYear?: string;
}

export interface VerifyOtpRequest {
  email: string;
  otp: string;
}

export interface InsightsTypeBreakdown {
  type: string;
  taxAmount: number;
  count: number;
}

export interface InsightsOrgBreakdown {
  organizationName: string;
  taxAmount: number;
  count: number;
}

export interface InsightsMonthlyPoint {
  month: string;
  taxAmount: number;
}

export interface InsightsResponse {
  financialYear: string | null;
  totalTransactions: number;
  totalAmount: number;
  totalTax: number;
  taxByType: InsightsTypeBreakdown[];
  topOrganizations: InsightsOrgBreakdown[];
  monthlyTrend: InsightsMonthlyPoint[];
  projectedNextMonthTax: number;
  trendNarrative: string;
}

export type ToastType = "info" | "success" | "error";
