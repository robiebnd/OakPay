import { useState } from 'react';
import { ActivityIndicator, Pressable, StyleSheet, Text, TextInput, View } from 'react-native';
import { router } from 'expo-router';
import { useAuth } from '../../context/AuthContext';

export default function LoginScreen() {
  const { signIn } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  async function handleLogin() {
    setError('');
    if (!email.trim() || !password) {
      setError('Enter your email and password.');
      return;
    }

    try {
      setSubmitting(true);
      await signIn({ email: email.trim().toLowerCase(), password });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to sign in.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <View style={styles.container}>
      <View style={styles.brand}>
        <Text style={styles.logo}>OakPay</Text>
        <Text style={styles.subtitle}>Simple. Secure. P2P.</Text>
      </View>

      <View style={styles.form}>
        <Text style={styles.title}>Welcome back</Text>
        <Text style={styles.description}>Sign in to manage your wallet and P2P orders.</Text>

        <TextInput
          value={email}
          onChangeText={setEmail}
          placeholder="Email address"
          placeholderTextColor="#8B929D"
          autoCapitalize="none"
          autoCorrect={false}
          keyboardType="email-address"
          style={styles.input}
        />
        <TextInput
          value={password}
          onChangeText={setPassword}
          placeholder="Password"
          placeholderTextColor="#8B929D"
          secureTextEntry
          style={styles.input}
        />

        {error ? <Text style={styles.error}>{error}</Text> : null}

        <Pressable style={[styles.button, submitting && styles.buttonDisabled]} onPress={handleLogin} disabled={submitting}>
          {submitting ? <ActivityIndicator color="#0D1017" /> : <Text style={styles.buttonText}>Sign in</Text>}
        </Pressable>

        <Pressable onPress={() => router.push('/(auth)/register')} disabled={submitting}>
          <Text style={styles.register}>Don't have an account? Create one</Text>
        </Pressable>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#0D1017', padding: 24, justifyContent: 'center' },
  brand: { marginBottom: 48 },
  logo: { fontSize: 40, fontWeight: '800', color: '#D8FF3E' },
  subtitle: { marginTop: 6, fontSize: 15, color: '#9BA2AE' },
  form: { width: '100%' },
  title: { fontSize: 29, fontWeight: '700', color: '#F5F7FA' },
  description: { marginTop: 8, marginBottom: 24, color: '#9BA2AE', lineHeight: 21 },
  input: { height: 54, backgroundColor: '#171B23', color: '#F5F7FA', borderRadius: 14, paddingHorizontal: 16, marginBottom: 12, borderWidth: 1, borderColor: '#272D38', fontSize: 16 },
  error: { color: '#FF7B7B', marginBottom: 12, lineHeight: 20 },
  button: { height: 54, borderRadius: 14, backgroundColor: '#D8FF3E', alignItems: 'center', justifyContent: 'center', marginTop: 8 },
  buttonDisabled: { opacity: 0.65 },
  buttonText: { color: '#0D1017', fontSize: 16, fontWeight: '800' },
  register: { textAlign: 'center', marginTop: 20, color: '#D8FF3E', fontWeight: '600' },
});
