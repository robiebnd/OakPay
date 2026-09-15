const BASE=(process.env.NEXT_PUBLIC_API_URL??'http://localhost:8080').replace(/\/$/,'');
export type TokenResponse={accessToken:string;refreshToken:string;expiresIn:number;requiresTwoFactor?:boolean;challengeToken?:string|null};
export type KycDocument={id:string;documentType:string;documentNumber:string|null;status:string;rejectionReason:string|null;frontUploaded:boolean;backUploaded:boolean;createdAt:string;updatedAt:string};
export type KycItem={kycId:string;userId:string;clientName:string;email:string;country:string|null;status:string;submittedAt:string|null;documents:KycDocument[]};
export type ResolutionDispute={id:string;tradeId:string;openedBy:string;reason:string;evidence:string|null;status:string;resolution:string|null;resolutionNote:string|null;resolvedBy:string|null;resolvedAt:string|null;createdAt:string;updatedAt:string};
export type ResolutionAudit={id:string;disputeId:string;tradeId:string;actorId:string;eventType:string;note:string|null;createdAt:string};

async function rawJson<T>(path:string,init:RequestInit={}):Promise<Response>{return fetch(`${BASE}${path}`,{...init,headers:{Accept:'application/json','Content-Type':'application/json',...(init.headers??{})}});}
async function parse<T>(r:Response):Promise<T>{const text=await r.text();let body:any=null;try{body=text?JSON.parse(text):null}catch{body=text}if(!r.ok)throw new Error(body?.message??body?.error??`Request failed with status ${r.status}`);return body as T;}
async function refreshAccessToken():Promise<string|null>{const refreshToken=session.getRefresh();if(!refreshToken)return null;try{const response=await rawJson<TokenResponse>('/api/v1/auth/refresh',{method:'POST',body:JSON.stringify({refreshToken})});if(!response.ok){session.clear();return null;}const token=await parse<TokenResponse>(response);session.set(token);return token.accessToken;}catch{session.clear();return null;}}
async function json<T>(path:string,init:RequestInit={}):Promise<T>{let response=await rawJson<T>(path,init);const authHeader=new Headers(init.headers).get('Authorization');if(response.status===401&&authHeader){const token=await refreshAccessToken();if(token){const headers=new Headers(init.headers);headers.set('Authorization',`Bearer ${token}`);response=await rawJson<T>(path,{...init,headers});}}return parse<T>(response);}

export const adminApi={
 login:(email:string,password:string)=>json<TokenResponse>('/api/v1/auth/login',{method:'POST',body:JSON.stringify({email,password})}),
 verify2fa:(challengeToken:string,code:string)=>json<TokenResponse>('/api/v1/auth/verify-2fa',{method:'POST',body:JSON.stringify({challengeToken,code})}),
 summary:(token:string)=>json<{pending:number;verified:number;rejected:number}>('/api/v1/admin/kyc/summary',{headers:{Authorization:`Bearer ${token}`}}),
 kyc:(token:string,status='PENDING')=>json<KycItem[]>(`/api/v1/admin/kyc?status=${encodeURIComponent(status)}`,{headers:{Authorization:`Bearer ${token}`}}),
 kycOne:(token:string,id:string)=>json<KycItem>(`/api/v1/admin/kyc/${id}`,{headers:{Authorization:`Bearer ${token}`}}),
 approve:(token:string,id:string)=>json(`/api/v1/admin/kyc/${id}/approve`,{method:'POST',headers:{Authorization:`Bearer ${token}`}}),
 reject:(token:string,id:string,reason:string)=>json(`/api/v1/admin/kyc/${id}/reject`,{method:'POST',headers:{Authorization:`Bearer ${token}`},body:JSON.stringify({reason})}),
 resolutionDisputes:(token:string)=>json<ResolutionDispute[]>('/api/v1/admin/resolution-centre/disputes',{headers:{Authorization:`Bearer ${token}`}}),
 resolutionAudit:(token:string,id:string)=>json<ResolutionAudit[]>(`/api/v1/admin/resolution-centre/disputes/${id}/audit`,{headers:{Authorization:`Bearer ${token}`}}),
 resolveDispute:(token:string,id:string,resolution:'BUYER_WINS'|'SELLER_WINS',note:string)=>json<ResolutionDispute>(`/api/v1/admin/resolution-centre/disputes/${id}/resolve`,{method:'POST',headers:{Authorization:`Bearer ${token}`},body:JSON.stringify({resolution,note})}),
};
export const session={get:()=>typeof window==='undefined'?null:localStorage.getItem('oakpay.admin.accessToken'),getRefresh:()=>typeof window==='undefined'?null:localStorage.getItem('oakpay.admin.refreshToken'),set:(t:TokenResponse)=>{localStorage.setItem('oakpay.admin.accessToken',t.accessToken);if(t.refreshToken)localStorage.setItem('oakpay.admin.refreshToken',t.refreshToken)},clear:()=>{localStorage.removeItem('oakpay.admin.accessToken');localStorage.removeItem('oakpay.admin.refreshToken')}};