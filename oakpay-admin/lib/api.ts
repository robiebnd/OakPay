const BASE=(process.env.NEXT_PUBLIC_API_URL??'http://localhost:8080').replace(/\/$/,'');
export type TokenResponse={accessToken:string;refreshToken:string;expiresIn:number;requiresTwoFactor?:boolean;challengeToken?:string|null};
export type KycDocument={id:string;documentType:string;documentNumber:string|null;status:string;rejectionReason:string|null;frontUploaded:boolean;backUploaded:boolean;createdAt:string;updatedAt:string};
export type KycItem={kycId:string;userId:string;clientName:string;email:string;country:string|null;status:string;submittedAt:string|null;documents:KycDocument[]};
async function json<T>(path:string,init:RequestInit={}):Promise<T>{const r=await fetch(`${BASE}${path}`,{...init,headers:{Accept:'application/json','Content-Type':'application/json',...(init.headers??{})}});const text=await r.text();let body:any=null;try{body=text?JSON.parse(text):null}catch{body=text}if(!r.ok)throw new Error(body?.message??body?.error??`Request failed with status ${r.status}`);return body as T;}
export const adminApi={
 login:(email:string,password:string)=>json<TokenResponse>('/api/v1/auth/login',{method:'POST',body:JSON.stringify({email,password})}),
 verify2fa:(challengeToken:string,code:string)=>json<TokenResponse>('/api/v1/auth/verify-2fa',{method:'POST',body:JSON.stringify({challengeToken,code})}),
 summary:(token:string)=>json<{pending:number;verified:number;rejected:number}>('/api/v1/admin/kyc/summary',{headers:{Authorization:`Bearer ${token}`}}),
 kyc:(token:string,status='PENDING')=>json<KycItem[]>(`/api/v1/admin/kyc?status=${encodeURIComponent(status)}`,{headers:{Authorization:`Bearer ${token}`}}),
 kycOne:(token:string,id:string)=>json<KycItem>(`/api/v1/admin/kyc/${id}`,{headers:{Authorization:`Bearer ${token}`}}),
 approve:(token:string,id:string)=>json(`/api/v1/admin/kyc/${id}/approve`,{method:'POST',headers:{Authorization:`Bearer ${token}`}}),
 reject:(token:string,id:string,reason:string)=>json(`/api/v1/admin/kyc/${id}/reject`,{method:'POST',headers:{Authorization:`Bearer ${token}`},body:JSON.stringify({reason})}),
};
export const session={get:()=>typeof window==='undefined'?null:localStorage.getItem('oakpay.admin.accessToken'),set:(t:TokenResponse)=>{localStorage.setItem('oakpay.admin.accessToken',t.accessToken);localStorage.setItem('oakpay.admin.refreshToken',t.refreshToken)},clear:()=>{localStorage.removeItem('oakpay.admin.accessToken');localStorage.removeItem('oakpay.admin.refreshToken')}};