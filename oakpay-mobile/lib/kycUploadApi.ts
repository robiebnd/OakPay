import * as SecureStore from 'expo-secure-store';
import { authApi, isAuthError, OakPayApiError } from './api';
const raw=(process.env.EXPO_PUBLIC_API_URL??'').trim();
const API_BASE_URL=raw?( /^https?:\/\//i.test(raw)?raw:`http://${raw}`).replace(/\/$/,''):'';
export async function uploadKycDocument(token:string,documentId:string,side:'front'|'back',file:{uri:string;name:string;type:string}){
 const send=async(accessToken:string)=>{const form=new FormData();form.append('file',file as any);const response=await fetch(`${API_BASE_URL}/api/v1/kyc/documents/${documentId}/upload?side=${side}`,{method:'POST',headers:{Authorization:`Bearer ${accessToken}`,Accept:'application/json'},body:form});const rawBody=await response.text();let body:any=null;try{body=rawBody?JSON.parse(rawBody):null;}catch{body=rawBody;}if(!response.ok)throw new OakPayApiError(response.status,body?.message??body?.error??`Upload failed with status ${response.status}`);return body;};
 try{return await send(token);}catch(error){if(!isAuthError(error))throw error;const refreshToken=await SecureStore.getItemAsync('oakpay.refreshToken');if(!refreshToken)throw error;const refreshed=await authApi.refresh(refreshToken);await SecureStore.setItemAsync('oakpay.accessToken',refreshed.accessToken);await SecureStore.setItemAsync('oakpay.refreshToken',refreshed.refreshToken);return send(refreshed.accessToken);}
}
