import * as SecureStore from 'expo-secure-store';
import { OakPayApiError, authApi } from './api';

const raw=(process.env.EXPO_PUBLIC_API_URL??'').trim();
const API_BASE_URL=raw?( /^https?:\/\//i.test(raw)?raw:`http://${raw}`).replace(/\/$/,''):'';

export type TwoFactorStatus={enabled:boolean};
export type TwoFactorSetup={secret:string;otpauthUri:string};
export type TwoFactorEnabledResponse={enabled:boolean;message:string};
export type TwoFactorLoginResponse={tokenType:string;accessToken:string;refreshToken:string;expiresIn:number;requiresTwoFactor:boolean;challengeToken?:string|null};

async function request<T>(path:string,options:RequestInit={}):Promise<T>{
  if(!API_BASE_URL) throw new Error('EXPO_PUBLIC_API_URL is not configured.');
  const response=await fetch(`${API_BASE_URL}${path}`,{...options,headers:{Accept:'application/json','Content-Type':'application/json',...(options.headers??{})}});
  const rawBody=await response.text();
  let body:any=null; try{body=rawBody?JSON.parse(rawBody):null;}catch{body=rawBody;}
  if(!response.ok) throw new OakPayApiError(response.status,body?.message??body?.error??`Request failed with status ${response.status}`);
  return body as T;
}

async function authenticated<T>(path:string,token:string,method:'GET'|'POST',body?:unknown):Promise<T>{
  try{return await request<T>(path,{method,headers:{Authorization:`Bearer ${token}`},...(body===undefined?{}:{body:JSON.stringify(body)})});}
  catch(error){
    if(!(error instanceof OakPayApiError)||!([401,403] as number[]).includes(error.status)) throw error;
    const refreshToken=await SecureStore.getItemAsync('oakpay.refreshToken');
    if(!refreshToken) throw error;
    const refreshed=await authApi.refresh(refreshToken);
    await SecureStore.setItemAsync('oakpay.accessToken',refreshed.accessToken);
    await SecureStore.setItemAsync('oakpay.refreshToken',refreshed.refreshToken);
    return request<T>(path,{method,headers:{Authorization:`Bearer ${refreshed.accessToken}`},...(body===undefined?{}:{body:JSON.stringify(body)})});
  }
}

export const twoFactorApi={
  status:(token:string)=>authenticated<TwoFactorStatus>('/api/v1/auth/2fa/status',token,'GET'),
  setup:(token:string)=>authenticated<TwoFactorSetup>('/api/v1/auth/2fa/setup',token,'POST'),
  enable:(token:string,code:string)=>authenticated<TwoFactorEnabledResponse>('/api/v1/auth/2fa/enable',token,'POST',{code}),
  disable:(token:string,password:string,code:string)=>authenticated<TwoFactorEnabledResponse>('/api/v1/auth/2fa/disable',token,'POST',{password,code}),
  verifyLogin:(challengeToken:string,code:string)=>request<TwoFactorLoginResponse>('/api/v1/auth/verify-2fa',{method:'POST',body:JSON.stringify({challengeToken,code})}),
};
