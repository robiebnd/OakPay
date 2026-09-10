import { Ionicons } from '@expo/vector-icons';
import { useFocusEffect } from 'expo-router';
import { useCallback, useState } from 'react';
import * as SecureStore from 'expo-secure-store';
import { Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useAuth } from '../../context/AuthContext';
import { authApi, UserResponse } from '../../lib/api';

const USER_KEY = 'oakpay.user';
const BG = '#0D1017';
const CARD = '#171B24';
const TEXT = '#F4F6F8';
const MUTED = '#8D95A3';
const LIME = '#D8FF3E';

export default function ProfileScreen() {
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
    <SafeAreaView style={styles.safe} edges={['top']}>
      <View style={styles.screen}>
        <ScrollView showsVerticalScrollIndicator={false} contentContainerStyle={{ paddingTop: 8, paddingBottom: 42 }}>
          <View style={styles.container}>
            <Text style={styles.title}>Profile</Text>

            <View style={styles.profileCard}>
              <View style={styles.avatar}><Text style={styles.avatarText}>{initial}</Text></View>
              <View style={styles.profileCopy}>
                <Text style={styles.name}>{fullName}</Text>
                <Text style={styles.email}>{currentUser?.email || 'OakPay User'}</Text>
              </View>
            </View>

            <View style={styles.section}>
              <Text style={styles.sectionTitle}>Account</Text>
              <View style={styles.card}>
                <Row icon="person-outline" label="Personal information" />
                <Row icon="shield-checkmark-outline" label="Security" />
                <Row icon="notifications-outline" label="Notifications" />
              </View>
            </View>

            <View style={styles.section}>
              <Text style={styles.sectionTitle}>Support</Text>
              <View style={styles.card}>
                <Row icon="help-circle-outline" label="Help centre" />
                <Row icon="document-text-outline" label="Terms & conditions" />
                <Row icon="lock-closed-outline" label="Privacy policy" />
              </View>
            </View>

            <Pressable style={styles.signOut} onPress={signOut}>
              <Ionicons name="log-out-outline" size={20} color="#FF6675" />
              <Text style={styles.signOutText}>Sign out</Text>
            </Pressable>
          </View>
        </ScrollView>
      </View>
    </SafeAreaView>
  );
}

function Row({ icon, label }: { icon: React.ComponentProps<typeof Ionicons>['name']; label: string }) {
  return (
    <Pressable style={styles.row}>
      <View style={styles.rowIcon}><Ionicons name={icon} size={18} color={LIME} /></View>
      <Text style={styles.rowLabel}>{label}</Text>
      <Ionicons name="chevron-forward" size={18} color={MUTED} />
    </Pressable>
  );
}

const styles = StyleSheet.create({
  safe:{flex:1,backgroundColor:BG},screen:{flex:1,backgroundColor:BG},container:{paddingHorizontal:20},title:{color:TEXT,fontFamily:'Inter_800ExtraBold',fontSize:28,lineHeight:35,marginBottom:20},profileCard:{backgroundColor:CARD,borderRadius:20,padding:18,flexDirection:'row',alignItems:'center',borderWidth:1,borderColor:'#242A34'},avatar:{width:58,height:58,borderRadius:18,backgroundColor:LIME,alignItems:'center',justifyContent:'center',marginRight:13},avatarText:{color:BG,fontFamily:'Inter_800ExtraBold',fontSize:22},profileCopy:{flex:1},name:{color:TEXT,fontFamily:'Inter_800ExtraBold',fontSize:18},email:{color:MUTED,fontFamily:'Inter_400Regular',fontSize:11,marginTop:4},section:{marginTop:25},sectionTitle:{color:MUTED,fontFamily:'Inter_700Bold',fontSize:11,letterSpacing:1.2,marginBottom:9},card:{backgroundColor:CARD,borderRadius:18,paddingHorizontal:14},row:{minHeight:59,flexDirection:'row',alignItems:'center',borderBottomWidth:1,borderBottomColor:'#242A34'},rowIcon:{width:35,height:35,borderRadius:11,backgroundColor:'#242A17',alignItems:'center',justifyContent:'center',marginRight:11},rowLabel:{flex:1,color:TEXT,fontFamily:'Inter_600SemiBold',fontSize:12},signOut:{marginTop:26,height:52,borderRadius:15,backgroundColor:'#24191D',flexDirection:'row',alignItems:'center',justifyContent:'center',gap:8},signOutText:{color:'#FF6675',fontFamily:'Inter_700Bold',fontSize:13}
});
