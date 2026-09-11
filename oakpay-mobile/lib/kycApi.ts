import { apiGet, apiPost } from './api';

export type KycDocument = {
  id: string;
  documentType: string;
  documentNumberMasked?: string | null;
  status: string;
  rejectionReason?: string | null;
  frontUploaded: boolean;
  backUploaded: boolean;
  createdAt: string;
  updatedAt: string;
};

export type KycProfile = {
  id: string;
  userId: string;
  status: 'NOT_STARTED' | 'PENDING' | 'VERIFIED' | 'REJECTED' | string;
  rejectionReason?: string | null;
  submittedAt?: string | null;
  reviewedAt?: string | null;
  documents: KycDocument[];
};

export const kycApi = {
  get: (token: string) => apiGet<KycProfile>('/api/v1/kyc', token),
  addDocument: (token: string, documentType: string, documentNumber?: string) =>
    apiPost<KycDocument>('/api/v1/kyc/documents', token, { documentType, documentNumber }),
  submit: (token: string) => apiPost<KycProfile>('/api/v1/kyc/submit', token),
};
