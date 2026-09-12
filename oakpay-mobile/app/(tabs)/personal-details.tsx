import { Ionicons } from '@expo/vector-icons';
import { router } from 'expo-router';
import { useFocusEffect } from 'expo-router';
import { useCallback, useState } from 'react';
import { Alert, KeyboardAvoidingView, Platform, Pressable, ScrollView, StyleSheet, Text, TextInput, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useAuth } from '../../context/AuthContext';
import { authApi, UserResponse } from '../../lib/api';
import { accountApi } from '../../lib/accountApi';

const BG='#0D1017',CARD='#171B24',TEXT='#F4F6F8',MUTED='#8D95A3',LIME='#D8FF3E',BORDER='#242A34';

export default function PersonalDetailsScreen(){
 const {user,accessToken}=useAuth();
 const [profile,setProfile]=useState<UserResponse|null>(user);
 const [firstName,setFirstName]=useState(user?.firstName??''); const [lastName,setLastName]=useState(user?.lastName??'');
 const [phoneNumber,setPhoneNumber]=useState(user?.phoneNumber??''); const [country,setCountry]=useState(user?.country??''); const [dateOfBirth,setDateOfBirth]=useState(user?.dateOfBirth??'');
 const [saving,setSaving]=useState(false); const [loading,setLoading]=useState(true);
 useFocusEffect(useCallback(()=>{let active=true;(async()=>{if(!accessToken){setLoading(false);return;}try{const p=await authApi.me(accessToken);if(!active)return;setProfile(p);setFirstName(p.firstName);setLastName(p.lastName);setPhoneNumber(p.phoneNumber??'');setCountry(p.country??'');setDateOfBirth(p.dateOfBirth??'');}catch{}finally{if(active)setLoading(false);}})();return()=>{active=false;};},[accessToken]));
 async function save(){if(!accessToken)return;if(!firstName.trim()||!lastName.trim()){Alert.alert('Missing information','First name and last name are required.');return;}setSaving(true);try{const updated=await accountApi.updateProfile(accessToken,{firstName:firstName.trim(),lastName:lastName.trim(),phoneNumber:phoneNumber.trim(),country:country.trim(),dateOfBirth:dateOfBirth.trim()||null});setProfile(updated);Alert.alert('Profile updated','Your personal details have been saved.',[{text:'OK',onPress:()=>router.back()}]);}catch(e){Alert.alert('Update failed',e instanceof Error?e.message:'We could not update your profile.');}finally{setSaving(false);}}
 const Field=({label,value,onChangeText,placeholder,keyboardType='default'}:{label:string;value:string;onChangeText:(v:string)=>void;placeholder:string;keyboardType?:'default'|'phone-pad'})=><View style={styles.field}><Text style={styles.label}>{label}</Text><TextInput value={value} onChangeText={onChangeText} placeholder={placeholder} placeholderTextColor={MUTED} keyboardType={keyboardType} returnKeyType="next" style={styles.input}/></View>;
 return <SafeAreaView style={styles.safe} edges={['top']}><KeyboardAvoidingView style={styles.keyboard} behavior={Platform.OS==='ios'?'padding':'height'} keyboardVerticalOffset={Platform.OS==='ios'?8:0}><ScrollView keyboardShouldPersistTaps="handled" keyboardDismissMode="on-drag" showsVerticalScrollIndicator={false} contentContainerStyle={styles.content}><View style={styles.container}>
  <View style={styles.header}><Pressable style={styles.back} onPress={()=>router.back()}><Ionicons name="arrow-back" size={21} color={TEXT}/></Pressable><Text style={styles.title}>Personal details</Text><View style={styles.spacer}/></View>
  <Text style={styles.subtitle}>Keep your OakPay account information up to date.</Text>
  <View style={styles.card}><Field label="FIRST NAME" value={firstName} onChangeText={setFirstName} placeholder="First name"/><Field label="LAST NAME" value={lastName} onChangeText={setLastName} placeholder="Last name"/><Field label="PHONE NUMBER" value={phoneNumber} onChangeText={setPhoneNumber} placeholder="Phone number" keyboardType="phone-pad"/><Field label="COUNTRY" value={country} onChangeText={setCountry} placeholder="Country"/><Field label="DATE OF BIRTH" value={dateOfBirth} onChangeText={setDateOfBirth} placeholder="YYYY-MM-DD"/>
   <Text style={styles.label}>EMAIL</Text><View style={styles.readonly}><Text style={styles.readonlyText}>{profile?.email??user?.email??'No email available'}</Text><Ionicons name="lock-closed-outline" size={15} color={MUTED}/></View><Text style={styles.hint}>Email changes require a separate verification process.</Text>
  </View>
  <Pressable disabled={saving||loading} style={[styles.save,(saving||loading)&&styles.disabled]} onPress={save}><Text style={styles.saveText}>{saving?'Saving…':'Save changes'}</Text></Pressable>
 </View></ScrollView></KeyboardAvoidingView></SafeAreaView>;
}
const styles=StyleSheet.create({safe:{flex:1,backgroundColor:BG},keyboard:{flex:1},content:{paddingBottom:50},container:{paddingHorizontal:20,paddingTop:8},header:{flexDirection:'row',alignItems:'center',justifyContent:'space-between',marginBottom:8},back:{width:42,height:42,borderRadius:14,backgroundColor:CARD,borderWidth:1,borderColor:BORDER,alignItems:'center',justifyContent:'center'},spacer:{width:42},title:{color:TEXT,fontSize:25,fontWeight:'800'},subtitle:{color:MUTED,fontSize:12,lineHeight:18,marginBottom:18},card:{backgroundColor:CARD,borderRadius:20,borderWidth:1,borderColor:BORDER,padding:16},field:{marginBottom:15},label:{color:MUTED,fontSize:9,fontWeight:'800',letterSpacing:1,marginBottom:7},input:{height:48,borderRadius:13,borderWidth:1,borderColor:BORDER,backgroundColor:'#10151D',paddingHorizontal:13,color:TEXT,fontSize:14},readonly:{height:48,borderRadius:13,borderWidth:1,borderColor:BORDER,backgroundColor:'#10151D',paddingHorizontal:13,flexDirection:'row',alignItems:'center',justifyContent:'space-between'},readonlyText:{color:MUTED,fontSize:14},hint:{color:MUTED,fontSize:10,lineHeight:15,marginTop:7},save:{height:52,borderRadius:15,backgroundColor:LIME,alignItems:'center',justifyContent:'center',marginTop:18},disabled:{opacity:.5},saveText:{color:'#10151D',fontWeight:'800',fontSize:14}});
