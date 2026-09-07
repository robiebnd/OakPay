import { useState } from 'react';
import { ActivityIndicator, Pressable, StyleSheet, Text, TextInput, View } from 'react-native';
import { router } from 'expo-router';
import { useAuth } from '../../context/AuthContext';

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
      await signUp({
        firstName: firstName.trim(),
        lastName: lastName.trim(),
        email: email.trim().toLowerCase(),
        password,
      });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to create your account.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <View style={styles.container}>
      <Text style={styles.logo}>OakPay</Text>
      <Text style={styles.title}>Create your account</Text>
      <Text style={styles.description}>Join OakPay and start using secure P2P payments.</Text>

      <View style={styles.row}>
        <TextInput value={firstName} onChangeText={setFirstName} placeholder="First name" placeholderTextColor="#8B929D" style={[styles.input, styles.half]} />
        <TextInput value={lastName} onChangeText={setLastName} placeholder="Last name" placeholderTextColor="#8B929D" style={[styles.input, styles.half]} />
      </View>
      <TextInput value={email} onChangeText={setEmail} placeholder="Email address" placeholderTextColor="#8B929D" autoCapitalize="none" autoCorrect={false} keyboardType="email-address" style={styles.input} />
      <TextInput value={password} onChangeText={setPassword} placeholder="Password" placeholderTextColor="#8B929D" secureTextEntry style={styles.input} />

      {error ? <Text style={styles.error}>{error}</Text> : null}

      <Pressable style={[styles.button, submitting && styles.buttonDisabled]} onPress={handleRegister} disabled={submitting}>
        {submitting ? <ActivityIndicator color="#0D1017" /> : <Text style={styles.buttonText}>Create account</Text>}
      </Pressable>

      <Pressable onPress={() => router.replace('/(auth)/login')} disabled={submitting}>
        <Text style={styles.login}>Already have an account? Sign in</Text>
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#0D1017', padding: 24, justifyContent: 'center' },
  logo: { fontSize: 38, fontWeight: '800', color: '#D8FF3E', marginBottom: 32 },
  title: { fontSize: 29, fontWeight: '700', color: '#F5F7FA' },
  description: { marginTop: 8, marginBottom: 24, color: '#9BA2AE', lineHeight: 21 },
  row: { flexDirection: 'row', gap: 10 },
  input: { height: 54, backgroundColor: '#171B23', color: '#F5F7FA', borderRadius: 14, paddingHorizontal: 16, marginBottom: 12, borderWidth: 1, borderColor: '#272D38', fontSize: 16 },
  half: { flex: 1 },
  error: { color: '#FF7B7B', marginBottom: 12, lineHeight: 20 },
  button: { height: 54, borderRadius: 14, backgroundColor: '#D8FF3E', alignItems: 'center', justifyContent: 'center', marginTop: 8 },
  buttonDisabled: { opacity: 0.65 },
  buttonText: { color: '#0D1017', fontSize: 16, fontWeight: '800' },
  login: { textAlign: 'center', marginTop: 20, color: '#D8FF3E', fontWeight: '600' },
});
