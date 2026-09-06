import { useState } from 'react';
import { Pressable, StyleSheet, Text, TextInput, View } from 'react-native';
import { router } from 'expo-router';

export default function LoginScreen() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  function handleLogin() {
    // API authentication is wired in the next mobile step.
    if (email.trim() && password) router.replace('/(tabs)');
  }

  return (
    <View style={styles.container}>
      <View style={styles.brand}>
        <Text style={styles.logo}>OakPay</Text>
        <Text style={styles.subtitle}>Simple. Secure. P2P.</Text>
      </View>

      <View style={styles.form}>
        <Text style={styles.title}>Welcome back</Text>
        <Text style={styles.description}>Sign in to manage your wallet and P2P trades.</Text>

        <TextInput
          value={email}
          onChangeText={setEmail}
          placeholder="Email address"
          autoCapitalize="none"
          keyboardType="email-address"
          style={styles.input}
        />
        <TextInput
          value={password}
          onChangeText={setPassword}
          placeholder="Password"
          secureTextEntry
          style={styles.input}
        />

        <Pressable style={styles.button} onPress={handleLogin}>
          <Text style={styles.buttonText}>Sign in</Text>
        </Pressable>

        <Pressable onPress={() => router.push('/(auth)/register')}>
          <Text style={styles.register}>Don't have an account? Create one</Text>
        </Pressable>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#F7F8FA', padding: 24, justifyContent: 'center' },
  brand: { marginBottom: 48 },
  logo: { fontSize: 38, fontWeight: '800', color: '#123B2A' },
  subtitle: { marginTop: 6, fontSize: 15, color: '#68737D' },
  form: { width: '100%' },
  title: { fontSize: 28, fontWeight: '700', color: '#17221D' },
  description: { marginTop: 8, marginBottom: 24, color: '#68737D', lineHeight: 21 },
  input: { height: 54, backgroundColor: '#FFF', borderRadius: 14, paddingHorizontal: 16, marginBottom: 12, borderWidth: 1, borderColor: '#E3E7E5', fontSize: 16 },
  button: { height: 54, borderRadius: 14, backgroundColor: '#123B2A', alignItems: 'center', justifyContent: 'center', marginTop: 8 },
  buttonText: { color: '#FFF', fontSize: 16, fontWeight: '700' },
  register: { textAlign: 'center', marginTop: 20, color: '#123B2A', fontWeight: '600' }
});
