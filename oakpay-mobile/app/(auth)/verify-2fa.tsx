import { useState } from 'react';
import { ActivityIndicator, KeyboardAvoidingView, Platform, Pressable, ScrollView, StyleSheet, Text, TextInput, View } from 'react-native';
import { router, useLocalSearchParams } from 'expo-router';
import * as SecureStore from 'expo-secure-store';
import { useAuth } from '../../context/AuthContext';
import { twoFactorApi } from '../../lib/twoFactorApi';

export default function VerifyTwoFactorScreen(){
  const { challengeToken }=useLocalSearchParams<{challengeToken?:string}>();
  const { refreshSession }=useAuth();
  const [code,setCode]=useState(''); const [error,setError]=useState(''); const [submitting,setSubmitting]=useState(false);
  async function verify(){
    setError('');
    if(!challengeToken){setError('Your sign-in challenge is missing. Please sign in again.');return;}
    if(!/^\d{6}$/.test(code)){setError('Enter the 6-digit code from your authenticator app.');return;}
    try{setSubmitting(true);const tokens=await twoFactorApi.verifyLogin(challengeToken,code);await SecureStore.setItemAsync('oakpay.accessToken',tokens.accessToken);await SecureStore.setItemAsync('oakpay.refreshToken',tokens.refreshToken);const ok=await refreshSession();if(!ok)throw new Error('Your session could not be restored. Please sign in again.');router.replace('/(tabs)');}
    catch(e){setError(e instanceof Error?e.message:'Invalid authenticator code.');}
    finally{setSubmitting(false);}
  }
  return <KeyboardAvoidingView style={styles.safe} behavior={Platform.OS==='ios'?'padding':'height'}><ScrollView contentContainerStyle={styles.scroll} keyboardShouldPersistTaps="handled"><View style={styles.card}><View style={styles.icon}><Text style={styles.iconText}>2FA</Text></View><Text style={styles.title}>Two-factor authentication</Text><Text style={styles.description}>Enter the 6-digit code shown in your authenticator app to finish signing in.</Text><TextInput value={code} onChangeText={v=>setCode(v.replace(/\D/g,'').slice(0,6))} keyboardType="number-pad" textContentType="oneTimeCode" autoFocus placeholder="000000" placeholderTextColor="#70757D" maxLength={6} style={styles.input}/>{error?<Text style={styles.error}>{error}</Text>:null}<Pressable onPress={verify} disabled={submitting} style={[styles.button,submitting&&styles.disabled]}>{submitting?<ActivityIndicator color="#FFFFFF"/>:<Text style={styles.buttonText}>Verify and continue</Text>}</Pressable><Pressable onPress={()=>router.replace('/(auth)/login')} disabled={submitting}><Text style={styles.back}>Back to sign in</Text></Pressable></View></ScrollView></KeyboardAvoidingView>;
}
const styles=StyleSheet.create({safe:{flex:1,backgroundColor:'#F7F8F9'},scroll:{flexGrow:1,justifyContent:'center',padding:28},card:{backgroundColor:'#FFFFFF',borderRadius:24,padding:24,borderWidth:1,borderColor:'#E7E9EC'},icon:{width:64,height:64,borderRadius:20,backgroundColor:'#123B2A',alignItems:'center',justifyContent:'center',alignSelf:'center',marginBottom:20},iconText:{color:'#D8FF3E',fontWeight:'800',fontSize:15},title:{fontFamily:'Inter_700Bold',fontSize:26,color:'#111916',textAlign:'center'},description:{fontFamily:'Inter_400Regular',fontSize:15,color:'#6F747B',lineHeight:22,textAlign:'center',marginTop:10,marginBottom:24},input:{height:62,borderRadius:17,borderWidth:1,borderColor:'#EEE9AE',backgroundColor:'#FFFCE0',fontFamily:'Inter_700Bold',fontSize:24,letterSpacing:8,textAlign:'center',color:'#111916'},error:{color:'#B42318',fontFamily:'Inter_600SemiBold',fontSize:13,lineHeight:19,textAlign:'center',marginTop:12},button:{height:56,borderRadius:28,backgroundColor:'#123B2A',alignItems:'center',justifyContent:'center',marginTop:16},disabled:{opacity:.6},buttonText:{color:'#FFFFFF',fontFamily:'Inter_700Bold',fontSize:15},back:{color:'#123B2A',fontFamily:'Inter_700Bold',fontSize:14,textAlign:'center',marginTop:20}});
