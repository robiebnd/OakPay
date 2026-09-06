import { StyleSheet, Text, View } from 'react-native';

export default function RegisterScreen() {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>Create your OakPay account</Text>
      <Text style={styles.text}>Registration will connect to the Auth service in the next step.</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, justifyContent: 'center', padding: 24, backgroundColor: '#F7F8FA' },
  title: { fontSize: 28, fontWeight: '700', color: '#17221D' },
  text: { marginTop: 12, color: '#68737D', lineHeight: 21 }
});
