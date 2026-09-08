import { Ionicons } from '@expo/vector-icons';
import * as Clipboard from 'expo-clipboard';
import QRCode from 'react-native-qrcode-svg';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { ActivityIndicator, Alert, Modal, Pressable, ScrollView, StyleSheet, Text, TextInput, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useAuth } from '../../context/AuthContext';
import { DepositAddress, LedgerTransaction, Wallet, walletApi } from '../../lib/api';

const BG = '#F5F5F3';
const CARD = '#FFFFFF';
const TEXT = '#111713';
const MUTED = '#7C847E';
const GREEN = '#123B2A';
const LIME = '#D8FF3E';
const RED = '#D84B5B';
const BORDER = '#E3E6E2';
const ZWG_PER_USDT = 26.56;

const money = (n: number) => Number(n || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
const cryptoMoney = (n: number) => Number(n || 0).toLocaleString(undefined, { minimumFractionDigits: 8, maximumFractionDigits: 8 });
const symbol = (currency: string) => ({ USDT: '₮', BTC: '₿', ETH: 'Ξ', BNB: 'B', SOL: 'S', USDC: '$', XRP: 'X', ADA: 'A', DOGE: 'Ð', TRX: 'T', LTC: 'Ł', AVAX: 'A' }[currency.toUpperCase()] ?? currency.slice(0, 1));
const name = (currency: string) => ({ USDT: 'Tether', BTC: 'Bitcoin', ETH: 'Ethereum', BNB: 'BNB', SOL: 'Solana', USDC: 'USD Coin', XRP: 'XRP', ADA: 'Cardano', DOGE: 'Dogecoin', TRX: 'TRON', LTC: 'Litecoin', AVAX: 'Avalanche' }[currency.toUpperCase()] ?? currency);
const isFiat = (currency: string) => ['USD', 'ZWG'].includes(currency.toUpperCase());

function usdValue(wallet: Wallet) {
  const c = wallet.currency.toUpperCase();
  if (c === 'USD' || c === 'USDT') return wallet.totalBalance;
  if (c === 'ZWG') return wallet.totalBalance / ZWG_PER_USDT;
  return null;
}

export default function WalletScreen() {
  const { accessToken } = useAuth();
  const [wallets, setWallets] = useState<Wallet[]>([]);
  const [transactions, setTransactions] = useState<LedgerTransaction[]>([]);
  const [addresses, setAddresses] = useState<DepositAddress[]>([]);
  const [loading, setLoading] = useState(true);
  const [receiveLoading, setReceiveLoading] = useState(false);
  const [error, setError] = useState('');
  const [modal, setModal] = useState<'receive' | 'withdraw' | null>(null);
  const [currency, setCurrency] = useState('USDT');
  const [network, setNetwork] = useState('');
  const [amount, setAmount] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const load = useCallback(async () => {
    if (!accessToken) return;
    try {
      setLoading(true);
      setError('');
      const [w, t, a] = await Promise.all([
        walletApi.wallets(accessToken),
        walletApi.transactions(accessToken),
        walletApi.depositAddresses(accessToken)
      ]);
      setWallets(w);
      setTransactions(t);
      setAddresses(a);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Unable to load wallet.');
    } finally {
      setLoading(false);
    }
  }, [accessToken]);

  useEffect(() => { load(); }, [load]);

  const orderedWallets = useMemo(() => [...wallets].sort((a, b) => {
    const rank = (c: string) => ({ USDT: 0, USDC: 1, BTC: 2, ETH: 3, BNB: 4, SOL: 5, ZWG: 90, USD: 99 }[c.toUpperCase()] ?? 50);
    return rank(a.currency) - rank(b.currency);
  }), [wallets]);

  const cryptoWallets = useMemo(() => orderedWallets.filter(w => !isFiat(w.currency)), [orderedWallets]);
  const portfolio = useMemo(() => wallets.reduce((sum, w) => sum + (usdValue(w) ?? 0), 0), [wallets]);
  const zwg = useMemo(() => wallets.find(w => w.currency.toUpperCase() === 'ZWG'), [wallets]);
  const selectedAddresses = useMemo(() => addresses.filter(a => a.currency.toUpperCase() === currency.toUpperCase()), [addresses, currency]);
  const selectedAddress = selectedAddresses.find(a => a.network.toUpperCase() === network.toUpperCase()) ?? selectedAddresses[0];

  async function openReceive(asset: string) {
    setCurrency(asset);
    setNetwork('');
    setModal('receive');
    if (!accessToken) return;
    try {
      setReceiveLoading(true);
      setError('');
      const a = await walletApi.depositAddresses(accessToken);
      setAddresses(a);
      const first = a.find(x => x.currency.toUpperCase() === asset.toUpperCase());
      if (first) setNetwork(first.network);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Unable to load deposit address.');
    } finally {
      setReceiveLoading(false);
    }
  }

  async function copyAddress() {
    if (!selectedAddress?.address) return;
    await Clipboard.setStringAsync(selectedAddress.address);
    Alert.alert('Address copied', 'The deposit address has been copied to your clipboard.');
  }

  async function submitWithdraw() {
    const value = Number(amount);
    if (!Number.isFinite(value) || value <= 0) { setError('Enter a valid amount.'); return; }
    if (!accessToken) return;
    try {
      setSubmitting(true);
      setError('');
      await walletApi.withdraw(accessToken, currency, value);
      setAmount('');
      setModal(null);
      await load();
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Withdrawal failed.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <SafeAreaView style={styles.safe} edges={['top']}>
      <View style={styles.screen}>
        <ScrollView contentContainerStyle={styles.content} showsVerticalScrollIndicator={false}>
          <View style={styles.container}>
            <View style={styles.header}>
              <View><Text style={styles.eyebrow}>OAKPAY WALLET</Text><Text style={styles.title}>Wallet</Text></View>
              <View style={styles.headerIcon}><Ionicons name="shield-checkmark-outline" size={20} color={GREEN} /></View>
            </View>

            <View style={styles.hero}>
              <View style={styles.heroTop}><Text style={styles.label}>TOTAL PORTFOLIO VALUE</Text><View style={styles.currencyBadge}><Text style={styles.currencyBadgeText}>USD</Text></View></View>
              <Text style={styles.balance}>$ {money(portfolio)}</Text>
              <Text style={styles.converted}>≈ {money(zwg?.totalBalance ?? portfolio * ZWG_PER_USDT)} ZWG</Text>
              <View style={styles.heroActions}>
                <Pressable style={styles.receiveButton} onPress={() => openReceive(cryptoWallets[0]?.currency ?? 'USDT')}><Ionicons name="arrow-down" size={18} color="#102016" /><Text style={styles.receiveText}>Receive</Text></Pressable>
                <Pressable style={styles.withdraw} onPress={() => { setCurrency('USDT'); setModal('withdraw'); }}><Ionicons name="arrow-up" size={18} color={GREEN} /><Text style={styles.withdrawText}>Withdraw</Text></Pressable>
              </View>
            </View>

            {error ? <View style={styles.error}><Ionicons name="alert-circle-outline" size={17} color={RED} /><Text style={styles.errorText}>{error}</Text></View> : null}

            <View style={styles.sectionRow}><Text style={styles.section}>Crypto</Text><Text style={styles.count}>{cryptoWallets.length} ASSETS</Text></View>
            {loading ? <View style={styles.loading}><ActivityIndicator color={GREEN} /><Text style={styles.muted}>Loading balances…</Text></View> : cryptoWallets.length === 0 ? <View style={styles.empty}><View style={styles.emptyIcon}><Ionicons name="wallet-outline" size={22} color={GREEN} /></View><Text style={styles.emptyTitle}>No crypto wallets yet</Text><Text style={styles.muted}>Your crypto wallets will appear here.</Text></View> : cryptoWallets.map(w => {
              const value = usdValue(w);
              return <Pressable key={w.id} style={styles.assetCard} onPress={() => openReceive(w.currency)}><View style={styles.assetLeft}><View style={styles.assetIcon}><Text style={styles.symbol}>{symbol(w.currency)}</Text></View><View><Text style={styles.assetName}>{w.currency.toUpperCase()}</Text><Text style={styles.assetSub}>{name(w.currency)}</Text></View></View><View style={styles.assetRight}><Text style={styles.assetAmount}>{cryptoMoney(w.totalBalance)}</Text><Text style={styles.assetUnit}>{w.currency.toUpperCase()}</Text>{value !== null ? <Text style={styles.assetUsd}>≈ ${money(value)}</Text> : <Text style={styles.pendingPrice}>Market price pending</Text>}{w.lockedBalance > 0 ? <Text style={styles.locked}>{cryptoMoney(w.lockedBalance)} locked</Text> : null}</View></Pressable>;
            })}

            <View style={styles.sectionRowMore}><Text style={styles.section}>Recent activity</Text><Text style={styles.count}>{transactions.length} TRANSACTIONS</Text></View>
            {transactions.length === 0 && !loading ? <View style={styles.empty}><View style={styles.emptyIcon}><Ionicons name="receipt-outline" size={22} color={GREEN} /></View><Text style={styles.emptyTitle}>No wallet activity</Text><Text style={styles.muted}>Deposits, withdrawals and P2P wallet movements will appear here.</Text></View> : transactions.slice(0, 8).map(t => <View style={styles.tx} key={t.id}><View style={styles.txIcon}><Ionicons name={t.direction === 'CREDIT' ? 'arrow-down' : 'arrow-up'} size={17} color={t.direction === 'CREDIT' ? GREEN : RED} /></View><View style={styles.txCopy}><Text style={styles.txTitle}>{t.transactionType.replaceAll('_', ' ')}</Text><Text style={styles.txSub}>{new Date(t.createdAt).toLocaleString()}</Text></View><View style={styles.txRight}><Text style={[styles.txAmount, { color: t.direction === 'CREDIT' ? GREEN : RED }]}>{t.direction === 'CREDIT' ? '+' : '-'}{isFiat(t.currency) ? money(t.amount) : cryptoMoney(t.amount)}</Text><Text style={styles.txSub}>{t.currency}</Text></View></View>)}
          </View>
        </ScrollView>

        <Modal visible={modal === 'receive'} transparent animationType="slide" onRequestClose={() => setModal(null)}>
          <View style={styles.overlay}><View style={styles.sheet}><View style={styles.sheetHandle} /><Text style={styles.sheetTitle}>Receive Crypto</Text><Text style={styles.sheetText}>Use your assigned OakPay address to receive {currency}.</Text>
            <Text style={styles.fieldLabel}>ASSET</Text>
            <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={styles.pills}>{cryptoWallets.map(w => <Pressable key={w.currency} style={[styles.currencyPill, currency === w.currency && styles.currencyPillActive]} onPress={() => { setCurrency(w.currency); setNetwork(''); const first = addresses.find(a => a.currency.toUpperCase() === w.currency.toUpperCase()); if (first) setNetwork(first.network); }}><Text style={[styles.currencyText, currency === w.currency && styles.currencyTextActive]}>{w.currency}</Text></Pressable>)}</ScrollView>
            <Text style={styles.fieldLabel}>NETWORK</Text>
            {receiveLoading ? <ActivityIndicator color={GREEN} style={styles.networkLoading} /> : selectedAddresses.length === 0 ? <View style={styles.noAddress}><Text style={styles.noAddressText}>No deposit network has been assigned for {currency} yet.</Text></View> : <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={styles.pills}>{selectedAddresses.map(a => <Pressable key={a.id} style={[styles.currencyPill, network === a.network && styles.currencyPillActive]} onPress={() => setNetwork(a.network)}><Text style={[styles.currencyText, network === a.network && styles.currencyTextActive]}>{a.network}</Text></Pressable>)}</ScrollView>}
            {selectedAddress ? <View style={styles.addressBox}><View style={styles.qrWrap}><QRCode value={selectedAddress.address} size={150} backgroundColor="#FFFFFF" color="#111713" /></View><Text style={styles.addressLabel}>YOUR {currency} DEPOSIT ADDRESS</Text><Text style={styles.networkSelected}>{selectedAddress.network}</Text><Text selectable style={styles.addressValue}>{selectedAddress.address}</Text>{selectedAddress.memoTag ? <Text style={styles.memo}>Memo / Tag: {selectedAddress.memoTag}</Text> : null}<Pressable style={styles.copyButton} onPress={copyAddress}><Ionicons name="copy-outline" size={17} color="#102016" /><Text style={styles.copyText}>Copy Address</Text></Pressable></View> : null}
            <Text style={styles.warning}>⚠ Send only {currency} using the selected network to this address.</Text><Pressable onPress={() => setModal(null)}><Text style={styles.cancel}>Close</Text></Pressable>
          </View></View>
        </Modal>

        <Modal visible={modal === 'withdraw'} transparent animationType="slide" onRequestClose={() => !submitting && setModal(null)}><View style={styles.overlay}><View style={styles.sheet}><View style={styles.sheetHandle} /><Text style={styles.sheetTitle}>Withdraw funds</Text><Text style={styles.sheetText}>Withdraw {currency} from your available balance.</Text><TextInput value={amount} onChangeText={setAmount} keyboardType="decimal-pad" placeholder="0.00000000" placeholderTextColor={MUTED} style={styles.amountInput} autoFocus /><Pressable style={[styles.confirm, submitting && { opacity: 0.6 }]} disabled={submitting} onPress={submitWithdraw}>{submitting ? <ActivityIndicator color="#102016" /> : <Text style={styles.confirmText}>Withdraw {currency}</Text>}</Pressable><Pressable onPress={() => setModal(null)} disabled={submitting}><Text style={styles.cancel}>Close</Text></Pressable></View></View></Modal>
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: BG }, screen: { flex: 1, backgroundColor: BG }, content: { paddingBottom: 36 }, container: { paddingHorizontal: 20, paddingTop: 8 },
  header: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 18 }, headerIcon: { width: 44, height: 44, borderRadius: 15, backgroundColor: CARD, borderWidth: 1, borderColor: BORDER, alignItems: 'center', justifyContent: 'center' },
  eyebrow: { fontFamily: 'Inter_700Bold', fontSize: 10, letterSpacing: 1.8, color: GREEN }, title: { fontFamily: 'Inter_800ExtraBold', fontSize: 30, lineHeight: 38, color: TEXT, marginTop: 4 },
  hero: { backgroundColor: CARD, borderRadius: 23, padding: 20, borderWidth: 1, borderColor: BORDER, marginBottom: 23 }, heroTop: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' }, label: { fontFamily: 'Inter_700Bold', fontSize: 10, letterSpacing: 1.3, color: MUTED }, currencyBadge: { paddingHorizontal: 9, paddingVertical: 5, borderRadius: 8, backgroundColor: '#EEF4DF' }, currencyBadgeText: { fontFamily: 'Inter_800ExtraBold', fontSize: 9, color: GREEN, letterSpacing: .7 }, balance: { fontFamily: 'Inter_800ExtraBold', fontSize: 34, lineHeight: 43, marginTop: 9, color: TEXT }, converted: { fontFamily: 'Inter_400Regular', fontSize: 13, marginTop: 3, color: MUTED }, heroActions: { flexDirection: 'row', gap: 10, marginTop: 17 }, receiveButton: { flex: 1, height: 48, borderRadius: 14, backgroundColor: LIME, flexDirection: 'row', gap: 7, alignItems: 'center', justifyContent: 'center' }, receiveText: { color: '#102016', fontFamily: 'Inter_800ExtraBold', fontSize: 13 }, withdraw: { flex: 1, height: 48, borderRadius: 14, flexDirection: 'row', gap: 7, alignItems: 'center', justifyContent: 'center', borderWidth: 1, borderColor: BORDER, backgroundColor: '#F1F3F0' }, withdrawText: { fontFamily: 'Inter_700Bold', fontSize: 13, color: GREEN },
  error: { flexDirection: 'row', alignItems: 'center', gap: 7, backgroundColor: '#FFF0F2', borderRadius: 13, padding: 12, marginBottom: 16 }, errorText: { flex: 1, color: RED, fontFamily: 'Inter_400Regular', fontSize: 12 }, sectionRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 11 }, sectionRowMore: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginTop: 24, marginBottom: 11 }, section: { fontFamily: 'Inter_800ExtraBold', fontSize: 18, color: TEXT }, count: { fontFamily: 'Inter_700Bold', fontSize: 9, letterSpacing: 1.1, color: MUTED }, loading: { backgroundColor: CARD, borderRadius: 17, padding: 25, alignItems: 'center', gap: 10, borderWidth: 1, borderColor: BORDER }, muted: { fontFamily: 'Inter_400Regular', fontSize: 12, color: MUTED, textAlign: 'center', lineHeight: 18 }, empty: { backgroundColor: CARD, borderRadius: 17, padding: 25, alignItems: 'center', borderWidth: 1, borderColor: BORDER }, emptyIcon: { width: 44, height: 44, borderRadius: 14, backgroundColor: '#EEF4DF', alignItems: 'center', justifyContent: 'center', marginBottom: 10 }, emptyTitle: { fontFamily: 'Inter_800ExtraBold', fontSize: 14, color: TEXT, marginBottom: 4 },
  assetCard: { backgroundColor: CARD, borderRadius: 17, padding: 16, borderWidth: 1, borderColor: BORDER, marginBottom: 9, flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' }, assetLeft: { flexDirection: 'row', alignItems: 'center', flex: 1 }, assetIcon: { width: 43, height: 43, borderRadius: 14, backgroundColor: '#EEF4DF', alignItems: 'center', justifyContent: 'center', marginRight: 11 }, symbol: { fontFamily: 'Inter_800ExtraBold', fontSize: 18, color: GREEN }, assetName: { fontFamily: 'Inter_800ExtraBold', fontSize: 14, color: TEXT }, assetSub: { fontFamily: 'Inter_400Regular', fontSize: 11, color: MUTED, marginTop: 2 }, assetRight: { alignItems: 'flex-end', maxWidth: '53%' }, assetAmount: { fontFamily: 'Inter_700Bold', fontSize: 13, color: TEXT }, assetUnit: { fontFamily: 'Inter_400Regular', fontSize: 10, color: MUTED, marginTop: 1 }, assetUsd: { fontFamily: 'Inter_700Bold', fontSize: 11, color: GREEN, marginTop: 4 }, pendingPrice: { fontFamily: 'Inter_400Regular', fontSize: 10, color: MUTED, marginTop: 4 }, locked: { fontFamily: 'Inter_400Regular', fontSize: 9, color: MUTED, marginTop: 3 },
  tx: { backgroundColor: CARD, borderRadius: 15, padding: 13, borderWidth: 1, borderColor: BORDER, marginBottom: 8, flexDirection: 'row', alignItems: 'center' }, txIcon: { width: 36, height: 36, borderRadius: 12, backgroundColor: '#F1F3F0', alignItems: 'center', justifyContent: 'center', marginRight: 10 }, txCopy: { flex: 1 }, txTitle: { fontFamily: 'Inter_700Bold', fontSize: 12, color: TEXT, textTransform: 'capitalize' }, txSub: { fontFamily: 'Inter_400Regular', fontSize: 9, color: MUTED, marginTop: 3 }, txRight: { alignItems: 'flex-end' }, txAmount: { fontFamily: 'Inter_800ExtraBold', fontSize: 12 },
  overlay: { flex: 1, backgroundColor: 'rgba(0,0,0,.48)', justifyContent: 'flex-end' }, sheet: { backgroundColor: CARD, borderTopLeftRadius: 28, borderTopRightRadius: 28, paddingHorizontal: 20, paddingTop: 10, paddingBottom: 30, maxHeight: '92%' }, sheetHandle: { width: 42, height: 4, borderRadius: 2, backgroundColor: '#D6D9D5', alignSelf: 'center', marginBottom: 18 }, sheetTitle: { fontFamily: 'Inter_800ExtraBold', fontSize: 22, color: TEXT }, sheetText: { fontFamily: 'Inter_400Regular', fontSize: 12, lineHeight: 18, color: MUTED, marginTop: 5, marginBottom: 18 }, fieldLabel: { fontFamily: 'Inter_700Bold', fontSize: 9, letterSpacing: 1.2, color: MUTED, marginBottom: 8, marginTop: 5 }, pills: { gap: 8, paddingBottom: 4 }, currencyPill: { paddingHorizontal: 14, paddingVertical: 9, borderRadius: 12, backgroundColor: '#F0F2EF', borderWidth: 1, borderColor: BORDER }, currencyPillActive: { backgroundColor: GREEN, borderColor: GREEN }, currencyText: { fontFamily: 'Inter_700Bold', fontSize: 11, color: MUTED }, currencyTextActive: { color: '#FFFFFF' }, networkLoading: { paddingVertical: 12 }, noAddress: { padding: 14, backgroundColor: '#FFF7DF', borderRadius: 12, marginBottom: 12 }, noAddressText: { fontFamily: 'Inter_400Regular', fontSize: 11, lineHeight: 17, color: '#715A18' }, addressBox: { backgroundColor: '#F5F5F3', borderRadius: 18, borderWidth: 1, borderColor: BORDER, padding: 16, alignItems: 'center', marginTop: 14 }, qrWrap: { backgroundColor: '#FFFFFF', padding: 12, borderRadius: 14, marginBottom: 14 }, addressLabel: { fontFamily: 'Inter_700Bold', fontSize: 9, letterSpacing: 1, color: MUTED, textAlign: 'center' }, networkSelected: { fontFamily: 'Inter_800ExtraBold', fontSize: 11, color: GREEN, marginTop: 5 }, addressValue: { fontFamily: 'Inter_700Bold', fontSize: 12, color: TEXT, textAlign: 'center', marginTop: 7, lineHeight: 18 }, memo: { fontFamily: 'Inter_400Regular', fontSize: 11, color: MUTED, marginTop: 5 }, copyButton: { marginTop: 14, height: 44, paddingHorizontal: 18, borderRadius: 13, backgroundColor: LIME, flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 7 }, copyText: { fontFamily: 'Inter_800ExtraBold', fontSize: 12, color: '#102016' }, warning: { fontFamily: 'Inter_400Regular', fontSize: 10, lineHeight: 15, color: '#8A6A16', textAlign: 'center', marginTop: 12 }, cancel: { fontFamily: 'Inter_700Bold', fontSize: 13, color: MUTED, textAlign: 'center', paddingVertical: 14 }, amountInput: { height: 52, borderRadius: 14, borderWidth: 1, borderColor: BORDER, backgroundColor: BG, paddingHorizontal: 15, color: TEXT, fontFamily: 'Inter_700Bold', fontSize: 16, marginBottom: 12 }, confirm: { height: 50, borderRadius: 14, backgroundColor: LIME, alignItems: 'center', justifyContent: 'center' }, confirmText: { fontFamily: 'Inter_800ExtraBold', fontSize: 13, color: '#102016' }
});
