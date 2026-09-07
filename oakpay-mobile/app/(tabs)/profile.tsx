import { Ionicons } from '@expo/vector-icons';
import { Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useAuth } from '../../context/AuthContext';

const BG = '#0D1017';
const CARD = '#171B24';
const TEXT = '#F4F6F8';
const MUTED = '#8D95A3';
const LIME = '#D8FF3E';

export default function ProfileScreen() {
  const insets = useSafeAreaInsets();
  const { user, signOut } = useAuth();
  const fullName = [user?.firstName, user?.lastName].filter(Boolean).join(' ') || 'OakPay User';
  const initial = (user?.firstName?.charAt(0) || 'O').toUpperCase();

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
                <Text style={styles.email}>{user?.email || 'No email available'}</Text>
              </View>
            </View>
            <View style={styles.statusRow}>
              <View style={styles.statusPill}>
                <Ionicons name={user?.emailVerified ? 'checkmark-circle' : 'shield-checkmark-outline'} size={15} color={LIME} />
                <Text style={styles.statusText}>{user?.emailVerified ? 'Email verified' : 'Account active'}</Text>
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
