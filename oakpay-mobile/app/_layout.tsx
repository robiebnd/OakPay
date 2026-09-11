import {
  GoogleSansFlex_400Regular,
  GoogleSansFlex_600SemiBold,
  GoogleSansFlex_700Bold,
  GoogleSansFlex_800ExtraBold,
  useFonts,
} from '@expo-google-fonts/google-sans-flex';
import { Stack } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { AuthProvider } from '../context/AuthContext';

export default function RootLayout() {
  const [fontsLoaded] = useFonts({
    Inter_400Regular: GoogleSansFlex_400Regular,
    Inter_600SemiBold: GoogleSansFlex_600SemiBold,
    Inter_700Bold: GoogleSansFlex_700Bold,
    Inter_800ExtraBold: GoogleSansFlex_800ExtraBold,
  });

  if (!fontsLoaded) return null;

  return (
    <SafeAreaProvider>
      <AuthProvider>
        <StatusBar style="light" />
        <Stack screenOptions={{ headerShown: false }}>
          <Stack.Screen name="index" />
          <Stack.Screen name="(auth)" />
          <Stack.Screen name="(tabs)" />
        </Stack>
      </AuthProvider>
    </SafeAreaProvider>
  );
}
