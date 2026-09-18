import { Ionicons } from '@expo/vector-icons';
import { useFocusEffect } from 'expo-router';
import { useCallback, useMemo, useState } from 'react';
import { ActivityIndicator, Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useAuth } from '../../context/AuthContext';
import { AppNotification, notificationsApi } from '../../lib/notificationsApi';

const BG = '#0D1017', CARD = '#171B24', CARD2 = '#1D222C', TEXT = '#F4F6F8', MUTED = '#8D95A3', LIME = '#D8FF3E', RED = '#FF6675';

function iconFor(type: string) {
  if (type.includes('PAYMENT')) return 'card-outline';
  if (type.includes('ORDER') || type.includes('P2P')) return 'receipt-outline';
  if (type.includes('DEPOSIT')) return 'arrow-down-circle-outline';
  if (type.includes('WITHDRAW')) return 'arrow-up-circle-outline';
  return 'notifications-outline';
}

function timeAgo(value: string) {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '';
  const seconds = Math.max(0, Math.floor((Date.now() - date.getTime()) / 1000));
  if (seconds < 60) return 'Just now';
  if (seconds < 3600) return `${Math.floor(seconds / 60)}m ago`;
  if (seconds < 86400) return `${Math.floor(seconds / 3600)}h ago`;
  return date.toLocaleDateString([], { day: '2-digit', month: 'short' });
}

export default function NotificationsScreen() {
  const { accessToken } = useAuth();
  const [items, setItems] = useState<AppNotification[]>([]);
  const [loading, setLoading] = useState(true);
  const [working, setWorking] = useState(false);
  const [testWorking, setTestWorking] = useState(false);
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    if (!accessToken) { setItems([]); setLoading(false); return; }
    try {
      setError('');
      setLoading(true);
      setItems(await notificationsApi.list(accessToken));
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Unable to load notifications.');
    } finally {
      setLoading(false);
    }
  }, [accessToken]);

  useFocusEffect(useCallback(() => { load(); }, [load]));

  const unread = useMemo(() => items.filter((item) => !item.read).length, [items]);

  async function createTestNotification() {
    if (!accessToken || testWorking) return;
    try {
      setTestWorking(true);
      setError('');
      const created = await notificationsApi.createTest(accessToken);
      setItems((rows) => [created, ...rows.filter((row) => row.id !== created.id)]);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Unable to create test notification.');
    } finally {
      setTestWorking(false);
    }
  }

  async function markRead(id: string) {
    if (!accessToken) return;
    try {
      await notificationsApi.markRead(accessToken, id);
      setItems((rows) => rows.map((row) => row.id === id ? { ...row, read: true } : row));
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Unable to update notification.');
    }
  }

  async function markAllRead() {
    if (!accessToken || unread === 0) return;
    try {
      setWorking(true);
      await notificationsApi.markAllRead(accessToken);
      setItems((rows) => rows.map((row) => ({ ...row, read: true })));
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Unable to mark notifications as read.');
    } finally {
      setWorking(false);
    }
  }

  return (
    <SafeAreaView style={styles.safe} edges={['top']}>
      <View style={styles.screen}>
        <ScrollView contentContainerStyle={styles.content} showsVerticalScrollIndicator={false}>
          <View style={styles.header}>
            <View style={{ flex: 1 }}>
              <Text style={styles.eyebrow}>PAYOAK</Text>
              <Text style={styles.title}>Notifications</Text>
              <Text style={styles.subtitle}>
                {unread > 0 ? `${unread} unread notification${unread === 1 ? '' : 's'}` : 'You are all caught up.'}
              </Text>
            </View>
            <Pressable
              style={[styles.readAll, (working || unread === 0) && styles.disabled]}
              disabled={working || unread === 0}
              onPress={markAllRead}
            >
              <Text style={styles.readAllText}>Read all</Text>
            </Pressable>
          </View>

          {error ? (
            <View style={styles.error}>
              <Ionicons name="alert-circle-outline" size={18} color={RED} />
              <Text style={styles.errorText}>{error}</Text>
            </View>
          ) : null}

          {__DEV__ ? (
            <Pressable
              style={[styles.testButton, testWorking && styles.disabled]}
              disabled={testWorking}
              onPress={createTestNotification}
            >
              <Ionicons name="flask-outline" size={17} color={BG} />
              <Text style={styles.testButtonText}>
                {testWorking ? 'Creating test notification…' : 'Create test notification'}
              </Text>
            </Pressable>
          ) : null}

          {loading ? (
            <View style={styles.loading}><ActivityIndicator color={LIME} /><Text style={styles.muted}>Loading notifications…</Text></View>
          ) : items.length === 0 ? (
            <View style={styles.empty}>
              <View style={styles.emptyIcon}><Ionicons name="notifications-outline" size={28} color={LIME} /></View>
              <Text style={styles.emptyTitle}>No notifications yet</Text>
              <Text style={styles.muted}>Payment, order, deposit and withdrawal updates will appear here.</Text>
            </View>
          ) : (
            <View style={styles.list}>
              {items.map((item) => (
                <Pressable
                  key={item.id}
                  style={[styles.row, !item.read && styles.unreadRow]}
                  onPress={() => !item.read && markRead(item.id)}
                >
                  <View style={styles.icon}><Ionicons name={iconFor(item.type) as any} size={21} color={LIME} /></View>
                  <View style={styles.copy}>
                    <View style={styles.titleRow}>
                      <Text style={styles.itemTitle}>{item.title}</Text>
                      {!item.read ? <View style={styles.dot} /> : null}
                    </View>
                    <Text style={styles.message}>{item.message}</Text>
                    <Text style={styles.time}>{timeAgo(item.createdAt)}</Text>
                  </View>
                </Pressable>
              ))}
            </View>
          )}
        </ScrollView>
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safe:{flex:1,backgroundColor:BG},
  screen:{flex:1,backgroundColor:BG},
  content:{paddingHorizontal:20,paddingTop:10,paddingBottom:48},
  header:{flexDirection:'row',alignItems:'flex-start',marginBottom:24},
  eyebrow:{color:LIME,fontFamily:'Inter_800ExtraBold',fontSize:11,letterSpacing:2.2},
  title:{color:TEXT,fontFamily:'Inter_800ExtraBold',fontSize:31,lineHeight:38,marginTop:3},
  subtitle:{color:MUTED,fontFamily:'Inter_400Regular',fontSize:12,marginTop:5},
  readAll:{backgroundColor:LIME,borderRadius:12,paddingHorizontal:13,paddingVertical:10,marginTop:8},
  readAllText:{color:BG,fontFamily:'Inter_800ExtraBold',fontSize:11},
  disabled:{opacity:.45},
  testButton:{backgroundColor:LIME,borderRadius:14,paddingVertical:12,paddingHorizontal:14,flexDirection:'row',alignItems:'center',justifyContent:'center',gap:8,marginBottom:16},
  testButtonText:{color:BG,fontFamily:'Inter_800ExtraBold',fontSize:12},
  error:{backgroundColor:'#24171B',borderColor:'#42272A',borderWidth:1,borderRadius:15,padding:13,flexDirection:'row',alignItems:'center',gap:8,marginBottom:15},
  errorText:{color:RED,fontFamily:'Inter_600SemiBold',fontSize:11,flex:1},
  loading:{height:160,backgroundColor:CARD,borderRadius:19,alignItems:'center',justifyContent:'center',gap:7},
  muted:{color:MUTED,fontFamily:'Inter_400Regular',fontSize:12,lineHeight:18,textAlign:'center'},
  empty:{backgroundColor:CARD,borderRadius:20,padding:28,alignItems:'center'},
  emptyIcon:{width:62,height:62,borderRadius:19,backgroundColor:'#242A17',alignItems:'center',justifyContent:'center',marginBottom:13},
  emptyTitle:{color:TEXT,fontFamily:'Inter_800ExtraBold',fontSize:18,marginBottom:7},
  list:{backgroundColor:CARD,borderRadius:19,overflow:'hidden'},
  row:{minHeight:98,padding:15,flexDirection:'row',alignItems:'flex-start',borderBottomWidth:1,borderBottomColor:'#242A34'},
  unreadRow:{backgroundColor:CARD2},
  icon:{width:44,height:44,borderRadius:14,backgroundColor:'#242A17',alignItems:'center',justifyContent:'center',marginRight:12},
  copy:{flex:1},
  titleRow:{flexDirection:'row',alignItems:'center',gap:7},
  itemTitle:{color:TEXT,fontFamily:'Inter_700Bold',fontSize:13,flexShrink:1},
  dot:{width:7,height:7,borderRadius:4,backgroundColor:LIME},
  message:{color:MUTED,fontFamily:'Inter_400Regular',fontSize:11,lineHeight:17,marginTop:4},
  time:{color:'#707887',fontFamily:'Inter_400Regular',fontSize:9,marginTop:7}
});
