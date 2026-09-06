import { StyleSheet, Text, View } from 'react-native';

export default function TradesScreen() {
  return <View style={styles.container}><Text style={styles.title}>My Trades</Text><View style={styles.empty}><Text style={styles.emptyTitle}>No trades yet</Text><Text style={styles.text}>Your active and completed P2P trades will appear here.</Text></View></View>;
}
const styles = StyleSheet.create({ container: { flex: 1, padding: 20, backgroundColor: '#F7F8FA' }, title: { fontSize: 28, fontWeight: '800', color: '#17221D', marginBottom: 20 }, empty: { backgroundColor: '#FFF', borderRadius: 16, padding: 20, borderWidth: 1, borderColor: '#E3E7E5' }, emptyTitle: { fontSize: 17, fontWeight: '700', color: '#17221D' }, text: { color: '#68737D', marginTop: 7, lineHeight: 20 } });
