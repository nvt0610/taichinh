export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  error?: ApiErrorBody;
  timestamp: string;
}

export interface ApiErrorBody {
  code: string;
  details?: FieldError[];
}

export interface FieldError {
  field: string;
  message: string;
}

export interface NormalizedApiError {
  message: string;
  code?: string;
  fieldErrors: Record<string, string>;
  status?: number;
}
