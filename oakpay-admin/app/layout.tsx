import type { Metadata } from 'next';
import AdminGuard from '../components/AdminGuard';
import './globals.css';
import './admin-actions.css';
export const metadata:Metadata={title:'OakPay Admin',description:'OakPay operations and client management'};
export default function RootLayout({children}:{children:React.ReactNode}){return <html lang="en"><body><AdminGuard>{children}</AdminGuard></body></html>}
