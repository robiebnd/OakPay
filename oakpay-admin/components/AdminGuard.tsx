'use client';
import {useEffect,useState} from 'react';
import {usePathname,useRouter} from 'next/navigation';
import {session} from '../lib/api';
export default function AdminGuard({children}:{children:React.ReactNode}){const path=usePathname();const router=useRouter();const[ready,setReady]=useState(path==='/login');useEffect(()=>{if(path==='/login'){setReady(true);return}if(!session.get()){router.replace('/login');return}setReady(true)},[path,router]);if(!ready)return <div className="boot-screen"><div className="brand">Oak<span>Pay</span></div><p>Loading operations portal…</p></div>;return <>{children}</>}
