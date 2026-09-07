import { Redirect, Tabs } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { StatusBar } from 'expo-status-bar';
import { useAuth } from '../../context/AuthContext';

const LIME = '#D8FF3E';
const MUTED = '#7D8490';
const NAVY = '#0D1017';

export default function TabsLayout() {
  const { accessToken, isLoading } = useAuth();

  if (isLoading) return null;
  if (!accessToken) return <Redirect href="/(auth)/login" />;

  return (
    <>
      <StatusBar style="light" />
      <Tabs
        screenOptions={{
          headerShown: false,
          tabBarActiveTintColor: LIME,
          tabBarInactiveTintColor: MUTED,
          tabBarStyle: {
            backgroundColor: NAVY,
            borderTopColor: '#1E232D',
            height: 76,
            paddingTop: 7,
            paddingBottom: 10,
          },
          tabBarLabelStyle: { fontFamily: 'Inter_600SemiBold', fontSize: 11 },
        }}
      >
        <Tabs.Screen name="index" options={{ title: 'Home', tabBarIcon: ({ color, size }) => <Ionicons name="home-outline" color={color} size={size} /> }} />
        <Tabs.Screen name="market" options={{ title: 'P2P', tabBarIcon: ({ color, size }) => <Ionicons name="people-outline" color={color} size={size} /> }} />
        <Tabs.Screen name="orders" options={{ title: 'Orders', tabBarIcon: ({ color, size }) => <Ionicons name="receipt-outline" color={color} size={size} /> }} />
        <Tabs.Screen name="wallet" options={{ title: 'Wallet', tabBarIcon: ({ color, size }) => <Ionicons name="wallet-outline" color={color} size={size} /> }} />
        <Tabs.Screen name="profile" options={{ title: 'Profile', tabBarIcon: ({ color, size }) => <Ionicons name="person-outline" color={color} size={size} /> }} />
      </Tabs>
    </>
  );
}
