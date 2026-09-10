import { Ionicons } from '@expo/vector-icons';
import * as SecureStore from 'expo-secure-store';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { ActivityIndicator, Modal, Pressable, ScrollView, StyleSheet, Text, TextInput, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useAuth } from '../../context/AuthContext';
import { P2PPayment, P2PTrade, isAuthError, p2pApi } from '../../lib/api';

const BG='#0D1017',CARD='#171B24',CARD2='#1D222C',TEXT='#F4F6F8',MUTED='#8D95A3',LIME='#D8FF3E',RED='#FF6675',AMBER='#FFC857';
const ACTIVE=['PAYMENT_PENDING','PAYMENT_MARKED','DISPUTED'];
const statusLabel=(s?:string)=>({ESCROWED:'Escrowed',PAYMENT_PENDING:'Payment pending',PAYMENT_MARKED:'Payment marked',COMPLETED:'Completed',CANCELLED:'Cancelled',DISPUTED:'Disputed',EXPIRED:'Expired'}[s||'']||s||'Unknown');
const money=(n:number|undefined)=>Number(n||0).toLocaleString(undefined,{minimumFractionDigits:2,maximumFractionDigits:8});

export default function OrdersScreen(){
 const {accessToken,user,refreshSession}=useAuth();
 const [trades,setTrades]=useState<P2PTrade[]>([]);
 const [tab,setTab]=useState<'ACTIVE'|'HISTORY'>('ACTIVE');
 const [loading,setLoading]=useState(true);
 const [error,setError]=useState('');
 const [selected,setSelected]=useState<P2PTrade|null>(null);
 const [payment,setPayment]=useState<P2PPayment|null>(null);
 const [paymentLoading,setPaymentLoading]=useState(false);
 const [paymentReference,setPaymentReference]=useState('');
 const [paymentNote,setPaymentNote]=useState('');
 const [submitting,setSubmitting]=useState(false);
 const [disputeVisible,setDisputeVisible]=useState(false);
 const [disputeReason,setDisputeReason]=useState('');
 const [disputeEvidence,setDisputeEvidence]=useState('');
 const [disputeError,setDisputeError]=useState('');
 const [disputeSubmitting,setDisputeSubmitting]=useState(false);

 const load=useCallback(async()=>{
  if(!accessToken){setTrades([]);setLoading(false);return;}
  try{setError('');setLoading(true);setTrades(await p2pApi.trades(accessToken));}
  catch(e){
   if(isAuthError(e)){const refreshed=await refreshSession();if(refreshed)return;}
   setError(e instanceof Error?e.message:'Unable to load P2P orders.');
  }finally{setLoading(false);}
 },[accessToken,refreshSession]);
 useEffect(()=>{load()},[load]);

 const visible=useMemo(()=>trades.filter(t=>tab==='ACTIVE'?ACTIVE.includes(t.status||''):[...ACTIVE].every(s=>s!==t.status)),[trades,tab]);
 const isBuyer=!!selected&&selected.buyerId===user?.id;
 const isSeller=!!selected&&selected.sellerId===user?.id;

 async function openTrade(trade:P2PTrade){
  setError('');
  setSelected(trade);
  setPayment(null);
  setPaymentReference(trade.paymentReference||'');
  setPaymentNote(trade.paymentNote||'');
  setDisputeVisible(false);
  setDisputeReason('');
  setDisputeEvidence('');
  setDisputeError('');
  if(!accessToken)return;
  if(trade.status==='PAYMENT_MARKED'||trade.status==='COMPLETED'||trade.status==='DISPUTED'){
   try{setPaymentLoading(true);setPayment(await p2pApi.payment(accessToken,trade.id));}
   catch(e){if(!isAuthError(e))setError(e instanceof Error?e.message:'Unable to load payment details.');}
   finally{setPaymentLoading(false);}
  }
 }

 async function action(kind:'paid'|'verify'|'confirm'|'cancel'){
  if(!accessToken||!selected)return;
  try{
   setSubmitting(true);setError('');
   if(kind==='paid'){
    if(!paymentReference.trim()){setError('Enter a payment reference.');return;}
    await p2pApi.markPaid(accessToken,selected.id,paymentReference.trim(),paymentNote.trim()||undefined);
   }else if(kind==='verify'){
    const verified=await p2pApi.verifyPayment(accessToken,selected.id);
    setPayment(verified);
    const refreshedTrade=await p2pApi.trade(accessToken,selected.id);
    setSelected(refreshedTrade);
    setTrades(current=>current.map(t=>t.id===refreshedTrade.id?refreshedTrade:t));
    return;
   }else if(kind==='confirm'){
    await p2pApi.confirm(accessToken,selected.id);
   }else{
    await p2pApi.cancel(accessToken,selected.id);
   }
   setSelected(null);setPayment(null);setPaymentReference('');setPaymentNote('');await load();
  }catch(e){
   if(isAuthError(e)){const refreshed=await refreshSession();if(!refreshed)setError('Your session has expired. Please sign in again.');}
   else setError(e instanceof Error?e.message:'Order action failed.');
  }finally{setSubmitting(false);}
 }

 function openDispute(){
  setDisputeReason('');
  setDisputeEvidence('');
  setDisputeError('');
  setDisputeVisible(true);
 }

 async function submitDispute(){
  if(!selected||!accessToken)return;
  const reason=disputeReason.trim();
  if(!reason){setDisputeError('Please provide a reason for the dispute.');return;}
  try{
   setDisputeSubmitting(true);setDisputeError('');setError('');
   const submit=async(token:string)=>p2pApi.dispute(token,selected.id,reason,disputeEvidence.trim()||undefined);
   try{
    await submit(accessToken);
   }catch(firstError){
    if(!isAuthError(firstError))throw firstError;
    const refreshed=await refreshSession();
    if(!refreshed)throw new Error('Your session has expired. Please sign in again.');
    const latestToken=await SecureStore.getItemAsync('oakpay.accessToken');
    if(!latestToken)throw new Error('Your session has expired. Please sign in again.');
    await submit(latestToken);
   }
   setDisputeVisible(false);
   setDisputeReason('');
   setDisputeEvidence('');
   setDisputeError('');
   const refreshedTrade=await p2pApi.trade((await SecureStore.getItemAsync('oakpay.accessToken'))||accessToken,selected.id);
   setSelected(refreshedTrade);
   setTrades(current=>current.map(t=>t.id===refreshedTrade.id?refreshedTrade:t));
  }catch(e){
   setDisputeError(e instanceof Error?e.message:'Unable to open the dispute.');
  }finally{setDisputeSubmitting(false);}
 }

 const paymentVerified=payment?.status==='VERIFIED';
 const canRelease=isSeller&&selected?.status==='PAYMENT_MARKED'&&paymentVerified;

 return <SafeAreaView style={styles.safe} edges={['top']}>
  <View style={styles.screen}>
   <ScrollView contentContainerStyle={styles.content} showsVerticalScrollIndicator={false}>
    <View style={styles.container}>
     <View style={styles.header}><View><Text style={styles.eyebrow}>OAKPAY ACCOUNT</Text><Text style={styles.title}>P2P Orders</Text></View><View style={styles.iconButton}><Ionicons name="notifications-outline" size={21} color={TEXT}/></View></View>
     <Text style={styles.subtitle}>Track purchases and sales from offer to completion.</Text>
     <View style={styles.tabs}><Pressable style={[styles.tab,tab==='ACTIVE'&&styles.activeTab]} onPress={()=>setTab('ACTIVE')}><Text style={[styles.tabText,tab==='ACTIVE'&&styles.activeText]}>Active</Text></Pressable><Pressable style={[styles.tab,tab==='HISTORY'&&styles.activeTab]} onPress={()=>setTab('HISTORY')}><Text style={[styles.tabText,tab==='HISTORY'&&styles.activeText]}>History</Text></Pressable></View>
     {error?<View style={styles.error}><Ionicons name="alert-circle-outline" size={17} color={RED}/><Text style={styles.errorText}>{error}</Text><Pressable onPress={()=>load()}><Text style={styles.retryText}>Retry</Text></Pressable></View>:null}
     <View style={styles.sectionRow}><Text style={styles.sectionTitle}>{tab==='ACTIVE'?'Current orders':'Order history'}</Text><Text style={styles.count}>{visible.length}</Text></View>
     {loading?<View style={styles.loading}><ActivityIndicator color={LIME}/><Text style={styles.muted}>Loading orders…</Text></View>:visible.length===0?<View style={styles.empty}><View style={styles.emptyIcon}><Ionicons name="receipt-outline" size={25} color={LIME}/></View><Text style={styles.emptyTitle}>{tab==='ACTIVE'?'No active orders':'No order history'}</Text><Text style={styles.muted}>{tab==='ACTIVE'?'Your P2P purchase and sale orders will appear here.':'Completed, cancelled, disputed and expired P2P orders will appear here.'}</Text></View>:visible.map(t=><Pressable key={t.id} style={styles.order} onPress={()=>openTrade(t)}><View style={styles.orderTop}><View style={styles.assetIcon}><Text style={styles.assetSymbol}>{t.asset?.slice(0,1)||'₮'}</Text></View><View style={styles.orderMain}><Text style={styles.orderAsset}>{t.asset||'Crypto'} <Text style={styles.orderSide}>{t.sellerId===user?.id?'SELL':'BUY'}</Text></Text><Text style={styles.orderId}>#{t.id.slice(0,8)}</Text></View><View style={[styles.status,{backgroundColor:t.status==='COMPLETED'?'#263015':t.status==='CANCELLED'||t.status==='EXPIRED'?'#24191D':'#26261B'}]}><Text style={[styles.statusText,{color:t.status==='COMPLETED'?LIME:t.status==='CANCELLED'||t.status==='EXPIRED'?RED:AMBER}]}>{statusLabel(t.status)}</Text></View></View><View style={styles.orderBottom}><View><Text style={styles.amount}>{money(t.quantity)} {t.asset}</Text><Text style={styles.meta}>{money(t.fiatAmount)} {t.fiatCurrency} · {money(t.unitPrice)} / {t.asset}</Text></View><Ionicons name="chevron-forward" size={18} color={MUTED}/></View></Pressable>)}
    </View>
   </ScrollView>

   <Modal visible={!!selected} transparent animationType="slide" onRequestClose={()=>!submitting&&!disputeSubmitting&&setSelected(null)}>
    <View style={styles.overlay}>
     <View style={styles.sheet}>
      <View style={styles.handle}/>
      <View style={styles.sheetHeader}><View><Text style={styles.sheetEyebrow}>P2P ORDER</Text><Text style={styles.sheetTitle}>{selected?.asset||'Crypto'} {isSeller?'Sale':'Purchase'}</Text></View><View style={styles.sheetStatus}><Text style={styles.statusText}>{statusLabel(selected?.status)}</Text></View></View>
      {selected?<>
       <View style={styles.detailGrid}><View><Text style={styles.detailLabel}>AMOUNT</Text><Text style={styles.detailValue}>{money(selected.quantity)} {selected.asset}</Text></View><View><Text style={styles.detailLabel}>TOTAL</Text><Text style={styles.detailValue}>{money(selected.fiatAmount)} {selected.fiatCurrency}</Text></View><View><Text style={styles.detailLabel}>PAYMENT</Text><Text style={styles.detailValue}>{selected.paymentMethod||'Not specified'}</Text></View><View><Text style={styles.detailLabel}>EXPIRES</Text><Text style={styles.detailValue}>{selected.expiresAt?new Date(selected.expiresAt).toLocaleString():'—'}</Text></View></View>
       {selected.status==='PAYMENT_MARKED'||selected.status==='COMPLETED'||selected.status==='DISPUTED'?<View style={styles.paymentCard}>{paymentLoading?<ActivityIndicator color={LIME}/>:payment?<><Text style={styles.detailLabel}>PAYMENT REFERENCE</Text><Text style={styles.paymentReference}>{payment.paymentReference}</Text><Text style={styles.paymentMeta}>{money(payment.amount)} {payment.currency} · {payment.status}</Text>{payment.note?<Text style={styles.paymentNote}>{payment.note}</Text>:null}</>:<Text style={styles.muted}>Payment details unavailable.</Text>}</View>:null}
       {selected.status==='DISPUTED'?<View style={styles.disputeNotice}><Ionicons name="shield-checkmark-outline" size={18} color={LIME}/><View style={styles.disputeNoticeText}><Text style={styles.disputeNoticeTitle}>Dispute opened</Text><Text style={styles.mutedLeft}>This order is now under dispute review.</Text></View></View>:null}
       {isBuyer&&selected.status==='PAYMENT_PENDING'?<><Text style={styles.inputLabel}>Payment reference</Text><TextInput value={paymentReference} onChangeText={setPaymentReference} placeholder="e.g. bank reference" placeholderTextColor={MUTED} style={styles.input}/><Text style={styles.inputLabel}>Payment note (optional)</Text><TextInput value={paymentNote} onChangeText={setPaymentNote} placeholder="Add a note" placeholderTextColor={MUTED} style={[styles.input,{height:48}]} /></>:null}
       <View style={styles.actions}>
        {isBuyer&&selected.status==='PAYMENT_PENDING'?<Pressable style={[styles.primary,submitting&&{opacity:.6}]} disabled={submitting} onPress={()=>action('paid')}>{submitting?<ActivityIndicator color={BG}/>:<><Ionicons name="checkmark-circle-outline" size={18} color={BG}/><Text style={styles.primaryText}>I have paid</Text></>}</Pressable>:null}
        {isSeller&&selected.status==='PAYMENT_MARKED'&&!paymentVerified?<Pressable style={[styles.primary,submitting&&{opacity:.6}]} disabled={submitting||paymentLoading} onPress={()=>action('verify')}>{submitting?<ActivityIndicator color={BG}/>:<><Ionicons name="shield-checkmark-outline" size={18} color={BG}/><Text style={styles.primaryText}>Verify payment</Text></>}</Pressable>:null}
        {canRelease?<Pressable style={[styles.primary,submitting&&{opacity:.6}]} disabled={submitting} onPress={()=>action('confirm')}>{submitting?<ActivityIndicator color={BG}/>:<><Ionicons name="lock-open-outline" size={18} color={BG}/><Text style={styles.primaryText}>Release crypto</Text></>}</Pressable>:null}
        {(isBuyer||isSeller)&&selected.status==='PAYMENT_MARKED'?<Pressable style={[styles.danger,submitting&&{opacity:.6}]} disabled={submitting||disputeSubmitting} onPress={openDispute}><Ionicons name="alert-circle-outline" size={18} color={RED}/><Text style={styles.dangerText}>Open dispute</Text></Pressable>:null}
        {(isBuyer||isSeller)&&selected.status==='PAYMENT_PENDING'?<Pressable style={styles.danger} disabled={submitting||disputeSubmitting} onPress={()=>action('cancel')}><Text style={styles.dangerText}>Cancel order</Text></Pressable>:null}
       </View>
      </>:null}
      <Pressable disabled={submitting||disputeSubmitting} onPress={()=>setSelected(null)}><Text style={styles.cancel}>Close</Text></Pressable>
     </View>
    </View>
   </Modal>

   <Modal visible={disputeVisible} transparent animationType="fade" onRequestClose={()=>!disputeSubmitting&&setDisputeVisible(false)}>
    <View style={styles.disputeOverlay}>
     <View style={styles.disputeCard}>
      <View style={styles.disputeHeader}><View style={styles.disputeIcon}><Ionicons name="alert-circle-outline" size={24} color={RED}/></View><View style={{flex:1}}><Text style={styles.disputeTitle}>Open dispute</Text><Text style={styles.disputeSubtitle}>Tell us what happened with this payment.</Text></View><Pressable disabled={disputeSubmitting} onPress={()=>setDisputeVisible(false)}><Ionicons name="close" size={22} color={MUTED}/></Pressable></View>
      {disputeError?<View style={styles.disputeError}><Ionicons name="alert-circle-outline" size={17} color={RED}/><Text style={styles.disputeErrorText}>{disputeError}</Text></View>:null}
      <Text style={styles.inputLabel}>Reason *</Text>
      <TextInput value={disputeReason} onChangeText={setDisputeReason} placeholder="e.g. Payment was made but seller has not confirmed it" placeholderTextColor={MUTED} style={[styles.input,styles.multiline]} multiline textAlignVertical="top" maxLength={500}/>
      <Text style={styles.inputLabel}>Evidence / additional details</Text>
      <TextInput value={disputeEvidence} onChangeText={setDisputeEvidence} placeholder="Payment reference, screenshots description, or other relevant details" placeholderTextColor={MUTED} style={[styles.input,styles.multilineSmall]} multiline textAlignVertical="top" maxLength={1000}/>
      <Pressable style={[styles.primary,disputeSubmitting&&{opacity:.6}]} disabled={disputeSubmitting} onPress={submitDispute}>{disputeSubmitting?<ActivityIndicator color={BG}/>:<><Ionicons name="shield-checkmark-outline" size={18} color={BG}/><Text style={styles.primaryText}>Submit dispute</Text></>}</Pressable>
      <Pressable disabled={disputeSubmitting} onPress={()=>setDisputeVisible(false)}><Text style={styles.cancel}>Cancel</Text></Pressable>
     </View>
    </View>
   </Modal>
  </View>
 </SafeAreaView>
}

const styles=StyleSheet.create({
 safe:{flex:1,backgroundColor:BG},screen:{flex:1,backgroundColor:BG},content:{paddingBottom:36},container:{paddingHorizontal:20,paddingTop:8},header:{flexDirection:'row',alignItems:'center',justifyContent:'space-between',marginBottom:7},eyebrow:{color:LIME,fontFamily:'Inter_700Bold',fontSize:10,letterSpacing:1.8},title:{color:TEXT,fontFamily:'Inter_800ExtraBold',fontSize:30,lineHeight:38,marginTop:4},subtitle:{color:MUTED,fontFamily:'Inter_400Regular',fontSize:12,lineHeight:19,marginBottom:18},iconButton:{width:44,height:44,borderRadius:15,backgroundColor:CARD,alignItems:'center',justifyContent:'center'},tabs:{flexDirection:'row',backgroundColor:CARD,borderRadius:16,padding:4,marginBottom:18},tab:{flex:1,paddingVertical:13,alignItems:'center',borderRadius:12},activeTab:{backgroundColor:LIME},tabText:{color:MUTED,fontFamily:'Inter_700Bold',fontSize:13},activeText:{color:BG},error:{flexDirection:'row',alignItems:'center',gap:7,backgroundColor:'#24191D',borderRadius:13,padding:11,marginBottom:14},errorText:{flex:1,color:'#FF9EA8',fontFamily:'Inter_400Regular',fontSize:11},retryText:{color:LIME,fontFamily:'Inter_700Bold',fontSize:10},sectionRow:{flexDirection:'row',justifyContent:'space-between',alignItems:'center',marginBottom:10},sectionTitle:{color:TEXT,fontFamily:'Inter_800ExtraBold',fontSize:19},count:{color:MUTED,fontFamily:'Inter_700Bold',fontSize:10},loading:{height:100,backgroundColor:CARD,borderRadius:19,alignItems:'center',justifyContent:'center',gap:7},muted:{color:MUTED,fontFamily:'Inter_400Regular',fontSize:12,textAlign:'center',lineHeight:20},mutedLeft:{color:MUTED,fontFamily:'Inter_400Regular',fontSize:11,lineHeight:18},empty:{backgroundColor:CARD,borderRadius:21,padding:30,alignItems:'center',marginTop:2},emptyIcon:{width:62,height:62,borderRadius:19,backgroundColor:'#242A17',alignItems:'center',justifyContent:'center',marginBottom:16},emptyTitle:{color:TEXT,fontFamily:'Inter_700Bold',fontSize:19,marginBottom:7},order:{backgroundColor:CARD,borderRadius:19,padding:15,marginBottom:10,borderWidth:1,borderColor:'#222934'},orderTop:{flexDirection:'row',alignItems:'center'},assetIcon:{width:45,height:45,borderRadius:14,backgroundColor:'#263015',alignItems:'center',justifyContent:'center'},assetSymbol:{color:LIME,fontFamily:'Inter_800ExtraBold',fontSize:17},orderMain:{flex:1,marginLeft:11},orderAsset:{color:TEXT,fontFamily:'Inter_700Bold',fontSize:14},orderSide:{color:MUTED,fontFamily:'Inter_600SemiBold',fontSize:9},orderId:{color:MUTED,fontFamily:'Inter_400Regular',fontSize:9,marginTop:3},status:{borderRadius:9,paddingHorizontal:8,paddingVertical:6},statusText:{fontFamily:'Inter_700Bold',fontSize:9},orderBottom:{flexDirection:'row',justifyContent:'space-between',alignItems:'center',marginTop:14,paddingTop:12,borderTopWidth:1,borderTopColor:'#242A33'},amount:{color:TEXT,fontFamily:'Inter_800ExtraBold',fontSize:15},meta:{color:MUTED,fontFamily:'Inter_400Regular',fontSize:9,marginTop:3},overlay:{flex:1,backgroundColor:'rgba(0,0,0,.68)',justifyContent:'flex-end'},sheet:{backgroundColor:CARD,borderTopLeftRadius:26,borderTopRightRadius:26,padding:22,paddingBottom:34,maxHeight:'90%'},handle:{width:42,height:4,borderRadius:2,backgroundColor:'#424956',alignSelf:'center',marginBottom:18},sheetHeader:{flexDirection:'row',justifyContent:'space-between',alignItems:'flex-start',marginBottom:18},sheetEyebrow:{color:LIME,fontFamily:'Inter_700Bold',fontSize:9,letterSpacing:1.4},sheetTitle:{color:TEXT,fontFamily:'Inter_800ExtraBold',fontSize:23,marginTop:4},sheetStatus:{backgroundColor:CARD2,borderRadius:10,paddingHorizontal:9,paddingVertical:7},detailGrid:{backgroundColor:BG,borderRadius:16,padding:14,gap:14,marginBottom:14},detailLabel:{color:MUTED,fontFamily:'Inter_700Bold',fontSize:9,letterSpacing:1.2,marginBottom:5},detailValue:{color:TEXT,fontFamily:'Inter_700Bold',fontSize:14},paymentCard:{backgroundColor:BG,borderRadius:16,padding:15,marginBottom:14},paymentReference:{color:TEXT,fontFamily:'Inter_800ExtraBold',fontSize:17,marginBottom:5},paymentMeta:{color:AMBER,fontFamily:'Inter_700Bold',fontSize:11},paymentNote:{color:MUTED,fontFamily:'Inter_400Regular',fontSize:11,lineHeight:18,marginTop:7},disputeNotice:{flexDirection:'row',alignItems:'center',gap:10,backgroundColor:'#242A17',borderRadius:14,padding:13,marginBottom:14},disputeNoticeText:{flex:1},disputeNoticeTitle:{color:LIME,fontFamily:'Inter_700Bold',fontSize:12,marginBottom:2},inputLabel:{color:MUTED,fontFamily:'Inter_700Bold',fontSize:9,letterSpacing:1.1,marginBottom:6,marginTop:7},input:{backgroundColor:BG,borderRadius:13,borderWidth:1,borderColor:'#252B35',color:TEXT,fontFamily:'Inter_400Regular',fontSize:12,paddingHorizontal:13,paddingVertical:11,marginBottom:10},multiline:{height:92},multilineSmall:{height:82},actions:{gap:9,marginTop:5},primary:{minHeight:50,borderRadius:15,backgroundColor:LIME,alignItems:'center',justifyContent:'center',flexDirection:'row',gap:8,paddingHorizontal:16},primaryText:{color:BG,fontFamily:'Inter_800ExtraBold',fontSize:13},danger:{minHeight:50,borderRadius:15,backgroundColor:'#24191D',alignItems:'center',justifyContent:'center',flexDirection:'row',gap:7,paddingHorizontal:16},dangerText:{color:RED,fontFamily:'Inter_800ExtraBold',fontSize:13},cancel:{color:MUTED,fontFamily:'Inter_700Bold',fontSize:13,textAlign:'center',paddingVertical:13},disputeOverlay:{flex:1,backgroundColor:'rgba(0,0,0,.72)',justifyContent:'center',padding:20},disputeCard:{backgroundColor:CARD,borderRadius:24,padding:20,borderWidth:1,borderColor:'#252B35'},disputeHeader:{flexDirection:'row',alignItems:'center',gap:11,marginBottom:16},disputeIcon:{width:44,height:44,borderRadius:14,backgroundColor:'#24191D',alignItems:'center',justifyContent:'center'},disputeTitle:{color:TEXT,fontFamily:'Inter_800ExtraBold',fontSize:20},disputeSubtitle:{color:MUTED,fontFamily:'Inter_400Regular',fontSize:11,lineHeight:17,marginTop:2},disputeError:{flexDirection:'row',gap:7,alignItems:'flex-start',backgroundColor:'#24191D',borderRadius:12,padding:11,marginBottom:12},disputeErrorText:{flex:1,color:'#FF9EA8',fontFamily:'Inter_400Regular',fontSize:11,lineHeight:17}
});
