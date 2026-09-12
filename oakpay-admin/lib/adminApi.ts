const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

export type AdminDashboardStats = {
  pendingKyc: number;
  openQueries: number;
  activeDisputes: number;
  pendingResolutions: number;
};

export type KycStatus =
  | "NOT_STARTED"
  | "PENDING"
  | "UNDER_REVIEW"
  | "APPROVED"
  | "REJECTED";

export type AdminKycApplication = {
  id: string;
  userId: string;
  email: string;
  firstName: string;
  lastName: string;
  status: KycStatus;
  documentType: string | null;
  documentNumberMasked: string | null;
  submittedAt: string | null;
  updatedAt: string | null;
};

export type KycDecisionRequest = {
  decision: "APPROVED" | "REJECTED";
  reason?: string;
};

export type ClientQuery = {
  id: string;
  userId: string;
  subject: string;
  category: string;
  status: string;
  priority: string;
  description: string;
  resolution: string | null;
  assignedAdminId: string | null;
  createdAt: string;
  updatedAt: string;
  resolvedAt: string | null;
};

function getToken() {
  if (typeof window === "undefined") {
    return null;
  }

  return localStorage.getItem("oakpay.accessToken");
}

async function request<T>(
  path: string,
  options: RequestInit = {},
): Promise<T> {
  const token = getToken();

  const headers = new Headers(options.headers);

  headers.set("Content-Type", "application/json");

  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers,
  });

  if (response.status === 401 || response.status === 403) {
    throw new Error("ADMIN_AUTH_REQUIRED");
  }

  if (!response.ok) {
    const body = await response.text();

    throw new Error(
      body || `Request failed with status ${response.status}`,
    );
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json();
}

export const adminApi = {
  dashboard() {
    return request<AdminDashboardStats>(
      "/api/v1/admin/dashboard",
    );
  },

  kyc: {
    list(status?: KycStatus) {
      const query = status
        ? `?status=${encodeURIComponent(status)}`
        : "";

      return request<AdminKycApplication[]>(
        `/api/v1/admin/kyc${query}`,
      );
    },

    get(id: string) {
      return request<AdminKycApplication>(
        `/api/v1/admin/kyc/${id}`,
      );
    },

    decide(id: string, data: KycDecisionRequest) {
      return request<AdminKycApplication>(
        `/api/v1/admin/kyc/${id}/decision`,
        {
          method: "POST",
          body: JSON.stringify(data),
        },
      );
    },
  },

  queries: {
    list(status?: string) {
      const query = status
        ? `?status=${encodeURIComponent(status)}`
        : "";

      return request<ClientQuery[]>(
        `/api/v1/admin/queries${query}`,
      );
    },

    assign(id: string, adminUserId: string) {
      return request<ClientQuery>(
        `/api/v1/admin/queries/${id}/assign`,
        {
          method: "POST",
          body: JSON.stringify({ adminUserId }),
        },
      );
    },

    resolve(id: string, resolution: string) {
      return request<ClientQuery>(
        `/api/v1/admin/queries/${id}/resolve`,
        {
          method: "POST",
          body: JSON.stringify({ resolution }),
        },
      );
    },
  },
};
