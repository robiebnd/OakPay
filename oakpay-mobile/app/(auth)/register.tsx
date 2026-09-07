import { useState } from 'react';
import { ActivityIndicator, Image, Pressable, StyleSheet, Text, TextInput, View } from 'react-native';
import { router } from 'expo-router';
import { useAuth } from '../../context/AuthContext';

const OAKPAY_LOGO = require('../../assets/Logo_OakPay.png');

export default function RegisterScreen() {
  const { signUp } = useAuth();
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  async function handleRegister() {
    setError('');
    if (!firstName.trim() || !lastName.trim() || !email.trim() || !password) {
      setError('Complete all fields.');
      return;
    }
    if (password.length < 8) {
      setError('Password must be at least 8 characters.');
      return;
    }

    try {
      setSubmitting(true);
      await signUp({ firstName: firstName.trim(), lastName: lastName.trim(), email: email.trim().toLowerCase(), password });
    } catch (err) {
      setError(err instanceof Error ? err.message : (err as { message?: string })?.message ?? 'Unable to create your account.');
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

      <Text style={styles.title}>Create your account</Text>
      <Text style={styles.description}>Join OakPay and start using secure P2P payments.</Text>

      <View style={styles.row}>
        <TextInput value={firstName} onChangeText={setFirstName} placeholder="First name" placeholderTextColor="#70757D" style={[styles.input, styles.half]} />
        <TextInput value={lastName} onChangeText={setLastName} placeholder="Last name" placeholderTextColor="#70757D" style={[styles.input, styles.half]} />
      </View>
      <TextInput value={email} onChangeText={setEmail} placeholder="Email address" placeholderTextColor="#70757D" autoCapitalize="none" autoCorrect={false} keyboardType="email-address" style={styles.input} />
      <TextInput value={password} onChangeText={setPassword} placeholder="Password" placeholderTextColor="#70757D" secureTextEntry style={styles.input} />

      {error ? <Text style={styles.error}>{error}</Text> : null}

      <Pressable style={[styles.button, submitting && styles.buttonDisabled]} onPress={handleRegister} disabled={submitting}>
        {submitting ? <ActivityIndicator color="#FFFFFF" /> : <Text style={styles.buttonText}>Create account</Text>}
      </Pressable>

      <Pressable onPress={() => router.replace('/(auth)/login')} disabled={submitting}>
        <Text style={styles.login}>Already have an account? Sign in</Text>
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#F7F8F9', paddingHorizontal: 52, paddingTop: 52, justifyContent: 'center' },
  brand: { marginBottom: 30, alignItems: 'flex-start' },
  logo: { width: 220, height: 74 },
  subtitle: { marginTop: 2, fontFamily: 'Inter_400Regular', fontSize: 14, color: '#6F747B' },
  title: { fontFamily: 'Inter_700Bold', fontSize: 29, color: '#111916', letterSpacing: -0.5 },
  description: { marginTop: 8, marginBottom: 24, fontFamily: 'Inter_400Regular', fontSize: 15, color: '#6F747B', lineHeight: 22 },
  row: { flexDirection: 'row', gap: 10 },
  input: { height: 54, backgroundColor: '#FFFFFF', color: '#111916', borderRadius: 15, paddingHorizontal: 16, marginBottom: 12, borderWidth: 1, borderColor: '#E1E4E3', fontFamily: 'Inter_400Regular', fontSize: 15 },
  half: { flex: 1 },
  error: { color: '#B42318', marginBottom: 12, fontFamily: 'Inter_600SemiBold', lineHeight: 20 },
  button: { height: 56, borderRadius: 15, backgroundColor: '#183F2C', alignItems: 'center', justifyContent: 'center', marginTop: 8 },
  buttonDisabled: { opacity: 0.65 },
  buttonText: { color: '#FFFFFF', fontFamily: 'Inter_700Bold', fontSize: 16 },
  login: { textAlign: 'center', marginTop: 20, color: '#183F2C', fontFamily: 'Inter_600SemiBold', fontSize: 15 },
});
