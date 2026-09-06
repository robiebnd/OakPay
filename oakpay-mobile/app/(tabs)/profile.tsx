import { StyleSheet, Text, View } from 'react-native';

export default function ProfileScreen() {
  return <View style={styles.container}><Text style={styles.title}>Profile</Text><View style={styles.card}><Text style={styles.name}>OakPay User</Text><Text style={styles.text}>Account and security settings will be connected to Auth.</Text></View></View>;
}
const styles = StyleSheet.create({ container: { flex: 1, padding: 20, backgroundColor: '#F7F8FA' }, title: { fontSize: 28, fontWeight: '800', color: '#17221D', marginBottom: 20 }, card: { backgroundColor: '#FFF', borderRadius: 16, padding: 20, borderWidth: 1, borderColor: '#E3E7E5' }, name: { fontSize: 18, fontWeight: '700', color: '#17221D' }, text: { color: '#68737D', marginTop: 7, lineHeight: 20 } });
