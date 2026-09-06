import { Ionicons } from '@expo/vector-icons';
import { StyleSheet, Text, View } from 'react-native';

const BG = '#0D1017';
const CARD = '#171B24';
const TEXT = '#F4F6F8';
const MUTED = '#8D95A3';
const LIME = '#D8FF3E';

export default function OrdersScreen() {
  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <View>
          <Text style={styles.eyebrow}>ACCOUNT</Text>
          <Text style={styles.title}>P2P Orders</Text>
        </View>
        <View style={styles.iconButton}><Ionicons name="notifications-outline" size={21} color={TEXT} /></View>
      </View>

      <View style={styles.tabs}>
        <View style={styles.activeTab}><Text style={styles.activeText}>Active</Text></View>
        <View style={styles.tab}><Text style={styles.tabText}>History</Text></View>
      </View>

      <View style={styles.emptyCard}>
        <View style={styles.emptyIcon}><Ionicons name="receipt-outline" size={26} color={LIME} /></View>
        <Text style={styles.emptyTitle}>No active orders</Text>
        <Text style={styles.emptyText}>Your P2P purchase and sale orders will appear here. Payment and verification steps will be handled from each order.</Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: BG, padding: 20 },
  header: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginTop: 8, marginBottom: 24 },
  eyebrow: { color: LIME, fontSize: 11, fontWeight: '800', letterSpacing: 1.6 },
  title: { color: TEXT, fontSize: 28, fontWeight: '800', marginTop: 5 },
  iconButton: { width: 44, height: 44, borderRadius: 15, backgroundColor: CARD, alignItems: 'center', justifyContent: 'center' },
  tabs: { flexDirection: 'row', backgroundColor: CARD, borderRadius: 14, padding: 4, marginBottom: 18 },
  activeTab: { flex: 1, backgroundColor: LIME, borderRadius: 11, paddingVertical: 12, alignItems: 'center' },
  activeText: { color: '#0D1017', fontWeight: '800' },
  tab: { flex: 1, paddingVertical: 12, alignItems: 'center' },
  tabText: { color: MUTED, fontWeight: '700' },
  emptyCard: { backgroundColor: CARD, borderRadius: 22, padding: 26, alignItems: 'center', marginTop: 8 },
  emptyIcon: { width: 58, height: 58, borderRadius: 18, backgroundColor: '#242A17', alignItems: 'center', justifyContent: 'center', marginBottom: 18 },
  emptyTitle: { color: TEXT, fontSize: 18, fontWeight: '800' },
  emptyText: { color: MUTED, textAlign: 'center', lineHeight: 21, marginTop: 9 },
});
