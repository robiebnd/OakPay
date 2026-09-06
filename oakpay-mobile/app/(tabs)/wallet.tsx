import { StyleSheet, Text, View } from 'react-native';

export default function WalletScreen() {
  return <View style={styles.container}><Text style={styles.title}>Wallet</Text><View style={styles.card}><Text style={styles.label}>USDT</Text><Text style={styles.balance}>0.00</Text><Text style={styles.available}>Available balance</Text></View><View style={styles.card}><Text style={styles.label}>ZWG</Text><Text style={styles.balance}>0.00</Text><Text style={styles.available}>Available balance</Text></View></View>;
}
const styles = StyleSheet.create({ container: { flex: 1, padding: 20, backgroundColor: '#F7F8FA' }, title: { fontSize: 28, fontWeight: '800', color: '#17221D', marginBottom: 20 }, card: { backgroundColor: '#FFF', borderRadius: 16, padding: 20, marginBottom: 12, borderWidth: 1, borderColor: '#E3E7E5' }, label: { color: '#68737D', fontWeight: '600' }, balance: { fontSize: 30, fontWeight: '800', color: '#17221D', marginTop: 6 }, available: { color: '#68737D', marginTop: 4 } });
