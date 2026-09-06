import { Pressable, StyleSheet, Text, View } from 'react-native';

export default function MarketScreen() {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>P2P Market</Text>
      <Text style={styles.subtitle}>Buy and sell USDT using ZWG.</Text>
      <View style={styles.switcher}><Pressable style={styles.active}><Text style={styles.activeText}>Buy USDT</Text></Pressable><Pressable style={styles.inactive}><Text>Sell USDT</Text></Pressable></View>
      <View style={styles.filters}><Text style={styles.filter}>USDT</Text><Text style={styles.filter}>ZWG</Text><Text style={styles.filter}>All payments</Text></View>
      <View style={styles.empty}><Text style={styles.emptyTitle}>Marketplace ready</Text><Text style={styles.emptyText}>Live advertisements will load from the OakPay P2P API here.</Text></View>
    </View>
  );
}
const styles = StyleSheet.create({
  container: { flex: 1, padding: 20, backgroundColor: '#F7F8FA' },
  title: { fontSize: 28, fontWeight: '800', color: '#17221D' },
  subtitle: { color: '#68737D', marginTop: 6, marginBottom: 20 },
  switcher: { flexDirection: 'row', backgroundColor: '#E9EDEA', borderRadius: 13, padding: 4 },
  active: { flex: 1, backgroundColor: '#FFF', borderRadius: 10, padding: 12, alignItems: 'center' },
  activeText: { fontWeight: '700', color: '#123B2A' },
  inactive: { flex: 1, padding: 12, alignItems: 'center' },
  filters: { flexDirection: 'row', gap: 8, marginVertical: 16 },
  filter: { backgroundColor: '#FFF', borderRadius: 10, paddingHorizontal: 13, paddingVertical: 9, color: '#17221D' },
  empty: { backgroundColor: '#FFF', borderRadius: 16, padding: 20, borderWidth: 1, borderColor: '#E3E7E5' },
  emptyTitle: { fontWeight: '700', fontSize: 16 },
  emptyText: { color: '#68737D', marginTop: 7, lineHeight: 20 }
});
