import { Tabs } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';

export default function TabsLayout() {
  return (
    <Tabs screenOptions={{ headerShown: false, tabBarActiveTintColor: '#123B2A', tabBarInactiveTintColor: '#8A938E' }}>
      <Tabs.Screen name="index" options={{ title: 'Home', tabBarIcon: ({ color, size }) => <Ionicons name="home-outline" color={color} size={size} /> }} />
      <Tabs.Screen name="market" options={{ title: 'P2P Market', tabBarIcon: ({ color, size }) => <Ionicons name="swap-horizontal-outline" color={color} size={size} /> }} />
      <Tabs.Screen name="trades" options={{ title: 'Trades', tabBarIcon: ({ color, size }) => <Ionicons name="receipt-outline" color={color} size={size} /> }} />
      <Tabs.Screen name="wallet" options={{ title: 'Wallet', tabBarIcon: ({ color, size }) => <Ionicons name="wallet-outline" color={color} size={size} /> }} />
      <Tabs.Screen name="profile" options={{ title: 'Profile', tabBarIcon: ({ color, size }) => <Ionicons name="person-outline" color={color} size={size} /> }} />
    </Tabs>
  );
}
