import { Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { router } from 'expo-router';

export default function HomeScreen() {
  return (
    <ScrollView contentContainerStyle={styles.container}>
      <View style={styles.header}>
        <View>
          <Text style={styles.muted}>Welcome back</Text>
          <Text style={styles.name}>OakPay</Text>
        </View>
        <Text style={styles.bell}>🔔</Text>
      </View>

      <View style={styles.balanceCard}>
        <Text style={styles.cardLabel}>Total balance</Text>
        <Text style={styles.balance}>$0.00</Text>
        <Text style={styles.cardHint}>Your wallet balance will appear here.</Text>
      </View>

      <View style={styles.actions}>
        <Pressable style={styles.action} onPress={() => router.push('/wallet')}><Text style={styles.actionIcon}>＋</Text><Text style={styles.actionText}>Deposit</Text></Pressable>
        <Pressable style={styles.action} onPress={() => router.push('/wallet')}><Text style={styles.actionIcon}>↑</Text><Text style={styles.actionText}>Withdraw</Text></Pressable>
      </View>

      <View style={styles.sectionHeader}><Text style={styles.sectionTitle}>P2P Market</Text><Pressable onPress={() => router.push('/(tabs)/market')}><Text style={styles.link}>View all</Text></Pressable></View>
      <Pressable style={styles.marketCard} onPress={() => router.push('/(tabs)/market')}>
        <View><Text style={styles.marketTitle}>Buy USDT</Text><Text style={styles.marketText}>Find trusted P2P offers in ZWG</Text></View><Text style={styles.arrow}>›</Text>
      </Pressable>

      <Text style={styles.sectionTitle}>Recent activity</Text>
      <View style={styles.empty}><Text style={styles.emptyTitle}>No transactions yet</Text><Text style={styles.emptyText}>Your wallet and trade activity will appear here.</Text></View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { padding: 20, paddingBottom: 32, backgroundColor: '#F7F8FA' },
  header: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 22 },
  muted: { color: '#68737D', fontSize: 14 },
  name: { fontSize: 25, fontWeight: '800', color: '#17221D', marginTop: 3 },
  bell: { fontSize: 22 },
  balanceCard: { backgroundColor: '#123B2A', borderRadius: 20, padding: 22, marginBottom: 14 },
  cardLabel: { color: '#D9E7E0', fontSize: 14 },
  balance: { color: '#FFF', fontSize: 36, fontWeight: '800', marginTop: 8 },
  cardHint: { color: '#D9E7E0', marginTop: 8 },
  actions: { flexDirection: 'row', gap: 12, marginBottom: 28 },
  action: { flex: 1, backgroundColor: '#FFF', borderRadius: 15, padding: 15, alignItems: 'center', borderWidth: 1, borderColor: '#E3E7E5' },
  actionIcon: { fontSize: 23, color: '#123B2A', fontWeight: '700' },
  actionText: { marginTop: 5, color: '#17221D', fontWeight: '600' },
  sectionHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 10 },
  sectionTitle: { fontSize: 19, fontWeight: '700', color: '#17221D', marginBottom: 10 },
  link: { color: '#123B2A', fontWeight: '700' },
  marketCard: { backgroundColor: '#FFF', borderRadius: 16, padding: 18, marginBottom: 28, flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', borderWidth: 1, borderColor: '#E3E7E5' },
  marketTitle: { fontSize: 16, fontWeight: '700', color: '#17221D' },
  marketText: { color: '#68737D', marginTop: 5 },
  arrow: { fontSize: 30, color: '#123B2A' },
  empty: { backgroundColor: '#FFF', borderRadius: 16, padding: 20, borderWidth: 1, borderColor: '#E3E7E5' },
  emptyTitle: { fontWeight: '700', color: '#17221D' },
  emptyText: { color: '#68737D', marginTop: 6, lineHeight: 20 }
});
