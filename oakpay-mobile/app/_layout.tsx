import {
  GoogleSansFlex_400Regular,
  GoogleSansFlex_600SemiBold,
  GoogleSansFlex_700Bold,
  GoogleSansFlex_800ExtraBold,
  useFonts,
} from '@expo-google-fonts/google-sans-flex';
import * as Notifications from 'expo-notifications';
import Constants from 'expo-constants';
import { Stack } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import { PropsWithChildren, useEffect } from 'react';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { Platform, Text, TextInput } from 'react-native';
import { AuthProvider, useAuth } from '../context/AuthContext';
import { notificationsApi } from '../lib/notificationsApi';

Notifications.setNotificationHandler({
  handleNotification: async () => ({
    shouldShowBanner: true,
    shouldShowList: true,
    shouldPlaySound: true,
    shouldSetBadge: true,
  }),
});

function NotificationBootstrap() {
  const { accessToken, user } = useAuth();

  useEffect(() => {
    let mounted = true;

    async function register() {
      if (!accessToken || !user) return;

      try {
        const permissions = await Notifications.getPermissionsAsync();
        let finalStatus = permissions.status;
        if (finalStatus !== 'granted') {
          const requested = await Notifications.requestPermissionsAsync();
          finalStatus = requested.status;
        }
        if (finalStatus !== 'granted') return;

        if (Platform.OS === 'android') {
          await Notifications.setNotificationChannelAsync('default', {
            name: 'PayOak notifications',
            importance: Notifications.AndroidImportance.DEFAULT,
            sound: 'default',
          });
        }

        const projectId = Constants.expoConfig?.extra?.eas?.projectId;
        const pushToken = projectId
          ? await Notifications.getExpoPushTokenAsync({ projectId })
          : await Notifications.getExpoPushTokenAsync();

        if (!mounted) return;

        await notificationsApi.registerDevice(accessToken, {
          expoPushToken: pushToken.data,
          platform: Platform.OS,
          deviceId: Constants.deviceName ?? null,
        });
      } catch {
        // Push registration must never prevent the user from entering PayOak.
      }
    }

    register();
    return () => { mounted = false; };
  }, [accessToken, user]);

  return null;
}

export default function RootLayout() {
  const [fontsLoaded] = useFonts({
    Inter_400Regular: GoogleSansFlex_400Regular,
    Inter_600SemiBold: GoogleSansFlex_600SemiBold,
    Inter_700Bold: GoogleSansFlex_700Bold,
    Inter_800ExtraBold: GoogleSansFlex_800ExtraBold,
  });

  if (!fontsLoaded) return null;

  Text.defaultProps = Text.defaultProps || {};
  Text.defaultProps.style = [{ fontFamily: 'Inter_400Regular' }, Text.defaultProps.style];
  TextInput.defaultProps = TextInput.defaultProps || {};
  TextInput.defaultProps.style = [{ fontFamily: 'Inter_400Regular' }, TextInput.defaultProps.style];

  return (
    <SafeAreaProvider>
      <AuthProvider>
        <NotificationBootstrap />
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
