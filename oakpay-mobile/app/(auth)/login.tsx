import { useState } from 'react';
import { ActivityIndicator, Image, KeyboardAvoidingView, Platform, Pressable, ScrollView, StyleSheet, Text, TextInput, View } from 'react-native';
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
    if (!email.trim() || !password) { setError('Enter your email and password.'); return; }
    try { setSubmitting(true); await signIn({ email: email.trim().toLowerCase(), password }); }
    catch (err) { const message = err instanceof Error ? err.message : (err as { message?: string })?.message; setError(message ?? 'Unable to sign in.'); }
    finally { setSubmitting(false); }
  }

  return (
    <KeyboardAvoidingView style={styles.keyboardView} behavior={Platform.OS === 'ios' ? 'padding' : 'height'}>
      <ScrollView contentContainerStyle={styles.scrollContent} keyboardShouldPersistTaps="handled" showsVerticalScrollIndicator={false}>
        <View style={styles.container}>
          <View style={styles.brand}><Image source={OAKPAY_LOGO} style={styles.logo} resizeMode="contain" /><Text style={styles.subtitle}>Simple. Secure. P2P.</Text></View>
          <View style={styles.form}>
            {registered === '1' ? <Text style={styles.success}>Account created successfully. Sign in to continue.</Text> : null}
            <Text style={styles.title}>Welcome back</Text>
            <Text style={styles.description}>Sign in to manage your wallet and P2P orders.</Text>
            <TextInput value={email} onChangeText={setEmail} placeholder="Email address" placeholderTextColor="#70757D" autoCapitalize="none" autoCorrect={false} keyboardType="email-address" returnKeyType="next" style={styles.input} />
            <TextInput value={password} onChangeText={setPassword} placeholder="Password" placeholderTextColor="#70757D" secureTextEntry returnKeyType="done" onSubmitEditing={handleLogin} style={styles.input} />
            <Pressable onPress={() => router.push('/(auth)/forgot-password')} disabled={submitting} style={styles.forgot}><Text style={styles.forgotText}>Forgot password?</Text></Pressable>
            {error ? <Text style={styles.error}>{error}</Text> : null}
            <Pressable style={[styles.button, submitting && styles.buttonDisabled]} onPress={handleLogin} disabled={submitting}>{submitting ? <ActivityIndicator color="#FFFFFF" /> : <Text style={styles.buttonText}>Sign in</Text>}</Pressable>
            <Pressable onPress={() => router.push('/(auth)/onboarding')} disabled={submitting}><Text style={styles.register}>Don't have an account? <Text style={styles.registerAccent}>Create one</Text></Text></Pressable>
          </View>
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  keyboardView: { flex: 1, backgroundColor: '#F7F8F9' },
  scrollContent: { flexGrow: 1, justifyContent: 'center' },
  container: { width: '100%', paddingHorizontal: 40, paddingTop: 52, paddingBottom: 36 },
  brand: { marginBottom: 38, alignItems: 'center' },
  logo: { width: 285, height: 96 },
  subtitle: { marginTop: 2, fontFamily: 'Inter_400Regular', fontSize: 15, color: '#6F747B' },
  form: { width: '100%' },
  success: { marginBottom: 14, color: '#183F2C', fontFamily: 'Inter_600SemiBold', fontSize: 14, lineHeight: 20, textAlign: 'center' },
  title: { fontFamily: 'Inter_700Bold', fontSize: 30, color: '#111916', letterSpacing: -0.6, textAlign: 'center' },
  description: { marginTop: 10, marginBottom: 24, fontFamily: 'Inter_400Regular', fontSize: 16, color: '#6F747B', lineHeight: 23, textAlign: 'center' },
  input: { height: 58, backgroundColor: '#FFFCE0', color: '#111916', borderRadius: 18, paddingHorizontal: 18, marginBottom: 14, borderWidth: 1, borderColor: '#EEE9AE', fontFamily: 'Inter_400Regular', fontSize: 16 },
  forgot: { alignSelf: 'flex-end', marginTop: -4, marginBottom: 8, paddingVertical: 4 },
  forgotText: { color: '#123B2A', fontFamily: 'Inter_700Bold', fontSize: 14 },
  error: { color: '#B42318', marginBottom: 12, fontFamily: 'Inter_600SemiBold', lineHeight: 20, textAlign: 'center' },
  button: { height: 58, borderRadius: 29, backgroundColor: '#123B2A', alignItems: 'center', justifyContent: 'center', marginTop: 8 },
  buttonDisabled: { opacity: 0.65 },
  buttonText: { color: '#FFFFFF', fontFamily: 'Inter_700Bold', fontSize: 16 },
  register: { textAlign: 'center', marginTop: 22, color: '#6F747B', fontFamily: 'Inter_400Regular', fontSize: 15 },
  registerAccent: { color: '#123B2A', fontFamily: 'Inter_700Bold' },
});
