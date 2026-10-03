import { api } from './api';

// Types mirroring the backend DTOs (com.traazu.auth_service.domain.dtos.*)

export interface MessageResponse {
  message: string;
  success: boolean;
  timestamp: string;
}

export interface TokenResponse {
  token: string;
  message: string;
}

export interface AuthResponse {
  accessToken: string;
}

export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
}

export type UserRole = 'SUPPORT' | 'ADMIN' | 'USER';

export interface ForgetPasswordPayload {
  email: string;
  userRole: UserRole;
}

export interface ChangePasswordPayload {
  token: string;
  email: string;
  userRole: UserRole;
  newPassword: string;
  repeatNewPassword: string;
}

export interface CompleteSignUpPayload {
  signUpToken: string;
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  repeatPassword: string;
}

export const authApi = {
  // POST /api/auth/signup/send-code
  sendVerificationCode: (email: string) =>
    api.post<MessageResponse>('/api/auth/signup/send-code', { email }),

  // POST /api/auth/signup/verify_code
  verifyCode: (email: string, otpCode: string) =>
    api.post<TokenResponse>('/api/auth/signup/verify_code', { email, otpCode }),

  // POST /api/auth/user/complete_signup
  completeUserSignUp: (payload: CompleteSignUpPayload) =>
    api.post<AuthResponse>('/api/auth/user/complete_signup', payload),

  // POST /api/auth/login  (refresh token arrives as HttpOnly cookie)
  login: (username: string, password: string, role: UserRole) =>
    api.post<AuthResponse>('/api/auth/login', { username, password, role }),

  // POST /api/auth/refresh (uses the HttpOnly refreshToken cookie)
  refresh: () => api.post<AuthResponse>('/api/auth/refresh'),

  // POST /forget-password/send-code
  forgetPasswordSendCode: (email: string, userRole: UserRole) =>
    api.post<MessageResponse>('/forget-password/send-code', {
      email,
      userRole,
    } as ForgetPasswordPayload),

  // POST /forget-password/verify_code  (returns a short-lived change-password token)
  forgetPasswordVerifyCode: (email: string, otpCode: string) =>
    api.post<TokenResponse>('/forget-password/verify_code', { email, otpCode }),

  // POST /forget-password/set-password
  forgetPasswordSetPassword: (payload: ChangePasswordPayload) =>
    api.post<MessageResponse>('/forget-password/set-password', payload),
};
