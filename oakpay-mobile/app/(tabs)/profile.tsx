import { Ionicons } from '@expo/vector-icons';
import { useFocusEffect } from 'expo-router';
import { useCallback, useState } from 'react';
import * as SecureStore from 'expo-secure-store';
import { Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useAuth } from '../../context/AuthContext';
import { authApi, UserResponse } from '../../lib/api';

const USER_KEY = 'oakpay.user';
const BG = '#0D1017';
const CARD = '#171B24';
const TEXT = '#F4F6F8';
const MUTED = '#8D95A3';
const LIME = '#D8FF3E';

export default function ProfileScreen() {
  const insets = useSafeAreaInsets();
  const { user, accessToken, signOut } = useAuth();
  const [profileUser, setProfileUser] = useState<UserResponse | null>(user);

  useFocusEffect(
    useCallback(() => {
      let active = true;

      async function refreshProfile() {
        if (!accessToken) return;
        try {
          const currentUser = await authApi.me(accessToken);
          if (!active) return;
          setProfileUser(currentUser);
          await SecureStore.setItemAsync(USER_KEY, JSON.stringify(currentUser));
        } catch {
          // Keep the authenticated context/cache if the API is temporarily unavailable.
        }
      }

      setProfileUser(user);
      refreshProfile();

      return () => {
        active = false;
      };
    }, [accessToken, user]),
  );

  const currentUser = profileUser ?? user;
  const fullName = [currentUser?.firstName, currentUser?.lastName].filter(Boolean).join(' ') || 'OakPay User';
  const initial = (currentUser?.firstName?.charAt(0) || currentUser?.lastName?.charAt(0) || 'O').toUpperCase();

  return (
    <View style={styles.screen}>
      <ScrollView
        showsVerticalScrollIndicator={false}
        contentContainerStyle={{ paddingTop: Math.max(insets.top + 8, 20), paddingBottom: 42 }}
      >
        <View style={styles.container}>
          <Text style={styles.title}>Profile</Text>

          <View style={styles.profileCard}>
            <View style={styles.profileTop}>
              <View style={styles.avatar}><Text style={styles.avatarText}>{initial}</Text></View>
              <View style={styles.identity}>
                <Text style={styles.name}>{fullName}</Text>
                <Text style={styles.email}>{currentUser?.email || 'No email available'}</Text>
              </View>
            </View>
            <View style={styles.statusRow}>
              <View style={styles.statusPill}>
                <Ionicons name={currentUser?.emailVerified ? 'checkmark-circle' : 'shield-checkmark-outline'} size={15} color={LIME} />
                <Text style={styles.statusText}>{currentUser?.emailVerified ? 'Email verified' : 'Account active'}</Text>
              </View>
              <Text style={styles.member}>OakPay account</Text>
            </View>
          </View>

          <Text style={styles.sectionTitle}>Account</Text>
          <View style={styles.menuCard}>
            <Pressable style={styles.menuRow}>
              <View style={styles.menuIcon}><Ionicons name="person-outline" size={20} color={LIME} /></View>
              <View style={styles.menuCopy}><Text style={styles.menuTitle}>Personal details</Text><Text style={styles.menuText}>Name and account information</Text></View>
              <Ionicons name="chevron-forward" size={20} color={MUTED} />
            </Pressable>
            <View style={styles.divider} />
            <Pressable style={styles.menuRow}>
              <View style={styles.menuIcon}><Ionicons name="shield-checkmark-outline" size={20} color={LIME} /></View>
              <View style={styles.menuCopy}><Text style={styles.menuTitle}>Security</Text><Text style={styles.menuText}>Protect your OakPay account</Text></View>
              <Ionicons name="chevron-forward" size={20} color={MUTED} />
            </Pressable>
            <View style={styles.divider} />
            <Pressable style={styles.menuRow}>
              <View style={styles.menuIcon}><Ionicons name="notifications-outline" size={20} color={LIME} /></View>
              <View style={styles.menuCopy}><Text style={styles.menuTitle}>Notifications</Text><Text style={styles.menuText}>Manage P2P and account alerts</Text></View>
              <Ionicons name="chevron-forward" size={20} color={MUTED} />
            </Pressable>
          </View>

          <Text style={styles.sectionTitle}>Support</Text>
          <View style={styles.menuCard}>
            <Pressable style={styles.menuRow}>
              <View style={styles.menuIcon}><Ionicons name="help-circle-outline" size={20} color={LIME} /></View>
              <View style={styles.menuCopy}><Text style={styles.menuTitle}>Help & support</Text><Text style={styles.menuText}>Get help with OakPay</Text></View>
              <Ionicons name="chevron-forward" size={20} color={MUTED} />
            </Pressable>
          </View>

          <Pressable style={styles.logout} onPress={signOut}>
            <Ionicons name="log-out-outline" size={20} color="#FF8F8F" />
            <Text style={styles.logoutText}>Sign out</Text>
          </Pressable>
        </View>
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  screen: { flex: 1, backgroundColor: BG },
  container: { paddingHorizontal: 20 },
  title: { color: TEXT, fontFamily: 'Inter_800ExtraBold', fontSize: 31, lineHeight: 38, marginBottom: 18 },
  profileCard: { backgroundColor: CARD, borderRadius: 22, padding: 19, borderWidth: 1, borderColor: '#242A34', marginBottom: 26 },
  profileTop: { flexDirection: 'row', alignItems: 'center' },
  avatar: { width: 58, height: 58, borderRadius: 18, backgroundColor: LIME, alignItems: 'center', justifyContent: 'center' },
  avatarText: { color: '#0D1017', fontFamily: 'Inter_800ExtraBold', fontSize: 23 },
  identity: { flex: 1, marginLeft: 14 },
  name: { color: TEXT, fontFamily: 'Inter_800ExtraBold', fontSize: 19 },
  email: { color: MUTED, fontFamily: 'Inter_400Regular', fontSize: 13, marginTop: 4 },
  statusRow: { marginTop: 18, paddingTop: 15, borderTopWidth: 1, borderTopColor: '#242A34', flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' },
  statusPill: { flexDirection: 'row', alignItems: 'center', gap: 6 },
  statusText: { color: LIME, fontFamily: 'Inter_700Bold', fontSize: 12 },
  member: { color: MUTED, fontFamily: 'Inter_400Regular', fontSize: 11 },
  sectionTitle: { color: TEXT, fontFamily: 'Inter_800ExtraBold', fontSize: 18, marginBottom: 10, marginTop: 2 },
  menuCard: { backgroundColor: CARD, borderRadius: 19, paddingHorizontal: 15, marginBottom: 25, borderWidth: 1, borderColor: '#202630' },
  menuRow: { minHeight: 70, flexDirection: 'row', alignItems: 'center' },
  menuIcon: { width: 40, height: 40, borderRadius: 13, backgroundColor: '#242A17', alignItems: 'center', justifyContent: 'center' },
  menuCopy: { flex: 1, marginHorizontal: 12 },
  menuTitle: { color: TEXT, fontFamily: 'Inter_700Bold', fontSize: 14 },
  menuText: { color: MUTED, fontFamily: 'Inter_400Regular', fontSize: 11, marginTop: 3 },
  divider: { height: 1, backgroundColor: '#242A34', marginLeft: 52 },
  logout: { height: 54, borderRadius: 16, borderWidth: 1, borderColor: '#42272A', backgroundColor: '#21171B', flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 8 },
  logoutText: { color: '#FF8F8F', fontFamily: 'Inter_700Bold', fontSize: 14 },
});
