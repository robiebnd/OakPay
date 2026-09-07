import { useState } from 'react';
import { ActivityIndicator, Image, Pressable, StyleSheet, Text, TextInput, View } from 'react-native';
import { router, useLocalSearchParams } from 'expo-router';
import { useAuth } from '../../context/AuthContext';

const OAKPAY_LOGO = require('../../assets/Logo_OakPay.png');

export default function LoginScreen() {
  const { signIn } = useAuth();
  const { registered } = useLocalSearchParams<{ registered?: string }>();
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
      const message = err instanceof Error ? err.message : (err as { message?: string })?.message;
      setError(message ?? 'Unable to sign in.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <View style={styles.container}>
      <View style={styles.brand}>
        <Image source={OAKPAY_LOGO} style={styles.logo} resizeMode="contain" />
        <Text style={styles.subtitle}>Simple. Secure. P2P.</Text>
      </View>

      <View style={styles.form}>
        {registered === '1' ? <Text style={styles.success}>Account created successfully. Sign in to continue.</Text> : null}
        <Text style={styles.title}>Welcome back</Text>
        <Text style={styles.description}>Sign in to manage your wallet and P2P orders.</Text>

        <TextInput value={email} onChangeText={setEmail} placeholder="Email address" placeholderTextColor="#70757D" autoCapitalize="none" autoCorrect={false} keyboardType="email-address" style={styles.input} />
        <TextInput value={password} onChangeText={setPassword} placeholder="Password" placeholderTextColor="#70757D" secureTextEntry style={styles.input} />

        {error ? <Text style={styles.error}>{error}</Text> : null}

        <Pressable style={[styles.button, submitting && styles.buttonDisabled]} onPress={handleLogin} disabled={submitting}>
          {submitting ? <ActivityIndicator color="#FFFFFF" /> : <Text style={styles.buttonText}>Sign in</Text>}
        </Pressable>

        <Pressable onPress={() => router.push('/(auth)/register')} disabled={submitting}>
          <Text style={styles.register}>Don't have an account? Create one</Text>
        </Pressable>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#F7F8F9', paddingHorizontal: 52, paddingTop: 72, justifyContent: 'center' },
  brand: { marginBottom: 52, alignItems: 'flex-start' },
  logo: { width: 285, height: 96 },
  subtitle: { marginTop: 4, fontFamily: 'Inter_400Regular', fontSize: 15, color: '#6F747B' },
  form: { width: '100%' },
  success: { marginBottom: 14, color: '#183F2C', fontFamily: 'Inter_600SemiBold', fontSize: 14, lineHeight: 20 },
  title: { fontFamily: 'Inter_700Bold', fontSize: 30, color: '#111916', letterSpacing: -0.6 },
  description: { marginTop: 10, marginBottom: 28, fontFamily: 'Inter_400Regular', fontSize: 16, color: '#6F747B', lineHeight: 23 },
  input: { height: 58, backgroundColor: '#FFFFFF', color: '#111916', borderRadius: 16, paddingHorizontal: 18, marginBottom: 14, borderWidth: 1, borderColor: '#E1E4E3', fontFamily: 'Inter_400Regular', fontSize: 16 },
  error: { color: '#B42318', marginBottom: 12, fontFamily: 'Inter_600SemiBold', lineHeight: 20 },
  button: { height: 58, borderRadius: 16, backgroundColor: '#183F2C', alignItems: 'center', justifyContent: 'center', marginTop: 8 },
  buttonDisabled: { opacity: 0.65 },
  buttonText: { color: '#FFFFFF', fontFamily: 'Inter_700Bold', fontSize: 16 },
  register: { textAlign: 'center', marginTop: 22, color: '#183F2C', fontFamily: 'Inter_600SemiBold', fontSize: 15 },
});
