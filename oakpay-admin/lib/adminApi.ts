const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8082";

export type AdminDashboardStats = { pendingKyc:number; openQueries:number; activeDisputes:number; pendingResolutions:number };
export type KycStatus = "NOT_STARTED"|"PENDING"|"UNDER_REVIEW"|"APPROVED"|"REJECTED";
export type AdminKycApplication = { id:string; userId:string; email:string; firstName:string; lastName:string; status:KycStatus; documentType:string|null; documentNumberMasked:string|null; submittedAt:string|null; updatedAt:string|null };
export type KycDecisionRequest = { decision:"APPROVED"|"REJECTED"; reason?:string };
export type ClientQuery = { id:string; userId:string; subject:string; category:string; status:string; priority:string; description:string; resolution:string|null; assignedAdminId:string|null; createdAt:string; updatedAt:string; resolvedAt:string|null };
export type ClientQueryStatus = "OPEN"|"ASSIGNED"|"ESCALATED"|"RESOLVED";
export type AdminUser = { id:string; email:string; firstName:string; lastName:string; role:string; status:"ACTIVE"|"INACTIVE"; emailVerified:boolean; createdAt:string };
export type AdminUserStatus = "ACTIVE"|"INACTIVE";
export type DisputeStatus = "OPEN"|"RESOLVED";
export type DisputeResolution = "BUYER_WINS"|"SELLER_WINS";
export type P2PDispute = { id:string; tradeId:string; openedBy:string; reason:string; evidence:string|null; status:DisputeStatus; resolution:DisputeResolution|null; resolutionNote:string|null; resolvedBy:string|null; resolvedAt:string|null; createdAt:string; updatedAt:string };
export type P2PDisputeAudit = { id:string; disputeId:string; tradeId:string; actorId:string; eventType:string; note:string|null; createdAt:string };
export type ResolveDisputeRequest = { resolution:DisputeResolution; note?:string };
export type P2PTradeStatus = "ESCROWED"|"PAYMENT_PENDING"|"PAYMENT_MARKED"|"COMPLETED"|"CANCELLED"|"DISPUTED"|"EXPIRED";
export type AdminTransaction = { id:string; buyerId:string; sellerId:string; advertisementId:string|null; asset:string; fiatCurrency:string; quantity:number|string; unitPrice:number|string; fiatAmount:number|string; paymentMethod:string; status:P2PTradeStatus|string; paymentReference:string|null; expiresAt:string; createdAt:string; updatedAt:string };

function getToken():string|null { if(typeof window === "undefined") return null; return localStorage.getItem("oakpay.admin.accessToken") || localStorage.getItem("oakpay.accessToken"); }
async function request<T>(path:string, options:RequestInit={}):Promise<T>{
  const token=getToken(); const headers=new Headers(options.headers);
  if(options.body && !(options.body instanceof FormData) && !headers.has("Content-Type")) headers.set("Content-Type","application/json");
  if(token) headers.set("Authorization",`Bearer ${token}`);
  const response=await fetch(`${API_BASE_URL}${path}`,{...options,cache:"no-store",headers});
  if(response.status===401||response.status===403) throw new Error("ADMIN_AUTH_REQUIRED");
  if(!response.ok){const body=await response.text();let message=body;try{const parsed=JSON.parse(body);message=parsed?.message||parsed?.error||parsed?.detail||body;}catch{}throw new Error(message||`Request failed with status ${response.status}`);}
  if(response.status===204) return undefined as T;
  const contentType=response.headers.get("content-type")||""; if(!contentType.includes("application/json")) return undefined as T; return response.json() as Promise<T>;
}

export const adminApi={
  dashboard:()=>request<AdminDashboardStats>("/api/v1/admin/dashboard"),
  kyc:{
    list:(status?:KycStatus)=>{const p=new URLSearchParams();if(status)p.set("status",status);const q=p.toString();return request<AdminKycApplication[]>(`/api/v1/admin/kyc${q?`?${q}`:""}`);},
    get:(id:string)=>request<AdminKycApplication>(`/api/v1/admin/kyc/${id}`),
    decide:(id:string,data:KycDecisionRequest)=>request<AdminKycApplication>(`/api/v1/admin/kyc/${id}/decision`,{method:"POST",body:JSON.stringify(data)})
  },
  queries:{
    list:(status?:ClientQueryStatus|string)=>{const p=new URLSearchParams();if(status)p.set("status",status);const q=p.toString();return request<ClientQuery[]>(`/api/v1/admin/queries${q?`?${q}`:""}`);},
    get:(id:string)=>request<ClientQuery>(`/api/v1/admin/queries/${id}`),
    assign:(id:string,adminUserId:string)=>request<ClientQuery>(`/api/v1/admin/queries/${id}/assign`,{method:"PATCH",body:JSON.stringify({adminUserId})}),
    resolve:(id:string,resolution:string)=>request<ClientQuery>(`/api/v1/admin/queries/${id}/resolve`,{method:"POST",body:JSON.stringify({resolution})})
  },
  users:{
    list:(status?:AdminUserStatus,role?:string)=>{const p=new URLSearchParams();if(status)p.set("status",status);if(role)p.set("role",role);const q=p.toString();return request<AdminUser[]>(`/api/v1/admin/users${q?`?${q}`:""}`);},
    get:(id:string)=>request<AdminUser>(`/api/v1/admin/users/${id}`),
    updateStatus:(id:string,status:AdminUserStatus)=>request<AdminUser>(`/api/v1/admin/users/${id}/status`,{method:"PATCH",body:JSON.stringify({status})})
  },
  disputes:{
    list:()=>request<P2PDispute[]>("/api/v1/p2p/admin/disputes"),
    get:(id:string)=>request<P2PDispute>(`/api/v1/p2p/admin/disputes/${id}`),
    audit:(id:string)=>request<P2PDisputeAudit[]>(`/api/v1/p2p/admin/disputes/${id}/audit`),
    resolve:(id:string,data:ResolveDisputeRequest)=>request<P2PDispute>(`/api/v1/p2p/admin/disputes/${id}/resolve`,{method:"POST",body:JSON.stringify(data)})
  },
  trades:{get:(id:string)=>request<AdminTransaction>(`/api/v1/p2p/trades/${id}`)},
  transactions:{list:(limit=100)=>request<AdminTransaction[]>(`/api/v1/admin/dashboard/transactions?limit=${Math.min(Math.max(limit,1),200)}`)}
};
